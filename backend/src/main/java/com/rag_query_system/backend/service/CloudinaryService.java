package com.rag_query_system.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    /**
     * Uploads a file to Cloudinary with auto resource type.
     * For PDFs, Cloudinary typically categorizes them under the 'image' or 'raw' resource type.
     */
    public Map<String, String> uploadPdf(MultipartFile file) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
            "resource_type", "auto"
        ));
        
        Map<String, String> result = new HashMap<>();
        result.put("url", (String) uploadResult.get("secure_url"));
        result.put("publicId", (String) uploadResult.get("public_id"));
        return result;
    }

    /**
     * Deletes a file from Cloudinary using its public ID.
     */
    public void deletePdf(String publicId) throws IOException {
        // By default, Cloudinary treats PDFs as 'image' resource type.
        // We delete it using resource_type = image. If that fails or does not apply, we try raw.
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image"));
        } catch (Exception e) {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "raw"));
        }
    }
}
