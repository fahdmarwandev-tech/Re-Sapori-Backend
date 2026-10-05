package com.resapori.e_commerce.northbound.dto.user;

import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequest {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private UUID branchId;
    private Set<String> roles;
}
