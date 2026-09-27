package fr.library.catalog.service;

/** Violation d'une règle métier du catalogue (plus d'exemplaire disponible, ISBN déjà utilisé...). */
public class CatalogRuleException extends RuntimeException {

    public CatalogRuleException(String message) {
        super(message);
    }
}
