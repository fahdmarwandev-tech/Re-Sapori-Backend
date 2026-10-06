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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pre-Diagnostics Order Consistency Test Suite (TC-01 through TC-26)
 * Validates that all 26 predefined scenarios are correctly calculated, persisted, and mapped.
 */
@ExtendWith(MockitoExtension.class)
class OrderConsistencyTestSuiteTest {

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
    private IOrderItemAddOnRepository orderItemAddOnRepository;
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
    private MenuItem margherita;
    private MenuItem chickenPesto;
    private MenuItem saladItem;
    private MenuAddOn extraCheese;
    private MenuAddOn truffleSauce;
    private MenuAddOn freeDip;

    @BeforeEach
    void setUp() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();

        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("guest@example.com");

        testBranch = new Branch();
        testBranch.setId(UUID.randomUUID());
        testBranch.setName("Zamalek Branch");

        margherita = new MenuItem();
        margherita.setId(UUID.randomUUID());
        margherita.setNameEn("Margherita Pizza");
        margherita.setNameAr("بيتزا مارجريتا");
        margherita.setCurrentPrice(BigDecimal.valueOf(250.00));
        margherita.setOriginalPrice(BigDecimal.valueOf(250.00));
        margherita.setDiscountPrice(BigDecimal.valueOf(200.00));
        margherita.setMiniPrice(BigDecimal.valueOf(150.00));
        margherita.setActive(true);
        margherita.setAvailable(true);

        chickenPesto = new MenuItem();
        chickenPesto.setId(UUID.randomUUID());
        chickenPesto.setNameEn("Chicken Pesto Pizza");
        chickenPesto.setNameAr("بيتزا بيستو دجاج");
        chickenPesto.setCurrentPrice(BigDecimal.valueOf(320.00));
        chickenPesto.setActive(true);
        chickenPesto.setAvailable(true);

        saladItem = new MenuItem();
        saladItem.setId(UUID.randomUUID());
        saladItem.setNameEn("Caesar Salad");
        saladItem.setNameAr("سلطة سيزر");
        saladItem.setCurrentPrice(BigDecimal.valueOf(140.00));
        saladItem.setActive(true);
        saladItem.setAvailable(true);

        extraCheese = new MenuAddOn();
        extraCheese.setId(UUID.randomUUID());
        extraCheese.setNameEn("Extra Mozzarella");
        extraCheese.setNameAr("جبنة موزاريلا إضافية");
        extraCheese.setPrice(BigDecimal.valueOf(35.00));
        extraCheese.setActive(true);

        truffleSauce = new MenuAddOn();
        truffleSauce.setId(UUID.randomUUID());
        truffleSauce.setNameEn("Truffle Cream");
        truffleSauce.setNameAr("كريمة ترفل");
        truffleSauce.setPrice(BigDecimal.valueOf(45.00));
        truffleSauce.setActive(true);

