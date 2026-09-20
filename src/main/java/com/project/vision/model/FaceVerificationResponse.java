package com.project.vision.model;

import org.opencv.core.Mat;

public class FaceVerificationResponse {

    private boolean verified;
    private String message;
    private Mat faces;
    private Boolean areSame;

    public FaceVerificationResponse() {
    }

    public FaceVerificationResponse(boolean verified, String message, Mat faces, Boolean areSame) {
        this.verified = verified;
        this.message = message;
        this.faces = faces;
        this.areSame = areSame;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public Boolean getAreSame() {
        return areSame;
    }

    public void setAreSame(Boolean areSame) {
        this.areSame = areSame;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Mat getFaces() {
        return faces;
    }

    public void setFaces(Mat faces) {
        this.faces = faces;
    }
}