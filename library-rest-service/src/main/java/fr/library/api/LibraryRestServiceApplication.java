package fr.library.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Service REST (point d'entrée public) : gère les adhérents et les emprunts,
 * et interroge le serveur RPC catalogue pour tout ce qui concerne les livres.
 */
@SpringBootApplication
public class LibraryRestServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibraryRestServiceApplication.class, args);
    }
}
