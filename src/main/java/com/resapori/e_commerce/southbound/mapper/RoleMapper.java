package com.resapori.e_commerce.southbound.mapper;

import com.resapori.e_commerce.northbound.dto.role.RoleRequest;
import com.resapori.e_commerce.northbound.dto.role.RoleResponse;
import com.resapori.e_commerce.southbound.entity.Role;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    Role toEntity(RoleRequest request);

    RoleResponse toResponse(Role entity);

    List<RoleResponse> toResponseList(List<Role> entities);
}
