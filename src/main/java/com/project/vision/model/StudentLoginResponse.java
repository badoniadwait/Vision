package com.project.vision.model;

public class StudentLoginResponse {
    private boolean success;
    private String message;
    private String token;
    private StudentRes student;
    private String ImgUrl;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public StudentRes getStudent() {
        return student;
    }

    public void setStudent(StudentRes student) {
        this.student = student;
    }

    public String getImgUrl() {
        return ImgUrl;
    }

    public void setImgUrl(String ImgUrl) {
        this.ImgUrl = ImgUrl;
    }


}