        freeDip = new MenuAddOn();
        freeDip.setId(UUID.randomUUID());
        freeDip.setNameEn("Complimentary Garlic Dip");
        freeDip.setNameAr("ثومية مجانية");
        freeDip.setPrice(BigDecimal.ZERO);
        freeDip.setActive(true);
    }

    private void mockStandardOrderSaves() {
        when(authUtil.getAuthenticatedUser()).thenReturn(testUser);
        when(branchRepository.findById(testBranch.getId())).thenReturn(Optional.of(testBranch));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
        when(orderItemRepository.saveAll(any())).thenAnswer(i -> i.getArgument(0));
        when(orderMapper.toResponse(any(Order.class))).thenAnswer(i -> {
            Order o = i.getArgument(0);
            return OrderResponse.builder()
                    .id(o.getId())
                    .totalAmount(o.getTotalAmount())
                    .orderNotes(o.getOrderNotes())
                    .status(o.getStatus())
                    .orderType(o.getOrderType())
                    .build();
        });
        when(orderItemMapper.toResponse(any(OrderItem.class))).thenAnswer(i -> {
            OrderItem oi = i.getArgument(0);
            return OrderItemResponse.builder()
                    .id(oi.getId())
                    .menuItemId(oi.getMenuItem().getId())
                    .nameEn(oi.getMenuItem().getNameEn())
                    .nameAr(oi.getMenuItem().getNameAr())
                    .quantity(oi.getQuantity())
                    .unitPriceAtPurchase(oi.getUnitPriceAtPurchase())
                    .lineTotal(oi.getUnitPriceAtPurchase().multiply(BigDecimal.valueOf(oi.getQuantity())))
                    .size(oi.getSize())
                    .isFree(oi.isFree())
                    .bundleGroupId(oi.getBundleGroupId())
                    .notes(oi.getNotes())
                    .build();
        });
    }

    // TC-01: Standard Regular Item
    @Test
    @DisplayName("TC-01: Standard Regular Item - single item, qty 1, standard size, 0 addons")
    void test_TC01_StandardRegularItem() {
        mockStandardOrderSaves();
        when(menuItemRepository.findAllById(any())).thenReturn(List.of(margherita));

        PlaceOrderRequest req = new PlaceOrderRequest();
        req.setOrderType(OrderType.PICKUP);
        req.setBranchId(testBranch.getId());
        req.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
        OrderItemInput item = new OrderItemInput();
        item.setMenuItemId(margherita.getId());
        item.setQuantity(1);
        item.setSize(ItemSize.REGULAR);
        req.setItems(List.of(item));

        OrderResponse res = orderService.placeOrder(req);

        assertNotNull(res);
        ArgumentCaptor<List<OrderItem>> itemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(orderItemRepository).saveAll(itemsCaptor.capture());
        OrderItem saved = itemsCaptor.getValue().get(0);

        assertEquals(margherita.getNameEn(), saved.getMenuItem().getNameEn());
        assertEquals(1, saved.getQuantity());
        assertEquals(ItemSize.REGULAR, saved.getSize());
        assertEquals(0, BigDecimal.valueOf(200.00).compareTo(saved.getUnitPriceAtPurchase()));
        assertTrue(saved.getAddOns().isEmpty());
    }

    // TC-02: Size Variant Item (MINI)
    @Test
    @DisplayName("TC-02: Size Variant Item (MINI) - resolves mini price and records MINI size")
    void test_TC02_SizeVariantItem_Mini() {
        mockStandardOrderSaves();
        when(menuItemRepository.findAllById(any())).thenReturn(List.of(margherita));

        PlaceOrderRequest req = new PlaceOrderRequest();
        req.setOrderType(OrderType.PICKUP);
        req.setBranchId(testBranch.getId());
        req.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
        OrderItemInput item = new OrderItemInput();
        item.setMenuItemId(margherita.getId());
        item.setQuantity(1);
        item.setSize(ItemSize.MINI);
        req.setItems(List.of(item));

        orderService.placeOrder(req);

        ArgumentCaptor<List<OrderItem>> itemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(orderItemRepository).saveAll(itemsCaptor.capture());
        OrderItem saved = itemsCaptor.getValue().get(0);

        assertEquals(ItemSize.MINI, saved.getSize());
        assertEquals(0, BigDecimal.valueOf(150.00).compareTo(saved.getUnitPriceAtPurchase()));
    }

    // TC-03: Multi-Quantity Regular Item
    @Test
    @DisplayName("TC-03: Multi-Quantity Regular Item - quantity = 4, total reflects 4x unit price")
    void test_TC03_MultiQuantityRegularItem() {
        mockStandardOrderSaves();
        when(menuItemRepository.findAllById(any())).thenReturn(List.of(chickenPesto));

        PlaceOrderRequest req = new PlaceOrderRequest();
        req.setOrderType(OrderType.PICKUP);
        req.setBranchId(testBranch.getId());
        req.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
        OrderItemInput item = new OrderItemInput();
        item.setMenuItemId(chickenPesto.getId());
        item.setQuantity(4);
        item.setSize(ItemSize.REGULAR);
        req.setItems(List.of(item));

        orderService.placeOrder(req);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        // 4 * 320 = 1280.00
        assertEquals(0, BigDecimal.valueOf(1280.00).compareTo(orderCaptor.getValue().getTotalAmount()));
    }

    // TC-04: Item with Single Add-On
    @Test
    @DisplayName("TC-04: Item with Single Add-On - extra cheese +35 EGP added to item total")
    void test_TC04_ItemWithSingleAddOn() {
        mockStandardOrderSaves();
        when(menuItemRepository.findAllById(any())).thenReturn(List.of(margherita));
        when(menuAddOnRepository.findAllById(any())).thenReturn(List.of(extraCheese));

        PlaceOrderRequest req = new PlaceOrderRequest();
        req.setOrderType(OrderType.PICKUP);
        req.setBranchId(testBranch.getId());
        req.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
        OrderItemInput item = new OrderItemInput();
        item.setMenuItemId(margherita.getId());
        item.setQuantity(1);
        item.setSize(ItemSize.REGULAR);
        item.setAddOnIds(List.of(extraCheese.getId()));
        req.setItems(List.of(item));

        orderService.placeOrder(req);

        ArgumentCaptor<List<OrderItem>> itemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(orderItemRepository).saveAll(itemsCaptor.capture());
        OrderItem saved = itemsCaptor.getValue().get(0);

        // 200 + 35 = 235.00
        assertEquals(0, BigDecimal.valueOf(235.00).compareTo(saved.getUnitPriceAtPurchase()));
        assertEquals(1, saved.getAddOns().size());
        assertEquals("Extra Mozzarella", saved.getAddOns().get(0).getNameEn());
        assertEquals(0, BigDecimal.valueOf(35.00).compareTo(saved.getAddOns().get(0).getPrice()));
    }

    // TC-05: Item with Multiple Add-Ons
    @Test
    @DisplayName("TC-05: Item with Multiple Add-Ons - extra cheese (+35) and truffle (+45)")
    void test_TC05_ItemWithMultipleAddOns() {
        mockStandardOrderSaves();
        when(menuItemRepository.findAllById(any())).thenReturn(List.of(margherita));
        when(menuAddOnRepository.findAllById(any())).thenReturn(List.of(extraCheese, truffleSauce));

        PlaceOrderRequest req = new PlaceOrderRequest();
        req.setOrderType(OrderType.PICKUP);
        req.setBranchId(testBranch.getId());
        req.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
        OrderItemInput item = new OrderItemInput();
        item.setMenuItemId(margherita.getId());
        item.setQuantity(1);
        item.setAddOnIds(List.of(extraCheese.getId(), truffleSauce.getId()));
        req.setItems(List.of(item));

        orderService.placeOrder(req);

        ArgumentCaptor<List<OrderItem>> itemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(orderItemRepository).saveAll(itemsCaptor.capture());
        OrderItem saved = itemsCaptor.getValue().get(0);

        // 200 + 35 + 45 = 280.00
        assertEquals(0, BigDecimal.valueOf(280.00).compareTo(saved.getUnitPriceAtPurchase()));
        assertEquals(2, saved.getAddOns().size());
    }

    // TC-06: Multi-Qty Item with Add-Ons
    @Test
    @DisplayName("TC-06: Multi-Qty Item with Add-Ons - qty 2, addons billed per item in unit price")
    void test_TC06_MultiQtyItemWithAddOns() {
        mockStandardOrderSaves();
        when(menuItemRepository.findAllById(any())).thenReturn(List.of(margherita));
        when(menuAddOnRepository.findAllById(any())).thenReturn(List.of(extraCheese));

        PlaceOrderRequest req = new PlaceOrderRequest();
        req.setOrderType(OrderType.PICKUP);
        req.setBranchId(testBranch.getId());
        req.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
        OrderItemInput item = new OrderItemInput();
        item.setMenuItemId(margherita.getId());
        item.setQuantity(2);
        item.setAddOnIds(List.of(extraCheese.getId()));
        req.setItems(List.of(item));

        orderService.placeOrder(req);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        // (200 + 35) * 2 = 470.00
        assertEquals(0, BigDecimal.valueOf(470.00).compareTo(orderCaptor.getValue().getTotalAmount()));
    }

    // TC-07: Custom Notes & Exclusions
    @Test
    @DisplayName("TC-07: Custom Notes & Exclusions - item notes and order instructions preserved")
    void test_TC07_CustomNotesAndExclusions() {
        mockStandardOrderSaves();
        when(menuItemRepository.findAllById(any())).thenReturn(List.of(margherita));

        PlaceOrderRequest req = new PlaceOrderRequest();
        req.setOrderType(OrderType.PICKUP);
        req.setBranchId(testBranch.getId());
        req.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
        req.setOrderNotes("Ring doorbell twice and leave on doorstep");

        OrderItemInput item = new OrderItemInput();
        item.setMenuItemId(margherita.getId());
        item.setQuantity(1);
        item.setNotes("No onions, extra crispy crust");
        req.setItems(List.of(item));

        orderService.placeOrder(req);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        assertEquals("Ring doorbell twice and leave on doorstep", orderCaptor.getValue().getOrderNotes());

        ArgumentCaptor<List<OrderItem>> itemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(orderItemRepository).saveAll(itemsCaptor.capture());
        assertEquals("No onions, extra crispy crust", itemsCaptor.getValue().get(0).getNotes());
    }

    // TC-08: Multi-Category Regular Items
    @Test
    @DisplayName("TC-08: Multi-Category Regular Items - Pizza + Salad in cart correctly saved")
    void test_TC08_MultiCategoryRegularItems() {
        mockStandardOrderSaves();
        when(menuItemRepository.findAllById(any())).thenReturn(List.of(margherita, saladItem));

        PlaceOrderRequest req = new PlaceOrderRequest();
        req.setOrderType(OrderType.PICKUP);
        req.setBranchId(testBranch.getId());
        req.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);

        OrderItemInput item1 = new OrderItemInput();
        item1.setMenuItemId(margherita.getId());
        item1.setQuantity(1);

        OrderItemInput item2 = new OrderItemInput();
        item2.setMenuItemId(saladItem.getId());
        item2.setQuantity(2);

        req.setItems(List.of(item1, item2));

        orderService.placeOrder(req);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        // 200 + (2 * 140) = 480.00
        assertEquals(0, BigDecimal.valueOf(480.00).compareTo(orderCaptor.getValue().getTotalAmount()));
    }

    // TC-09: Fixed-Price Offer (Single Item/Slot)
    @Test
    @DisplayName("TC-09: Fixed-Price Offer (Single Item/Slot) - Bundle header, bundleGroupId, fixed price")
    void test_TC09_FixedPriceOffer_SingleSlot() {
        mockStandardOrderSaves();
        UUID offerId = UUID.randomUUID();
        UUID slot1Id = UUID.randomUUID();
        UUID slot2Id = UUID.randomUUID();

        OfferSlot slot1 = new OfferSlot();
        slot1.setId(slot1Id);
        slot1.setSlotNameEn("First Pizza");
        slot1.setQuantity(1);
        slot1.setFree(false);

        OfferSlot slot2 = new OfferSlot();
        slot2.setId(slot2Id);
        slot2.setSlotNameEn("Second Pizza");
        slot2.setQuantity(1);
        slot2.setFree(false);

        Offer fixedOffer = new Offer();
        fixedOffer.setId(offerId);
        fixedOffer.setNameEn("Duo Pizza Deal");
        fixedOffer.setDiscountTarget(DiscountTarget.FIXED_PRICE);
        fixedOffer.setFixedPrice(BigDecimal.valueOf(399.00));
        fixedOffer.setActive(true);
        fixedOffer.setSlots(List.of(slot1, slot2));

        when(offerRepository.findByIdWithSlots(offerId)).thenReturn(Optional.of(fixedOffer));
        when(menuItemRepository.findAllById(any())).thenReturn(List.of(margherita, chickenPesto));

        PlaceOrderRequest req = new PlaceOrderRequest();
        req.setOrderType(OrderType.PICKUP);
        req.setBranchId(testBranch.getId());
        req.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);

        OrderOfferInput offerInput = new OrderOfferInput();
        offerInput.setOfferId(offerId);
        offerInput.setSelections(List.of(
                OfferSelectionInput.builder().slotId(slot1Id).menuItemId(margherita.getId()).build(),
                OfferSelectionInput.builder().slotId(slot2Id).menuItemId(chickenPesto.getId()).build()
        ));
        req.setOffers(List.of(offerInput));

        orderService.placeOrder(req);

        ArgumentCaptor<List<OrderItem>> itemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(orderItemRepository).saveAll(itemsCaptor.capture());
        List<OrderItem> savedItems = itemsCaptor.getValue();
        assertEquals(2, savedItems.size());
        assertNotNull(savedItems.get(0).getBundleGroupId());
        assertEquals(savedItems.get(0).getBundleGroupId(), savedItems.get(1).getBundleGroupId());
        // First item carries the fixed price (399), second carries 0.00
        BigDecimal bundleSum = savedItems.get(0).getUnitPriceAtPurchase().add(savedItems.get(1).getUnitPriceAtPurchase());
        assertEquals(0, BigDecimal.valueOf(399.00).compareTo(bundleSum));
    }

    // TC-10: Fixed-Price Offer with Multi-Qty Slots
    @Test
    @DisplayName("TC-10: Fixed-Price Offer with Multi-Qty Slots - Slot with 2 items assigned same bundleGroupId")
    void test_TC10_FixedPriceOffer_MultiQtySlots() {
        mockStandardOrderSaves();
        UUID offerId = UUID.randomUUID();
        UUID slotId = UUID.randomUUID();

        OfferSlot slot = new OfferSlot();
        slot.setId(slotId);
        slot.setSlotNameEn("Choose 2 Pizzas");
        slot.setQuantity(2);
        slot.setFree(false);

        Offer multiSlotOffer = new Offer();
        multiSlotOffer.setId(offerId);
        multiSlotOffer.setNameEn("Trio Deal");
        multiSlotOffer.setDiscountTarget(DiscountTarget.FIXED_PRICE);
        multiSlotOffer.setFixedPrice(BigDecimal.valueOf(550.00));
        multiSlotOffer.setActive(true);
        multiSlotOffer.setSlots(List.of(slot));

        when(offerRepository.findByIdWithSlots(offerId)).thenReturn(Optional.of(multiSlotOffer));
        when(menuItemRepository.findAllById(any())).thenReturn(List.of(margherita, chickenPesto));

        PlaceOrderRequest req = new PlaceOrderRequest();
        req.setOrderType(OrderType.PICKUP);
        req.setBranchId(testBranch.getId());
        req.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);

        OrderOfferInput offerInput = new OrderOfferInput();
        offerInput.setOfferId(offerId);
        offerInput.setSelections(List.of(
                OfferSelectionInput.builder().slotId(slotId).menuItemId(margherita.getId()).build(),
                OfferSelectionInput.builder().slotId(slotId).menuItemId(chickenPesto.getId()).build()
        ));
        req.setOffers(List.of(offerInput));

        orderService.placeOrder(req);

        ArgumentCaptor<List<OrderItem>> itemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(orderItemRepository).saveAll(itemsCaptor.capture());
        List<OrderItem> savedItems = itemsCaptor.getValue();
        assertEquals(2, savedItems.size());
        assertEquals(savedItems.get(0).getBundleGroupId(), savedItems.get(1).getBundleGroupId());
    }

    // TC-11: Percentage Discount Offer Bundle
    @Test
    @DisplayName("TC-11: Percentage Discount Offer Bundle - 20% discount applied to bundle items")
    void test_TC11_PercentageDiscountOfferBundle() {
        mockStandardOrderSaves();
        UUID offerId = UUID.randomUUID();
        UUID slotId = UUID.randomUUID();

        OfferSlot slot1 = new OfferSlot();
        slot1.setId(slotId);
        slot1.setSlotNameEn("Any Pizza");
        slot1.setQuantity(1);
        slot1.setFree(false);

        Offer pctOffer = new Offer();
        pctOffer.setId(offerId);
        pctOffer.setNameEn("20 Percent Off Combo");
        pctOffer.setDiscountTarget(DiscountTarget.TOTAL_BUNDLE);
        pctOffer.setDiscountPercentage(BigDecimal.valueOf(20.00));
        pctOffer.setActive(true);
        pctOffer.setSlots(List.of(slot1));

        when(offerRepository.findByIdWithSlots(offerId)).thenReturn(Optional.of(pctOffer));
        when(menuItemRepository.findAllById(any())).thenReturn(List.of(chickenPesto));

        PlaceOrderRequest req = new PlaceOrderRequest();
        req.setOrderType(OrderType.PICKUP);
        req.setBranchId(testBranch.getId());
        req.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);

        OrderOfferInput offerInput = new OrderOfferInput();
        offerInput.setOfferId(offerId);
        offerInput.setSelections(List.of(
                OfferSelectionInput.builder().slotId(slotId).menuItemId(chickenPesto.getId()).build()
        ));
        req.setOffers(List.of(offerInput));

        orderService.placeOrder(req);

        ArgumentCaptor<List<OrderItem>> itemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(orderItemRepository).saveAll(itemsCaptor.capture());
        OrderItem saved = itemsCaptor.getValue().get(0);

        // 320 - 20% (64) = 256.00
        assertEquals(0, BigDecimal.valueOf(256.00).compareTo(saved.getUnitPriceAtPurchase()));
    }

    // TC-12: Cheapest-Item-Free Offer (BOGO)
    @Test
    @DisplayName("TC-12: Cheapest-Item-Free Offer (BOGO) - cheapest item unit price is 0.00 and isFree=true")
    void test_TC12_CheapestItemFreeOffer_BOGO() {
        mockStandardOrderSaves();
        UUID offerId = UUID.randomUUID();
        UUID slot1Id = UUID.randomUUID();
        UUID slot2Id = UUID.randomUUID();

        OfferSlot slot1 = new OfferSlot();
        slot1.setId(slot1Id);
        slot1.setSlotNameEn("Pizza 1");
        slot1.setQuantity(1);
        slot1.setFree(false);

        OfferSlot slot2 = new OfferSlot();
        slot2.setId(slot2Id);
        slot2.setSlotNameEn("Pizza 2");
        slot2.setQuantity(1);
        slot2.setFree(false);

        Offer bogoOffer = new Offer();
        bogoOffer.setId(offerId);
        bogoOffer.setNameEn("Buy 1 Get 1 Free");
        bogoOffer.setDiscountTarget(DiscountTarget.CHEAPEST_ITEM);
        bogoOffer.setBuyQuantity(1);
        bogoOffer.setGetQuantity(1);
        bogoOffer.setDiscountPercentage(BigDecimal.valueOf(100.00));
        bogoOffer.setActive(true);
        bogoOffer.setSlots(List.of(slot1, slot2));

        when(offerRepository.findByIdWithSlots(offerId)).thenReturn(Optional.of(bogoOffer));
        // margherita = 200, chickenPesto = 320 -> margherita is cheaper and free
        when(menuItemRepository.findAllById(any())).thenReturn(List.of(chickenPesto, margherita));

        PlaceOrderRequest req = new PlaceOrderRequest();
        req.setOrderType(OrderType.PICKUP);
        req.setBranchId(testBranch.getId());
        req.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);

        OrderOfferInput offerInput = new OrderOfferInput();
        offerInput.setOfferId(offerId);
        offerInput.setSelections(List.of(
                OfferSelectionInput.builder().slotId(slot1Id).menuItemId(chickenPesto.getId()).build(),
                OfferSelectionInput.builder().slotId(slot2Id).menuItemId(margherita.getId()).build()
        ));
        req.setOffers(List.of(offerInput));

        orderService.placeOrder(req);

        ArgumentCaptor<List<OrderItem>> itemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(orderItemRepository).saveAll(itemsCaptor.capture());
        List<OrderItem> savedItems = itemsCaptor.getValue();

        OrderItem freeItem = savedItems.stream().filter(OrderItem::isFree).findFirst().orElseThrow();
        assertEquals(margherita.getId(), freeItem.getMenuItem().getId());
        assertEquals(0, BigDecimal.ZERO.compareTo(freeItem.getUnitPriceAtPurchase()));
        assertTrue(freeItem.isFree());
    }

    // TC-13: Offer Item with Size Selection
    @Test
    @DisplayName("TC-13: Offer Item with Size Selection - size MINI recorded for offer slot item")
    void test_TC13_OfferItemWithSizeSelection() {
        mockStandardOrderSaves();
        UUID offerId = UUID.randomUUID();
        UUID slotId = UUID.randomUUID();

        OfferSlot slot = new OfferSlot();
        slot.setId(slotId);
        slot.setSlotNameEn("Mini Choice");
        slot.setQuantity(1);
        slot.setFree(false);

        Offer offer = new Offer();
        offer.setId(offerId);
        offer.setNameEn("Single Slice Deal");
        offer.setDiscountTarget(DiscountTarget.TOTAL_BUNDLE);
        offer.setDiscountPercentage(BigDecimal.valueOf(10.00));
        offer.setActive(true);
        offer.setSlots(List.of(slot));

        when(offerRepository.findByIdWithSlots(offerId)).thenReturn(Optional.of(offer));
        when(menuItemRepository.findAllById(any())).thenReturn(List.of(margherita));

        PlaceOrderRequest req = new PlaceOrderRequest();
        req.setOrderType(OrderType.PICKUP);
        req.setBranchId(testBranch.getId());
        req.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);

        OrderOfferInput offerInput = new OrderOfferInput();
        offerInput.setOfferId(offerId);
        offerInput.setSelections(List.of(
                OfferSelectionInput.builder().slotId(slotId).menuItemId(margherita.getId()).size(ItemSize.MINI).build()
        ));
        req.setOffers(List.of(offerInput));

        orderService.placeOrder(req);

        ArgumentCaptor<List<OrderItem>> itemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(orderItemRepository).saveAll(itemsCaptor.capture());
        OrderItem saved = itemsCaptor.getValue().get(0);

        assertEquals(ItemSize.MINI, saved.getSize());
        // 150 - 10% (15) = 135.00
        assertEquals(0, BigDecimal.valueOf(135.00).compareTo(saved.getUnitPriceAtPurchase()));
    }

    // TC-14: Offer Item with Single Add-On
    @Test
    @DisplayName("TC-14: Offer Item with Single Add-On - Slot item gets extra cheese add-on attached")
    void test_TC14_OfferItemWithSingleAddOn() {
        mockStandardOrderSaves();
        UUID offerId = UUID.randomUUID();
        UUID slotId = UUID.randomUUID();

        OfferSlot slot = new OfferSlot();
        slot.setId(slotId);
        slot.setSlotNameEn("Pizza");
        slot.setQuantity(1);
        slot.setFree(false);

        Offer offer = new Offer();
        offer.setId(offerId);
        offer.setNameEn("Special Pizza Deal");
        offer.setDiscountTarget(DiscountTarget.FIXED_PRICE);
        offer.setFixedPrice(BigDecimal.valueOf(250.00));
        offer.setActive(true);
        offer.setSlots(List.of(slot));

        when(offerRepository.findByIdWithSlots(offerId)).thenReturn(Optional.of(offer));
        when(menuItemRepository.findAllById(any())).thenReturn(List.of(margherita));
        when(menuAddOnRepository.findAllById(any())).thenReturn(List.of(extraCheese));

        PlaceOrderRequest req = new PlaceOrderRequest();
        req.setOrderType(OrderType.PICKUP);
        req.setBranchId(testBranch.getId());
        req.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);

        OrderOfferInput offerInput = new OrderOfferInput();
        offerInput.setOfferId(offerId);
        offerInput.setSelections(List.of(
                OfferSelectionInput.builder()
                        .slotId(slotId)
                        .menuItemId(margherita.getId())
                        .addOnIds(List.of(extraCheese.getId()))
                        .build()
        ));
        req.setOffers(List.of(offerInput));

        orderService.placeOrder(req);

        ArgumentCaptor<List<OrderItem>> itemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(orderItemRepository).saveAll(itemsCaptor.capture());
        OrderItem saved = itemsCaptor.getValue().get(0);

        // 250 fixed + 35 cheese = 285.00
        assertEquals(0, BigDecimal.valueOf(285.00).compareTo(saved.getUnitPriceAtPurchase()));
        assertEquals(1, saved.getAddOns().size());
        assertEquals("Extra Mozzarella", saved.getAddOns().get(0).getNameEn());
    }

    // TC-15: Offer Item with Multiple Add-Ons
    @Test
    @DisplayName("TC-15: Offer Item with Multiple Add-Ons - Slot 1 gets cheese, Slot 2 gets truffle")
    void test_TC15_OfferItemWithMultipleAddOns() {
        mockStandardOrderSaves();
        UUID offerId = UUID.randomUUID();
        UUID slot1Id = UUID.randomUUID();
        UUID slot2Id = UUID.randomUUID();

        OfferSlot slot1 = new OfferSlot();
        slot1.setId(slot1Id);
        slot1.setSlotNameEn("Pizza 1");
        slot1.setQuantity(1);
        slot1.setFree(false);

        OfferSlot slot2 = new OfferSlot();
        slot2.setId(slot2Id);
        slot2.setSlotNameEn("Pizza 2");
        slot2.setQuantity(1);
        slot2.setFree(false);

        Offer offer = new Offer();
        offer.setId(offerId);
        offer.setNameEn("Double Feast");
        offer.setDiscountTarget(DiscountTarget.FIXED_PRICE);
        offer.setFixedPrice(BigDecimal.valueOf(450.00));
        offer.setActive(true);
        offer.setSlots(List.of(slot1, slot2));

        when(offerRepository.findByIdWithSlots(offerId)).thenReturn(Optional.of(offer));
        when(menuItemRepository.findAllById(any())).thenReturn(List.of(margherita, chickenPesto));
        when(menuAddOnRepository.findAllById(any())).thenReturn(List.of(extraCheese, truffleSauce));

        PlaceOrderRequest req = new PlaceOrderRequest();
        req.setOrderType(OrderType.PICKUP);
        req.setBranchId(testBranch.getId());
        req.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);

        OrderOfferInput offerInput = new OrderOfferInput();
        offerInput.setOfferId(offerId);
        offerInput.setSelections(List.of(
                OfferSelectionInput.builder()
                        .slotId(slot1Id)
                        .menuItemId(margherita.getId())
                        .addOnIds(List.of(extraCheese.getId()))
                        .build(),
                OfferSelectionInput.builder()
                        .slotId(slot2Id)
                        .menuItemId(chickenPesto.getId())
                        .addOnIds(List.of(truffleSauce.getId()))
                        .build()
        ));
        req.setOffers(List.of(offerInput));

        orderService.placeOrder(req);

        ArgumentCaptor<List<OrderItem>> itemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(orderItemRepository).saveAll(itemsCaptor.capture());
        List<OrderItem> savedItems = itemsCaptor.getValue();

        assertEquals(1, savedItems.get(0).getAddOns().size());
        assertEquals("Extra Mozzarella", savedItems.get(0).getAddOns().get(0).getNameEn());

        assertEquals(1, savedItems.get(1).getAddOns().size());
        assertEquals("Truffle Cream", savedItems.get(1).getAddOns().get(0).getNameEn());
    }

    // TC-16: Offer with Complimentary Items
    @Test
    @DisplayName("TC-16: Offer with Complimentary Items - Slot configured with free=true marked isFree")
    void test_TC16_OfferWithComplimentaryItems() {
        mockStandardOrderSaves();
        UUID offerId = UUID.randomUUID();
        UUID mainSlotId = UUID.randomUUID();
        UUID freeSlotId = UUID.randomUUID();

        OfferSlot mainSlot = new OfferSlot();
        mainSlot.setId(mainSlotId);
        mainSlot.setSlotNameEn("Main Item");
        mainSlot.setQuantity(1);
        mainSlot.setFree(false);

        OfferSlot freeSlot = new OfferSlot();
        freeSlot.setId(freeSlotId);
        freeSlot.setSlotNameEn("Complimentary Item");
        freeSlot.setQuantity(1);
        freeSlot.setFree(true);

        Offer offer = new Offer();
        offer.setId(offerId);
        offer.setNameEn("Complimentary Offer");
        offer.setDiscountTarget(DiscountTarget.CHEAPEST_ITEM);
        offer.setActive(true);
        offer.setSlots(List.of(mainSlot, freeSlot));

        when(offerRepository.findByIdWithSlots(offerId)).thenReturn(Optional.of(offer));
        when(menuItemRepository.findAllById(any())).thenReturn(List.of(chickenPesto, saladItem));

        PlaceOrderRequest req = new PlaceOrderRequest();
        req.setOrderType(OrderType.PICKUP);
        req.setBranchId(testBranch.getId());
        req.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);

        OrderOfferInput offerInput = new OrderOfferInput();
        offerInput.setOfferId(offerId);
        offerInput.setSelections(List.of(
                OfferSelectionInput.builder().slotId(mainSlotId).menuItemId(chickenPesto.getId()).build(),
                OfferSelectionInput.builder().slotId(freeSlotId).menuItemId(saladItem.getId()).build()
        ));
        req.setOffers(List.of(offerInput));

        orderService.placeOrder(req);

        ArgumentCaptor<List<OrderItem>> itemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(orderItemRepository).saveAll(itemsCaptor.capture());
        OrderItem compItem = itemsCaptor.getValue().stream().filter(OrderItem::isFree).findFirst().orElseThrow();
        assertTrue(compItem.isFree());
        assertEquals(0, BigDecimal.ZERO.compareTo(compItem.getUnitPriceAtPurchase()));
    }

    // TC-17: Multi-Quantity Offer Bundle
    @Test
    @DisplayName("TC-17: Multi-Quantity Offer Bundle - Qty 2 of same bundle produces separate bundle groups")
    void test_TC17_MultiQuantityOfferBundle() {
        mockStandardOrderSaves();
        UUID offerId = UUID.randomUUID();
        UUID slotId = UUID.randomUUID();

        OfferSlot slot = new OfferSlot();
        slot.setId(slotId);
        slot.setSlotNameEn("Pizza");
        slot.setQuantity(1);
        slot.setFree(false);

        Offer offer = new Offer();
        offer.setId(offerId);
        offer.setNameEn("Solo Pizza Offer");
        offer.setDiscountTarget(DiscountTarget.FIXED_PRICE);
        offer.setFixedPrice(BigDecimal.valueOf(180.00));
        offer.setActive(true);
        offer.setSlots(List.of(slot));

        when(offerRepository.findByIdWithSlots(offerId)).thenReturn(Optional.of(offer));
        when(menuItemRepository.findAllById(any())).thenReturn(List.of(margherita));

        PlaceOrderRequest req = new PlaceOrderRequest();
        req.setOrderType(OrderType.PICKUP);
        req.setBranchId(testBranch.getId());
        req.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);

        // Bundle 1
        OrderOfferInput offerInput1 = new OrderOfferInput();
        offerInput1.setOfferId(offerId);
        offerInput1.setSelections(List.of(
                OfferSelectionInput.builder().slotId(slotId).menuItemId(margherita.getId()).build()
        ));

        // Bundle 2
        OrderOfferInput offerInput2 = new OrderOfferInput();
        offerInput2.setOfferId(offerId);
        offerInput2.setSelections(List.of(
                OfferSelectionInput.builder().slotId(slotId).menuItemId(margherita.getId()).build()
        ));

        req.setOffers(List.of(offerInput1, offerInput2));

        orderService.placeOrder(req);

        ArgumentCaptor<List<OrderItem>> itemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(orderItemRepository).saveAll(itemsCaptor.capture());
        List<OrderItem> savedItems = itemsCaptor.getValue();
        assertEquals(2, savedItems.size());
        assertNotEquals(savedItems.get(0).getBundleGroupId(), savedItems.get(1).getBundleGroupId());
    }

    // TC-18: Multiple Distinct Offers
    @Test
    @DisplayName("TC-18: Multiple Distinct Offers - Offer A (Fixed) + Offer B (BOGO) in same cart")
    void test_TC18_MultipleDistinctOffers() {
        mockStandardOrderSaves();
        UUID offerAId = UUID.randomUUID();
        UUID slotAId = UUID.randomUUID();
        OfferSlot slotA = new OfferSlot();
        slotA.setId(slotAId);
        slotA.setSlotNameEn("Slot A");
        slotA.setQuantity(1);
        slotA.setFree(false);

        Offer offerA = new Offer();
        offerA.setId(offerAId);
        offerA.setNameEn("Offer A - Fixed");
        offerA.setDiscountTarget(DiscountTarget.FIXED_PRICE);
        offerA.setFixedPrice(BigDecimal.valueOf(200.00));
        offerA.setActive(true);
        offerA.setSlots(List.of(slotA));

        UUID offerBId = UUID.randomUUID();
        UUID slotB1Id = UUID.randomUUID();
        UUID slotB2Id = UUID.randomUUID();
        OfferSlot slotB1 = new OfferSlot();
        slotB1.setId(slotB1Id);
        slotB1.setSlotNameEn("Slot B1");
        slotB1.setQuantity(1);
        slotB1.setFree(false);

        OfferSlot slotB2 = new OfferSlot();
        slotB2.setId(slotB2Id);
        slotB2.setSlotNameEn("Slot B2");
        slotB2.setQuantity(1);
        slotB2.setFree(false);

        Offer offerB = new Offer();
        offerB.setId(offerBId);
        offerB.setNameEn("Offer B - BOGO");
        offerB.setDiscountTarget(DiscountTarget.CHEAPEST_ITEM);
        offerB.setBuyQuantity(1);
        offerB.setGetQuantity(1);
        offerB.setDiscountPercentage(BigDecimal.valueOf(100.00));
        offerB.setActive(true);
        offerB.setSlots(List.of(slotB1, slotB2));

        when(offerRepository.findByIdWithSlots(offerAId)).thenReturn(Optional.of(offerA));
        when(offerRepository.findByIdWithSlots(offerBId)).thenReturn(Optional.of(offerB));
        when(menuItemRepository.findAllById(any())).thenReturn(List.of(margherita, chickenPesto, saladItem));

        PlaceOrderRequest req = new PlaceOrderRequest();
        req.setOrderType(OrderType.PICKUP);
        req.setBranchId(testBranch.getId());
        req.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);

        OrderOfferInput offerInputA = new OrderOfferInput();
        offerInputA.setOfferId(offerAId);
        offerInputA.setSelections(List.of(
                OfferSelectionInput.builder().slotId(slotAId).menuItemId(margherita.getId()).build()
        ));

        OrderOfferInput offerInputB = new OrderOfferInput();
        offerInputB.setOfferId(offerBId);
        offerInputB.setSelections(List.of(
                OfferSelectionInput.builder().slotId(slotB1Id).menuItemId(chickenPesto.getId()).build(),
                OfferSelectionInput.builder().slotId(slotB2Id).menuItemId(saladItem.getId()).build()
        ));

        req.setOffers(List.of(offerInputA, offerInputB));

        orderService.placeOrder(req);

        ArgumentCaptor<List<OrderItem>> itemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(orderItemRepository).saveAll(itemsCaptor.capture());
        List<OrderItem> savedItems = itemsCaptor.getValue();
        assertEquals(3, savedItems.size());

        UUID bundleA = savedItems.get(0).getBundleGroupId();
        UUID bundleB = savedItems.get(1).getBundleGroupId();
        assertNotNull(bundleA);
        assertNotNull(bundleB);
        assertNotEquals(bundleA, bundleB);
        assertEquals(bundleB, savedItems.get(2).getBundleGroupId());
    }

    // TC-19: Mixed Cart: Regular + Offer
    @Test
    @DisplayName("TC-19: Mixed Cart: Regular + Offer - 1 regular pizza and 1 offer bundle")
    void test_TC19_MixedCart_RegularPlusOffer() {
        mockStandardOrderSaves();
        UUID offerId = UUID.randomUUID();
        UUID slotId = UUID.randomUUID();

        OfferSlot slot = new OfferSlot();
        slot.setId(slotId);
        slot.setSlotNameEn("Slot");
        slot.setQuantity(1);
        slot.setFree(false);

        Offer offer = new Offer();
        offer.setId(offerId);
        offer.setNameEn("Single Offer");
        offer.setDiscountTarget(DiscountTarget.FIXED_PRICE);
        offer.setFixedPrice(BigDecimal.valueOf(190.00));
        offer.setActive(true);
        offer.setSlots(List.of(slot));

        when(offerRepository.findByIdWithSlots(offerId)).thenReturn(Optional.of(offer));
        when(menuItemRepository.findAllById(any())).thenReturn(List.of(saladItem, chickenPesto));

        PlaceOrderRequest req = new PlaceOrderRequest();
        req.setOrderType(OrderType.PICKUP);
        req.setBranchId(testBranch.getId());
        req.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);

        OrderItemInput regularItem = new OrderItemInput();
        regularItem.setMenuItemId(saladItem.getId());
        regularItem.setQuantity(1);
        req.setItems(List.of(regularItem));

        OrderOfferInput offerInput = new OrderOfferInput();
        offerInput.setOfferId(offerId);
        offerInput.setSelections(List.of(
                OfferSelectionInput.builder().slotId(slotId).menuItemId(chickenPesto.getId()).build()
        ));
        req.setOffers(List.of(offerInput));

        orderService.placeOrder(req);

        ArgumentCaptor<List<OrderItem>> itemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(orderItemRepository).saveAll(itemsCaptor.capture());
        List<OrderItem> savedItems = itemsCaptor.getValue();
        assertEquals(2, savedItems.size());

        // Regular item has no bundleGroupId
        assertNull(savedItems.get(0).getBundleGroupId());
        // Offer item has bundleGroupId
        assertNotNull(savedItems.get(1).getBundleGroupId());
    }

    // TC-20: Mixed Cart with Full Add-Ons
    @Test
    @DisplayName("TC-20: Mixed Cart with Full Add-Ons - Regular with cheese + Offer slot with truffle")
    void test_TC20_MixedCartWithFullAddOns() {
        mockStandardOrderSaves();
        UUID offerId = UUID.randomUUID();
        UUID slotId = UUID.randomUUID();

        OfferSlot slot = new OfferSlot();
        slot.setId(slotId);
        slot.setSlotNameEn("Feast Slot");
        slot.setQuantity(1);
        slot.setFree(false);

        Offer offer = new Offer();
        offer.setId(offerId);
        offer.setNameEn("Feast Offer");
        offer.setDiscountTarget(DiscountTarget.FIXED_PRICE);
        offer.setFixedPrice(BigDecimal.valueOf(300.00));
        offer.setActive(true);
        offer.setSlots(List.of(slot));

        when(offerRepository.findByIdWithSlots(offerId)).thenReturn(Optional.of(offer));
        when(menuItemRepository.findAllById(any())).thenReturn(List.of(margherita, chickenPesto));
        when(menuAddOnRepository.findAllById(any())).thenReturn(List.of(extraCheese, truffleSauce));

        PlaceOrderRequest req = new PlaceOrderRequest();
        req.setOrderType(OrderType.PICKUP);
        req.setBranchId(testBranch.getId());
        req.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);

        OrderItemInput regularItem = new OrderItemInput();
        regularItem.setMenuItemId(margherita.getId());
        regularItem.setQuantity(1);
        regularItem.setAddOnIds(List.of(extraCheese.getId()));
        req.setItems(List.of(regularItem));

        OrderOfferInput offerInput = new OrderOfferInput();
        offerInput.setOfferId(offerId);
        offerInput.setSelections(List.of(
                OfferSelectionInput.builder()
                        .slotId(slotId)
                        .menuItemId(chickenPesto.getId())
                        .addOnIds(List.of(truffleSauce.getId()))
                        .build()
        ));
        req.setOffers(List.of(offerInput));

        orderService.placeOrder(req);

        ArgumentCaptor<List<OrderItem>> itemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(orderItemRepository).saveAll(itemsCaptor.capture());
        List<OrderItem> savedItems = itemsCaptor.getValue();

        assertEquals(1, savedItems.get(0).getAddOns().size());
        assertEquals("Extra Mozzarella", savedItems.get(0).getAddOns().get(0).getNameEn());

        assertEquals(1, savedItems.get(1).getAddOns().size());
        assertEquals("Truffle Cream", savedItems.get(1).getAddOns().get(0).getNameEn());
    }

    // TC-21: Mixed Cart with Promo Code
    @Test
    @DisplayName("TC-21: Mixed Cart with Promo Code - 10% promo code applied to order")
    void test_TC21_MixedCartWithPromoCode() {
        mockStandardOrderSaves();
        PromoCode promo = new PromoCode();
        promo.setId(UUID.randomUUID());
        promo.setCode("SAVE10");
        promo.setDiscountType(DiscountType.PERCENTAGE);
        promo.setDiscountValue(BigDecimal.valueOf(10.00));
        promo.setCurrentUses(0);
        promo.setMaxUses(100);
        promo.setMaxUsesPerUser(1);
        promo.setActive(true);

        when(promoCodeRepository.findByCodeIgnoreCaseAndIsActiveTrue("SAVE10")).thenReturn(Optional.of(promo));
        when(promoCodeRedemptionRepository.countByPromoCodeIdAndUserId(promo.getId(), testUser.getId())).thenReturn(0L);
        when(promoCodeRedemptionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(menuItemRepository.findAllById(any())).thenReturn(List.of(chickenPesto));

        PlaceOrderRequest req = new PlaceOrderRequest();
        req.setOrderType(OrderType.PICKUP);
        req.setBranchId(testBranch.getId());
        req.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
        req.setPromoCode("SAVE10");

        OrderItemInput item = new OrderItemInput();
        item.setMenuItemId(chickenPesto.getId());
        item.setQuantity(1);
        req.setItems(List.of(item));

        orderService.placeOrder(req);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        // 320 - 10% (32) = 288.00
        assertEquals(0, BigDecimal.valueOf(288.00).compareTo(orderCaptor.getValue().getTotalAmount()));
    }

    // TC-22: Delivery Order with GPS Pin & Notes
    @Test
    @DisplayName("TC-22: Delivery Order with GPS Pin & Notes - GPS link, driver note, delivery fee")
    void test_TC22_DeliveryOrderWithGpsPinAndNotes() {
        mockStandardOrderSaves();
        when(menuItemRepository.findAllById(any())).thenReturn(List.of(margherita));

        UUID addressId = UUID.randomUUID();
        UserAddress address = new UserAddress();
        address.setId(addressId);
        address.setUser(testUser);
        address.setCity("Cairo");
        address.setDistrict("Maadi");
        address.setStreet("Road 9");

        when(userAddressRepository.findById(addressId)).thenReturn(Optional.of(address));
        DeliveryFeeResponse feeResponse = DeliveryFeeResponse.builder()
                .distanceKm(new BigDecimal("4.50"))
                .deliveryFee(new BigDecimal("30.00"))
                .branchId(testBranch.getId())
                .branchName("Main Branch")
                .currency("EGP")
                .build();
        when(deliveryService.calculateForAddress(addressId, null)).thenReturn(feeResponse);

        PlaceOrderRequest req = new PlaceOrderRequest();
        req.setOrderType(OrderType.DELIVERY);
        req.setAddressId(addressId);
        req.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
        req.setOrderNotes("[Customer Delivery GPS: https://maps.google.com/?q=29.9592,31.2585] Please do not ring buzzer");

        OrderItemInput item = new OrderItemInput();
        item.setMenuItemId(margherita.getId());
        item.setQuantity(1);
        req.setItems(List.of(item));

        orderService.placeOrder(req);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();

        assertEquals(OrderType.DELIVERY, savedOrder.getOrderType());
        assertTrue(savedOrder.getOrderNotes().contains("GPS: https://maps.google.com"));
        // 200 + 30 delivery fee = 230.00
        assertEquals(0, BigDecimal.valueOf(230.00).compareTo(savedOrder.getTotalAmount()));
    }

    // TC-23: Car Pickup / Curbside Order
    @Test
    @DisplayName("TC-23: Car Pickup / Curbside Order - Car details and pickup branch")
    void test_TC23_CarPickupCurbsideOrder() {
        mockStandardOrderSaves();
        when(menuItemRepository.findAllById(any())).thenReturn(List.of(margherita));

        PlaceOrderRequest req = new PlaceOrderRequest();
        req.setOrderType(OrderType.PICKUP);
        req.setBranchId(testBranch.getId());
        req.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
        req.setOrderNotes("Car Pickup: White Hyundai Tucson, Plate 789 XYZ");

        OrderItemInput item = new OrderItemInput();
        item.setMenuItemId(margherita.getId());
        item.setQuantity(1);
        req.setItems(List.of(item));

        orderService.placeOrder(req);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        assertEquals("Car Pickup: White Hyundai Tucson, Plate 789 XYZ", orderCaptor.getValue().getOrderNotes());
    }

    // TC-24: In-Store Pickup Order
    @Test
    @DisplayName("TC-24: In-Store Pickup Order - Branch assigned, delivery fee = 0.00")
    void test_TC24_InStorePickupOrder() {
        mockStandardOrderSaves();
        when(menuItemRepository.findAllById(any())).thenReturn(List.of(margherita));

        PlaceOrderRequest req = new PlaceOrderRequest();
        req.setOrderType(OrderType.PICKUP);
        req.setBranchId(testBranch.getId());
        req.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);

        OrderItemInput item = new OrderItemInput();
        item.setMenuItemId(margherita.getId());
        item.setQuantity(1);
        req.setItems(List.of(item));

        orderService.placeOrder(req);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();

        assertEquals(OrderType.PICKUP, savedOrder.getOrderType());
        assertEquals(0, BigDecimal.valueOf(200.00).compareTo(savedOrder.getTotalAmount()));
    }

    // TC-25: Zero-Price Add-Ons vs Paid Add-Ons
    @Test
    @DisplayName("TC-25: Zero-Price Add-Ons vs Paid Add-Ons - Free garlic dip (0 EGP) alongside paid cheese (35 EGP)")
    void test_TC25_ZeroPriceAddOnsVsPaidAddOns() {
        mockStandardOrderSaves();
        when(menuItemRepository.findAllById(any())).thenReturn(List.of(margherita));
        when(menuAddOnRepository.findAllById(any())).thenReturn(List.of(freeDip, extraCheese));

        PlaceOrderRequest req = new PlaceOrderRequest();
        req.setOrderType(OrderType.PICKUP);
        req.setBranchId(testBranch.getId());
        req.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);

        OrderItemInput item = new OrderItemInput();
        item.setMenuItemId(margherita.getId());
        item.setQuantity(1);
        item.setAddOnIds(List.of(freeDip.getId(), extraCheese.getId()));
        req.setItems(List.of(item));

        orderService.placeOrder(req);

        ArgumentCaptor<List<OrderItem>> itemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(orderItemRepository).saveAll(itemsCaptor.capture());
        OrderItem saved = itemsCaptor.getValue().get(0);

        // 200 + 0 + 35 = 235.00
        assertEquals(0, BigDecimal.valueOf(235.00).compareTo(saved.getUnitPriceAtPurchase()));
        assertEquals(2, saved.getAddOns().size());

        OrderItemAddOn freeSavedAddon = saved.getAddOns().stream()
                .filter(a -> a.getPrice().compareTo(BigDecimal.ZERO) == 0)
                .findFirst().orElseThrow();
        assertEquals("Complimentary Garlic Dip", freeSavedAddon.getNameEn());

        OrderItemAddOn paidSavedAddon = saved.getAddOns().stream()
                .filter(a -> a.getPrice().compareTo(BigDecimal.valueOf(35.00)) == 0)
                .findFirst().orElseThrow();
        assertEquals("Extra Mozzarella", paidSavedAddon.getNameEn());
    }

    // TC-26: Multilingual & Special Characters
    @Test
    @DisplayName("TC-26: Multilingual & Special Characters - Arabic and English text preserved without mojibake")
    void test_TC26_MultilingualAndSpecialCharacters() {
        mockStandardOrderSaves();
        when(menuItemRepository.findAllById(any())).thenReturn(List.of(margherita));

        PlaceOrderRequest req = new PlaceOrderRequest();
        req.setOrderType(OrderType.PICKUP);
        req.setBranchId(testBranch.getId());
        req.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
        req.setOrderNotes("الرجاء عدم الاتصال بالجرس 🔔 - Please ring doorbell!");

        OrderItemInput item = new OrderItemInput();
        item.setMenuItemId(margherita.getId());
        item.setQuantity(1);
        item.setNotes("بدون بصل وزيادة جبنة - Extra crispy crust! 🍕");
        req.setItems(List.of(item));

        orderService.placeOrder(req);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        assertEquals("الرجاء عدم الاتصال بالجرس 🔔 - Please ring doorbell!", orderCaptor.getValue().getOrderNotes());

        ArgumentCaptor<List<OrderItem>> itemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(orderItemRepository).saveAll(itemsCaptor.capture());
        assertEquals("بدون بصل وزيادة جبنة - Extra crispy crust! 🍕", itemsCaptor.getValue().get(0).getNotes());
    }

    // TC-27: Family Meal with Paid Sauces and Complimentary Drink
    @Test
    @DisplayName("TC-27: Family Meal with Paid Sauces - Sauces charged at menu price, cheapest pizza is free, drink is free")
    void test_TC27_FamilyMealWithPaidSaucesAndFreeDrink() {
        mockStandardOrderSaves();
        UUID offerId = UUID.randomUUID();
        UUID pizzaSlotId = UUID.randomUUID();
        UUID sauceSlotId = UUID.randomUUID();
        UUID drinkSlotId = UUID.randomUUID();

        // Menu items
        MenuItem pizza1 = new MenuItem();
        pizza1.setId(UUID.randomUUID());
        pizza1.setNameEn("Quattro");
        pizza1.setCurrentPrice(BigDecimal.valueOf(450.00));
        pizza1.setActive(true);
        pizza1.setAvailable(true);

        MenuItem pizza2 = new MenuItem();
        pizza2.setId(UUID.randomUUID());
        pizza2.setNameEn("Creamy Salmon");
        pizza2.setCurrentPrice(BigDecimal.valueOf(520.00));
        pizza2.setActive(true);
        pizza2.setAvailable(true);

        MenuItem pizza3 = new MenuItem();
        pizza3.setId(UUID.randomUUID());
        pizza3.setNameEn("Burrata");
        pizza3.setCurrentPrice(BigDecimal.valueOf(450.00));
        pizza3.setActive(true);
        pizza3.setAvailable(true);

        MenuItem pizza4 = new MenuItem();
        pizza4.setId(UUID.randomUUID());
        pizza4.setNameEn("Vegetarian");
        pizza4.setCurrentPrice(BigDecimal.valueOf(320.00));
        pizza4.setActive(true);
        pizza4.setAvailable(true);

        MenuItem sauce1 = new MenuItem();
        sauce1.setId(UUID.randomUUID());
        sauce1.setNameEn("Barbecue");
        sauce1.setCurrentPrice(BigDecimal.valueOf(20.00));
        sauce1.setActive(true);
        sauce1.setAvailable(true);

        MenuItem sauce2 = new MenuItem();
        sauce2.setId(UUID.randomUUID());
        sauce2.setNameEn("Caesar Dressing");
        sauce2.setCurrentPrice(BigDecimal.valueOf(30.00));
        sauce2.setActive(true);
        sauce2.setAvailable(true);

        MenuItem drink1 = new MenuItem();
        drink1.setId(UUID.randomUUID());
        drink1.setNameEn("Maxi Cola 1L");
        drink1.setCurrentPrice(BigDecimal.valueOf(30.00));
        drink1.setActive(true);
        drink1.setAvailable(true);

        // Offer & Slots
        OfferSlot pizzaSlot = new OfferSlot();
        pizzaSlot.setId(pizzaSlotId);
        pizzaSlot.setSlotNameEn("Choose 4 Pizzas");
        pizzaSlot.setQuantity(4);
        pizzaSlot.setFree(false);

        OfferSlot sauceSlot = new OfferSlot();
        sauceSlot.setId(sauceSlotId);
        sauceSlot.setSlotNameEn("Choose 2 Sauces");
        sauceSlot.setQuantity(2);
        sauceSlot.setFree(false); // PAID!

        OfferSlot drinkSlot = new OfferSlot();
        drinkSlot.setId(drinkSlotId);
        drinkSlot.setSlotNameEn("Free 1L Drink");
        drinkSlot.setQuantity(1);
        drinkSlot.setFree(true); // FREE!

        Offer familyOffer = new Offer();
        familyOffer.setId(offerId);
        familyOffer.setNameEn("Family Meal");
        familyOffer.setDiscountTarget(DiscountTarget.CHEAPEST_ITEM);
        familyOffer.setBuyQuantity(3);
        familyOffer.setGetQuantity(1);
        familyOffer.setDiscountPercentage(BigDecimal.valueOf(100.00));
        familyOffer.setActive(true);
        familyOffer.setSlots(List.of(pizzaSlot, sauceSlot, drinkSlot));

        when(offerRepository.findByIdWithSlots(offerId)).thenReturn(Optional.of(familyOffer));
        when(menuItemRepository.findAllById(any())).thenReturn(List.of(pizza1, pizza2, pizza3, pizza4, sauce1, sauce2, drink1));

        PlaceOrderRequest req = new PlaceOrderRequest();
        req.setOrderType(OrderType.PICKUP);
        req.setBranchId(testBranch.getId());
        req.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);

        OrderOfferInput offerInput = new OrderOfferInput();
        offerInput.setOfferId(offerId);
        offerInput.setSelections(List.of(
                OfferSelectionInput.builder().slotId(pizzaSlotId).menuItemId(pizza1.getId()).quantity(1).build(),
                OfferSelectionInput.builder().slotId(pizzaSlotId).menuItemId(pizza2.getId()).quantity(1).build(),
                OfferSelectionInput.builder().slotId(pizzaSlotId).menuItemId(pizza3.getId()).quantity(1).build(),
                OfferSelectionInput.builder().slotId(pizzaSlotId).menuItemId(pizza4.getId()).quantity(1).build(),
                OfferSelectionInput.builder().slotId(sauceSlotId).menuItemId(sauce1.getId()).quantity(1).build(),
                OfferSelectionInput.builder().slotId(sauceSlotId).menuItemId(sauce2.getId()).quantity(1).build(),
                OfferSelectionInput.builder().slotId(drinkSlotId).menuItemId(drink1.getId()).quantity(1).build()
        ));
        req.setOffers(List.of(offerInput));

        orderService.placeOrder(req);

        ArgumentCaptor<List<OrderItem>> itemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(orderItemRepository).saveAll(itemsCaptor.capture());
        List<OrderItem> savedItems = itemsCaptor.getValue();
        assertEquals(7, savedItems.size());

        // Check Vegetarian is cheapest pizza and FREE
        OrderItem vegPizza = savedItems.stream()
                .filter(i -> i.getMenuItem().getNameEn().equals("Vegetarian"))
                .findFirst().orElseThrow();
        assertTrue(vegPizza.isFree());
        assertEquals(0, BigDecimal.ZERO.compareTo(vegPizza.getUnitPriceAtPurchase()));

        // Check Drink is FREE
        OrderItem drink = savedItems.stream()
                .filter(i -> i.getMenuItem().getNameEn().equals("Maxi Cola 1L"))
                .findFirst().orElseThrow();
        assertTrue(drink.isFree());
        assertEquals(0, BigDecimal.ZERO.compareTo(drink.getUnitPriceAtPurchase()));

        // Check Sauces are PAID at menu price (20 and 30)
        OrderItem bbq = savedItems.stream()
                .filter(i -> i.getMenuItem().getNameEn().equals("Barbecue"))
                .findFirst().orElseThrow();
        assertFalse(bbq.isFree());
        assertEquals(0, BigDecimal.valueOf(20.00).compareTo(bbq.getUnitPriceAtPurchase()));

        OrderItem caesar = savedItems.stream()
                .filter(i -> i.getMenuItem().getNameEn().equals("Caesar Dressing"))
                .findFirst().orElseThrow();
        assertFalse(caesar.isFree());
        assertEquals(0, BigDecimal.valueOf(30.00).compareTo(caesar.getUnitPriceAtPurchase()));

        // Check paid pizzas (520 + 450 + 450)
        OrderItem salmon = savedItems.stream()
                .filter(i -> i.getMenuItem().getNameEn().equals("Creamy Salmon"))
                .findFirst().orElseThrow();
        assertFalse(salmon.isFree());
        assertEquals(0, BigDecimal.valueOf(520.00).compareTo(salmon.getUnitPriceAtPurchase()));

        // Check Order total amount
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        // Total = 520 + 450 + 450 + 0 + 20 + 30 + 0 = 1470.00
        BigDecimal expectedTotal = BigDecimal.valueOf(1470.00);
        assertEquals(0, expectedTotal.compareTo(orderCaptor.getValue().getTotalAmount()));
    }
}
