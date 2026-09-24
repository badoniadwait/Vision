package com.project.vision.dto;

import org.springframework.web.multipart.MultipartFile;

public class StudentDto {
    private String sapId;
    private String name;
    private String password;
    private MultipartFile referencePhotoPath;

    public MultipartFile getReferencePhotoPath() {
        return referencePhotoPath;
    }

    public void setReferencePhotoPath(MultipartFile referencePhotoPath) {
        this.referencePhotoPath = referencePhotoPath;
    }

    public String getSapId() {
        return sapId;
    }

    public void setSapId(String sapId) {
        this.sapId = sapId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
