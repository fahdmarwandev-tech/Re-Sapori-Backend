package com.resapori.e_commerce.northbound.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VerifyOtpResponse {

    @Schema(description = "Confirmation message", example = "OTP verified successfully")
    private String message;

    @Schema(description = "Single-use reset password token", example = "c39a03fd-54c3-4d43-98fe-891d293cfba1")
    private String resetPasswordToken;
}
