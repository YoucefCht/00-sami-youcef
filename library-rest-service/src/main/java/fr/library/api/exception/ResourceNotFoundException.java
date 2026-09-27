package fr.library.api.exception;

/** Ressource inexistante : renvoyée en HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
