package com.resapori.e_commerce.service;

import com.resapori.e_commerce.service.impl.WhatsAppServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class WhatsAppServiceTest {

    private final WhatsAppServiceImpl service = new WhatsAppServiceImpl(
            "123456789",
            "fake_token",
            "https://graph.facebook.com/v21.0"
    );

    @ParameterizedTest(name = "Formatting ''{0}'' should result in ''{1}''")
    @CsvSource({
            "01122406458, 201122406458",
            "+201122406458, 201122406458",
            "201122406458, 201122406458",
            "01012345678, 201012345678",
            "+20 10 1234 5678, 201012345678",
            "1122406458, 201122406458"
    })
    @DisplayName("Should correctly format Egyptian numbers to international standard with 20 prefix")
    void shouldFormatEgyptianPhoneNumbers(String input, String expected) {
        assertEquals(expected, service.formatPhoneNumber(input));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when phone number is null or blank")
    void shouldThrowExceptionForInvalidPhone() {
        assertThrows(IllegalArgumentException.class, () -> service.formatPhoneNumber(null));
        assertThrows(IllegalArgumentException.class, () -> service.formatPhoneNumber("   "));
    }

    @Test
    @DisplayName("Should throw IllegalStateException if credentials are not configured")
    void shouldThrowWhenCredentialsAreMissing() {
        WhatsAppServiceImpl unconfiguredService = new WhatsAppServiceImpl("", "", "");
        assertThrows(IllegalStateException.class, () -> unconfiguredService.sendTemplate("01122406458"));
    }
}
