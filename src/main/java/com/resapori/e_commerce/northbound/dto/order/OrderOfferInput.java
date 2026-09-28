package com.resapori.e_commerce.northbound.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderOfferInput {
    @NotNull(message = "Offer ID is required")
    private UUID offerId;
    @Builder.Default
    private Integer quantity = 1;
    @Valid
    private List<OfferSelectionInput> selections;
}
