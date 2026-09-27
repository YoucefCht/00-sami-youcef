package fr.library.catalog.service;

import fr.library.catalog.entity.Book;
import fr.library.catalog.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    @Test
    void reserveCopyDecrementsAvailableCopies() {
        Book book = new Book("isbn", "Titre", "Auteur", 2);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        Book result = bookService.reserveCopy(1L);

        assertEquals(1, result.getAvailableCopies());
    }

    @Test
    void reserveCopyFailsWhenNoCopyLeft() {
        Book book = new Book("isbn", "Titre", "Auteur", 1);
        book.setAvailableCopies(0);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        assertThrows(CatalogRuleException.class, () -> bookService.reserveCopy(1L));
    }

    @Test
    void getBookFailsWhenUnknownId() {
        when(bookRepository.findById(42L)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> bookService.getBook(42L));
    }

    @Test
    void releaseCopyFailsWhenAllCopiesAlreadyBack() {
        Book book = new Book("isbn", "Titre", "Auteur", 1);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        assertThrows(CatalogRuleException.class, () -> bookService.releaseCopy(1L));
    }
}
