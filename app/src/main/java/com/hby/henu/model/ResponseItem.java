package com.hby.henu.model;

public class ResponseItem {
    private String message;
    private String result;
    private String status;

    public ResponseItem(String message, String result, String status) {
        this.message = message;
        this.result = result;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
