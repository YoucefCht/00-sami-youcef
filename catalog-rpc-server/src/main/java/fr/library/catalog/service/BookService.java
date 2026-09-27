package fr.library.catalog.service;

import fr.library.catalog.entity.Book;
import fr.library.catalog.repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Couche service : règles métier du catalogue.
 * Ne connaît ni gRPC ni HTTP, seulement les entités et le repository.
 */
@Service
public class BookService {

    private static final Logger log = LoggerFactory.getLogger(BookService.class);

    private final BookRepository bookRepository;

    @Autowired
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Transactional(readOnly = true)
    public Book getBook(Long id) {
        return bookRepository.findById(id).orElseThrow(() -> new BookNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Book> listBooks() {
        return bookRepository.findAll();
    }

    @Transactional
    public Book addBook(String isbn, String title, String author, int totalCopies) {
        if (isBlank(isbn) || isBlank(title) || isBlank(author)) {
            throw new IllegalArgumentException("ISBN, titre et auteur sont obligatoires");
        }
        if (totalCopies < 1) {
            throw new IllegalArgumentException("Le nombre d'exemplaires doit être >= 1");
        }
        if (bookRepository.existsByIsbn(isbn)) {
            throw new CatalogRuleException("Un livre avec l'ISBN " + isbn + " existe déjà");
        }
        Book saved = bookRepository.save(new Book(isbn, title, author, totalCopies));
        log.info("Livre ajouté : id={}, titre='{}', exemplaires={}", saved.getId(), title, totalCopies);
        return saved;
    }

    @Transactional
    public Book reserveCopy(Long id) {
        Book book = getBook(id);
        if (book.getAvailableCopies() <= 0) {
            log.warn("Réservation refusée : aucun exemplaire disponible pour le livre id={}", id);
            throw new CatalogRuleException("Aucun exemplaire disponible pour '" + book.getTitle() + "'");
        }
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        log.info("Exemplaire réservé : livre id={}, restants={}", id, book.getAvailableCopies());
        return book;
    }

    @Transactional
    public Book releaseCopy(Long id) {
        Book book = getBook(id);
        if (book.getAvailableCopies() >= book.getTotalCopies()) {
            throw new CatalogRuleException("Tous les exemplaires de '" + book.getTitle() + "' sont déjà rendus");
        }
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        log.info("Exemplaire rendu : livre id={}, disponibles={}", id, book.getAvailableCopies());
        return book;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
