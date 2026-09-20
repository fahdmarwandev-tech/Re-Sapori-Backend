package com.resapori.e_commerce.northbound.controllers;

import com.resapori.e_commerce.service.IWhatsAppService;
import com.resapori.e_commerce.service.impl.WhatsAppServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {WhatsAppServiceImpl.class, WhatsAppController.class})
class WhatsAppContextTest {

    @Autowired
    private WhatsAppController controller;

    @Autowired
    private IWhatsAppService service;

    @Test
    @DisplayName("Should successfully instantiate and inject WhatsAppController and WhatsAppServiceImpl in Spring context")
    void contextLoadsWhatsAppBeans() {
        assertNotNull(controller);
        assertNotNull(service);
    }
}
