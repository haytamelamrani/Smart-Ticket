package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // N'inclut pas les champs null dans le JSON
public class ErrorResponse {
    private int statusCode;
    private String message;
    private LocalDateTime timestamp;
    private String path; // L'URL qui a causé l'erreur
    private Map<String, String> validationErrors; // Pour les erreurs de validation spécifiques aux champs

    public ErrorResponse(int statusCode, String message, String path) {
        this.statusCode = statusCode;
        this.message = message;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(int statusCode, String message, String path, Map<String, String> validationErrors) {
        this(statusCode, message, path);
        this.validationErrors = validationErrors;
    }
}
