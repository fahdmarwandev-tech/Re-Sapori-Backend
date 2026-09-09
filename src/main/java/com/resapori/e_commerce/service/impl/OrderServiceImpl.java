package com.resapori.e_commerce.service.impl;

import com.resapori.e_commerce.common.exception.ResourceNotFoundException;
import com.resapori.e_commerce.common.security.AuthUtil;
import com.resapori.e_commerce.northbound.dto.order.OrderItemInput;
import com.resapori.e_commerce.northbound.dto.order.OrderItemResponse;
import com.resapori.e_commerce.northbound.dto.order.OrderResponse;
import com.resapori.e_commerce.northbound.dto.order.PlaceOrderRequest;
import com.resapori.e_commerce.northbound.dto.order.UpdateOrderStatusRequest;
import com.resapori.e_commerce.service.IOrderService;
import com.resapori.e_commerce.southbound.entity.*;
import com.resapori.e_commerce.southbound.enums.ItemSize;
import com.resapori.e_commerce.southbound.enums.OrderStatus;
import com.resapori.e_commerce.southbound.enums.OrderType;
import com.resapori.e_commerce.southbound.mapper.OrderItemMapper;
import com.resapori.e_commerce.southbound.mapper.OrderMapper;
import com.resapori.e_commerce.southbound.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class OrderServiceImpl implements IOrderService {

    private final IOrderRepository orderRepository;
    private final IOrderItemRepository orderItemRepository;
    private final IMenuItemRepository menuItemRepository;
    private final IBranchRepository branchRepository;
    private final IUserAddressRepository userAddressRepository;
    
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final AuthUtil authUtil;

    @Override
    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest request) {
        User user = getAuthenticatedUserOrThrow();
        Order order = initOrder(request, user);
        List<OrderItem> orderItems = buildOrderItems(request);
        BigDecimal totalAmount = calculateTotal(orderItems);
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);
        orderItems.forEach(item -> item.setOrder(savedOrder));
        orderItemRepository.saveAll(orderItems);

        return mapToResponse(savedOrder, orderItems);
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
        if (orders.isEmpty()) {
            return Collections.emptyList();
        }
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
        if (orders.isEmpty()) {
            return Collections.emptyList();
        }
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
        order.setStatus(request.getStatus());
        Order savedOrder = orderRepository.save(order);
        List<OrderItem> items = orderItemRepository.findByOrderIdWithMenuItem(savedOrder.getId());
        return mapToResponse(savedOrder, items);
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
            order.setDeliveryAddress(resolveDeliveryAddress(request.getAddressId(), user.getId()));
        } else {
            order.setBranch(resolveBranch(request.getBranchId()));
        }
        return order;
    }

    private String resolveDeliveryAddress(UUID addressId, UUID userId) {
        if (addressId == null) {
            throw new IllegalArgumentException("Address is required for delivery orders");
        }
        UserAddress address = userAddressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        if (!address.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Address does not belong to user");
        }
        return formatAddress(address);
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
        return branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
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
        BigDecimal unitPrice = (size == ItemSize.MINI) ? menuItem.getMiniPrice() : menuItem.getCurrentPrice();

        OrderItem item = new OrderItem();
        item.setMenuItem(menuItem);
        item.setQuantity(input.getQuantity());
        item.setSize(size);
        item.setUnitPriceAtPurchase(unitPrice);
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
}
