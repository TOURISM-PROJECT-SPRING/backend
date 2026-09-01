package com.example.spring_boot_project_api.exception;

public class CloudinaryDeleteException extends RuntimeException {

    public CloudinaryDeleteException(String message) {
        super(message);
    }

    public CloudinaryDeleteException(String message, Throwable cause) {
        super(message, cause);
    }
}