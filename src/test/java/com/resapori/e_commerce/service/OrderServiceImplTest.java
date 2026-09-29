package com.resapori.e_commerce.service;

import com.resapori.e_commerce.common.security.AuthUtil;
import com.resapori.e_commerce.northbound.dto.delivery.DeliveryFeeResponse;
import com.resapori.e_commerce.northbound.dto.order.*;
import com.resapori.e_commerce.service.impl.OrderServiceImpl;
import com.resapori.e_commerce.southbound.entity.*;
import com.resapori.e_commerce.southbound.enums.*;
import com.resapori.e_commerce.southbound.mapper.OrderItemMapper;
import com.resapori.e_commerce.southbound.mapper.OrderMapper;
import com.resapori.e_commerce.southbound.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private IOrderRepository orderRepository;
    @Mock
    private IOrderItemRepository orderItemRepository;
    @Mock
    private IMenuItemRepository menuItemRepository;
    @Mock
    private IBranchRepository branchRepository;
    @Mock
    private IUserAddressRepository userAddressRepository;
    @Mock
    private IPromoCodeRepository promoCodeRepository;
    @Mock
    private IPromoCodeRedemptionRepository promoCodeRedemptionRepository;
    @Mock
    private IOfferRepository offerRepository;
    @Mock
    private IMenuAddOnRepository menuAddOnRepository;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderItemMapper orderItemMapper;
    @Mock
    private AuthUtil authUtil;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private IDeliveryService deliveryService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User testUser;
    private Branch testBranch;
    private MenuItem pizzaItem;
    private MenuItem pestoItem;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("user@example.com");

        testBranch = new Branch();
        testBranch.setId(UUID.randomUUID());
        testBranch.setName("Main Branch");

        pizzaItem = new MenuItem();
        pizzaItem.setId(UUID.randomUUID());
        pizzaItem.setNameEn("Margherita Pizza");
        pizzaItem.setCurrentPrice(BigDecimal.valueOf(300.00));
        pizzaItem.setOriginalPrice(BigDecimal.valueOf(300.00));
        pizzaItem.setDiscountPrice(BigDecimal.valueOf(240.00));
        pizzaItem.setActive(true);
        pizzaItem.setAvailable(true);

        pestoItem = new MenuItem();
        pestoItem.setId(UUID.randomUUID());
        pestoItem.setNameEn("Chicken Pesto Pizza");
        pestoItem.setCurrentPrice(BigDecimal.valueOf(480.00));
        pestoItem.setActive(true);
        pestoItem.setAvailable(true);
    }

    @Test
    @DisplayName("placeOrder: Uses discountPrice when present on regular menu item")
    void placeOrder_UsesDiscountPrice() {
        mockCommonOrderFlow();

        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setOrderType(OrderType.PICKUP);
        request.setBranchId(testBranch.getId());
        request.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);

        OrderItemInput itemInput = new OrderItemInput();
        itemInput.setMenuItemId(pizzaItem.getId());
        itemInput.setQuantity(2);
        itemInput.setSize(ItemSize.REGULAR);
        request.setItems(List.of(itemInput));

        OrderResponse response = orderService.placeOrder(request);

        assertNotNull(response);
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());

        // 2 items * 240.00 discount price = 480.00
        assertEquals(0, BigDecimal.valueOf(480.00).compareTo(orderCaptor.getValue().getTotalAmount()));
    }

    @Test
    @DisplayName("placeOrder: Applies percentage promo code discount and records redemption")
    void placeOrder_AppliesPercentagePromoCode() {
        mockCommonOrderFlow();

        PromoCode promo = new PromoCode();
        promo.setId(UUID.randomUUID());
        promo.setCode("SAPORI10");
        promo.setDiscountType(DiscountType.PERCENTAGE);
        promo.setDiscountValue(BigDecimal.valueOf(10.00));
        promo.setCurrentUses(0);
        promo.setMaxUses(100);
        promo.setMaxUsesPerUser(1);
        promo.setActive(true);

        when(promoCodeRepository.findByCodeIgnoreCaseAndIsActiveTrue("SAPORI10")).thenReturn(Optional.of(promo));
        when(promoCodeRedemptionRepository.countByPromoCodeIdAndUserId(promo.getId(), testUser.getId())).thenReturn(0L);

        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setOrderType(OrderType.PICKUP);
        request.setBranchId(testBranch.getId());
        request.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
        request.setPromoCode("SAPORI10");

        OrderItemInput itemInput = new OrderItemInput();
        itemInput.setMenuItemId(pizzaItem.getId());
        itemInput.setQuantity(2);
        itemInput.setSize(ItemSize.REGULAR);
        request.setItems(List.of(itemInput));

        OrderResponse response = orderService.placeOrder(request);

        assertNotNull(response);
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());

        // Base total: 2 * 240 = 480. 10% discount = 48. New total = 432.00
        assertEquals(0, BigDecimal.valueOf(432.00).compareTo(orderCaptor.getValue().getTotalAmount()));
        verify(promoCodeRedemptionRepository).save(any(PromoCodeRedemption.class));
        verify(promoCodeRepository).incrementCurrentUses(promo.getId());
    }

    @Test
    @DisplayName("placeOrder: Applies CHEAPEST_ITEM offer pricing correctly (pay most expensive)")
    void placeOrder_AppliesCheapestItemOffer() {
        when(authUtil.getAuthenticatedUser()).thenReturn(testUser);
        when(branchRepository.findById(testBranch.getId())).thenReturn(Optional.of(testBranch));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
        when(orderMapper.toResponse(any(Order.class))).thenReturn(new OrderResponse());
        when(orderItemMapper.toResponse(any(OrderItem.class))).thenReturn(new OrderItemResponse());

        UUID offerId = UUID.randomUUID();
        UUID slot1Id = UUID.randomUUID();
        UUID slot2Id = UUID.randomUUID();

        OfferSlot slot1 = new OfferSlot();
        slot1.setId(slot1Id);
        slot1.setSlotNameEn("Pizza 1");
        slot1.setFree(false);

        OfferSlot slot2 = new OfferSlot();
        slot2.setId(slot2Id);
        slot2.setSlotNameEn("Pizza 2");
        slot2.setFree(false);

        Offer offer = new Offer();
        offer.setId(offerId);
        offer.setNameEn("Duo Pizza Deal");
        offer.setActive(true);
        offer.setDiscountTarget(DiscountTarget.CHEAPEST_ITEM);
        offer.setBuyQuantity(1);
        offer.setGetQuantity(1);
        offer.setDiscountPercentage(BigDecimal.valueOf(100.00));
        offer.setSlots(List.of(slot1, slot2));

        when(offerRepository.findByIdWithSlots(offerId)).thenReturn(Optional.of(offer));
        when(menuItemRepository.findAllById(any())).thenReturn(List.of(pestoItem, pizzaItem));

        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setOrderType(OrderType.PICKUP);
        request.setBranchId(testBranch.getId());
        request.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);

        OrderOfferInput offerInput = new OrderOfferInput();
        offerInput.setOfferId(offerId);
        offerInput.setQuantity(1);
        offerInput.setSelections(List.of(
                OfferSelectionInput.builder().slotId(slot1Id).menuItemId(pestoItem.getId()).build(),
                OfferSelectionInput.builder().slotId(slot2Id).menuItemId(pizzaItem.getId()).build()
        ));
        request.setOffers(List.of(offerInput));

        OrderResponse response = orderService.placeOrder(request);

        assertNotNull(response);
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());

        // Pesto (480) + Margherita (300). Pay most expensive = 480.00
        assertEquals(0, BigDecimal.valueOf(480.00).compareTo(orderCaptor.getValue().getTotalAmount()));
    }

    @Test
    @DisplayName("placeOrder: Applies TOTAL_BUNDLE discount percentage correctly")
    void placeOrder_AppliesTotalBundleDiscount() {
        when(authUtil.getAuthenticatedUser()).thenReturn(testUser);
        when(branchRepository.findById(testBranch.getId())).thenReturn(Optional.of(testBranch));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
        when(orderMapper.toResponse(any(Order.class))).thenReturn(new OrderResponse());
        when(orderItemMapper.toResponse(any(OrderItem.class))).thenReturn(new OrderItemResponse());

        UUID offerId = UUID.randomUUID();
        UUID slot1Id = UUID.randomUUID();
        OfferSlot slot1 = new OfferSlot();
        slot1.setId(slot1Id);
        slot1.setSlotNameEn("Pizzas");
        slot1.setQuantity(2);
        slot1.setFree(false);

        Offer offer = new Offer();
        offer.setId(offerId);
        offer.setNameEn("10% Off Bundle");
        offer.setActive(true);
        offer.setDiscountTarget(DiscountTarget.TOTAL_BUNDLE);
        offer.setDiscountPercentage(BigDecimal.valueOf(10.00));
        offer.setSlots(List.of(slot1));

        when(offerRepository.findByIdWithSlots(offerId)).thenReturn(Optional.of(offer));
        when(menuItemRepository.findAllById(any())).thenReturn(List.of(pestoItem));

        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setOrderType(OrderType.PICKUP);
        request.setBranchId(testBranch.getId());
        request.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);

        OrderOfferInput offerInput = new OrderOfferInput();
        offerInput.setOfferId(offerId);
        offerInput.setSelections(List.of(
                OfferSelectionInput.builder().slotId(slot1Id).menuItemId(pestoItem.getId()).quantity(2).build()
        ));
        request.setOffers(List.of(offerInput));

        OrderResponse response = orderService.placeOrder(request);
        assertNotNull(response);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());

        // 2 * 480 = 960. 10% off each = 432 * 2 = 864.00
        assertEquals(0, BigDecimal.valueOf(864.00).compareTo(orderCaptor.getValue().getTotalAmount()));
    }

    @Test
    @DisplayName("placeOrder: Applies FIXED_PRICE meal with complimentary free item")
    void placeOrder_AppliesFixedPriceWithComplimentaryFreeItem() {
        when(authUtil.getAuthenticatedUser()).thenReturn(testUser);
        when(branchRepository.findById(testBranch.getId())).thenReturn(Optional.of(testBranch));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
        when(orderMapper.toResponse(any(Order.class))).thenReturn(new OrderResponse());
        when(orderItemMapper.toResponse(any(OrderItem.class))).thenReturn(new OrderItemResponse());

        UUID offerId = UUID.randomUUID();
        UUID mealSlotId = UUID.randomUUID();
        UUID drinkSlotId = UUID.randomUUID();

        OfferSlot mealSlot = new OfferSlot();
        mealSlot.setId(mealSlotId);
        mealSlot.setFree(false);

        OfferSlot drinkSlot = new OfferSlot();
        drinkSlot.setId(drinkSlotId);
        drinkSlot.setFree(true);

        Offer offer = new Offer();
        offer.setId(offerId);
        offer.setNameEn("Single Meal Combo");
        offer.setActive(true);
        offer.setDiscountTarget(DiscountTarget.FIXED_PRICE);
        offer.setFixedPrice(BigDecimal.valueOf(250.00));
        offer.setSlots(List.of(mealSlot, drinkSlot));

        MenuItem drink = new MenuItem();
        drink.setId(UUID.randomUUID());
        drink.setNameEn("Pepsi");
        drink.setCurrentPrice(BigDecimal.valueOf(35.00));
        drink.setActive(true);
        drink.setAvailable(true);

        when(offerRepository.findByIdWithSlots(offerId)).thenReturn(Optional.of(offer));
        when(menuItemRepository.findAllById(any())).thenReturn(List.of(pizzaItem, drink));

        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setOrderType(OrderType.PICKUP);
        request.setBranchId(testBranch.getId());
        request.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);

        OrderOfferInput offerInput = new OrderOfferInput();
        offerInput.setOfferId(offerId);
        offerInput.setSelections(List.of(
                OfferSelectionInput.builder().slotId(mealSlotId).menuItemId(pizzaItem.getId()).build(),
                OfferSelectionInput.builder().slotId(drinkSlotId).menuItemId(drink.getId()).build()
        ));
        request.setOffers(List.of(offerInput));

        OrderResponse response = orderService.placeOrder(request);
        assertNotNull(response);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());

        // Fixed price 250.00 + Drink 0.00 = 250.00
        assertEquals(0, BigDecimal.valueOf(250.00).compareTo(orderCaptor.getValue().getTotalAmount()));
    }

    @Test
    @DisplayName("placeOrder: Applies Family Meal Cheapest Item discount (4 pizzas, 2 sauces, 1 free drink)")
    void placeOrder_AppliesFamilyMealCheapestItemDiscount() {
        when(authUtil.getAuthenticatedUser()).thenReturn(testUser);
        when(branchRepository.findById(testBranch.getId())).thenReturn(Optional.of(testBranch));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
        when(orderMapper.toResponse(any(Order.class))).thenReturn(new OrderResponse());
        when(orderItemMapper.toResponse(any(OrderItem.class))).thenReturn(new OrderItemResponse());

        UUID offerId = UUID.randomUUID();
        UUID pizzaSlotId = UUID.randomUUID();
        UUID sauceSlotId = UUID.randomUUID();
        UUID drinkSlotId = UUID.randomUUID();

        OfferSlot pizzaSlot = new OfferSlot();
        pizzaSlot.setId(pizzaSlotId);
        pizzaSlot.setSlotNameEn("Pizzas");
        pizzaSlot.setQuantity(4);
        pizzaSlot.setFree(false);

        OfferSlot sauceSlot = new OfferSlot();
        sauceSlot.setId(sauceSlotId);
        sauceSlot.setSlotNameEn("Sauces");
        sauceSlot.setQuantity(2);
        sauceSlot.setFree(false);

        OfferSlot drinkSlot = new OfferSlot();
        drinkSlot.setId(drinkSlotId);
        drinkSlot.setSlotNameEn("Drink");
        drinkSlot.setQuantity(1);
        drinkSlot.setFree(true);

        Offer offer = new Offer();
        offer.setId(offerId);
        offer.setNameEn("Family Meal");
        offer.setActive(true);
        offer.setDiscountTarget(DiscountTarget.CHEAPEST_ITEM);
        offer.setDiscountPercentage(BigDecimal.valueOf(100.00));
        offer.setBuyQuantity(3);
        offer.setGetQuantity(1);
        offer.setSlots(List.of(pizzaSlot, sauceSlot, drinkSlot));

        MenuItem salmon = createTestItem("Creamy Salmon", BigDecimal.valueOf(650.00));
        MenuItem pesto = createTestItem("Chicken Pesto", BigDecimal.valueOf(480.00));
        MenuItem pepperoni = createTestItem("Pepperoni", BigDecimal.valueOf(450.00));
        MenuItem margherita = createTestItem("Margherita", BigDecimal.valueOf(240.00));
        MenuItem bbq = createTestItem("Barbecue Sauce", BigDecimal.valueOf(20.00));
        MenuItem ranch = createTestItem("Ranch Sauce", BigDecimal.valueOf(20.00));
        MenuItem juice = createTestItem("Fresh Juice", BigDecimal.valueOf(90.00));

        when(offerRepository.findByIdWithSlots(offerId)).thenReturn(Optional.of(offer));
        when(menuItemRepository.findAllById(any())).thenReturn(List.of(salmon, pesto, pepperoni, margherita, bbq, ranch, juice));

        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setOrderType(OrderType.PICKUP);
        request.setBranchId(testBranch.getId());
        request.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);

        OrderOfferInput offerInput = new OrderOfferInput();
        offerInput.setOfferId(offerId);
        offerInput.setSelections(List.of(
                OfferSelectionInput.builder().slotId(pizzaSlotId).menuItemId(salmon.getId()).build(),
                OfferSelectionInput.builder().slotId(pizzaSlotId).menuItemId(pesto.getId()).build(),
                OfferSelectionInput.builder().slotId(pizzaSlotId).menuItemId(pepperoni.getId()).build(),
                OfferSelectionInput.builder().slotId(pizzaSlotId).menuItemId(margherita.getId()).build(),
                OfferSelectionInput.builder().slotId(sauceSlotId).menuItemId(bbq.getId()).build(),
                OfferSelectionInput.builder().slotId(sauceSlotId).menuItemId(ranch.getId()).build(),
                OfferSelectionInput.builder().slotId(drinkSlotId).menuItemId(juice.getId()).build()
        ));
        request.setOffers(List.of(offerInput));

        OrderResponse response = orderService.placeOrder(request);
        assertNotNull(response);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());

        // 650 + 480 + 450 + 0 (cheapest Margherita 240 is 100% off) + 20 + 20 + 0 (drink free) = 1,620.00 EGP
        assertEquals(0, BigDecimal.valueOf(1620.00).compareTo(orderCaptor.getValue().getTotalAmount()));
    }

    @Test
    @DisplayName("placeOrder: Rejects slot quantity under-selection")
    void placeOrder_RejectsSlotQuantityMismatch() {
        when(authUtil.getAuthenticatedUser()).thenReturn(testUser);
        when(branchRepository.findById(testBranch.getId())).thenReturn(Optional.of(testBranch));

        UUID offerId = UUID.randomUUID();
        UUID pizzaSlotId = UUID.randomUUID();

        OfferSlot pizzaSlot = new OfferSlot();
        pizzaSlot.setId(pizzaSlotId);
        pizzaSlot.setSlotNameEn("Pizzas");
        pizzaSlot.setQuantity(4);

        Offer offer = new Offer();
        offer.setId(offerId);
        offer.setNameEn("Family Meal");
        offer.setActive(true);
        offer.setSlots(List.of(pizzaSlot));

        when(offerRepository.findByIdWithSlots(offerId)).thenReturn(Optional.of(offer));

        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setOrderType(OrderType.PICKUP);
        request.setBranchId(testBranch.getId());
        request.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);

        OrderOfferInput offerInput = new OrderOfferInput();
        offerInput.setOfferId(offerId);
        // Only 3 pizzas submitted instead of 4
        offerInput.setSelections(List.of(
                OfferSelectionInput.builder().slotId(pizzaSlotId).menuItemId(UUID.randomUUID()).build(),
                OfferSelectionInput.builder().slotId(pizzaSlotId).menuItemId(UUID.randomUUID()).build(),
                OfferSelectionInput.builder().slotId(pizzaSlotId).menuItemId(UUID.randomUUID()).build()
        ));
        request.setOffers(List.of(offerInput));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> orderService.placeOrder(request));
        assertTrue(ex.getMessage().contains("requires 4, got 3"));
    }

    private MenuItem createTestItem(String name, BigDecimal price) {
        MenuItem item = new MenuItem();
        item.setId(UUID.randomUUID());
        item.setNameEn(name);
        item.setCurrentPrice(price);
        item.setActive(true);
        item.setAvailable(true);
        return item;
    }

    private void mockCommonOrderFlow() {
        when(authUtil.getAuthenticatedUser()).thenReturn(testUser);
        when(branchRepository.findById(testBranch.getId())).thenReturn(Optional.of(testBranch));
        when(menuItemRepository.findAllById(any())).thenReturn(List.of(pizzaItem));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderMapper.toResponse(any(Order.class))).thenReturn(new OrderResponse());
        when(orderItemMapper.toResponse(any(OrderItem.class))).thenReturn(new OrderItemResponse());
    }

    @Test
    @DisplayName("placeOrder: For DELIVERY orders, calculates delivery fee and adds it to total amount")
    void placeOrder_DeliveryOrder_CalculatesFeeAndSetsTotal() {
        when(authUtil.getAuthenticatedUser()).thenReturn(testUser);
        when(menuItemRepository.findAllById(any())).thenReturn(List.of(pizzaItem));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderMapper.toResponse(any(Order.class))).thenReturn(new OrderResponse());
        when(orderItemMapper.toResponse(any(OrderItem.class))).thenReturn(new OrderItemResponse());

        UUID addressId = UUID.randomUUID();
        UserAddress address = new UserAddress();
        address.setId(addressId);
        address.setUser(testUser);
        address.setStreet("10 El-Batal Ahmed Abdel Aziz");
        address.setCity("Giza");

        when(userAddressRepository.findById(addressId)).thenReturn(Optional.of(address));

        DeliveryFeeResponse feeResponse = DeliveryFeeResponse.builder()
                .distanceKm(new BigDecimal("6.00"))
                .deliveryFee(new BigDecimal("49.00")) // 35 + 7(2) = 49.00
                .branchId(testBranch.getId())
                .branchName("Main Branch")
                .currency("EGP")
                .build();

        when(deliveryService.calculateForAddress(addressId, null)).thenReturn(feeResponse);
        when(branchRepository.findById(testBranch.getId())).thenReturn(Optional.of(testBranch));

        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setOrderType(OrderType.DELIVERY);
        request.setAddressId(addressId);
        request.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);

        OrderItemInput itemInput = new OrderItemInput();
        itemInput.setMenuItemId(pizzaItem.getId());
        itemInput.setQuantity(1); // 240 EGP (discounted price)
        itemInput.setSize(ItemSize.REGULAR);
        request.setItems(List.of(itemInput));

        OrderResponse response = orderService.placeOrder(request);

        assertNotNull(response);
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());

        Order saved = orderCaptor.getValue();
        assertEquals(0, new BigDecimal("49.00").compareTo(saved.getDeliveryFee()));
        // Items: 240.00 + Delivery Fee: 49.00 = 289.00
        assertEquals(0, new BigDecimal("289.00").compareTo(saved.getTotalAmount()));
        assertEquals(testBranch, saved.getBranch());
    }

    @Test
    @DisplayName("placeOrder: For Car Delivery orders, sets delivery fee to ZERO")
    void placeOrder_CarDeliveryOrder_SetsZeroDeliveryFee() {
        when(authUtil.getAuthenticatedUser()).thenReturn(testUser);
        when(menuItemRepository.findAllById(any())).thenReturn(List.of(pizzaItem));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderMapper.toResponse(any(Order.class))).thenReturn(new OrderResponse());
        when(orderItemMapper.toResponse(any(OrderItem.class))).thenReturn(new OrderItemResponse());

        UUID carAddrId = UUID.randomUUID();
        UserAddress carAddress = new UserAddress();
        carAddress.setId(carAddrId);
        carAddress.setUser(testUser);
        carAddress.setLabel("Car Delivery");
        carAddress.setStreet("Car Delivery - Plate: ABC 123");
        when(userAddressRepository.findById(carAddrId)).thenReturn(Optional.of(carAddress));
        when(branchRepository.findByIsActiveTrue()).thenReturn(List.of(testBranch));

        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setOrderType(OrderType.DELIVERY);
        request.setAddressId(carAddrId);
        request.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);

        OrderItemInput itemInput = new OrderItemInput();
        itemInput.setMenuItemId(pizzaItem.getId());
        itemInput.setQuantity(1); // 240 EGP
        itemInput.setSize(ItemSize.REGULAR);
        request.setItems(List.of(itemInput));

        OrderResponse response = orderService.placeOrder(request);

        assertNotNull(response);
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository, atLeastOnce()).save(orderCaptor.capture());

        Order saved = orderCaptor.getValue();
        assertEquals(0, BigDecimal.ZERO.compareTo(saved.getDeliveryFee()));
        // Items: 240.00 + Delivery Fee: 0.00 = 240.00
        assertEquals(0, new BigDecimal("240.00").compareTo(saved.getTotalAmount()));
    }
}
