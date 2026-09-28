package com.resapori.e_commerce.service;

import org.springframework.web.multipart.MultipartFile;

public interface IUploadService {

    /**
     * Uploads an image file to local storage.
     *
     * @param file   the multipart image file to upload
     * @param folder the folder to store the image in (e.g. "menu-items")
     * @return the public URL or relative path of the uploaded image
     */
    String uploadImage(MultipartFile file, String folder);

    /**
     * Deletes an image from storage by its URL or path.
     *
     * @param imageUrlOrPath the URL or relative path of the image to delete
     */
    void deleteImage(String imageUrlOrPath);
}
