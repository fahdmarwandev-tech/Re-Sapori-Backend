package com.resapori.e_commerce.service.impl;

import com.resapori.e_commerce.common.event.OrderEvent;
import com.resapori.e_commerce.common.event.OrderEvent.EventType;
import com.resapori.e_commerce.common.exception.ResourceNotFoundException;
import com.resapori.e_commerce.common.security.AuthUtil;
import com.resapori.e_commerce.northbound.dto.order.*;
import com.resapori.e_commerce.northbound.dto.delivery.DeliveryFeeResponse;
import com.resapori.e_commerce.service.IDeliveryService;
import com.resapori.e_commerce.service.IOrderService;
import com.resapori.e_commerce.southbound.entity.*;
import com.resapori.e_commerce.southbound.enums.*;
import com.resapori.e_commerce.southbound.mapper.OrderItemMapper;
import com.resapori.e_commerce.southbound.mapper.OrderMapper;
import com.resapori.e_commerce.southbound.repository.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class OrderServiceImpl implements IOrderService {

    private final IOrderRepository orderRepository;
    private final IOrderItemRepository orderItemRepository;
    private final IMenuItemRepository menuItemRepository;
    private final IBranchRepository branchRepository;
    private final IUserAddressRepository userAddressRepository;
    private final IPromoCodeRepository promoCodeRepository;
    private final IPromoCodeRedemptionRepository promoCodeRedemptionRepository;
    private final IOfferRepository offerRepository;
    private final IMenuAddOnRepository menuAddOnRepository;
    private final IDeliveryService deliveryService;

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final AuthUtil authUtil;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest request) {
        validateOrderPayload(request);
        User user = getAuthenticatedUserOrThrow();
        Order order = initOrder(request, user);
        List<OrderItem> orderItems = new ArrayList<>(buildAllOrderItems(request));
        order.setTotalAmount(calculateTotal(orderItems));

        PromoContext promoCtx = applyPromoCodeIfPresent(request.getPromoCode(), user, orderItems, order);
        if (order.getDeliveryFee() != null && order.getDeliveryFee().compareTo(BigDecimal.ZERO) > 0) {
            order.setTotalAmount(order.getTotalAmount().add(order.getDeliveryFee()));
        }
        if (order.getTotalAmount() != null) {
            order.setTotalAmount(order.getTotalAmount().setScale(0, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP));
        }

        Order savedOrder = orderRepository.save(order);
        persistOrderItems(savedOrder, orderItems);
        finalizePromoRedemption(promoCtx, user, savedOrder);

        OrderResponse response = mapToResponse(savedOrder, orderItems);
        eventPublisher.publishEvent(new OrderEvent(response, EventType.NEW_ORDER));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getById(UUID id) {
        Order order = orderRepository.findByIdWithUserAndBranch(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        validateOrderAccess(order);
        List<OrderItem> items = orderItemRepository.findByOrderIdWithMenuItem(order.getId());
        return mapToResponse(order, items);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAll() {
        List<Order> orders = orderRepository.findAllWithUserAndBranch();
        if (orders.isEmpty()) return Collections.emptyList();
        Map<UUID, List<OrderItem>> itemsByOrderId = fetchItemsGroupedByOrderId(orders);
        return orders.stream()
                .map(order -> mapToResponse(order, itemsByOrderId.getOrDefault(order.getId(), List.of())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders() {
        User user = getAuthenticatedUserOrThrow();
        List<Order> orders = orderRepository.findByUserIdWithUserAndBranch(user.getId());
        if (orders.isEmpty()) return Collections.emptyList();
        Map<UUID, List<OrderItem>> itemsByOrderId = fetchItemsGroupedByOrderId(orders);
        return orders.stream()
                .map(order -> mapToResponse(order, itemsByOrderId.getOrDefault(order.getId(), List.of())))
                .toList();
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(UUID id, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findByIdWithUserAndBranch(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        OrderStatus previousStatus = order.getStatus();
        order.setStatus(request.getStatus());
        Order savedOrder = orderRepository.save(order);
        List<OrderItem> items = orderItemRepository.findByOrderIdWithMenuItem(savedOrder.getId());
        OrderResponse response = mapToResponse(savedOrder, items);
        eventPublisher.publishEvent(new OrderEvent(response, EventType.ORDER_UPDATED, previousStatus));
        return response;
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        order.setActive(false);
        orderRepository.save(order);
    }

    // ─── Private Helpers (each <= 20 lines) ───────────────────────────

    private void validateOrderPayload(PlaceOrderRequest req) {
        boolean hasItems = req.getItems() != null && !req.getItems().isEmpty();
        boolean hasOffers = req.getOffers() != null && !req.getOffers().isEmpty();
        if (!hasItems && !hasOffers) {
            throw new IllegalArgumentException("Order must contain at least one item or offer");
        }
    }

    private User getAuthenticatedUserOrThrow() {
        User user = authUtil.getAuthenticatedUser();
        if (user == null) {
            throw new AccessDeniedException("Must be logged in to proceed");
        }
        return user;
    }

    private Order initOrder(PlaceOrderRequest request, User user) {
        Order order = new Order();
        order.setUser(user);
        order.setOrderType(request.getOrderType());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setStatus(OrderStatus.PENDING);
        order.setCurrency("EGP");

        if (request.getOrderType() == OrderType.DELIVERY) {
            UserAddress address = resolveDeliveryAddress(request.getAddressId(), user.getId());
            order.setDeliveryAddress(formatAddress(address));

            DeliveryFeeResponse feeRes = deliveryService.calculateForAddress(address.getId(), request.getBranchId());
            order.setDeliveryFee(feeRes != null && feeRes.getDeliveryFee() != null ? feeRes.getDeliveryFee() : BigDecimal.valueOf(35.00));
            if (feeRes != null && feeRes.getBranchId() != null) {
                branchRepository.findById(feeRes.getBranchId()).ifPresent(order::setBranch);
            }
            if (order.getBranch() == null) {
                branchRepository.findByIsActiveTrue().stream().findFirst().ifPresent(order::setBranch);
            }
        } else {
            order.setDeliveryFee(BigDecimal.ZERO);
            order.setBranch(resolveBranch(request.getBranchId()));
        }
        return order;
    }

    private UserAddress resolveDeliveryAddress(UUID addressId, UUID userId) {
        if (addressId == null) {
            throw new IllegalArgumentException("Address is required for delivery orders");
        }
        UserAddress address = userAddressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        if (!address.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Address does not belong to user");
        }
        return address;
    }

    private String formatAddress(UserAddress a) {
        String district = a.getDistrict() != null ? a.getDistrict() : "";
        String floor = a.getFloor() != null ? a.getFloor() : "-";
        String apt = a.getApartment() != null ? a.getApartment() : "-";
        return String.format("%s, %s, %s, Floor: %s, Apt: %s", a.getStreet(), a.getCity(), district, floor, apt);
    }

    private Branch resolveBranch(UUID branchId) {
        if (branchId == null) {
            throw new IllegalArgumentException("Branch is required for pickup/dine-in orders");
        }
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
        if (!branch.isActive()) {
            throw new IllegalArgumentException("The selected branch is currently inactive or closed");
        }
        return branch;
    }

    private List<OrderItem> buildAllOrderItems(PlaceOrderRequest request) {
        List<OrderItem> items = new ArrayList<>();
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            items.addAll(buildOrderItems(request));
        }
        if (request.getOffers() != null && !request.getOffers().isEmpty()) {
            items.addAll(buildOfferItems(request.getOffers()));
        }
        return items;
    }

    private List<OrderItem> buildOrderItems(PlaceOrderRequest request) {
        List<UUID> menuItemIds = request.getItems().stream().map(OrderItemInput::getMenuItemId).toList();
        Map<UUID, MenuItem> menuItems = menuItemRepository.findAllById(menuItemIds).stream()
                .collect(Collectors.toMap(MenuItem::getId, m -> m));

        return request.getItems().stream()
                .map(input -> createOrderItem(input, menuItems.get(input.getMenuItemId())))
                .toList();
    }

    private OrderItem createOrderItem(OrderItemInput input, MenuItem menuItem) {
        if (menuItem == null || !menuItem.isActive() || !menuItem.isAvailable()) {
            throw new IllegalArgumentException("Menu item unavailable: " + input.getMenuItemId());
        }
        ItemSize size = input.getSize() != null ? input.getSize() : ItemSize.REGULAR;
        if (size == ItemSize.MINI && menuItem.getMiniPrice() == null) {
            throw new IllegalArgumentException("Item \"" + menuItem.getNameEn() + "\" has no mini size");
        }
        BigDecimal unitPrice = resolveItemPrice(menuItem, size);

        OrderItem item = new OrderItem();
        item.setMenuItem(menuItem);
        item.setQuantity(input.getQuantity());
        item.setSize(size);
        item.setUnitPriceAtPurchase(unitPrice);
        return item;
    }

    private BigDecimal resolveItemPrice(MenuItem menuItem, ItemSize size) {
        if (size == ItemSize.MINI) {
            return menuItem.getMiniPrice();
        }
        if (menuItem.getDiscountPrice() != null && menuItem.getDiscountPrice().compareTo(BigDecimal.ZERO) > 0) {
            return menuItem.getDiscountPrice();
        }
        return menuItem.getCurrentPrice();
    }

    private List<OrderItem> buildOfferItems(List<OrderOfferInput> offerInputs) {
        List<OrderItem> items = new ArrayList<>();
        for (OrderOfferInput input : offerInputs) {
            Offer offer = offerRepository.findByIdWithSlots(input.getOfferId())
                    .orElseThrow(() -> new ResourceNotFoundException("Offer not found: " + input.getOfferId()));
            if (!offer.isActive()) {
                throw new IllegalArgumentException("Offer is no longer active: " + offer.getNameEn());
            }
            int count = input.getQuantity() != null && input.getQuantity() > 0 ? input.getQuantity() : 1;
            for (int i = 0; i < count; i++) {
                items.addAll(processSingleOfferBundle(offer, input.getSelections()));
            }
        }
        return items;
    }

    private List<OrderItem> processSingleOfferBundle(Offer offer, List<OfferSelectionInput> selections) {
        if (selections == null || selections.isEmpty()) {
            throw new IllegalArgumentException("Selections required for offer: " + offer.getNameEn());
        }
        validateSlotQuantities(offer, selections);
        UUID bundleGroupId = UUID.randomUUID();
        Map<UUID, MenuItem> itemMap = fetchSelectionItems(selections);
        Map<UUID, MenuAddOn> addOnMap = fetchSelectionAddOns(selections);
        Map<UUID, OfferSlot> slotMap = offer.getSlots().stream()
                .collect(Collectors.toMap(OfferSlot::getId, s -> s));

        List<PriceContext> contexts = buildSelectionContexts(selections, slotMap, itemMap, addOnMap);
        applyOfferPricing(offer, contexts);
        return contexts.stream()
                .map(ctx -> createOfferOrderItem(ctx, offer, bundleGroupId))
                .toList();
    }

    private Map<UUID, MenuItem> fetchSelectionItems(List<OfferSelectionInput> selections) {
        List<UUID> ids = selections.stream().map(OfferSelectionInput::getMenuItemId).toList();
        return menuItemRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(MenuItem::getId, m -> m));
    }

    private Map<UUID, MenuAddOn> fetchSelectionAddOns(List<OfferSelectionInput> selections) {
        List<UUID> allAddOnIds = selections.stream()
                .filter(s -> s.getAddOnIds() != null)
                .flatMap(s -> s.getAddOnIds().stream())
                .toList();
        if (allAddOnIds.isEmpty()) return Collections.emptyMap();
        return menuAddOnRepository.findAllById(allAddOnIds).stream()
                .collect(Collectors.toMap(MenuAddOn::getId, a -> a));
    }

    private List<PriceContext> buildSelectionContexts(
            List<OfferSelectionInput> selections, Map<UUID, OfferSlot> slotMap,
            Map<UUID, MenuItem> itemMap, Map<UUID, MenuAddOn> addOnMap) {
        List<PriceContext> contexts = new ArrayList<>();
        for (OfferSelectionInput sel : selections) {
            OfferSlot slot = slotMap.get(sel.getSlotId());
            if (slot == null) throw new IllegalArgumentException("Invalid slot ID: " + sel.getSlotId());
            MenuItem item = itemMap.get(sel.getMenuItemId());
            validateSlotSelection(slot, item);
            BigDecimal addOns = computeAddOnsTotal(sel.getAddOnIds(), addOnMap);
            ItemSize size = sel.getSize() != null ? sel.getSize() : ItemSize.REGULAR;
            BigDecimal base = (size == ItemSize.MINI && item.getMiniPrice() != null) ? item.getMiniPrice() : item.getCurrentPrice();
            int qty = sel.getQuantity() != null && sel.getQuantity() > 0 ? sel.getQuantity() : 1;
            for (int i = 0; i < qty; i++) {
                contexts.add(new PriceContext(slot, item, size, base, addOns, BigDecimal.ZERO));
            }
        }
        return contexts;
    }

    private void validateSlotSelection(OfferSlot slot, MenuItem item) {
        if (item == null || !item.isActive() || !item.isAvailable()) {
            throw new IllegalArgumentException("Selected item is not available: " + (item != null ? item.getNameEn() : ""));
        }
        if (slot.getMenuItem() != null && !slot.getMenuItem().getId().equals(item.getId())) {
            throw new IllegalArgumentException("Slot requires fixed item: " + slot.getMenuItem().getNameEn());
        }
        if (slot.getCategory() != null && !item.getCategory().getId().equals(slot.getCategory().getId())) {
            throw new IllegalArgumentException("Item does not belong to category: " + slot.getCategory().getNameEn());
        }
        if (slot.getEligibleItems() != null && !slot.getEligibleItems().isEmpty()) {
            boolean allowed = slot.getEligibleItems().stream().anyMatch(e -> e.getId().equals(item.getId()));
            if (!allowed) throw new IllegalArgumentException("Item is not eligible for this slot: " + item.getNameEn());
        }
    }

    private void validateSlotQuantities(Offer offer, List<OfferSelectionInput> selections) {
        if (offer.getSlots() == null || offer.getSlots().isEmpty()) return;
        Map<UUID, Integer> submitted = new HashMap<>();
        for (OfferSelectionInput sel : selections) {
            int qty = sel.getQuantity() != null && sel.getQuantity() > 0 ? sel.getQuantity() : 1;
            submitted.merge(sel.getSlotId(), qty, Integer::sum);
        }
        for (OfferSlot slot : offer.getSlots()) {
            int expected = slot.getQuantity() != null ? slot.getQuantity() : 1;
            int actual = submitted.getOrDefault(slot.getId(), 0);
            if (actual != expected) {
                throw new IllegalArgumentException("Slot \"" + slot.getSlotNameEn() + "\" requires " + expected + ", got " + actual);
            }
        }
    }

    private BigDecimal computeAddOnsTotal(List<UUID> addOnIds, Map<UUID, MenuAddOn> addOnMap) {
        if (addOnIds == null || addOnIds.isEmpty()) return BigDecimal.ZERO;
        return addOnIds.stream()
                .map(id -> addOnMap.containsKey(id) ? addOnMap.get(id).getPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void applyOfferPricing(Offer offer, List<PriceContext> contexts) {
        List<PriceContext> freeContexts = contexts.stream().filter(c -> c.slot().isFree()).toList();
        freeContexts.forEach(c -> c.setFinalPrice(c.addOnsTotal()));

        List<PriceContext> paidContexts = contexts.stream().filter(c -> !c.slot().isFree()).toList();
        if (paidContexts.isEmpty()) return;

        if (offer.getDiscountTarget() == DiscountTarget.FIXED_PRICE) {
            applyFixedPrice(offer, paidContexts);
        } else if (offer.getDiscountTarget() == DiscountTarget.TOTAL_BUNDLE) {
            applyTotalBundleDiscount(offer, paidContexts);
        } else {
            applyCheapestItemDiscount(offer, paidContexts);
        }
    }

    private void applyFixedPrice(Offer offer, List<PriceContext> paid) {
        BigDecimal fixed = offer.getFixedPrice() != null ? offer.getFixedPrice() : BigDecimal.ZERO;
        paid.get(0).setFinalPrice(fixed.add(paid.get(0).addOnsTotal()));
        for (int i = 1; i < paid.size(); i++) {
            paid.get(i).setFinalPrice(paid.get(i).addOnsTotal());
        }
    }

    private void applyTotalBundleDiscount(Offer offer, List<PriceContext> paid) {
        BigDecimal pct = offer.getDiscountPercentage() != null
                ? offer.getDiscountPercentage().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        for (PriceContext ctx : paid) {
            BigDecimal discountedBase = ctx.basePrice().multiply(BigDecimal.ONE.subtract(pct))
                    .setScale(2, RoundingMode.HALF_UP);
            ctx.setFinalPrice(discountedBase.add(ctx.addOnsTotal()));
        }
    }

    private void applyCheapestItemDiscount(Offer offer, List<PriceContext> paid) {
        List<PriceContext> sorted = new ArrayList<>(paid);
        sorted.sort(Comparator.comparing(PriceContext::basePrice).reversed());

        int buyQty = offer.getBuyQuantity() != null ? offer.getBuyQuantity() : 1;
        int getQty = offer.getGetQuantity() != null ? offer.getGetQuantity() : 1;
        BigDecimal pct = offer.getDiscountPercentage() != null
                ? offer.getDiscountPercentage().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
                : BigDecimal.ONE;

        for (int i = 0; i < sorted.size(); i++) {
            PriceContext ctx = sorted.get(i);
            boolean isDiscounted = (i >= buyQty && i < buyQty + getQty);
            BigDecimal factor = isDiscounted ? BigDecimal.ONE.subtract(pct) : BigDecimal.ONE;
            BigDecimal base = ctx.basePrice().multiply(factor).setScale(2, RoundingMode.HALF_UP);
            ctx.setFinalPrice(base.add(ctx.addOnsTotal()));
        }
    }

    private OrderItem createOfferOrderItem(PriceContext ctx, Offer offer, UUID bundleGroupId) {
        OrderItem item = new OrderItem();
        item.setMenuItem(ctx.item());
        item.setQuantity(1);
        item.setSize(ctx.size());
        item.setUnitPriceAtPurchase(ctx.finalPrice());
        item.setOffer(offer);
        item.setBundleGroupId(bundleGroupId);
        item.setFree(ctx.slot().isFree());
        return item;
    }

    private BigDecimal calculateTotal(List<OrderItem> items) {
        return items.stream()
                .map(i -> i.getUnitPriceAtPurchase().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Map<UUID, List<OrderItem>> fetchItemsGroupedByOrderId(List<Order> orders) {
        List<UUID> orderIds = orders.stream().map(Order::getId).toList();
        List<OrderItem> items = orderItemRepository.findByOrderIdInWithMenuItem(orderIds);
        return items.stream().collect(Collectors.groupingBy(oi -> oi.getOrder().getId()));
    }

    private void validateOrderAccess(Order order) {
        User user = authUtil.getAuthenticatedUser();
        if (!isAdmin() && (user == null || !order.getUser().getId().equals(user.getId()))) {
            throw new AccessDeniedException("Cannot access other users' orders");
        }
    }

    private OrderResponse mapToResponse(Order order, List<OrderItem> items) {
        OrderResponse response = orderMapper.toResponse(order);
        List<OrderItemResponse> itemResponses = items.stream().map(item -> {
            OrderItemResponse itemRes = orderItemMapper.toResponse(item);
            BigDecimal price = item.getUnitPriceAtPurchase() != null ? item.getUnitPriceAtPurchase() : BigDecimal.ZERO;
            itemRes.setLineTotal(price.multiply(BigDecimal.valueOf(item.getQuantity())));
            return itemRes;
        }).toList();
        response.setItems(itemResponses);
        return response;
    }

    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private PromoContext applyPromoCodeIfPresent(String code, User user, List<OrderItem> items, Order order) {
        if (code == null || code.trim().isBlank()) return null;
        PromoCode promoCode = promoCodeRepository.findByCodeIgnoreCaseAndIsActiveTrue(code.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or inactive promo code: " + code));
        validatePromoEligibility(promoCode, user);
        BigDecimal discount = calculateDiscount(promoCode, items, order);
        order.setTotalAmount(order.getTotalAmount().subtract(discount).max(BigDecimal.ZERO));
        return new PromoContext(promoCode, discount);
    }

    private void validatePromoEligibility(PromoCode promo, User user) {
        if (promo.getExpiryDate() != null && promo.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Promo code has expired");
        }
        if (promo.getMaxUses() != null && promo.getCurrentUses() >= promo.getMaxUses()) {
            throw new IllegalArgumentException("Promo code global limit reached");
        }
        if (promo.getUser() != null && !promo.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("This promo code is not assigned to your account");
        }
        long userUses = promoCodeRedemptionRepository.countByPromoCodeIdAndUserId(promo.getId(), user.getId());
        if (userUses >= promo.getMaxUsesPerUser()) {
            throw new IllegalArgumentException("You have reached the maximum redemptions for this promo code");
        }
    }

    private BigDecimal calculateDiscount(PromoCode promo, List<OrderItem> items, Order order) {
        if (promo.getDiscountType() == DiscountType.PERCENTAGE) {
            BigDecimal pct = promo.getDiscountValue().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            return order.getTotalAmount().multiply(pct).setScale(2, RoundingMode.HALF_UP);
        }
        if (promo.getDiscountType() == DiscountType.FREE_ITEM && promo.getFreeItem() != null) {
            OrderItem freeItem = new OrderItem();
            freeItem.setMenuItem(promo.getFreeItem());
            freeItem.setQuantity(1);
            freeItem.setSize(ItemSize.REGULAR);
            freeItem.setUnitPriceAtPurchase(BigDecimal.ZERO);
            freeItem.setFree(true);
            items.add(freeItem);
            return promo.getFreeItem().getCurrentPrice();
        }
        return BigDecimal.ZERO;
    }

    private void persistOrderItems(Order savedOrder, List<OrderItem> items) {
        items.forEach(item -> item.setOrder(savedOrder));
        orderItemRepository.saveAll(items);
    }

    private void finalizePromoRedemption(PromoContext ctx, User user, Order savedOrder) {
        if (ctx == null) return;
        PromoCodeRedemption redemption = new PromoCodeRedemption();
        redemption.setPromoCode(ctx.promoCode());
        redemption.setUser(user);
        redemption.setOrder(savedOrder);
        redemption.setDiscountApplied(ctx.discountApplied());
        redemption.setRedeemedAt(LocalDateTime.now());
        promoCodeRedemptionRepository.save(redemption);
        promoCodeRepository.incrementCurrentUses(ctx.promoCode().getId());
    }

    @Getter
    @Setter
    @AllArgsConstructor
    private static class PriceContext {
        private final OfferSlot slot;
        private final MenuItem item;
        private final ItemSize size;
        private final BigDecimal basePrice;
        private final BigDecimal addOnsTotal;
        private BigDecimal finalPrice;

        public OfferSlot slot() { return slot; }
        public MenuItem item() { return item; }
        public ItemSize size() { return size; }
        public BigDecimal basePrice() { return basePrice; }
        public BigDecimal addOnsTotal() { return addOnsTotal; }
        public BigDecimal finalPrice() { return finalPrice; }
    }

    private record PromoContext(PromoCode promoCode, BigDecimal discountApplied) {}
}
