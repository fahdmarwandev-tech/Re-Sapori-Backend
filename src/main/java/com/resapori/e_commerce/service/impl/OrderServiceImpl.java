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
import java.util.List;
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
        User user = authUtil.getAuthenticatedUser();
        if (user == null) {
            throw new AccessDeniedException("Must be logged in to place an order");
        }

        Order order = new Order();
        order.setUser(user);
        order.setOrderType(request.getOrderType());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setStatus(OrderStatus.PENDING);
        order.setCurrency("EGP");

        if (request.getOrderType() == OrderType.DELIVERY) {
            if (request.getAddressId() == null) {
                throw new IllegalArgumentException("Address is required for delivery orders");
            }
            UserAddress address = userAddressRepository.findById(request.getAddressId())
                    .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
            
            if (!address.getUser().getId().equals(user.getId())) {
                throw new AccessDeniedException("Address does not belong to user");
            }
            
            String formattedAddress = String.format("%s, %s, %s, Floor: %s, Apt: %s", 
                address.getStreet(), address.getCity(), 
                address.getDistrict() != null ? address.getDistrict() : "",
                address.getFloor() != null ? address.getFloor() : "-", 
                address.getApartment() != null ? address.getApartment() : "-");
            order.setDeliveryAddress(formattedAddress);
        } else {
            if (request.getBranchId() == null) {
                throw new IllegalArgumentException("Branch is required for pickup/dine-in orders");
            }
            Branch branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
            order.setBranch(branch);
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemInput itemInput : request.getItems()) {
            MenuItem menuItem = menuItemRepository.findById(itemInput.getMenuItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("MenuItem not found: " + itemInput.getMenuItemId()));
            
            if (!menuItem.isActive() || !menuItem.isAvailable()) {
                throw new IllegalArgumentException("Item is not available: " + menuItem.getNameEn());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setMenuItem(menuItem);
            orderItem.setQuantity(itemInput.getQuantity());
            orderItem.setUnitPriceAtPurchase(menuItem.getCurrentPrice());
            
            BigDecimal lineTotal = menuItem.getCurrentPrice().multiply(BigDecimal.valueOf(itemInput.getQuantity()));
            totalAmount = totalAmount.add(lineTotal);
            
            orderItems.add(orderItem);
        }

        if (request.getPromoCode() != null && !request.getPromoCode().isBlank()) {
            // TODO: Phase 5 - Apply promo code logic
        }

        order.setTotalAmount(totalAmount);
        
        // Save order first to get ID
        Order savedOrder = orderRepository.save(order);
        
        // Set order on items and save
        for (OrderItem item : orderItems) {
            item.setOrder(savedOrder);
            orderItemRepository.save(item);
        }

        return mapToResponse(savedOrder, orderItems);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getById(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
                
        User user = authUtil.getAuthenticatedUser();
        if (!isAdmin() && !order.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Cannot access other users' orders");
        }
        
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        return mapToResponse(order, items);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAll() {
        return orderRepository.findAll().stream()
                .filter(Order::isActive)
                .map(order -> {
                    List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
                    return mapToResponse(order, items);
                })
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders() {
        User user = authUtil.getAuthenticatedUser();
        if (user == null) {
            throw new AccessDeniedException("Must be logged in to view orders");
        }
        
        return orderRepository.findByUserId(user.getId()).stream()
                .filter(Order::isActive)
                .map(order -> {
                    List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
                    return mapToResponse(order, items);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(UUID id, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        order.setStatus(request.getStatus());
        order = orderRepository.save(order);
        
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        return mapToResponse(order, items);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        order.setActive(false);
        orderRepository.save(order);
    }
    
    private OrderResponse mapToResponse(Order order, List<OrderItem> items) {
        OrderResponse response = orderMapper.toResponse(order);
        List<OrderItemResponse> itemResponses = items.stream().map(item -> {
            OrderItemResponse itemRes = orderItemMapper.toResponse(item);
            itemRes.setLineTotal(item.getUnitPriceAtPurchase().multiply(BigDecimal.valueOf(item.getQuantity())));
            return itemRes;
        }).collect(Collectors.toList());
        
        response.setItems(itemResponses);
        return response;
    }
    
    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
