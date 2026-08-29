package com.resapori.e_commerce.southbound.mapper;

import com.resapori.e_commerce.northbound.dto.user.UserResponse;
import com.resapori.e_commerce.southbound.entity.Role;
import com.resapori.e_commerce.southbound.entity.User;
import java.util.List;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "roles", target = "roles", qualifiedByName = "rolesToNames")
    UserResponse toResponse(User entity);

    List<UserResponse> toResponseList(List<User> entities);

    @Named("rolesToNames")
    default Set<String> rolesToNames(Set<Role> roles) {
        if (roles == null) return java.util.Collections.emptySet();
        return roles.stream()
                .map(Role::getName)
                .collect(java.util.stream.Collectors.toSet());
    }
}
