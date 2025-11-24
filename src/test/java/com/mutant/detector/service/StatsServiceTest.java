package com.mutant.detector.service;

import com.mutant.detector.dto.StatsResponse;
import com.mutant.detector.repository.DnaRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests dedicados para la funcionalidad de Estadísticas en MutantDetectorService.
 * Se enfoca en el cálculo de ratio y casos extremos.
 */
@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private DnaRecordRepository dnaRecordRepository;

    @InjectMocks
    private MutantDetectorService mutantDetectorService;

    @BeforeEach
    void setUp() {
        reset(dnaRecordRepository);
    }

    @Nested
    @DisplayName("Pruebas de Cálculo de Ratio")
    class RatioCalculationTests {

        @Test
        @DisplayName("Debería calcular el ratio correctamente con 40 mutantes y 100 humanos")
        void testRatioCalculation_40_100() {
            when(dnaRecordRepository.countByIsMutant(true)).thenReturn(40L);
            when(dnaRecordRepository.countByIsMutant(false)).thenReturn(100L);

            StatsResponse stats = mutantDetectorService.getStats();

            assertEquals(40L, stats.getCountMutantDna());
            assertEquals(100L, stats.getCountHumanDna());
            // 40 mutants / 140 total = 0.2857...
            assertEquals(40.0 / 140.0, stats.getRatio(), 0.0001);
        }

        @Test
        @DisplayName("Debería calcular un ratio de 0.5 con igual cantidad de mutantes y humanos")
        void testRatioCalculation_Equal() {
            when(dnaRecordRepository.countByIsMutant(true)).thenReturn(50L);
            when(dnaRecordRepository.countByIsMutant(false)).thenReturn(50L);

            StatsResponse stats = mutantDetectorService.getStats();

            assertEquals(50L, stats.getCountMutantDna());
            assertEquals(50L, stats.getCountHumanDna());
            assertEquals(0.5, stats.getRatio(), 0.0001);
        }

        @Test
        @DisplayName("Debería calcular un ratio de 1.0 cuando solo existen mutantes")
        void testRatioCalculation_OnlyMutants() {
            when(dnaRecordRepository.countByIsMutant(true)).thenReturn(100L);
            when(dnaRecordRepository.countByIsMutant(false)).thenReturn(0L);

            StatsResponse stats = mutantDetectorService.getStats();

            assertEquals(100L, stats.getCountMutantDna());
            assertEquals(0L, stats.getCountHumanDna());
            assertEquals(1.0, stats.getRatio(), 0.0001);
        }

        @Test
        @DisplayName("Debería calcular un ratio de 0.0 cuando solo existen humanos")
        void testRatioCalculation_OnlyHumans() {
            when(dnaRecordRepository.countByIsMutant(true)).thenReturn(0L);
            when(dnaRecordRepository.countByIsMutant(false)).thenReturn(150L);

            StatsResponse stats = mutantDetectorService.getStats();

            assertEquals(0L, stats.getCountMutantDna());
            assertEquals(150L, stats.getCountHumanDna());
            assertEquals(0.0, stats.getRatio(), 0.0001);
        }

        @Test
        @DisplayName("Debería calcular un ratio de 0.0 cuando no existen registros")
        void testRatioCalculation_NoRecords() {
            when(dnaRecordRepository.countByIsMutant(true)).thenReturn(0L);
            when(dnaRecordRepository.countByIsMutant(false)).thenReturn(0L);

            StatsResponse stats = mutantDetectorService.getStats();

            assertEquals(0L, stats.getCountMutantDna());
            assertEquals(0L, stats.getCountHumanDna());
            assertEquals(0.0, stats.getRatio());
        }

        @Test
        @DisplayName("Debería calcular el ratio correctamente con 1 mutante y 999 humanos")
        void testRatioCalculation_LowMutantRatio() {
            when(dnaRecordRepository.countByIsMutant(true)).thenReturn(1L);
            when(dnaRecordRepository.countByIsMutant(false)).thenReturn(999L);

            StatsResponse stats = mutantDetectorService.getStats();

            assertEquals(1L, stats.getCountMutantDna());
            assertEquals(999L, stats.getCountHumanDna());
            assertEquals(1.0 / 1000.0, stats.getRatio(), 0.0001);
        }

        @Test
        @DisplayName("Debería calcular el ratio correctamente con 999 mutantes y 1 humano")
        void testRatioCalculation_HighMutantRatio() {
            when(dnaRecordRepository.countByIsMutant(true)).thenReturn(999L);
            when(dnaRecordRepository.countByIsMutant(false)).thenReturn(1L);

            StatsResponse stats = mutantDetectorService.getStats();

            assertEquals(999L, stats.getCountMutantDna());
            assertEquals(1L, stats.getCountHumanDna());
            assertEquals(999.0 / 1000.0, stats.getRatio(), 0.0001);
        }

        @Test
        @DisplayName("Debería calcular el ratio con números muy grandes")
        void testRatioCalculation_LargeNumbers() {
            when(dnaRecordRepository.countByIsMutant(true)).thenReturn(1000000L);
            when(dnaRecordRepository.countByIsMutant(false)).thenReturn(2000000L);

            StatsResponse stats = mutantDetectorService.getStats();

            assertEquals(1000000L, stats.getCountMutantDna());
            assertEquals(2000000L, stats.getCountHumanDna());
            assertEquals(1000000.0 / 3000000.0, stats.getRatio(), 0.0001);
        }

        @Test
        @DisplayName("Debería calcular el ratio con 1 mutante y 1 humano")
        void testRatioCalculation_OneEach() {
            when(dnaRecordRepository.countByIsMutant(true)).thenReturn(1L);
            when(dnaRecordRepository.countByIsMutant(false)).thenReturn(1L);

            StatsResponse stats = mutantDetectorService.getStats();

            assertEquals(1L, stats.getCountMutantDna());
            assertEquals(1L, stats.getCountHumanDna());
            assertEquals(0.5, stats.getRatio(), 0.0001);
        }

        @Test
        @DisplayName("Debería calcular el ratio con 3 mutantes y 7 humanos (0.3)")
        void testRatioCalculation_3_7() {
            when(dnaRecordRepository.countByIsMutant(true)).thenReturn(3L);
            when(dnaRecordRepository.countByIsMutant(false)).thenReturn(7L);

            StatsResponse stats = mutantDetectorService.getStats();

            assertEquals(3L, stats.getCountMutantDna());
            assertEquals(7L, stats.getCountHumanDna());
            assertEquals(0.3, stats.getRatio(), 0.0001);
        }
    }

    @Nested
    @DisplayName("Pruebas de Interacción con el Repositorio")
    class RepositoryInteractionTests {

        @Test
        @DisplayName("Debería llamar al repositorio countByIsMutant dos veces")
        void testRepositoryCalls() {
            when(dnaRecordRepository.countByIsMutant(true)).thenReturn(40L);
            when(dnaRecordRepository.countByIsMutant(false)).thenReturn(100L);

            mutantDetectorService.getStats();

            verify(dnaRecordRepository, times(1)).countByIsMutant(true);
            verify(dnaRecordRepository, times(1)).countByIsMutant(false);
        }

        @Test
        @DisplayName("No debería llamar al método save al obtener estadísticas")
        void testNoSaveCallsForStats() {
            when(dnaRecordRepository.countByIsMutant(true)).thenReturn(40L);
            when(dnaRecordRepository.countByIsMutant(false)).thenReturn(100L);

            mutantDetectorService.getStats();

            verify(dnaRecordRepository, never()).save(any());
        }

        @Test
        @DisplayName("No debería llamar a findByDnaHash al obtener estadísticas")
        void testNoFindByHashCallsForStats() {
            when(dnaRecordRepository.countByIsMutant(true)).thenReturn(40L);
            when(dnaRecordRepository.countByIsMutant(false)).thenReturn(100L);

            mutantDetectorService.getStats();

            verify(dnaRecordRepository, never()).findByDnaHash(anyString());
        }
    }

    @Nested
    @DisplayName("Pruebas del Objeto de Respuesta")
    class ResponseObjectTests {

        @Test
        @DisplayName("Debería retornar un StatsResponse no nulo")
        void testNonNullResponse() {
            when(dnaRecordRepository.countByIsMutant(true)).thenReturn(40L);
            when(dnaRecordRepository.countByIsMutant(false)).thenReturn(100L);

            StatsResponse stats = mutantDetectorService.getStats();

            assertNotNull(stats);
        }

        @Test
        @DisplayName("Debería retornar un StatsResponse con todos los campos poblados")
        void testAllFieldsPopulated() {
            when(dnaRecordRepository.countByIsMutant(true)).thenReturn(40L);
            when(dnaRecordRepository.countByIsMutant(false)).thenReturn(100L);

            StatsResponse stats = mutantDetectorService.getStats();

            assertNotNull(stats.getCountMutantDna());
            assertNotNull(stats.getCountHumanDna());
            assertNotNull(stats.getRatio());
        }

        @Test
        @DisplayName("Debería retornar un StatsResponse con valores no negativos")
        void testNonNegativeValues() {
            when(dnaRecordRepository.countByIsMutant(true)).thenReturn(40L);
            when(dnaRecordRepository.countByIsMutant(false)).thenReturn(100L);

            StatsResponse stats = mutantDetectorService.getStats();

            assertTrue(stats.getCountMutantDna() >= 0);
            assertTrue(stats.getCountHumanDna() >= 0);
            assertTrue(stats.getRatio() >= 0.0);
        }

        @Test
        @DisplayName("Debería retornar un ratio entre 0.0 y 1.0")
        void testRatioRange() {
            when(dnaRecordRepository.countByIsMutant(true)).thenReturn(40L);
            when(dnaRecordRepository.countByIsMutant(false)).thenReturn(100L);

            StatsResponse stats = mutantDetectorService.getStats();

            assertTrue(stats.getRatio() >= 0.0);
            assertTrue(stats.getRatio() <= 1.0);
        }
    }

    @Nested
    @DisplayName("Pruebas de Casos Límite y Frontera")
    class EdgeCaseTests {

        @Test
        @DisplayName("Debería manejar el valor máximo de long para el conteo de mutantes")
        void testMaxLongMutantCount() {
            when(dnaRecordRepository.countByIsMutant(true)).thenReturn(Long.MAX_VALUE);
            when(dnaRecordRepository.countByIsMutant(false)).thenReturn(0L);

            StatsResponse stats = mutantDetectorService.getStats();

            assertEquals(Long.MAX_VALUE, stats.getCountMutantDna());
            assertEquals(0L, stats.getCountHumanDna());
            assertEquals(1.0, stats.getRatio(), 0.0001);
        }

        @Test
        @DisplayName("Debería manejar el valor máximo de long para el conteo de humanos")
        void testMaxLongHumanCount() {
            when(dnaRecordRepository.countByIsMutant(true)).thenReturn(0L);
            when(dnaRecordRepository.countByIsMutant(false)).thenReturn(Long.MAX_VALUE);

            StatsResponse stats = mutantDetectorService.getStats();

            assertEquals(0L, stats.getCountMutantDna());
            assertEquals(Long.MAX_VALUE, stats.getCountHumanDna());
            assertEquals(0.0, stats.getRatio(), 0.0001);
        }

        @Test
        @DisplayName("Debería manejar un único registro de mutante")
        void testSingleMutantRecord() {
            when(dnaRecordRepository.countByIsMutant(true)).thenReturn(1L);
            when(dnaRecordRepository.countByIsMutant(false)).thenReturn(0L);

            StatsResponse stats = mutantDetectorService.getStats();

            assertEquals(1L, stats.getCountMutantDna());
            assertEquals(0L, stats.getCountHumanDna());
            assertEquals(1.0, stats.getRatio(), 0.0001);
        }

        @Test
        @DisplayName("Debería manejar un único registro de humano")
        void testSingleHumanRecord() {
            when(dnaRecordRepository.countByIsMutant(true)).thenReturn(0L);
            when(dnaRecordRepository.countByIsMutant(false)).thenReturn(1L);

            StatsResponse stats = mutantDetectorService.getStats();

            assertEquals(0L, stats.getCountMutantDna());
            assertEquals(1L, stats.getCountHumanDna());
            assertEquals(0.0, stats.getRatio(), 0.0001);
        }
    }

    @Nested
    @DisplayName("Pruebas de Precisión")
    class PrecisionTests {

        @Test
        @DisplayName("Debería calcular el ratio con alta precisión")
        void testHighPrecisionRatio() {
            when(dnaRecordRepository.countByIsMutant(true)).thenReturn(1L);
            when(dnaRecordRepository.countByIsMutant(false)).thenReturn(3L);

            StatsResponse stats = mutantDetectorService.getStats();

            // 1/4 = 0.25
            assertEquals(0.25, stats.getRatio(), 0.0000001);
        }

        @Test
        @DisplayName("Debería manejar ratios con decimales repetitivos")
        void testRepeatingDecimalRatio() {
            when(dnaRecordRepository.countByIsMutant(true)).thenReturn(1L);
            when(dnaRecordRepository.countByIsMutant(false)).thenReturn(2L);

            StatsResponse stats = mutantDetectorService.getStats();

            // 1/3 = 0.333...
            assertEquals(1.0 / 3.0, stats.getRatio(), 0.0000001);
        }

        @Test
        @DisplayName("Debería calcular el ratio con precisión de 7 decimales")
        void testSevenDecimalPrecision() {
            when(dnaRecordRepository.countByIsMutant(true)).thenReturn(1L);
            when(dnaRecordRepository.countByIsMutant(false)).thenReturn(6L);

            StatsResponse stats = mutantDetectorService.getStats();

            // 1/7 = 0.1428571...
            assertEquals(1.0 / 7.0, stats.getRatio(), 0.0000001);
        }
    }
}
