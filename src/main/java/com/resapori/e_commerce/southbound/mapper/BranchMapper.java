package com.resapori.e_commerce.southbound.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resapori.e_commerce.northbound.dto.branch.BranchRequest;
import com.resapori.e_commerce.northbound.dto.branch.BranchResponse;
import com.resapori.e_commerce.northbound.dto.delivery.DeliveryZoneDto;
import com.resapori.e_commerce.southbound.entity.Branch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring")
public abstract class BranchMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mapping(target = "deliveryZones", source = "deliveryZones", qualifiedByName = "zonesToJson")
    public abstract Branch toEntity(BranchRequest request);

    @Mapping(target = "deliveryZones", source = "deliveryZones", qualifiedByName = "jsonToZones")
    @Mapping(target = "isActive", expression = "java(entity.isActive())")
    public abstract BranchResponse toResponse(Branch entity);

    public abstract List<BranchResponse> toResponseList(List<Branch> entities);

    @Named("zonesToJson")
    public String zonesToJson(List<DeliveryZoneDto> zones) {
        if (zones == null) return null;
        try {
            return objectMapper.writeValueAsString(zones);
        } catch (Exception e) {
            return null;
        }
    }

    @Named("jsonToZones")
    public List<DeliveryZoneDto> jsonToZones(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<List<DeliveryZoneDto>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
