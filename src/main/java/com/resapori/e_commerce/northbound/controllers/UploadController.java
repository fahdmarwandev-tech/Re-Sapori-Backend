package com.resapori.e_commerce.northbound.controllers;

import com.resapori.e_commerce.northbound.dto.upload.UploadResponse;
import com.resapori.e_commerce.service.IUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB

    private final IUploadService uploadService;

    /**
     * Upload an image to Cloudinary.
     * Requires ADMIN role.
     *
     * @param file   the image file (multipart/form-data, field name: "file")
     * @param folder optional Cloudinary folder name (default: "menu-items")
     * @return JSON with the secure URL: { "url": "https://res.cloudinary.com/..." }
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "menu-items") String folder
    ) {
        validateImage(file);
        String url = uploadService.uploadImage(file, folder);
        return ResponseEntity.ok(new UploadResponse(url));
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file provided.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException(
                    "Invalid file type '%s'. Only image files are allowed.".formatted(contentType));
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException(
                    "File size exceeds the 5 MB limit (got %d bytes).".formatted(file.getSize()));
        }
    }
}
