package fr.library.api.exception;

/** Le serveur RPC catalogue ne répond pas : renvoyée en HTTP 503. */
public class CatalogUnavailableException extends RuntimeException {

    public CatalogUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
