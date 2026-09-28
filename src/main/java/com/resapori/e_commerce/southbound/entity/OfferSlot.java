package com.resapori.e_commerce.southbound.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "offer_slots")
public class OfferSlot extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offer_id", nullable = false)
    private Offer offer;

    @Column(name = "slot_name_en", nullable = false)
    private String slotNameEn;

    @Column(name = "slot_name_ar", nullable = false)
    private String slotNameAr;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id")
    private MenuItem menuItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private MenuCategory category;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "is_free", nullable = false)
    private boolean isFree = false;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "offer_slot_eligible_items",
        joinColumns = @JoinColumn(name = "slot_id"),
        inverseJoinColumns = @JoinColumn(name = "menu_item_id")
    )
    @org.hibernate.annotations.BatchSize(size = 30)
    private List<MenuItem> eligibleItems = new ArrayList<>();
}
