package com.resapori.e_commerce.northbound.dto.promo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PromoCodeRequestDeserializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldDeserializeDateOnlyFormat() throws Exception {
        String json = "{\"code\":\"VIP-5\",\"expiryDate\":\"2027-05-01\"}";
        PromoCodeRequest request = objectMapper.readValue(json, PromoCodeRequest.class);

        assertNotNull(request.getExpiryDate());
        assertEquals(LocalDateTime.of(2027, 5, 1, 23, 59, 59), request.getExpiryDate());
    }

    @Test
    void shouldDeserializeIsoDateTimeFormat() throws Exception {
        String json = "{\"code\":\"VIP-5\",\"expiryDate\":\"2027-05-01T15:30:00\"}";
        PromoCodeRequest request = objectMapper.readValue(json, PromoCodeRequest.class);

        assertNotNull(request.getExpiryDate());
        assertEquals(LocalDateTime.of(2027, 5, 1, 15, 30, 0), request.getExpiryDate());
    }

    @Test
    void shouldDeserializeNullAndEmpty() throws Exception {
        String jsonNull = "{\"code\":\"VIP-5\",\"expiryDate\":null}";
        PromoCodeRequest requestNull = objectMapper.readValue(jsonNull, PromoCodeRequest.class);
        assertNull(requestNull.getExpiryDate());

        String jsonEmpty = "{\"code\":\"VIP-5\",\"expiryDate\":\"\"}";
        PromoCodeRequest requestEmpty = objectMapper.readValue(jsonEmpty, PromoCodeRequest.class);
        assertNull(requestEmpty.getExpiryDate());
    }
}
