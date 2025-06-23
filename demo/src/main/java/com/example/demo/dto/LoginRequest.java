package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "L'email ne peut pas être vide.")
    @Email(message = "Le format de l'email est invalide.")
    private String email;

    @NotBlank(message = "Le mot de passe ne peut pas être vide.")
    private String password;
}
