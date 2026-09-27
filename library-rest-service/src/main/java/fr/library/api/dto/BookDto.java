package fr.library.api.dto;

public record BookDto(Long id, String isbn, String title, String author,
                      int totalCopies, int availableCopies) {
}
