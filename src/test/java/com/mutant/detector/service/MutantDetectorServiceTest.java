package com.mutant.detector.service;

import com.mutant.detector.entity.DnaRecord;
import com.mutant.detector.exception.InvalidDnaException;
import com.mutant.detector.repository.DnaRecordRepository;
import com.mutant.detector.dto.StatsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
 * Tests unitarios para MutantDetectorService.
 */
@ExtendWith(MockitoExtension.class)
class MutantDetectorServiceTest {

    @Mock
    private DnaRecordRepository dnaRecordRepository;

    @InjectMocks
    private MutantDetectorService mutantDetectorService;

    @BeforeEach
    void setUp() {
        // Resetear mocks antes de cada test
        reset(dnaRecordRepository);
    }

    @Test
    @DisplayName("Debería detectar mutante con secuencias horizontales")
    void testMutantWithHorizontalSequences() {
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

        assertTrue(result, "Should detect mutant with horizontal sequences");
        verify(dnaRecordRepository, times(1)).save(any(DnaRecord.class));
    }

    @Test
    @DisplayName("Debería detectar mutante con secuencias verticales")
    void testMutantWithVerticalSequences() {
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

        assertTrue(result, "Should detect mutant with vertical sequences");
    }

    @Test
    @DisplayName("Debería detectar mutante con secuencias diagonales")
    void testMutantWithDiagonalSequences() {
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

        assertTrue(result, "Should detect mutant with diagonal sequences");
    }

    @Test
    @DisplayName("Debería detectar humano (no mutante)")
    void testHumanDna() {
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

        assertFalse(result, "Should detect human DNA (not mutant)");
    }

    @Test
    @DisplayName("Debería usar resultado en caché cuando el hash de ADN existe")
    void testCachedResult() {
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

        assertTrue(result, "Should return cached result");
        verify(dnaRecordRepository, never()).save(any(DnaRecord.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción para ADN nulo")
    void testNullDna() {
        assertThrows(InvalidDnaException.class, () -> {
            mutantDetectorService.isMutant(null);
        }, "Should throw InvalidDnaException for null DNA");
    }

    @Test
    @DisplayName("Debería lanzar excepción para arreglo de ADN vacío")
    void testEmptyDna() {
        String[] dna = {};

        assertThrows(InvalidDnaException.class, () -> {
            mutantDetectorService.isMutant(dna);
        }, "Should throw InvalidDnaException for empty DNA");
    }

    @Test
    @DisplayName("Debería lanzar excepción para matriz no cuadrada")
    void testNonSquareMatrix() {
        String[] dna = {
            "ATGCGA",
            "CAGTGC",
            "TTAT"  // Wrong length
        };

        assertThrows(InvalidDnaException.class, () -> {
            mutantDetectorService.isMutant(dna);
        }, "Should throw InvalidDnaException for non-square matrix");
    }

    @Test
    @DisplayName("Debería lanzar excepción para caracteres inválidos")
    void testInvalidCharacters() {
        String[] dna = {
            "ATGCGA",
            "CAGTGC",
            "TTATXT",  // X is invalid
            "AGAAGG",
            "CCCCTA",
            "TCACTG"
        };

        assertThrows(InvalidDnaException.class, () -> {
            mutantDetectorService.isMutant(dna);
        }, "Should throw InvalidDnaException for invalid characters");
    }

    @Test
    @DisplayName("Debería calcular estadísticas correctas")
    void testGetStats() {
        when(dnaRecordRepository.countByIsMutant(true)).thenReturn(40L);
        when(dnaRecordRepository.countByIsMutant(false)).thenReturn(100L);

        StatsResponse stats = mutantDetectorService.getStats();

        assertEquals(40L, stats.getCountMutantDna());
        assertEquals(100L, stats.getCountHumanDna());
        assertEquals(0.2857142857142857, stats.getRatio(), 0.0001);
    }

    @Test
    @DisplayName("Debería manejar ratio cero cuando no existen registros")
    void testGetStatsWithNoRecords() {
        when(dnaRecordRepository.countByIsMutant(true)).thenReturn(0L);
        when(dnaRecordRepository.countByIsMutant(false)).thenReturn(0L);

        StatsResponse stats = mutantDetectorService.getStats();

        assertEquals(0L, stats.getCountMutantDna());
        assertEquals(0L, stats.getCountHumanDna());
        assertEquals(0.0, stats.getRatio());
    }

    @Test
    @DisplayName("Debería detectar mutante con exactamente 2 secuencias (terminación temprana)")
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

        assertTrue(result, "Should detect mutant and terminate early after finding 2 sequences");
    }

    @Test
    @DisplayName("Debería detectar humano con solo 1 secuencia")
    void testHumanWithOneSequence() {
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

        assertFalse(result, "Should detect human with only 1 sequence");
    }

    @Test
    @DisplayName("Debería manejar matriz 4x4")
    void testSmallMatrix() {
        String[] dna = {
            "AAAA",
            "CCCC",
            "TTAT",
            "AGAC"
        };

        when(dnaRecordRepository.findByDnaHash(anyString())).thenReturn(Optional.empty());
        when(dnaRecordRepository.save(any(DnaRecord.class))).thenReturn(new DnaRecord());

        boolean result = mutantDetectorService.isMutant(dna);

        assertTrue(result, "Should detect mutant in 4x4 matrix");
    }
}
