package com.resapori.e_commerce.service.impl;

import com.resapori.e_commerce.service.IUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
public class UploadServiceImpl implements IUploadService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.upload.base-url:/uploads}")
    private String uploadBaseUrl;

    @Override
    public String uploadImage(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload an empty file.");
        }

        String sanitizedFolder = sanitizeFolder(folder);

        try {
            Path targetDir = Paths.get(uploadDir, sanitizedFolder).toAbsolutePath().normalize();
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            String originalFilename = StringUtils.cleanPath(
                    file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg"
            );
            String extension = "";
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex >= 0) {
                extension = originalFilename.substring(dotIndex).toLowerCase();
            } else {
                extension = ".jpg";
            }

            String uniqueFilename = UUID.randomUUID().toString() + extension;
            Path destinationFile = targetDir.resolve(uniqueFilename).normalize();

            // Guard against directory traversal attacks
            if (!destinationFile.startsWith(targetDir)) {
                throw new SecurityException("Cannot store file outside target directory.");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            log.info("Image saved locally at: {}", destinationFile);

            String baseUrl = uploadBaseUrl.replaceAll("/+$", "");
            return baseUrl + "/" + sanitizedFolder + "/" + uniqueFilename;

        } catch (IOException e) {
            log.error("Failed to store uploaded file locally", e);
            throw new RuntimeException("Could not store image locally: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteImage(String imageUrlOrPath) {
        if (imageUrlOrPath == null || imageUrlOrPath.isBlank()) {
            return;
        }

        try {
            String relative = imageUrlOrPath;
            String baseUrl = uploadBaseUrl.replaceAll("/+$", "");
            if (relative.contains(baseUrl)) {
                relative = relative.substring(relative.indexOf(baseUrl) + baseUrl.length());
            }
            if (relative.startsWith("/")) {
                relative = relative.substring(1);
            }

            Path filePath = Paths.get(uploadDir, relative).toAbsolutePath().normalize();
            Path rootUploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();

            if (!filePath.startsWith(rootUploadPath)) {
                log.warn("Security check failed for file deletion: {}", imageUrlOrPath);
                return;
            }

            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                log.info("Deleted local image file: {}", filePath);
            } else {
                log.warn("Image file not found for deletion: {}", filePath);
            }
        } catch (IOException e) {
            log.error("Failed to delete local image: {}", imageUrlOrPath, e);
        }
    }

    private String sanitizeFolder(String folder) {
        if (folder == null || folder.isBlank()) {
            return "menu-items";
        }
        String sanitized = folder.replaceAll("[^a-zA-Z0-9_-]", "");
        return sanitized.isBlank() ? "menu-items" : sanitized;
    }
}
