package com.project.vision.service;

import org.springframework.http.HttpStatus;

public class FaceVerificationException extends RuntimeException {

    private final HttpStatus status;

    public FaceVerificationException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}