package com.resapori.e_commerce.southbound.entity;


import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "menu_categories")
public class MenuCategory extends BaseEntity {

    @Column(name = "name_en", nullable = false)
    private String nameEn;

    @Column(name = "name_ar")
    private String nameAr;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "subtitle_en")
    private String subtitleEn;

    @Column(name = "subtitle_ar")
    private String subtitleAr;

    @Column(name = "is_visible", nullable = false)
    private boolean isVisible = true;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MenuAddOn> addOns = new ArrayList<>();
}
