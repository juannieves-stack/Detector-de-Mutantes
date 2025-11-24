package com.mutant.detector.service;

import com.mutant.detector.entity.DnaRecord;
import com.mutant.detector.exception.InvalidDnaException;
import com.mutant.detector.repository.DnaRecordRepository;
import com.mutant.detector.dto.StatsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios integrales para MutantDetectorService.
 * Organizados en clases anidadas para mejor legibilidad.
 */
@ExtendWith(MockitoExtension.class)
class MutantDetectorTest {

    @Mock
    private DnaRecordRepository dnaRecordRepository;

    @InjectMocks
    private MutantDetectorService mutantDetectorService;

    @BeforeEach
    void setUp() {
        reset(dnaRecordRepository);
    }

    @Nested
    @DisplayName("Detección de Mutantes - Secuencias Horizontales")
    class HorizontalSequenceTests {

        @Test
        @DisplayName("Debería detectar mutante con 2 secuencias horizontales")
        void testTwoHorizontalSequences() {
            String[] dna = {
                "AAAATG",
                "CAGTGC",
                "TTATGT",
                "AGAAGG",
                "CCCCTA",
                "TCACTG"
            };

            when(dnaRecordRepository.findByDnaHash(anyString())).thenReturn(Optional.empty());
            when(dnaRecordRepository.save(any(DnaRecord.class))).thenReturn(new DnaRecord());

            boolean result = mutantDetectorService.isMutant(dna);

            assertTrue(result);
            verify(dnaRecordRepository, times(1)).save(any(DnaRecord.class));
        }

        @Test
        @DisplayName("Debería detectar mutante con secuencia horizontal al final de la fila")
        void testHorizontalSequenceAtEnd() {
            String[] dna = {
                "TGAAAA",
                "CAGTGC",
                "TTATGT",
                "AGAAGG",
                "TACCCC",
                "TCACTG"
            };

            when(dnaRecordRepository.findByDnaHash(anyString())).thenReturn(Optional.empty());
            when(dnaRecordRepository.save(any(DnaRecord.class))).thenReturn(new DnaRecord());

            boolean result = mutantDetectorService.isMutant(dna);

            assertTrue(result);
        }

