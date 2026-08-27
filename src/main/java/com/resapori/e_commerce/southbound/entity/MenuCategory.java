package com.resapori.e_commerce.southbound.entity;


import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

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

}
