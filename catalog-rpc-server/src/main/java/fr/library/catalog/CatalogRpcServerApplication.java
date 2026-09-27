package fr.library.catalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Serveur RPC (gRPC) : gère le catalogue de livres et le stock d'exemplaires.
 * Il possède sa propre base de données, indépendante de celle du service REST.
 */
@SpringBootApplication
public class CatalogRpcServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatalogRpcServerApplication.class, args);
    }
}