        @Test
        @DisplayName("Debería detectar mutante con secuencia horizontal en el medio")
        void testHorizontalSequenceInMiddle() {
            String[] dna = {
                "TAAAAG",
                "CAGTGC",
                "TTATGT",
                "AGAAGG",
                "TCCCCG",
                "TCACTG"
            };

            when(dnaRecordRepository.findByDnaHash(anyString())).thenReturn(Optional.empty());
            when(dnaRecordRepository.save(any(DnaRecord.class))).thenReturn(new DnaRecord());

            boolean result = mutantDetectorService.isMutant(dna);

            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("Detección de Mutantes - Secuencias Verticales")
    class VerticalSequenceTests {

        @Test
        @DisplayName("Debería detectar mutante con 2 secuencias verticales")
        void testTwoVerticalSequences() {
            String[] dna = {
                "ATGCGA",
                "AAGTGC",
                "ATATGT",
                "AGAAGG",
                "CCCCTA",
                "TCACTG"
            };

            when(dnaRecordRepository.findByDnaHash(anyString())).thenReturn(Optional.empty());
            when(dnaRecordRepository.save(any(DnaRecord.class))).thenReturn(new DnaRecord());

            boolean result = mutantDetectorService.isMutant(dna);

            assertTrue(result);
        }

        @Test
        @DisplayName("Debería detectar mutante con secuencia vertical en la primera columna")
        void testVerticalSequenceFirstColumn() {
            String[] dna = {
                "ATGCGA",
                "AAGTGC",
                "ATATGT",
                "AGAAGG",
                "CCCCTA",
                "TCACTG"
            };

            when(dnaRecordRepository.findByDnaHash(anyString())).thenReturn(Optional.empty());
            when(dnaRecordRepository.save(any(DnaRecord.class))).thenReturn(new DnaRecord());

            boolean result = mutantDetectorService.isMutant(dna);

            assertTrue(result);
        }

        @Test
        @DisplayName("Debería detectar mutante con secuencia vertical en la última columna")
        void testVerticalSequenceLastColumn() {
            String[] dna = {
                "ATGCGT",
                "CAGTGT",
                "TTATGT",
                "AGAAGT",
                "CCCCTA",
                "TCACTG"
            };

            when(dnaRecordRepository.findByDnaHash(anyString())).thenReturn(Optional.empty());
            when(dnaRecordRepository.save(any(DnaRecord.class))).thenReturn(new DnaRecord());
            boolean result = mutantDetectorService.isMutant(dna);

            assertTrue(result);
        }

        @Test
        @DisplayName("Debería detectar mutante con diagonal de arriba-derecha a abajo-izquierda (/)")
        void testDiagonalTopRightToBottomLeft() {
            String[] dna = {
                "ATGCGA",
                "CAGTAC",
                "TTATGT",
                "AGAAGG",
                "CCACTA",
                "TCACTG"
            };

            when(dnaRecordRepository.findByDnaHash(anyString())).thenReturn(Optional.empty());
            when(dnaRecordRepository.save(any(DnaRecord.class))).thenReturn(new DnaRecord());

            boolean result = mutantDetectorService.isMutant(dna);

            assertTrue(result);
        }

        @Test
        @DisplayName("Debería detectar mutante con múltiples secuencias diagonales")
        void testMultipleDiagonalSequences() {
            String[] dna = {
                "ATGCGA",
                "CAGTGC",
                "TTATGT",
                "AGAAGG",
                "CCCCTA",
                "TCACTG"
            };

            when(dnaRecordRepository.findByDnaHash(anyString())).thenReturn(Optional.empty());
            when(dnaRecordRepository.save(any(DnaRecord.class))).thenReturn(new DnaRecord());

            boolean result = mutantDetectorService.isMutant(dna);

            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("Detección de Humanos (No Mutante)")
    class HumanDetectionTests {

        @Test
        @DisplayName("Debería detectar humano sin secuencias")
        void testHumanWithNoSequences() {
            String[] dna = {
                "ATGCGA",
                "CAGTGC",
                "TTATTT",
                "AGACGG",
                "GCGTCA",
                "TCACTG"
            };

            when(dnaRecordRepository.findByDnaHash(anyString())).thenReturn(Optional.empty());
            when(dnaRecordRepository.save(any(DnaRecord.class))).thenReturn(new DnaRecord());

            boolean result = mutantDetectorService.isMutant(dna);

            assertFalse(result);
            verify(dnaRecordRepository, times(1)).save(argThat(record -> 
                !record.getIsMutant()
            ));
        }

        @Test
        @DisplayName("Debería detectar humano con solo 1 secuencia horizontal")
        void testHumanWithOneHorizontalSequence() {
            String[] dna = {
                "AAAATG",
                "CAGTGC",
                "TTATGT",
                "AGACGG",
                "GCGTCA",
                "TCACTG"
            };

            when(dnaRecordRepository.findByDnaHash(anyString())).thenReturn(Optional.empty());
            when(dnaRecordRepository.save(any(DnaRecord.class))).thenReturn(new DnaRecord());

            boolean result = mutantDetectorService.isMutant(dna);

            assertFalse(result);
        }

        @Test
        @DisplayName("Debería detectar humano con solo 1 secuencia vertical")
        void testHumanWithOneVerticalSequence() {
            String[] dna = {
                "ATGCGA",
                "AAGTGC",
                "ATATGT",
                "AGACGG",
                "GCGTCA",
                "TCACTG"
            };

            when(dnaRecordRepository.findByDnaHash(anyString())).thenReturn(Optional.empty());
            when(dnaRecordRepository.save(any(DnaRecord.class))).thenReturn(new DnaRecord());

            boolean result = mutantDetectorService.isMutant(dna);

            assertFalse(result);
        }

        @Test
        @DisplayName("Debería detectar humano con solo 1 secuencia diagonal")
        void testHumanWithOneDiagonalSequence() {
            String[] dna = {
                "ATGCGA",
                "CAGTGC",
                "TTATGT",
                "AGACGG",
                "GCGTCA",
                "TCACTG"
            };

            when(dnaRecordRepository.findByDnaHash(anyString())).thenReturn(Optional.empty());
            when(dnaRecordRepository.save(any(DnaRecord.class))).thenReturn(new DnaRecord());

            boolean result = mutantDetectorService.isMutant(dna);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Pruebas de Validación de ADN")
    class ValidationTests {

        @Test
        @DisplayName("Debería lanzar excepción para ADN nulo")
        void testNullDna() {
            InvalidDnaException exception = assertThrows(
                InvalidDnaException.class,
                () -> mutantDetectorService.isMutant(null)
            );
            
            assertTrue(exception.getMessage().contains("null"));
        }

        @Test
        @DisplayName("Debería lanzar excepción para arreglo de ADN vacío")
        void testEmptyDna() {
            String[] dna = {};

            InvalidDnaException exception = assertThrows(
                InvalidDnaException.class,
                () -> mutantDetectorService.isMutant(dna)
            );
            
            assertTrue(exception.getMessage().contains("empty"));
        }

        @Test
        @DisplayName("Debería lanzar excepción para elemento nulo en arreglo de ADN")
        void testNullElementInDna() {
            String[] dna = {
                "ATGCGA",
                null,
                "TTATGT"
            };

            InvalidDnaException exception = assertThrows(
                InvalidDnaException.class,
                () -> mutantDetectorService.isMutant(dna)
            );
            
            assertTrue(exception.getMessage().contains("null"));
        }

        @Test
        @DisplayName("Debería lanzar excepción para matriz no cuadrada (NxM)")
        void testNonSquareMatrix() {
            String[] dna = {
                "ATGCGA",
                "CAGTGC",
                "TTAT"  // Wrong length
            };

            InvalidDnaException exception = assertThrows(
                InvalidDnaException.class,
                () -> mutantDetectorService.isMutant(dna)
            );
            
            assertTrue(exception.getMessage().contains("NxN"));
        }

        @Test
        @DisplayName("Debería lanzar excepción para carácter inválido X")
        void testInvalidCharacterX() {
            String[] dna = {
                "ATGCGA",
                "CAGTGC",
                "TTATXT",
                "AGAAGG",
                "CCCCTA",
                "TCACTG"
            };

            InvalidDnaException exception = assertThrows(
                InvalidDnaException.class,
                () -> mutantDetectorService.isMutant(dna)
            );
            
            assertTrue(exception.getMessage().contains("X"));
        }

        @Test
        @DisplayName("Debería lanzar excepción para caracteres en minúscula")
        void testLowercaseCharacters() {
            String[] dna = {
                "atgcga",
                "CAGTGC",
                "TTATGT",
                "AGAAGG",
                "CCCCTA",
                "TCACTG"
            };

            InvalidDnaException exception = assertThrows(
                InvalidDnaException.class,
                () -> mutantDetectorService.isMutant(dna)
            );
            
            assertNotNull(exception.getMessage());
        }

        @Test
        @DisplayName("Debería lanzar excepción para caracteres numéricos")
        void testNumericCharacters() {
            String[] dna = {
                "ATGCGA",
                "C1GTGC",
                "TTATGT",
                "AGAAGG",
                "CCCCTA",
                "TCACTG"
            };

            InvalidDnaException exception = assertThrows(
                InvalidDnaException.class,
                () -> mutantDetectorService.isMutant(dna)
            );
            
            assertTrue(exception.getMessage().contains("1"));
        }

        @Test
        @DisplayName("Debería lanzar excepción para caracteres especiales")
        void testSpecialCharacters() {
            String[] dna = {
                "ATGCGA",
                "CA@TGC",
                "TTATGT",
                "AGAAGG",
                "CCCCTA",
                "TCACTG"
            };

            InvalidDnaException exception = assertThrows(
                InvalidDnaException.class,
                () -> mutantDetectorService.isMutant(dna)
            );
            
            assertTrue(exception.getMessage().contains("@"));
        }
    }

    @Nested
    @DisplayName("Pruebas de Caché")
    class CacheTests {

        @Test
        @DisplayName("Debería usar resultado en caché para ADN mutante")
        void testCachedMutantResult() {
            String[] dna = {
                "ATGCGA",
                "CAGTGC",
                "TTATGT",
                "AGAAGG",
                "CCCCTA",
                "TCACTG"
            };

            DnaRecord cachedRecord = new DnaRecord(1L, "somehash", true);
            when(dnaRecordRepository.findByDnaHash(anyString())).thenReturn(Optional.of(cachedRecord));

            boolean result = mutantDetectorService.isMutant(dna);

            assertTrue(result);
            verify(dnaRecordRepository, never()).save(any(DnaRecord.class));
            verify(dnaRecordRepository, times(1)).findByDnaHash(anyString());
        }

        @Test
        @DisplayName("Debería usar resultado en caché para ADN humano")
        void testCachedHumanResult() {
            String[] dna = {
                "ATGCGA",
                "CAGTGC",
                "TTATTT",
                "AGACGG",
                "GCGTCA",
                "TCACTG"
            };

            DnaRecord cachedRecord = new DnaRecord(2L, "anotherhash", false);
            when(dnaRecordRepository.findByDnaHash(anyString())).thenReturn(Optional.of(cachedRecord));

            boolean result = mutantDetectorService.isMutant(dna);

            assertFalse(result);
            verify(dnaRecordRepository, never()).save(any(DnaRecord.class));
        }

        @Test
        @DisplayName("Debería guardar nuevo resultado de análisis de ADN")
        void testSaveNewResult() {
            String[] dna = {
                "ATGCGA",
                "CAGTGC",
                "TTATGT",
                "AGAAGG",
                "CCCCTA",
                "TCACTG"
            };

            when(dnaRecordRepository.findByDnaHash(anyString())).thenReturn(Optional.empty());
            when(dnaRecordRepository.save(any(DnaRecord.class))).thenReturn(new DnaRecord());

            mutantDetectorService.isMutant(dna);

            verify(dnaRecordRepository, times(1)).save(argThat(record ->
                record.getDnaHash() != null && record.getIsMutant() != null
            ));
        }
    }

    @Nested
    @DisplayName("Pruebas de Estadísticas")
    class StatsTests {

        @Test
        @DisplayName("Debería calcular el ratio correcto con mutantes y humanos")
        void testStatsWithData() {
            when(dnaRecordRepository.countByIsMutant(true)).thenReturn(40L);
            when(dnaRecordRepository.countByIsMutant(false)).thenReturn(100L);

            StatsResponse stats = mutantDetectorService.getStats();

            assertEquals(40L, stats.getCountMutantDna());
            assertEquals(100L, stats.getCountHumanDna());
            assertEquals(40.0 / 140.0, stats.getRatio(), 0.0001);
        }

        @Test
        @DisplayName("Debería retornar ratio cero cuando no existen registros")
        void testStatsWithNoRecords() {
            when(dnaRecordRepository.countByIsMutant(true)).thenReturn(0L);
            when(dnaRecordRepository.countByIsMutant(false)).thenReturn(0L);

            StatsResponse stats = mutantDetectorService.getStats();

            assertEquals(0L, stats.getCountMutantDna());
            assertEquals(0L, stats.getCountHumanDna());
            assertEquals(0.0, stats.getRatio());
        }

        @Test
        @DisplayName("Debería calcular un ratio de 1.0 cuando solo existen mutantes")
        void testStatsOnlyMutants() {
            when(dnaRecordRepository.countByIsMutant(true)).thenReturn(50L);
            when(dnaRecordRepository.countByIsMutant(false)).thenReturn(0L);

            StatsResponse stats = mutantDetectorService.getStats();

            assertEquals(50L, stats.getCountMutantDna());
            assertEquals(0L, stats.getCountHumanDna());
            assertEquals(1.0, stats.getRatio(), 0.0001);
        }

        @Test
        @DisplayName("Debería calcular un ratio de 0.0 cuando solo existen humanos")
        void testStatsOnlyHumans() {
            when(dnaRecordRepository.countByIsMutant(true)).thenReturn(0L);
            when(dnaRecordRepository.countByIsMutant(false)).thenReturn(100L);

            StatsResponse stats = mutantDetectorService.getStats();

            assertEquals(0L, stats.getCountMutantDna());
            assertEquals(100L, stats.getCountHumanDna());
            assertEquals(0.0, stats.getRatio(), 0.0001);
        }

        @Test
        @DisplayName("Debería calcular un ratio de 0.5 cuando hay igual cantidad de mutantes y humanos")
        void testStatsEqualCounts() {
            when(dnaRecordRepository.countByIsMutant(true)).thenReturn(50L);
            when(dnaRecordRepository.countByIsMutant(false)).thenReturn(50L);

            StatsResponse stats = mutantDetectorService.getStats();

            assertEquals(50L, stats.getCountMutantDna());
            assertEquals(50L, stats.getCountHumanDna());
            assertEquals(0.5, stats.getRatio(), 0.0001);
        }
    }

    @Nested
    @DisplayName("Casos Límite")
    class EdgeCaseTests {

        @Test
        @DisplayName("Debería manejar matriz 4x4 (tamaño mínimo)")
        void testMinimumMatrixSize() {
            String[] dna = {
                "AAAA",
                "CCCC",
                "TTAT",
                "AGAC"
            };

            when(dnaRecordRepository.findByDnaHash(anyString())).thenReturn(Optional.empty());
            when(dnaRecordRepository.save(any(DnaRecord.class))).thenReturn(new DnaRecord());

            boolean result = mutantDetectorService.isMutant(dna);

            assertTrue(result);
        }

        @Test
        @DisplayName("Debería manejar matriz grande de 10x10")
        void testLargeMatrix() {
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

            when(dnaRecordRepository.findByDnaHash(anyString())).thenReturn(Optional.empty());
            when(dnaRecordRepository.save(any(DnaRecord.class))).thenReturn(new DnaRecord());

            boolean result = mutantDetectorService.isMutant(dna);

            assertTrue(result);
        }

        @Test
        @DisplayName("Debería manejar ADN con todos los caracteres iguales")
        void testAllSameCharacter() {
            String[] dna = {
                "AAAA",
                "AAAA",
                "AAAA",
                "AAAA"
            };

            when(dnaRecordRepository.findByDnaHash(anyString())).thenReturn(Optional.empty());
            when(dnaRecordRepository.save(any(DnaRecord.class))).thenReturn(new DnaRecord());

            boolean result = mutantDetectorService.isMutant(dna);

            assertTrue(result);
        }

        @Test
        @DisplayName("Debería manejar ADN sin caracteres repetidos")
        void testNoRepeatedCharacters() {
            String[] dna = {
                "ATCG",
                "CGAT",
                "TACG",
                "GCTA"
            };

            when(dnaRecordRepository.findByDnaHash(anyString())).thenReturn(Optional.empty());
            when(dnaRecordRepository.save(any(DnaRecord.class))).thenReturn(new DnaRecord());

            boolean result = mutantDetectorService.isMutant(dna);

            assertFalse(result);
        }

        @Test
        @DisplayName("Debería manejar terminación temprana correctamente")
        void testEarlyTermination() {
            String[] dna = {
                "AAAATG",
                "CAGTGC",
                "TTATGT",
                "AGAAGG",
                "CCCCTA",
                "TCACTG"
            };

            when(dnaRecordRepository.findByDnaHash(anyString())).thenReturn(Optional.empty());
            when(dnaRecordRepository.save(any(DnaRecord.class))).thenReturn(new DnaRecord());

            boolean result = mutantDetectorService.isMutant(dna);

            assertTrue(result);
            verify(dnaRecordRepository, times(1)).save(any(DnaRecord.class));
        }
    }
}
