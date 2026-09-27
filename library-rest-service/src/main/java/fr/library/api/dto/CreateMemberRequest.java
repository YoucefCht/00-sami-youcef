package fr.library.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateMemberRequest(
        @NotBlank(message = "le nom est obligatoire") String name,
        @NotBlank(message = "l'email est obligatoire") @Email(message = "email invalide") String email) {
}
