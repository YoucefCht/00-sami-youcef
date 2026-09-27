package fr.library.catalog.service;

public class BookNotFoundException extends RuntimeException {

    public BookNotFoundException(Long id) {
        super("Livre introuvable : id=" + id);
    }
}
