package fr.library.api.exception;

/** Règle métier non respectée (livre indisponible, trop d'emprunts...) : renvoyée en HTTP 409. */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
