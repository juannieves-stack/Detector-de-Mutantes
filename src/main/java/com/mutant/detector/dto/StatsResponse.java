package com.mutant.detector.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la respuesta de estadísticas.
 * 
 * Contiene conteos de secuencias de ADN mutantes y humanas analizadas,
 * junto con el ratio de mutantes sobre el total de secuencias.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    description = "Estadísticas de las secuencias de ADN analizadas",
    example = """
        {
          "count_mutant_dna": 40,
          "count_human_dna": 100,
          "ratio": 0.4
        }
        """
)
public class StatsResponse {

    @JsonProperty("count_mutant_dna")
    @Schema(
        description = "Cantidad de secuencias de ADN mutantes detectadas",
        example = "40",
        minimum = "0"
    )
    private long countMutantDna;

    @JsonProperty("count_human_dna")
    @Schema(
        description = "Cantidad de secuencias de ADN humanas (no mutantes) detectadas",
        example = "100",
        minimum = "0"
    )
    private long countHumanDna;

    @JsonProperty("ratio")
    @Schema(
        description = "Ratio de mutantes sobre el total de secuencias analizadas (mutantes / total)",
        example = "0.4",
        minimum = "0.0",
        maximum = "1.0"
    )
    private double ratio;
}
