package com.resapori.e_commerce.northbound.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordRequest {

    @Schema(description = "Reset password token obtained from /verify-otp", example = "c39a03fd-54c3-4d43-98fe-891d293cfba1")
    @NotBlank(message = "Reset password token is required")
    private String token;

    @Schema(description = "New password", example = "NewSecretPassword123")
    @NotBlank(message = "New password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String newPassword;
}
