package com.example.androidproject.models;

public class NetworkStatus {
    String message;
    Status status;

    public NetworkStatus(String message, Status status) {
        this.message = message;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

   public enum Status{
        SUCCESS,ERROR,LOADING
    }
}
