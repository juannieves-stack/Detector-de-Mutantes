package com.mutant.detector.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO de respuesta de error estándar.
 * 
 * Proporciona un formato de error consistente en toda la API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Respuesta de error estándar de la API")
public class ErrorResponse {

    @Schema(
        description = "Timestamp del error",
        example = "2025-11-22T11:45:00"
    )
    private LocalDateTime timestamp;

    @Schema(
        description = "Código de estado HTTP",
        example = "400"
    )
    private int status;

    @Schema(
        description = "Nombre del error HTTP",
        example = "Bad Request"
    )
    private String error;

    @Schema(
        description = "Mensaje de error principal",
        example = "Invalid DNA sequence"
    )
    private String message;

    @Schema(
        description = "Ruta del endpoint que generó el error",
        example = "/mutant"
    )
    private String path;

    @Schema(
        description = "Detalles adicionales del error (opcional)",
        example = "{\"dna\": \"DNA array cannot be empty\"}"
    )
    private Map<String, String> details;

    /**
     * Constructor para respuestas de error simples.
     */
    public ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }
}
