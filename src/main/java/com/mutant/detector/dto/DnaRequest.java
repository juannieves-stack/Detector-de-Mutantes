package com.mutant.detector.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mutant.detector.validation.ValidDna;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la solicitud de secuencia de ADN.
 * 
 * Recibe un array de secuencias de ADN donde cada string representa una fila
 * en la matriz de ADN.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    description = "Solicitud de análisis de ADN",
    example = """
        {
          "dna": [
            "ATGCGA",
            "CAGTGC",
            "TTATGT",
            "AGAAGG",
            "CCCCTA",
            "TCACTG"
          ]
        }
        """
)
public class DnaRequest {

    @NotNull(message = "El array de ADN no puede ser nulo")
    @NotEmpty(message = "El array de ADN no puede estar vacío")
    @ValidDna
    @JsonProperty("dna")
    @Schema(
        description = "Matriz de ADN representada como un array de strings. Cada string es una fila de la matriz NxN.",
        example = "[\"ATGCGA\",\"CAGTGC\",\"TTATGT\",\"AGAAGG\",\"CCCCTA\",\"TCACTG\"]",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minLength = 4,
        implementation = String[].class
    )
    private String[] dna;
}
