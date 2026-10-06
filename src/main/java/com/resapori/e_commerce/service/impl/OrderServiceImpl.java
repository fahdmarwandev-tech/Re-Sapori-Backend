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
    private final IUserRepository userRepository;
    private final IUserAddressRepository userAddressRepository;
    private final IPromoCodeRepository promoCodeRepository;
    private final IPromoCodeRedemptionRepository promoCodeRedemptionRepository;
    private final IOfferRepository offerRepository;
    private final IMenuAddOnRepository menuAddOnRepository;
    private final IOrderItemAddOnRepository orderItemAddOnRepository;
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

        // Always associate and persist the entered contact phone number with the customer's account
        String enteredPhone = request.getEffectivePhoneNumber();
        if (enteredPhone != null && !enteredPhone.isBlank()) {
            user.setPhoneNumber(enteredPhone.trim());
            user = userRepository.save(user);
        }

        // Also associate customer name if profile name is missing
        if (request.getCustomerName() != null && !request.getCustomerName().isBlank()) {
            if (user.getFirstName() == null || user.getFirstName().isBlank()) {
                String[] parts = request.getCustomerName().trim().split(" ", 2);
                user.setFirstName(parts[0]);
                if (parts.length > 1) {
                    user.setLastName(parts[1]);
                }
                user = userRepository.save(user);
            }
        }

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

        if (!isAdmin()) {
            User user = authUtil.getAuthenticatedUser();
            if (user == null) return Collections.emptyList();

            if (isCashier()) {
                UUID branchId = user.getBranch() != null ? user.getBranch().getId() : null;
                if (branchId == null) {
                    return Collections.emptyList();
                }
                orders = orders.stream()
                        .filter(o -> o.getBranch() != null && o.getBranch().getId().equals(branchId))
                        .toList();
            } else if (isDelivery()) {
                UUID branchId = user.getBranch() != null ? user.getBranch().getId() : null;
                if (branchId == null) {
                    return Collections.emptyList();
                }
                orders = orders.stream()
                        .filter(o -> o.getOrderType() == OrderType.DELIVERY
                                && o.getBranch() != null && o.getBranch().getId().equals(branchId)
                                && (o.getStatus() == OrderStatus.PREPARING || o.getStatus() == OrderStatus.READY))
                        .toList();
            }
        }

        if (orders.isEmpty()) return Collections.emptyList();
        Map<UUID, List<OrderItem>> itemsByOrderId = fetchItemsGroupedByOrderId(orders);
        Map<UUID, List<OrderItemAddOn>> addOnsByItemId = fetchAddOnsGroupedByOrderItemId(itemsByOrderId);
        return orders.stream()
                .map(order -> mapToResponse(order, itemsByOrderId.getOrDefault(order.getId(), List.of()), addOnsByItemId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders() {
        User user = getAuthenticatedUserOrThrow();
        List<Order> orders = orderRepository.findByUserIdWithUserAndBranch(user.getId());
        if (orders.isEmpty()) return Collections.emptyList();
        Map<UUID, List<OrderItem>> itemsByOrderId = fetchItemsGroupedByOrderId(orders);
        Map<UUID, List<OrderItemAddOn>> addOnsByItemId = fetchAddOnsGroupedByOrderItemId(itemsByOrderId);
        return orders.stream()
                .map(order -> mapToResponse(order, itemsByOrderId.getOrDefault(order.getId(), List.of()), addOnsByItemId))
                .toList();
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(UUID id, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findByIdWithUserAndBranch(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        validateOrderStatusUpdate(order, request.getStatus());

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

            boolean isCarDelivery = isCarDeliveryOrder(request, address);

            if (isCarDelivery) {
                order.setDeliveryFee(BigDecimal.ZERO);
                if (request.getBranchId() != null) {
                    branchRepository.findById(request.getBranchId())
                            .filter(Branch::isActive)
                            .ifPresent(order::setBranch);
                }
            } else {
                DeliveryFeeResponse feeRes = deliveryService.calculateForAddress(address.getId(), request.getBranchId());
                if (feeRes != null && !feeRes.isCovered()) {
                    throw new IllegalArgumentException("عذراً، العنوان المحدد يقع خارج نطاق التوصيل لجميع فروعنا (Selected address is outside our delivery zones)");
                }
                order.setDeliveryFee(feeRes != null && feeRes.getDeliveryFee() != null ? feeRes.getDeliveryFee() : BigDecimal.valueOf(35.00));
                if (feeRes != null && feeRes.getBranchId() != null) {
                    branchRepository.findById(feeRes.getBranchId())
                            .filter(Branch::isActive)
                            .ifPresent(order::setBranch);
                }
            }
            if (order.getBranch() == null) {
                branchRepository.findByIsActiveTrue().stream().findFirst().ifPresent(order::setBranch);
            }
            if (order.getBranch() == null || !order.getBranch().isActive()) {
                throw new IllegalStateException("عذراً، لا يوجد أي فرع متاح حالياً لاستلام وتوصيل الطلبات (No active branch is currently available to fulfill orders)");
            }
        } else {
            order.setDeliveryFee(BigDecimal.ZERO);
            order.setBranch(resolveBranch(request.getBranchId()));
        }
        order.setOrderNotes(request.getOrderNotes());
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

    private boolean isCarDeliveryOrder(PlaceOrderRequest request, UserAddress address) {
        if (isCarDeliveryAddress(address)) {
            return true;
        }
        if (request != null && request.getOrderNotes() != null) {
            String notes = request.getOrderNotes().toLowerCase();
            return notes.contains("car delivery") || notes.contains("car pickup")
                    || notes.contains("استلام من السيارة") || notes.contains("استلام بالسيارة")
                    || notes.contains("لوحة السيارة");
        }
        return false;
    }

    private boolean isCarDeliveryAddress(UserAddress address) {
        if (address == null) return false;
        String label = address.getLabel() != null ? address.getLabel().toLowerCase().trim() : "";
        String street = address.getStreet() != null ? address.getStreet().toLowerCase().trim() : "";
        String district = address.getDistrict() != null ? address.getDistrict().toLowerCase().trim() : "";
        return label.contains("car") || label.contains("سيارة") || label.contains("استلام")
                || street.contains("car delivery") || street.contains("car pickup")
                || street.contains("استلام بالسيارة") || street.contains("استلام من السيارة")
                || street.contains("توصيل للسيارة") || street.contains("لوحة") || street.contains("plate:")
                || district.contains("car delivery") || district.contains("استلام بالسيارة");
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

        List<UUID> allAddOnIds = request.getItems().stream()
                .filter(i -> i.getAddOnIds() != null)
                .flatMap(i -> i.getAddOnIds().stream())
                .toList();
        Map<UUID, MenuAddOn> addOnMap = allAddOnIds.isEmpty() ? Collections.emptyMap() :
                menuAddOnRepository.findAllById(allAddOnIds).stream()
                        .collect(Collectors.toMap(MenuAddOn::getId, a -> a));

        return request.getItems().stream()
                .map(input -> createOrderItem(input, menuItems.get(input.getMenuItemId()), addOnMap))
                .toList();
    }

    private OrderItem createOrderItem(OrderItemInput input, MenuItem menuItem, Map<UUID, MenuAddOn> addOnMap) {
        if (menuItem == null || !menuItem.isActive() || !menuItem.isAvailable()) {
            throw new IllegalArgumentException("Menu item unavailable: " + input.getMenuItemId());
        }
        ItemSize size = input.getSize() != null ? input.getSize() : ItemSize.REGULAR;
        if (size == ItemSize.MINI && menuItem.getMiniPrice() == null) {
            throw new IllegalArgumentException("Item \"" + menuItem.getNameEn() + "\" has no mini size");
        }
        BigDecimal basePrice = resolveItemPrice(menuItem, size);
        BigDecimal addOnsTotal = BigDecimal.ZERO;
        List<OrderItemAddOn> itemAddOns = new ArrayList<>();

        if (input.getAddOnIds() != null && !input.getAddOnIds().isEmpty()) {
            for (UUID addOnId : input.getAddOnIds()) {
                MenuAddOn addOn = addOnMap.get(addOnId);
                if (addOn != null) {
                    BigDecimal price = addOn.getPrice() != null ? addOn.getPrice() : BigDecimal.ZERO;
                    addOnsTotal = addOnsTotal.add(price);

                    OrderItemAddOn itemAddOn = new OrderItemAddOn();
                    itemAddOn.setAddOn(addOn);
                    itemAddOn.setNameEn(addOn.getNameEn());
                    itemAddOn.setNameAr(addOn.getNameAr());
                    itemAddOn.setPrice(price);
                    itemAddOns.add(itemAddOn);
                }
            }
        }

        OrderItem item = new OrderItem();
        item.setMenuItem(menuItem);
        item.setQuantity(input.getQuantity());
        item.setSize(size);
        item.setUnitPriceAtPurchase(basePrice.add(addOnsTotal));
        item.setNotes(input.getNotes());
        for (OrderItemAddOn addOnEntity : itemAddOns) {
            addOnEntity.setOrderItem(item);
            item.getAddOns().add(addOnEntity);
        }
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
                .map(ctx -> createOfferOrderItem(ctx, offer, bundleGroupId, addOnMap))
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
                contexts.add(new PriceContext(slot, item, size, base, addOns, BigDecimal.ZERO, sel.getAddOnIds(), sel.getNotes(), slot.isFree()));
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
            int buyQty = offer.getBuyQuantity() != null ? offer.getBuyQuantity() : 1;
            int getQty = offer.getGetQuantity() != null ? offer.getGetQuantity() : 1;
            int targetCount = buyQty + getQty;

            Map<UUID, List<PriceContext>> bySlot = paidContexts.stream()
                    .collect(Collectors.groupingBy(c -> c.slot().getId()));

            UUID targetSlotId = bySlot.entrySet().stream()
                    .filter(e -> e.getValue().size() >= targetCount)
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(null);

            if (targetSlotId != null && bySlot.size() > 1) {
                applyCheapestItemDiscount(offer, bySlot.get(targetSlotId));
                for (Map.Entry<UUID, List<PriceContext>> entry : bySlot.entrySet()) {
                    if (!entry.getKey().equals(targetSlotId)) {
                        for (PriceContext ctx : entry.getValue()) {
                            ctx.setFinalPrice(ctx.basePrice().add(ctx.addOnsTotal()));
                        }
                    }
                }
            } else {
                applyCheapestItemDiscount(offer, paidContexts);
            }
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
            if (isDiscounted && pct.compareTo(BigDecimal.ONE) >= 0) {
                ctx.setFree(true);
            }
        }
    }

    private OrderItem createOfferOrderItem(PriceContext ctx, Offer offer, UUID bundleGroupId, Map<UUID, MenuAddOn> addOnMap) {
        OrderItem item = new OrderItem();
        item.setMenuItem(ctx.item());
        item.setQuantity(1);
        item.setSize(ctx.size());
        item.setUnitPriceAtPurchase(ctx.finalPrice());
        item.setOffer(offer);
        item.setBundleGroupId(bundleGroupId);
        item.setFree(ctx.isFree() || ctx.slot().isFree());
        item.setNotes(ctx.notes());

        if (ctx.addOnIds() != null && !ctx.addOnIds().isEmpty()) {
            for (UUID addOnId : ctx.addOnIds()) {
                MenuAddOn addOn = addOnMap.get(addOnId);
                if (addOn != null) {
                    OrderItemAddOn entity = new OrderItemAddOn();
                    entity.setOrderItem(item);
                    entity.setAddOn(addOn);
                    entity.setNameEn(addOn.getNameEn());
                    entity.setNameAr(addOn.getNameAr());
                    entity.setPrice(addOn.getPrice() != null ? addOn.getPrice() : BigDecimal.ZERO);
                    item.getAddOns().add(entity);
                }
            }
        }
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

    private Map<UUID, List<OrderItemAddOn>> fetchAddOnsGroupedByOrderItemId(Map<UUID, List<OrderItem>> itemsByOrderId) {
        List<UUID> allItemIds = itemsByOrderId.values().stream()
                .flatMap(List::stream)
                .map(OrderItem::getId)
                .filter(Objects::nonNull)
                .toList();
        if (allItemIds.isEmpty()) return Collections.emptyMap();
        return orderItemAddOnRepository.findByOrderItemIdIn(allItemIds).stream()
                .filter(a -> a.getOrderItem() != null && a.getOrderItem().getId() != null)
                .collect(Collectors.groupingBy(a -> a.getOrderItem().getId()));
    }

    private void validateOrderAccess(Order order) {
        if (isAdmin()) return;
        User user = authUtil.getAuthenticatedUser();
        if (user == null) {
            throw new AccessDeniedException("Must be authenticated");
        }
        if (isCashier() || isDelivery()) {
            if (user.getBranch() == null || order.getBranch() == null || !order.getBranch().getId().equals(user.getBranch().getId())) {
                throw new AccessDeniedException("Cannot access orders outside your assigned branch");
            }
            return;
        }
        if (!order.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Cannot access other users' orders");
        }
    }

    private void validateOrderStatusUpdate(Order order, OrderStatus newStatus) {
        if (isAdmin()) {
            return; // Admin has full transition control
        }

        User user = authUtil.getAuthenticatedUser();
        if (user == null) {
            throw new AccessDeniedException("Must be authenticated to update order status");
        }

        if (user.getBranch() == null || order.getBranch() == null || !order.getBranch().getId().equals(user.getBranch().getId())) {
            throw new AccessDeniedException("Cannot update orders belonging to another branch");
        }

        OrderStatus prev = order.getStatus();

        if (isCashier()) {
            if (order.getOrderType() == OrderType.DELIVERY) {
                // Cashier on delivery: only PENDING -> PREPARING
                if ((prev == OrderStatus.PENDING || prev == OrderStatus.PAID) && newStatus == OrderStatus.PREPARING) {
                    return;
                }
                throw new AccessDeniedException("Cashiers can only start preparation (PREPARING) on delivery orders");
            } else {
                // Cashier on pickup or dine-in: PENDING -> PREPARING, and PREPARING/READY -> DELIVERED (mark done)
                if ((prev == OrderStatus.PENDING || prev == OrderStatus.PAID) && newStatus == OrderStatus.PREPARING) {
                    return;
                }
                if ((prev == OrderStatus.PREPARING || prev == OrderStatus.READY) && newStatus == OrderStatus.DELIVERED) {
                    return;
                }
                throw new AccessDeniedException("Cashiers can only start prep or mark pickup orders as completed");
            }
        }

        if (isDelivery()) {
            if (order.getOrderType() != OrderType.DELIVERY) {
                throw new AccessDeniedException("Delivery staff can only update delivery orders");
            }
            // 1. Delivery claims order: PREPARING -> READY
            if (prev == OrderStatus.PREPARING && newStatus == OrderStatus.READY) {
                return;
            }
            // 2. Delivery marks order delivered: READY -> DELIVERED
            if (prev == OrderStatus.READY && newStatus == OrderStatus.DELIVERED) {
                return;
            }
            throw new AccessDeniedException("Delivery staff can only mark orders as READY (claimed) or DELIVERED");
        }

        throw new AccessDeniedException("Unauthorized to update order status");
    }

    private OrderResponse mapToResponse(Order order, List<OrderItem> items) {
        return mapToResponse(order, items, null);
    }

    private OrderResponse mapToResponse(Order order, List<OrderItem> items, Map<UUID, List<OrderItemAddOn>> preloadedAddOns) {
        OrderResponse response = orderMapper.toResponse(order);
        if (order.getOrderNotes() != null) {
            response.setOrderNotes(order.getOrderNotes());
            response.setNotes(order.getOrderNotes());
        }

        List<UUID> itemIds = items.stream().map(OrderItem::getId).filter(Objects::nonNull).toList();
        Map<UUID, List<OrderItemAddOn>> addOnsByItemId = preloadedAddOns;
        if (addOnsByItemId == null && !itemIds.isEmpty()) {
            List<OrderItemAddOn> allAddOns = orderItemAddOnRepository.findByOrderItemIdIn(itemIds);
            addOnsByItemId = allAddOns.stream()
                    .filter(a -> a.getOrderItem() != null && a.getOrderItem().getId() != null)
                    .collect(Collectors.groupingBy(a -> a.getOrderItem().getId()));
        }

        final Map<UUID, List<OrderItemAddOn>> finalAddOnsMap = addOnsByItemId;

        List<OrderItemResponse> itemResponses = items.stream().map(item -> {
            OrderItemResponse itemRes = orderItemMapper.toResponse(item);
            BigDecimal price = item.getUnitPriceAtPurchase() != null ? item.getUnitPriceAtPurchase() : BigDecimal.ZERO;
            itemRes.setLineTotal(price.multiply(BigDecimal.valueOf(item.getQuantity())));
            itemRes.setNotes(item.getNotes());

            List<OrderItemAddOn> addOnEntities = Collections.emptyList();
            if (item.getId() != null && finalAddOnsMap != null && finalAddOnsMap.containsKey(item.getId())) {
                addOnEntities = finalAddOnsMap.get(item.getId());
            } else if (item.getAddOns() != null && !item.getAddOns().isEmpty()) {
                addOnEntities = item.getAddOns();
            }

            if (!addOnEntities.isEmpty()) {
                List<OrderItemAddOnResponse> addOnResponses = addOnEntities.stream()
                        .map(a -> OrderItemAddOnResponse.builder()
                                .id(a.getId())
                                .addOnId(a.getAddOn() != null ? a.getAddOn().getId() : null)
                                .nameEn(a.getNameEn())
                                .nameAr(a.getNameAr())
                                .price(a.getPrice())
                                .build())
                        .toList();
                itemRes.setAddOns(addOnResponses);
            } else {
                itemRes.setAddOns(Collections.emptyList());
            }

            StringBuilder detailsBuilder = new StringBuilder();
            if (item.getSize() == ItemSize.MINI) {
                detailsBuilder.append("Mini");
            }
            if (item.isFree()) {
                if (!detailsBuilder.isEmpty()) detailsBuilder.append(" | ");
                detailsBuilder.append("Complimentary / Free");
            }
            if (item.getOffer() != null) {
                if (!detailsBuilder.isEmpty()) detailsBuilder.append(" | ");
                detailsBuilder.append("Offer: ").append(item.getOffer().getNameEn());
            }
            if (itemRes.getAddOns() != null && !itemRes.getAddOns().isEmpty()) {
                if (!detailsBuilder.isEmpty()) detailsBuilder.append(" | ");
                String addOnsStr = itemRes.getAddOns().stream()
                        .map(a -> a.getNameEn() + (a.getPrice() != null && a.getPrice().compareTo(BigDecimal.ZERO) > 0 ? " (+" + a.getPrice() + " EGP)" : ""))
                        .collect(Collectors.joining(", "));
                detailsBuilder.append("Add-ons: ").append(addOnsStr);
            }
            if (item.getNotes() != null && !item.getNotes().isBlank()) {
                if (!detailsBuilder.isEmpty()) detailsBuilder.append(" | ");
                detailsBuilder.append("Note: ").append(item.getNotes().trim());
            }
            itemRes.setDetails(detailsBuilder.isEmpty() ? null : detailsBuilder.toString());

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

    private boolean isCashier() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CASHIER"));
    }

    private boolean isDelivery() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_DELIVERY"));
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
        items.forEach(item -> {
            item.setOrder(savedOrder);
            if (item.getAddOns() != null) {
                item.getAddOns().forEach(addon -> addon.setOrderItem(item));
            }
        });
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
        private final List<UUID> addOnIds;
        private final String notes;
        private boolean isFree;

        public OfferSlot slot() { return slot; }
        public MenuItem item() { return item; }
        public ItemSize size() { return size; }
        public BigDecimal basePrice() { return basePrice; }
        public BigDecimal addOnsTotal() { return addOnsTotal; }
        public BigDecimal finalPrice() { return finalPrice; }
        public void setFinalPrice(BigDecimal finalPrice) { this.finalPrice = finalPrice; }
        public List<UUID> addOnIds() { return addOnIds; }
        public String notes() { return notes; }
        public boolean isFree() { return isFree; }
        public void setFree(boolean isFree) { this.isFree = isFree; }
    }

    private record PromoContext(PromoCode promoCode, BigDecimal discountApplied) {}
}
