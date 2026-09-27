package fr.library.api.service;

import fr.library.api.client.CatalogClient;
import fr.library.api.dto.BookDto;
import fr.library.api.dto.CreateBookRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/** Les livres sont gérés par le serveur RPC : ce service délègue au client gRPC. */
@Service
public class BookService {

    private static final Logger log = LoggerFactory.getLogger(BookService.class);

    private final CatalogClient catalogClient;

    @Autowired
    public BookService(CatalogClient catalogClient) {
        this.catalogClient = catalogClient;
    }

    public List<BookDto> listBooks() {
        return catalogClient.listBooks();
    }

    public BookDto getBook(Long id) {
        return catalogClient.getBook(id);
    }

    public BookDto addBook(CreateBookRequest request) {
        BookDto book = catalogClient.addBook(request.isbn(), request.title(),
                request.author(), request.totalCopies());
        log.info("Livre créé via le catalogue : id={}, titre='{}'", book.id(), book.title());
        return book;
    }
}
