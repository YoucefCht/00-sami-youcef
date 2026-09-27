package fr.library.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateBookRequest(
        @NotBlank(message = "l'ISBN est obligatoire") String isbn,
        @NotBlank(message = "le titre est obligatoire") String title,
        @NotBlank(message = "l'auteur est obligatoire") String author,
        @Min(value = 1, message = "il faut au moins 1 exemplaire") int totalCopies) {
}
