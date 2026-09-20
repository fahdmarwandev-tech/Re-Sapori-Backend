package com.resapori.e_commerce.service.impl;

import com.resapori.e_commerce.service.IWhatsAppService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

@Slf4j
@Service
public class WhatsAppServiceImpl implements IWhatsAppService {

    private final String phoneNumberId;
    private final String accessToken;
    private final String apiUrl;
    private final RestClient restClient;

    @Autowired
    public WhatsAppServiceImpl(
            @Value("${meta.whatsapp.phone-number-id:}") String phoneNumberId,
            @Value("${meta.whatsapp.access-token:}") String accessToken,
            @Value("${meta.whatsapp.api-url:https://graph.facebook.com/v21.0}") String apiUrl) {
        this(phoneNumberId, accessToken, apiUrl, RestClient.builder());
    }

    public WhatsAppServiceImpl(
            String phoneNumberId,
            String accessToken,
            String apiUrl,
            RestClient.Builder restClientBuilder) {
        this.phoneNumberId = phoneNumberId != null ? phoneNumberId.trim() : "";
        this.accessToken = accessToken != null ? accessToken.trim() : "";
        this.apiUrl = (apiUrl != null && !apiUrl.isBlank()) ? apiUrl.trim() : "https://graph.facebook.com/v21.0";
        this.restClient = restClientBuilder
                .baseUrl(this.apiUrl)
                .defaultHeader("Authorization", "Bearer " + this.accessToken)
                .build();
    }

    @Override
    public String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("Phone number cannot be empty");
        }

        // Automatically format Egyptian numbers (e.g., 010... or 011... -> 2010... / 2011...)
        String formattedPhone = phoneNumber.replaceAll("[^0-9]", "");
        if (formattedPhone.startsWith("0")) {
            formattedPhone = "2" + formattedPhone;
        } else if (!formattedPhone.startsWith("20")) {
            formattedPhone = "20" + formattedPhone;
        }
        return formattedPhone;
    }

    @Override
    public String sendTemplate(String phoneNumber, String templateName, String languageCode) {
        if (phoneNumberId.isEmpty() || accessToken.isEmpty()
                || "YOUR_PHONE_NUMBER_ID".equals(phoneNumberId)
                || "YOUR_PERMANENT_SYSTEM_USER_TOKEN".equals(accessToken)) {
            throw new IllegalStateException(
                    "WhatsApp Cloud API credentials are not configured. Please set META_WHATSAPP_PHONE_NUMBER_ID and META_WHATSAPP_ACCESS_TOKEN in .env"
            );
        }

        String formattedPhone = formatPhoneNumber(phoneNumber);
        String selectedTemplate = (templateName != null && !templateName.isBlank()) ? templateName.trim() : "testingtemplate";
        String selectedLanguage = (languageCode != null && !languageCode.isBlank()) ? languageCode.trim() : "en";

        log.info("Dispatching WhatsApp template '{}' ({}) to '{}'", selectedTemplate, selectedLanguage, formattedPhone);

        Map<String, Object> payload = Map.of(
                "messaging_product", "whatsapp",
                "recipient_type", "individual",
                "to", formattedPhone,
                "type", "template",
                "template", Map.of(
                        "name", selectedTemplate,
                        "language", Map.of("code", selectedLanguage)
                )
        );

        try {
            String response = restClient.post()
                    .uri("/{phoneNumberId}/messages", phoneNumberId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(String.class);

            log.info("WhatsApp template successfully dispatched to {}. Response: {}", formattedPhone, response);
            return response;
        } catch (RestClientResponseException ex) {
            log.error("Meta WhatsApp Cloud API error: HTTP {} - {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw ex;
        }
    }

    @Override
    public String sendTemplate(String phoneNumber) {
        return sendTemplate(phoneNumber, "testingtemplate", "en");
    }
}
