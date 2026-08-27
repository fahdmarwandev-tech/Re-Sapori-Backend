package com.resapori.e_commerce.southbound.entity;


import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "menu_items")
public class MenuItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private MenuCategory category;

    @Column(name = "name_en", nullable = false)
    private String nameEn;

    @Column(name = "name_ar")
    private String nameAr;

    @Column(name = "description_en")
    private String descriptionEn;

    @Column(name = "description_ar")
    private String descriptionAr;

    @Column(name = "current_price", nullable = false)
    private BigDecimal currentPrice;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_available", nullable = false)
    private boolean isAvailable = true;

    @Column(name = "stock")
    private Integer stock;

}
