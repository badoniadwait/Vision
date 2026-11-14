package com.project.vision.dto;

public class QuizResultsDto {

    private String quizId;
    private int marksObtained;
    private int totalMarks;

    public QuizResultsDto() {
    }

    public QuizResultsDto(String quizId, int marksObtained, int totalMarks) {
        this.quizId = quizId;
        this.marksObtained = marksObtained;
        this.totalMarks = totalMarks;
    }

    public String getQuizId() {
        return quizId;
    }

    public void setQuizId(String quizId) {
        this.quizId = quizId;
    }

    public double getMarksObtained() {
        return marksObtained;
    }

    public void setMarksObtained(int marksObtained) {
        this.marksObtained = marksObtained;
    }

    public int getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(int totalMarks) {
        this.totalMarks = totalMarks;
    }
}
