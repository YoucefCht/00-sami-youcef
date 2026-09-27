package fr.library.api.client;

import fr.library.api.dto.BookDto;
import fr.library.api.exception.BusinessRuleException;
import fr.library.api.exception.CatalogUnavailableException;
import fr.library.api.exception.ResourceNotFoundException;
import fr.library.grpc.AddBookRequest;
import fr.library.grpc.BookIdRequest;
import fr.library.grpc.BookResponse;
import fr.library.grpc.CatalogServiceGrpc;
import fr.library.grpc.ListBooksRequest;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Supplier;

/**
 * Client RPC : seul endroit du service REST qui parle gRPC.
 * Il convertit les messages protobuf en DTO et les statuts gRPC en exceptions métier,
 * pour que le reste de l'application ne dépende pas de gRPC.
 */
@Component
public class CatalogClient {

    private static final Logger log = LoggerFactory.getLogger(CatalogClient.class);

    /** Stub injecté par le starter gRPC (configuration "grpc.client.catalog" dans application.yml). */
    @GrpcClient("catalog")
    private CatalogServiceGrpc.CatalogServiceBlockingStub stub;

    public List<BookDto> listBooks() {
        return call("ListBooks", () -> stub.listBooks(ListBooksRequest.getDefaultInstance())
                .getBooksList().stream().map(CatalogClient::toDto).toList());
    }

    public BookDto getBook(Long id) {
        return call("GetBook", () -> toDto(stub.getBook(idRequest(id))));
    }

    public BookDto addBook(String isbn, String title, String author, int totalCopies) {
        AddBookRequest request = AddBookRequest.newBuilder()
                .setIsbn(isbn).setTitle(title).setAuthor(author).setTotalCopies(totalCopies)
                .build();
        return call("AddBook", () -> toDto(stub.addBook(request)));
    }

    public BookDto reserveCopy(Long id) {
        return call("ReserveCopy", () -> toDto(stub.reserveCopy(idRequest(id))));
    }

    public BookDto releaseCopy(Long id) {
        return call("ReleaseCopy", () -> toDto(stub.releaseCopy(idRequest(id))));
    }

    private <T> T call(String operation, Supplier<T> rpc) {
        try {
            log.debug("Appel RPC {}", operation);
            return rpc.get();
        } catch (StatusRuntimeException e) {
            String message = e.getStatus().getDescription() != null
                    ? e.getStatus().getDescription()
                    : e.getStatus().getCode().name();
            log.warn("RPC {} en échec : {} - {}", operation, e.getStatus().getCode(), message);
            switch (e.getStatus().getCode()) {
                case NOT_FOUND:
                    throw new ResourceNotFoundException(message);
                case FAILED_PRECONDITION:
                case ABORTED:
                    throw new BusinessRuleException(message);
                case INVALID_ARGUMENT:
                    throw new IllegalArgumentException(message);
                case UNAVAILABLE:
                case DEADLINE_EXCEEDED:
                    throw new CatalogUnavailableException("Le service catalogue est indisponible", e);
                default:
                    throw e;
            }
        }
    }

    private static BookIdRequest idRequest(Long id) {
        return BookIdRequest.newBuilder().setId(id).build();
    }

    private static BookDto toDto(BookResponse b) {
        return new BookDto(b.getId(), b.getIsbn(), b.getTitle(), b.getAuthor(),
                b.getTotalCopies(), b.getAvailableCopies());
    }
}
