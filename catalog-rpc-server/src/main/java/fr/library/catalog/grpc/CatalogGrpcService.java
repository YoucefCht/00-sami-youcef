package fr.library.catalog.grpc;

import fr.library.catalog.entity.Book;
import fr.library.catalog.service.BookService;
import fr.library.grpc.AddBookRequest;
import fr.library.grpc.BookIdRequest;
import fr.library.grpc.BookListResponse;
import fr.library.grpc.BookResponse;
import fr.library.grpc.CatalogServiceGrpc;
import fr.library.grpc.ListBooksRequest;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Couche "web" du serveur RPC : reçoit les appels gRPC, délègue au service,
 * convertit les entités en messages protobuf.
 * Les exceptions sont traduites en statuts gRPC par {@link GrpcExceptionAdvice}.
 * Aucun état n'est conservé entre deux appels (stateless).
 */
@GrpcService
public class CatalogGrpcService extends CatalogServiceGrpc.CatalogServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(CatalogGrpcService.class);

    private final BookService bookService;

    @Autowired
    public CatalogGrpcService(BookService bookService) {
        this.bookService = bookService;
    }

    @Override
    public void getBook(BookIdRequest request, StreamObserver<BookResponse> responseObserver) {
        log.debug("RPC GetBook id={}", request.getId());
        reply(responseObserver, toResponse(bookService.getBook(request.getId())));
    }

    @Override
    public void listBooks(ListBooksRequest request, StreamObserver<BookListResponse> responseObserver) {
        log.debug("RPC ListBooks");
        BookListResponse.Builder builder = BookListResponse.newBuilder();
        bookService.listBooks().forEach(book -> builder.addBooks(toResponse(book)));
        reply(responseObserver, builder.build());
    }

    @Override
    public void addBook(AddBookRequest request, StreamObserver<BookResponse> responseObserver) {
        log.debug("RPC AddBook isbn={}", request.getIsbn());
        Book book = bookService.addBook(request.getIsbn(), request.getTitle(),
                request.getAuthor(), request.getTotalCopies());
        reply(responseObserver, toResponse(book));
    }

    @Override
    public void reserveCopy(BookIdRequest request, StreamObserver<BookResponse> responseObserver) {
        log.debug("RPC ReserveCopy id={}", request.getId());
        reply(responseObserver, toResponse(bookService.reserveCopy(request.getId())));
    }

    @Override
    public void releaseCopy(BookIdRequest request, StreamObserver<BookResponse> responseObserver) {
        log.debug("RPC ReleaseCopy id={}", request.getId());
        reply(responseObserver, toResponse(bookService.releaseCopy(request.getId())));
    }

    private static <T> void reply(StreamObserver<T> observer, T response) {
        observer.onNext(response);
        observer.onCompleted();
    }

    private static BookResponse toResponse(Book book) {
        return BookResponse.newBuilder()
                .setId(book.getId())
                .setIsbn(book.getIsbn())
                .setTitle(book.getTitle())
                .setAuthor(book.getAuthor())
                .setTotalCopies(book.getTotalCopies())
                .setAvailableCopies(book.getAvailableCopies())
                .build();
    }
}
