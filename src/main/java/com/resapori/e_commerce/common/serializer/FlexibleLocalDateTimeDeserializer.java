package com.resapori.e_commerce.common.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class FlexibleLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        value = value.trim();

        // 1. Date only (e.g. "2027-05-01") -> set to end of day 23:59:59
        if (value.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            return LocalDate.parse(value).atTime(23, 59, 59);
        }

        // 2. Standard ISO LocalDateTime (e.g. "2027-05-01T23:59:59")
        try {
            return LocalDateTime.parse(value);
        } catch (Exception ignored) {
        }

        // 3. Offset / Zoned ISO DateTime (e.g. "2027-05-01T23:59:59Z" or "+03:00")
        try {
            return OffsetDateTime.parse(value).toLocalDateTime();
        } catch (Exception ignored) {
        }

        try {
            return ZonedDateTime.parse(value).toLocalDateTime();
        } catch (Exception ignored) {
        }

        // 4. Space-separated format (e.g. "2027-05-01 23:59:59")
        try {
            return LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm[:ss]"));
        } catch (Exception ignored) {
        }

        return (LocalDateTime) ctxt.handleWeirdStringValue(LocalDateTime.class, value,
                "Cannot deserialize LocalDateTime from %s", value);
    }
}
