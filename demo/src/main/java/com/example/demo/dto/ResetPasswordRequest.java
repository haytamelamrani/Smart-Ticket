package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @NotBlank(message = "L'email ne peut pas être vide.")
    @Email(message = "Le format de l'email est invalide.")
    private String email;

    @NotBlank(message = "Le nouveau mot de passe ne peut pas être vide.")
    @Size(min = 8, message = "Le nouveau mot de passe doit contenir au moins 8 caractères.")
    // Vous pourriez ajouter la même regex de complexité que pour l'inscription si souhaité
    private String newPassword;
}
