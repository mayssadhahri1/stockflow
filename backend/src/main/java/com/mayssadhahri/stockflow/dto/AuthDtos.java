package com.mayssadhahri.stockflow.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Objets échangés lors de l'inscription et de la connexion. */
public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank(message = "Le nom est obligatoire") @Size(max = 80) String nom,
            @NotBlank(message = "L'email est obligatoire") @Email(message = "Email invalide") String email,
            @NotBlank @Size(min = 8, max = 100, message = "Le mot de passe doit contenir au moins 8 caractères") String password) {
    }

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password) {
    }

    public record AuthResponse(String token, String nom, String email, String role) {
    }
}
