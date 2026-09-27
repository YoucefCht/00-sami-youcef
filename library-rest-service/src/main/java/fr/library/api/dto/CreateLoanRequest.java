package fr.library.api.dto;

import jakarta.validation.constraints.NotNull;

public record CreateLoanRequest(
        @NotNull(message = "memberId est obligatoire") Long memberId,
        @NotNull(message = "bookId est obligatoire") Long bookId) {
}
