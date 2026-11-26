package com.resapori.e_commerce.service;

import com.resapori.e_commerce.northbound.dto.menu.MenuAddOnResponse;
import com.resapori.e_commerce.northbound.dto.menu.MenuCategoryRequest;
import com.resapori.e_commerce.northbound.dto.menu.MenuCategoryResponse;
import com.resapori.e_commerce.northbound.dto.menu.MenuItemResponse;
import com.resapori.e_commerce.service.impl.MenuCategoryServiceImpl;
import com.resapori.e_commerce.service.impl.MenuItemServiceImpl;
import com.resapori.e_commerce.southbound.entity.MenuAddOn;
import com.resapori.e_commerce.southbound.entity.MenuCategory;
import com.resapori.e_commerce.southbound.entity.MenuItem;
import com.resapori.e_commerce.southbound.mapper.MenuAddOnMapper;
import com.resapori.e_commerce.southbound.mapper.MenuCategoryMapper;
import com.resapori.e_commerce.southbound.mapper.MenuItemMapper;
import com.resapori.e_commerce.southbound.repository.IMenuAddOnRepository;
import com.resapori.e_commerce.southbound.repository.IMenuCategoryRepository;
import com.resapori.e_commerce.southbound.repository.IMenuItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuCategoryAddOnServiceTest {

    @Mock
    private IMenuItemRepository menuItemRepository;
    @Mock
    private IMenuCategoryRepository menuCategoryRepository;
    @Mock
    private IMenuAddOnRepository menuAddOnRepository;
    @Mock
    private MenuItemMapper menuItemMapper;
    @Mock
    private MenuCategoryMapper menuCategoryMapper;
    @Mock
    private MenuAddOnMapper menuAddOnMapper;

    @InjectMocks
    private MenuItemServiceImpl menuItemService;

    @InjectMocks
    private MenuCategoryServiceImpl menuCategoryService;

    @Test
    void getAll_mergesCategoryComboAddOnIntoEligibleItemsOnly() {
        UUID pizzaCatId = UUID.randomUUID();
        UUID drinksCatId = UUID.randomUUID();
        UUID comboAddOnId = UUID.randomUUID();

        MenuCategory pizzaCat = new MenuCategory();
        pizzaCat.setId(pizzaCatId);
        pizzaCat.setNameEn("Pizza");

        MenuCategory drinksCat = new MenuCategory();
        drinksCat.setId(drinksCatId);
        drinksCat.setNameEn("Drinks");

        MenuItem margherita = new MenuItem();
        margherita.setId(UUID.randomUUID());
        margherita.setCategory(pizzaCat);
        margherita.setNameEn("Margherita");

        MenuItem cola = new MenuItem();
        cola.setId(UUID.randomUUID());
        cola.setCategory(drinksCat);
        cola.setNameEn("Cola");

        MenuAddOn comboAddOn = new MenuAddOn();
        comboAddOn.setId(comboAddOnId);
        comboAddOn.setCategory(pizzaCat);
        comboAddOn.setNameEn("Make it a Combo");
        comboAddOn.setNameAr("خليها كومبو");
        comboAddOn.setPrice(BigDecimal.valueOf(50.00));
        comboAddOn.setActive(true);

        MenuAddOnResponse dishAddOnRes = MenuAddOnResponse.builder()
                .id(UUID.randomUUID())
                .nameEn("Extra Cheese")
                .price(BigDecimal.valueOf(30.00))
                .active(true)
                .categoryLevel(false)
                .build();

        MenuAddOnResponse comboAddOnRes = MenuAddOnResponse.builder()
                .id(comboAddOnId)
                .nameEn("Make it a Combo")
                .nameAr("خليها كومبو")
                .price(BigDecimal.valueOf(50.00))
                .active(true)
                .categoryLevel(true)
                .build();

        MenuItemResponse margheritaRes = MenuItemResponse.builder()
                .id(margherita.getId())
                .categoryId(pizzaCatId)
                .nameEn("Margherita")
                .addOns(new ArrayList<>(List.of(dishAddOnRes)))
                .build();

        MenuItemResponse colaRes = MenuItemResponse.builder()
                .id(cola.getId())
                .categoryId(drinksCatId)
                .nameEn("Cola")
                .addOns(new ArrayList<>())
                .build();

        when(menuItemRepository.findAllActiveAndAvailable()).thenReturn(List.of(margherita, cola));
        when(menuItemMapper.toResponseList(any())).thenReturn(List.of(margheritaRes, colaRes));
        when(menuAddOnRepository.findByCategoryIdInAndIsActiveTrue(any())).thenReturn(List.of(comboAddOn));
        when(menuAddOnMapper.toResponse(comboAddOn)).thenReturn(comboAddOnRes);

        List<MenuItemResponse> results = menuItemService.getAll();

        assertEquals(2, results.size());
        MenuItemResponse pizzaOut = results.get(0);
        MenuItemResponse drinkOut = results.get(1);

        assertEquals(2, pizzaOut.getAddOns().size());
        assertEquals("Make it a Combo", pizzaOut.getAddOns().get(0).getNameEn());
        assertEquals(BigDecimal.valueOf(50.00), pizzaOut.getAddOns().get(0).getPrice());
        assertTrue(Boolean.TRUE.equals(pizzaOut.getAddOns().get(0).getCategoryLevel()));
        assertEquals("Extra Cheese", pizzaOut.getAddOns().get(1).getNameEn());

        assertTrue(drinkOut.getAddOns().isEmpty());
    }

    @Test
    void createCategory_autoSeedsComboForEligibleCategoryAndSkipsExcluded() {
        MenuCategory calzoneEntity = new MenuCategory();
        calzoneEntity.setNameEn("Calzone");

        MenuCategoryRequest calzoneReq = MenuCategoryRequest.builder()
                .nameEn("Calzone")
                .isVisible(true)
                .build();

        when(menuCategoryMapper.toEntity(calzoneReq)).thenReturn(calzoneEntity);
        when(menuCategoryRepository.findByIsActiveTrueOrderByDisplayOrderAsc()).thenReturn(new ArrayList<>());
        when(menuCategoryMapper.toResponse(calzoneEntity)).thenReturn(MenuCategoryResponse.builder().nameEn("Calzone").build());

        menuCategoryService.create(calzoneReq);

        assertEquals(1, calzoneEntity.getAddOns().size());
        assertEquals("Make it a Combo", calzoneEntity.getAddOns().get(0).getNameEn());
        assertEquals(BigDecimal.valueOf(50.00), calzoneEntity.getAddOns().get(0).getPrice());
        assertEquals(calzoneEntity, calzoneEntity.getAddOns().get(0).getCategory());

        MenuCategory sauceEntity = new MenuCategory();
        sauceEntity.setNameEn("Sauce");
        MenuCategoryRequest sauceReq = MenuCategoryRequest.builder()
                .nameEn("Sauce")
                .isVisible(true)
                .build();

        when(menuCategoryMapper.toEntity(sauceReq)).thenReturn(sauceEntity);
        when(menuCategoryMapper.toResponse(sauceEntity)).thenReturn(MenuCategoryResponse.builder().nameEn("Sauce").build());

        menuCategoryService.create(sauceReq);

        assertTrue(sauceEntity.getAddOns().isEmpty());
        verify(menuCategoryRepository, times(2)).saveAll(any());
    }
}
