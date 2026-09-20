package com.resapori.e_commerce.northbound.controllers;

import com.resapori.e_commerce.service.IWhatsAppService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WhatsAppControllerTest {

    private final IWhatsAppService service = Mockito.mock(IWhatsAppService.class);
    private final WhatsAppController controller = new WhatsAppController(service);

    @Test
    @DisplayName("sendTemplate should delegate to service and return 200 OK on success")
    void shouldReturnOkOnSuccessfulSend() {
        String mockMetaResponse = "{\"messaging_product\":\"whatsapp\",\"messages\":[{\"id\":\"wamid.123\"}]}";
        when(service.sendTemplate("01122406458", "testingtemplate", "en")).thenReturn(mockMetaResponse);

        ResponseEntity<String> response = controller.sendTemplate("01122406458", "testingtemplate", "en");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockMetaResponse, response.getBody());
        verify(service).sendTemplate("01122406458", "testingtemplate", "en");
    }

    @Test
    @DisplayName("sendTemplate should return downstream status and body when RestClientResponseException is thrown")
    void shouldPropagateMetaErrorResponse() {
        String metaErrorJson = "{\"error\":{\"message\":\"(#132001) Template does not exist\",\"code\":132001}}";
        RestClientResponseException ex = HttpClientErrorException.create(
                HttpStatusCode.valueOf(400),
                "Bad Request",
                HttpHeaders.EMPTY,
                metaErrorJson.getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );

        when(service.sendTemplate("01122406458", "testingtemplate", "en")).thenThrow(ex);

        ResponseEntity<String> response = controller.sendTemplate("01122406458", "testingtemplate", "en");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(metaErrorJson, response.getBody());
    }
}
