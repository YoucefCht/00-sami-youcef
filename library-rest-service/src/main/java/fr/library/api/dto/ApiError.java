package fr.library.api.dto;

import java.time.Instant;
import java.util.List;

/** Corps JSON renvoyé pour toute erreur HTTP. */
public record ApiError(Instant timestamp, int status, String error, String message,
                       String path, List<String> details) {
}
