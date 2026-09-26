package com.project.vision.model;

import org.springframework.web.multipart.MultipartFile;

public class FaceVerificationRequest {
    private String imgUrl;
    private MultipartFile image;

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public MultipartFile getImage() {
        return image;
    }

    public void setImage(MultipartFile image) {
        this.image = image;
    }
}
