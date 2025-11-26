package com.mutant.detector.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mutant.detector.dto.DnaRequest;
import com.mutant.detector.service.MutantDetectorService;
import com.mutant.detector.dto.StatsResponse;
import com.mutant.detector.exception.InvalidDnaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Tests de integración para MutantController usando MockMvc.
 * Prueba todos los endpoints REST con varios escenarios.
 */
@WebMvcTest(MutantController.class)
class MutantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MutantDetectorService mutantDetectorService;

    @Nested
    @DisplayName("POST /mutant - Pruebas de Detección de Mutantes")
    class MutantDetectionTests {

        @Test
        @DisplayName("Debería retornar 200 OK para ADN mutante")
        void testDetectMutant_ReturnOk() throws Exception {
            String[] dna = {
                "ATGCGA",
                "CAGTGC",
                "TTATGT",
                "AGAAGG",
                "CCCCTA",
                "TCACTG"
            };
            DnaRequest request = new DnaRequest(dna);

            when(mutantDetectorService.isMutant(any(String[].class))).thenReturn(true);

            mockMvc.perform(post("/mutant")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Debería retornar 403 Forbidden para ADN humano")
        void testDetectMutant_ReturnForbidden() throws Exception {
            String[] dna = {
                "ATGCGA",
                "CAGTGC",
                "TTATTT",
                "AGACGG",
                "GCGTCA",
                "TCACTG"
            };
            DnaRequest request = new DnaRequest(dna);

            when(mutantDetectorService.isMutant(any(String[].class))).thenReturn(false);

            mockMvc.perform(post("/mutant")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Debería retornar 200 para mutante con secuencias horizontales")
        void testMutantWithHorizontalSequences() throws Exception {
            String[] dna = {
                "AAAATG",
                "CAGTGC",
                "TTATGT",
                "AGAAGG",
                "CCCCTA",
                "TCACTG"
            };
            DnaRequest request = new DnaRequest(dna);

            when(mutantDetectorService.isMutant(any(String[].class))).thenReturn(true);

            mockMvc.perform(post("/mutant")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Debería retornar 200 para mutante con secuencias verticales")
        void testMutantWithVerticalSequences() throws Exception {
            String[] dna = {
                "ATGCGA",
                "AAGTGC",
                "ATATGT",
                "AGAAGG",
                "CCCCTA",
                "TCACTG"
            };
            DnaRequest request = new DnaRequest(dna);

            when(mutantDetectorService.isMutant(any(String[].class))).thenReturn(true);

            mockMvc.perform(post("/mutant")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Debería retornar 200 para mutante con secuencias diagonales")
        void testMutantWithDiagonalSequences() throws Exception {
            String[] dna = {
                "ATGCGA",
                "CAGTGC",
                "TTATGT",
                "AGAAGG",
                "CCCCTA",
                "TCACTG"
            };
            DnaRequest request = new DnaRequest(dna);

            when(mutantDetectorService.isMutant(any(String[].class))).thenReturn(true);

            mockMvc.perform(post("/mutant")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Debería retornar 403 para humano con solo una secuencia")
        void testHumanWithOneSequence() throws Exception {
            String[] dna = {
                "AAAATG",
                "CAGTGC",
                "TTATGT",
                "AGACGG",
                "GCGTCA",
                "TCACTG"
            };
            DnaRequest request = new DnaRequest(dna);

            when(mutantDetectorService.isMutant(any(String[].class))).thenReturn(false);

            mockMvc.perform(post("/mutant")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Debería manejar matriz 4x4")
        void testSmallMatrix() throws Exception {
            String[] dna = {
                "AAAA",
                "CCCC",
                "TTAT",
                "AGAC"
            };
            DnaRequest request = new DnaRequest(dna);

            when(mutantDetectorService.isMutant(any(String[].class))).thenReturn(true);

            mockMvc.perform(post("/mutant")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("POST /mutant - Pruebas de Validación")
    class ValidationTests {

        @Test
        @DisplayName("Debería retornar 400 para arreglo de ADN nulo")
        void testNullDna() throws Exception {
            DnaRequest request = new DnaRequest(null);

            mockMvc.perform(post("/mutant")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Debería retornar 400 para arreglo de ADN vacío")
        void testEmptyDna() throws Exception {
            DnaRequest request = new DnaRequest(new String[]{});

            mockMvc.perform(post("/mutant")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Debería retornar 400 para formato de ADN inválido (no cuadrado)")
        void testNonSquareMatrix() throws Exception {
            String[] dna = {
                "AAAA",
                "CCCC",
                "TTAT",
                "AGA"  // Wrong length (3 instead of 4)
            };
            DnaRequest request = new DnaRequest(dna);

            when(mutantDetectorService.isMutant(any(String[].class)))
                    .thenThrow(new InvalidDnaException("Secuencia de ADN inválida: debe ser una matriz cuadrada NxN"));

            mockMvc.perform(post("/mutant")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.details").exists())
                    .andExpect(jsonPath("$.details").value(org.hamcrest.Matchers.hasValue(containsString("NxN"))));
        }

        @Test
        @DisplayName("Debería retornar 400 para caracteres inválidos")
        void testInvalidCharacters() throws Exception {
            String[] dna = {
                "ATGCGA",
                "CAGTGC",
                "TTATXT",
                "AGAAGG",
                "CCCCTA",
                "TCACTG"
            };
            DnaRequest request = new DnaRequest(dna);

            when(mutantDetectorService.isMutant(any(String[].class)))
                    .thenThrow(new InvalidDnaException("Secuencia de ADN inválida: carácter 'X' no permitido"));

            mockMvc.perform(post("/mutant")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").exists());
        }

        @Test
        @DisplayName("Debería retornar 400 para JSON mal formado")
        void testMalformedJson() throws Exception {
            String malformedJson = "{\"dna\": [\"ATGCGA\", \"CAGTGC\"";

            mockMvc.perform(post("/mutant")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(malformedJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Debería retornar 400 para campo DNA faltante")
        void testMissingDnaField() throws Exception {
            String jsonWithoutDna = "{}";

            mockMvc.perform(post("/mutant")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonWithoutDna))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Debería retornar 400 para elemento nulo en arreglo de ADN")
        void testNullElementInArray() throws Exception {
            String[] dna = {
                "ATGCGA",
                null,
                "TTATGT"
            };
            DnaRequest request = new DnaRequest(dna);

            when(mutantDetectorService.isMutant(any(String[].class)))
                    .thenThrow(new InvalidDnaException("Secuencia de ADN inválida: elemento nulo en índice 1"));

            mockMvc.perform(post("/mutant")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").exists());
        }
    }

    @Nested
    @DisplayName("GET /stats - Pruebas de Estadísticas")
    class StatsTests {

        @Test
        @DisplayName("Debería retornar estadísticas con formato correcto")
        void testGetStats() throws Exception {
            StatsResponse stats = new StatsResponse(40L, 100L, 0.4);

            when(mutantDetectorService.getStats()).thenReturn(stats);

            mockMvc.perform(get("/stats")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count_mutant_dna").value(40))
                    .andExpect(jsonPath("$.count_human_dna").value(100))
                    .andExpect(jsonPath("$.ratio").value(0.4));
        }

        @Test
        @DisplayName("Debería retornar valores cero cuando no existen datos")
        void testGetStatsNoData() throws Exception {
            StatsResponse stats = new StatsResponse(0L, 0L, 0.0);

            when(mutantDetectorService.getStats()).thenReturn(stats);

            mockMvc.perform(get("/stats")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count_mutant_dna").value(0))
                    .andExpect(jsonPath("$.count_human_dna").value(0))
                    .andExpect(jsonPath("$.ratio").value(0.0));
        }

        @Test
        @DisplayName("Debería retornar ratio de 1.0 cuando solo existen mutantes")
        void testGetStatsOnlyMutants() throws Exception {
            StatsResponse stats = new StatsResponse(50L, 0L, 1.0);

            when(mutantDetectorService.getStats()).thenReturn(stats);

            mockMvc.perform(get("/stats")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count_mutant_dna").value(50))
                    .andExpect(jsonPath("$.count_human_dna").value(0))
                    .andExpect(jsonPath("$.ratio").value(1.0));
        }

        @Test
        @DisplayName("Debería retornar ratio de 0.0 cuando solo existen humanos")
        void testGetStatsOnlyHumans() throws Exception {
            StatsResponse stats = new StatsResponse(0L, 100L, 0.0);

            when(mutantDetectorService.getStats()).thenReturn(stats);

            mockMvc.perform(get("/stats")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count_mutant_dna").value(0))
                    .andExpect(jsonPath("$.count_human_dna").value(100))
                    .andExpect(jsonPath("$.ratio").value(0.0));
        }

        @Test
        @DisplayName("Debería retornar ratio de 0.5 cuando hay conteos iguales")
        void testGetStatsEqualCounts() throws Exception {
            StatsResponse stats = new StatsResponse(50L, 50L, 0.5);

            when(mutantDetectorService.getStats()).thenReturn(stats);

            mockMvc.perform(get("/stats")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count_mutant_dna").value(50))
                    .andExpect(jsonPath("$.count_human_dna").value(50))
                    .andExpect(jsonPath("$.ratio").value(0.5));
        }

        @Test
        @DisplayName("Debería retornar el tipo de contenido correcto")
        void testGetStatsContentType() throws Exception {
            StatsResponse stats = new StatsResponse(40L, 100L, 0.4);

            when(mutantDetectorService.getStats()).thenReturn(stats);

            mockMvc.perform(get("/stats"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        }
    }

    @Nested
    @DisplayName("Casos Límite y Manejo de Errores")
    class EdgeCaseTests {

        @Test
        @DisplayName("Debería manejar matriz de ADN muy grande")
        void testLargeMatrix() throws Exception {
            String[] dna = {
                "ATGCGAATGC",
                "CAGTGCAGTG",
                "TTATGTTTAT",
                "AGAAGGAGAA",
                "CCCCTACCCC",
                "TCACTGTCAC",
                "ATGCGAATGC",
                "CAGTGCAGTG",
                "TTATGTTTAT",
                "AGAAGGAGAA"
            };
            DnaRequest request = new DnaRequest(dna);

            when(mutantDetectorService.isMutant(any(String[].class))).thenReturn(true);

            mockMvc.perform(post("/mutant")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Debería manejar petición POST sin encabezado Content-Type")
        void testMissingContentType() throws Exception {
            String[] dna = {"ATGCGA", "CAGTGC", "TTATGT", "AGAAGG", "CCCCTA", "TCACTG"};
            DnaRequest request = new DnaRequest(dna);

            mockMvc.perform(post("/mutant")
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("Debería manejar petición GET al endpoint /mutant")
        void testGetRequestToMutantEndpoint() throws Exception {
            mockMvc.perform(get("/mutant"))
                    .andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("Debería manejar petición POST al endpoint /stats")
        void testPostRequestToStatsEndpoint() throws Exception {
            mockMvc.perform(post("/stats")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isMethodNotAllowed());
        }
    }
}
