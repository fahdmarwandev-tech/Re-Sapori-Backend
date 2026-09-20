package com.resapori.e_commerce.northbound.controllers;

import com.resapori.e_commerce.service.IWhatsAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientResponseException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/whatsapp")
@Tag(name = "WhatsApp Marketing", description = "Send WhatsApp Cloud API templates")
@PreAuthorize("hasRole('ADMIN')")
public class WhatsAppController {

    private final IWhatsAppService whatsAppService;

    @PostMapping(value = "/send-template", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Send template to phone number",
            description = "Dispatches a pre-approved WhatsApp template message to the specified recipient via Meta Cloud API. Automatically formats Egyptian phone numbers."
    )
    public ResponseEntity<String> sendTemplate(
            @Parameter(description = "Recipient phone number (e.g., 01122406458, +201122406458, or international)", example = "01122406458")
            @RequestParam(defaultValue = "01122406458") String phoneNumber,

            @Parameter(description = "Meta pre-approved template name", example = "testingtemplate")
            @RequestParam(defaultValue = "testingtemplate", required = false) String templateName,

            @Parameter(description = "Language code of the template", example = "en")
            @RequestParam(defaultValue = "en", required = false) String languageCode) {

        try {
            String response = whatsAppService.sendTemplate(phoneNumber, templateName, languageCode);
            return ResponseEntity.ok(response);
        } catch (RestClientResponseException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ex.getResponseBodyAsString());
        }
    }
}
