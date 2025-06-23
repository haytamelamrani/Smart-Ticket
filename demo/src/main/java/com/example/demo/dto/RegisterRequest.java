package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Le prénom ne peut pas être vide.")
    @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères.")
    private String firstName;

    @NotBlank(message = "Le nom de famille ne peut pas être vide.")
    @Size(min = 2, max = 50, message = "Le nom de famille doit contenir entre 2 et 50 caractères.")
    private String lastName;

    @NotBlank(message = "L'email ne peut pas être vide.")
    @Email(message = "Le format de l'email est invalide.")
    @Size(max = 100, message = "L'email ne doit pas dépasser 100 caractères.")
    private String email;

    @NotBlank(message = "Le mot de passe ne peut pas être vide.")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères.")
    // Vous pourriez ajouter une regex pour la complexité du mot de passe si nécessaire, par exemple :
    // @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$",
    //          message = "Le mot de passe doit contenir au moins une minuscule, une majuscule, un chiffre, un caractère spécial et avoir au moins 8 caractères.")
    private String password;

    // Le champ 'company' peut être optionnel, donc pas de @NotBlank sauf si requis.
    @Size(max = 100, message = "Le nom de l'entreprise ne doit pas dépasser 100 caractères.")
    private String company;
}