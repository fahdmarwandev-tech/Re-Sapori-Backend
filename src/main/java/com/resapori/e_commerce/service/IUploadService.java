package com.resapori.e_commerce.service;

import org.springframework.web.multipart.MultipartFile;

public interface IUploadService {

    /**
     * Uploads an image file to Cloudinary.
     *
     * @param file   the multipart image file to upload
     * @param folder the Cloudinary folder to store the image in (e.g. "menu-items")
     * @return the secure public URL of the uploaded image
     */
    String uploadImage(MultipartFile file, String folder);

    /**
     * Deletes an asset from Cloudinary by its public ID.
     *
     * @param publicId the Cloudinary public ID of the asset to delete
     */
    void deleteImage(String publicId);
}
