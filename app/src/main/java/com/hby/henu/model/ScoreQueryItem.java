package com.hby.henu.model;

public class ScoreQueryItem {
    private String course;
    private String score;

    public ScoreQueryItem(String course, String score) {
        this.course = course;
        this.score = score;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }
}
