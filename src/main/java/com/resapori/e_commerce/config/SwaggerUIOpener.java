package com.resapori.e_commerce.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

import java.io.IOException;

@Configuration
@Profile("dev")
public class SwaggerUIOpener {

    private static final Logger logger = LoggerFactory.getLogger(SwaggerUIOpener.class);
    private final Environment environment;

    public SwaggerUIOpener(Environment environment) {
        this.environment = environment;
    }

    @EventListener({ApplicationReadyEvent.class})
    public void applicationReadyEvent() {
        logger.info("Application started in dev profile. Attempting to open Swagger UI in browser...");
        
        String port = environment.getProperty("server.port", "8080");
        String swaggerUrl = "http://localhost:" + port + "/swagger-ui/index.html";
        
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                Runtime.getRuntime().exec("cmd /c start " + swaggerUrl);
                logger.info("Swagger UI launched at {}", swaggerUrl);
            } else if (os.contains("mac")) {
                Runtime.getRuntime().exec("open " + swaggerUrl);
                logger.info("Swagger UI launched at {}", swaggerUrl);
            } else if (os.contains("nix") || os.contains("nux")) {
                Runtime.getRuntime().exec("xdg-open " + swaggerUrl);
                logger.info("Swagger UI launched at {}", swaggerUrl);
            } else {
                logger.warn("Unsupported OS for automatically launching browser. Swagger UI available at {}", swaggerUrl);
            }
        } catch (IOException e) {
            logger.error("Failed to automatically launch Swagger UI", e);
        }
    }
}
