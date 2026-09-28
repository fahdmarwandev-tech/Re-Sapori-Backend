package com.resapori.e_commerce.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        File uploadFolder = uploadPath.toFile();
        if (!uploadFolder.exists()) {
            boolean created = uploadFolder.mkdirs();
            log.info("Created upload directory at {}: {}", uploadPath, created);
        }

        String uploadUri = uploadPath.toUri().toString();
        if (!uploadUri.endsWith("/")) {
            uploadUri += "/";
        }

        log.info("Mapping /uploads/** to file path: {}", uploadUri);

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadUri);
    }
}
