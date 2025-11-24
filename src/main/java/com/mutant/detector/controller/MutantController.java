package com.mutant.detector.controller;

import com.mutant.detector.dto.DnaRequest;
import com.mutant.detector.dto.StatsResponse;
import com.mutant.detector.service.MutantDetectorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la API de Detección de Mutantes.
 * 
 * Proporciona endpoints para:
 * - Detectar si una secuencia de ADN pertenece a un mutante
 * - Obtener estadísticas sobre las secuencias de ADN analizadas
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(
    name = "API de Detección de Mutantes",
    description = """
        API para detectar mutantes basada en secuencias de ADN.
        
        Proporciona dos endpoints principales:
        - POST /mutant: Analiza una secuencia de ADN y determina si pertenece a un mutante
        - GET /stats: Obtiene estadísticas sobre las secuencias analizadas
        """
)
public class MutantController {

    private final MutantDetectorService mutantDetectorService;

    /**
     * POST /mutant
     * 
     * Analiza una secuencia de ADN para determinar si pertenece a un mutante.
     * Retorna 200 OK si es mutante, 403 Forbidden si es humano.
     * 
     * @param request Solicitud de ADN que contiene el array de secuencias
     * @return 200 si es mutante, 403 si es humano
     */
    @PostMapping(value = "/mutant", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Detectar si un ADN es mutante",
        description = """
            Analiza una secuencia de ADN para determinar si pertenece a un mutante.
            
            Criterio de Detección:
            Un humano es mutante si se encuentra más de una secuencia de 4 letras iguales 
            (A, T, C, G) en cualquier dirección:
            - Horizontal
            - Vertical
            - Diagonal descendente
            - Diagonal ascendente
            
            Optimizaciones:
            - Early Termination: Se detiene al encontrar la 2da secuencia
            - Cache SHA-256: Evita re-analizar el mismo ADN
            
            Validaciones:
            - La matriz debe ser NxN (cuadrada)
            - Solo se permiten caracteres: A, T, C, G (mayúsculas)
            - El array no puede ser nulo o vacío
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "El ADN pertenece a un mutante",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403",
            description = "El ADN pertenece a un humano (no es mutante)",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Error de validación: formato de ADN inválido",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(
                        name = "Matriz no cuadrada",
                        value = """
                            {
                              "error": "El ADN debe ser una matriz NxN. Se esperaba longitud 6 pero se obtuvo 3 en el índice 2"
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Caracteres inválidos",
                        value = """
                            {
                              "error": "Base de ADN inválida 'X'. Solo se permiten A, T, C, G"
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Array vacío",
                        value = """
                            {
                              "dna": "El array de ADN no puede estar vacío"
                            }
                            """
                    )
                }
            )
        )
    })
    public ResponseEntity<Void> detectMutant(
            @Parameter(
                description = "Solicitud con la matriz de ADN a analizar",
                required = true,
                schema = @Schema(implementation = DnaRequest.class)
            )
            @Valid @RequestBody DnaRequest request
    ) {
        log.info("Solicitud de detección de ADN recibida");
        
        boolean isMutant = mutantDetectorService.isMutant(request.getDna());
        
        if (isMutant) {
            log.info("Resultado: MUTANTE detectado");
            return ResponseEntity.ok().build();
        } else {
            log.info("Resultado: HUMANO detectado");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * GET /stats
     * 
     * Retorna estadísticas sobre las secuencias de ADN analizadas.
     * 
     * @return StatsResponse con conteos y ratio
     */
    @GetMapping(value = "/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Obtener estadísticas de análisis de ADN",
        description = """
            Retorna estadísticas sobre todas las secuencias de ADN analizadas.
            
            Información Incluida:
            - count_mutant_dna: Cantidad total de mutantes detectados
            - count_human_dna: Cantidad total de humanos detectados
            - ratio: Proporción de mutantes sobre el total (mutantes / total)
            
            Cálculo del Ratio:
            - ratio = count_mutant_dna / (count_mutant_dna + count_human_dna)
            - Si no hay registros, ratio = 0.0
            - El ratio siempre está entre 0.0 y 1.0
            
            Rendimiento:
            - Utiliza índices en base de datos para conteos rápidos
            - Tiempo de respuesta: O(1)
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Estadísticas obtenidas exitosamente",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = StatsResponse.class),
                examples = {
                    @ExampleObject(
                        name = "Estadísticas con datos",
                        description = "Ejemplo con 40 mutantes y 100 humanos",
                        value = """
                            {
                              "count_mutant_dna": 40,
                              "count_human_dna": 100,
                              "ratio": 0.2857142857142857
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Sin datos",
                        description = "Cuando no se han analizado secuencias",
                        value = """
                            {
                              "count_mutant_dna": 0,
                              "count_human_dna": 0,
                              "ratio": 0.0
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Solo mutantes",
                        description = "Cuando todas las secuencias son mutantes",
                        value = """
                            {
                              "count_mutant_dna": 50,
                              "count_human_dna": 0,
                              "ratio": 1.0
                            }
                            """
                    )
                }
            )
        )
    })
    public ResponseEntity<StatsResponse> getStats() {
        log.info("Solicitud de estadísticas recibida");
        
        StatsResponse stats = mutantDetectorService.getStats();
        
        log.info("Retornando estadísticas: mutantes={}, humanos={}, ratio={}", 
                 stats.getCountMutantDna(), 
                 stats.getCountHumanDna(), 
                 stats.getRatio());
        
        return ResponseEntity.ok(stats);
    }

    /**
     * Clase interna para documentación del esquema de respuesta de error.
     */
    @Schema(description = "Respuesta de error para validaciones")
    private static class ErrorResponse {
        @Schema(description = "Mensaje de error descriptivo", example = "El ADN debe ser una matriz NxN")
        public String error;
    }
}
