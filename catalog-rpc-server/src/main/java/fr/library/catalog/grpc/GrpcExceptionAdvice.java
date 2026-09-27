package fr.library.catalog.grpc;

import fr.library.catalog.service.BookNotFoundException;
import fr.library.catalog.service.CatalogRuleException;
import io.grpc.Status;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

/**
 * Gestionnaire d'exceptions global du serveur RPC (équivalent gRPC de @RestControllerAdvice).
 * Chaque exception métier devient un statut gRPC avec un message lisible par le client.
 */
@GrpcAdvice
public class GrpcExceptionAdvice {

    private static final Logger log = LoggerFactory.getLogger(GrpcExceptionAdvice.class);

    @GrpcExceptionHandler(BookNotFoundException.class)
    public Status handleNotFound(BookNotFoundException e) {
        log.warn("NOT_FOUND : {}", e.getMessage());
        return Status.NOT_FOUND.withDescription(e.getMessage()).withCause(e);
    }

    @GrpcExceptionHandler(CatalogRuleException.class)
    public Status handleRule(CatalogRuleException e) {
        log.warn("FAILED_PRECONDITION : {}", e.getMessage());
        return Status.FAILED_PRECONDITION.withDescription(e.getMessage()).withCause(e);
    }

    @GrpcExceptionHandler(IllegalArgumentException.class)
    public Status handleInvalid(IllegalArgumentException e) {
        log.warn("INVALID_ARGUMENT : {}", e.getMessage());
        return Status.INVALID_ARGUMENT.withDescription(e.getMessage()).withCause(e);
    }

    @GrpcExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public Status handleConcurrentUpdate(ObjectOptimisticLockingFailureException e) {
        log.warn("ABORTED : modification concurrente", e);
        return Status.ABORTED.withDescription("Modification concurrente, veuillez réessayer").withCause(e);
    }

    @GrpcExceptionHandler(Exception.class)
    public Status handleUnexpected(Exception e) {
        log.error("Erreur inattendue dans le serveur RPC", e);
        return Status.INTERNAL.withDescription("Erreur interne du serveur catalogue").withCause(e);
    }
}
