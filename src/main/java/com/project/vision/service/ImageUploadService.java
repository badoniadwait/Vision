package com.project.vision.service;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageUploadService {

    private final RestClient restClient;
    private final String storageBaseUrl;
    private final String serviceKey;
    private final String bucket;

    public ImageUploadService(
            RestClient.Builder restClientBuilder,
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.service-key}") String serviceKey,
            @Value("${supabase.bucket}") String bucket) {
        this.restClient = restClientBuilder.build();
        this.storageBaseUrl = supabaseUrl.replaceFirst("/rest/v1/?$", "").replaceAll("/$", "");
        this.serviceKey = serviceKey;
        this.bucket = bucket;
    }

    public String uploadToSupabase(MultipartFile image) throws IOException {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("A non-empty image is required.");
        }

        String extension = StringUtils.getFilenameExtension(image.getOriginalFilename());
        String objectName = UUID.randomUUID() + (StringUtils.hasText(extension) ? "." + extension : "");
        MediaType contentType = MediaType.APPLICATION_OCTET_STREAM;
        if (StringUtils.hasText(image.getContentType())) {
            try {
                contentType = MediaType.parseMediaType(image.getContentType());
            } catch (IllegalArgumentException ignored) {
                // Upload with a safe fallback when the client sends an invalid content type.
            }
        }

        String uploadUrl = storageBaseUrl
                + "/storage/v1/object/"
                + bucket
                + "/"
                + objectName;

        restClient.post()
                .uri(uploadUrl)
                .header("apikey", serviceKey)
                .header("Authorization", "Bearer " + serviceKey)
                .header("x-upsert", "false")
                .contentType(contentType)
                .body(image.getBytes())
                .retrieve()
                .toBodilessEntity();

        return fetchImage(objectName);
    }

    public String fetchImage(String imageName) {
        if (!StringUtils.hasText(imageName)) {
            throw new IllegalArgumentException("Image name is required.");
        }
        String normalizedName = imageName.replaceFirst("^/+", "");
        return storageBaseUrl + "/storage/v1/object/public/" + bucket + "/" + normalizedName;
    }
}
