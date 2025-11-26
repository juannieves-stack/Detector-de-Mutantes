package com.mutant.detector.service;

import com.mutant.detector.entity.DnaRecord;
import com.mutant.detector.repository.DnaRecordRepository;
import com.mutant.detector.dto.StatsResponse;
import com.mutant.detector.exception.InvalidDnaException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

/**
 * Servicio para detectar mutantes basado en secuencias de ADN.
 *
 * Implementa optimizaciones:
 * 1. Caché basado en hash: Calcula hash SHA-256 del ADN para evitar re‑analizar.
 * 2. Terminación temprana: Detiene la búsqueda después de encontrar 2 secuencias.
 * 3. Conversión a matriz de caracteres para acceso rápido.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MutantDetectorService {

    private static final int SEQUENCE_LENGTH = 4;
    private static final char[] VALID_BASES = {'A', 'T', 'C', 'G'};

    private final DnaRecordRepository dnaRecordRepository;

    /**
     * Determina si una secuencia de ADN pertenece a un mutante.
     *
     * @param dna Array de secuencias de ADN (matriz NxN).
     * @return true si es mutante, false si es humano.
     * @throws InvalidDnaException si el ADN es inválido.
     */
    @Transactional
    public boolean isMutant(String[] dna) {
        // Validar ADN
        validateDna(dna);

        // Calcular hash y buscar en caché
        String dnaHash = calculateHash(dna);
        log.debug("Hash del ADN: {}", dnaHash);
        Optional<DnaRecord> cached = dnaRecordRepository.findByDnaHash(dnaHash);
        if (cached.isPresent()) {
            log.info("ADN encontrado en caché, retornando resultado en caché");
            return cached.get().getIsMutant();
        }

        // Analizar ADN
        boolean mutant = detectMutant(dna);
        log.info("Resultado del análisis de ADN: {}", mutant ? "MUTANTE" : "HUMANO");

        // Guardar resultado
        dnaRecordRepository.save(new DnaRecord(dnaHash, mutant));
        return mutant;
    }

    /**
     * Obtiene estadísticas sobre las secuencias de ADN analizadas.
     *
     * @return StatsResponse con conteos y ratio.
     */
    /**
     * Obtiene estadísticas sobre las secuencias de ADN analizadas.
     *
     * @return StatsResponse con conteos y ratio.
     */
    public StatsResponse getStats() {
        long mutantCount = dnaRecordRepository.countByIsMutant(true);
        long humanCount = dnaRecordRepository.countByIsMutant(false);
        long totalCount = mutantCount + humanCount;
        
        double ratio;
        if (totalCount == 0) {
            ratio = 0.0;
        } else {
            ratio = (double) mutantCount / totalCount;
        }
        
        log.debug("Estadísticas - Mutantes: {}, Humanos: {}, Ratio: {}", mutantCount, humanCount, ratio);
        return new StatsResponse(mutantCount, humanCount, ratio);
    }

    /**
     * Valida la matriz de ADN.
     *
     * @param dna Array de ADN.
     * @throws InvalidDnaException si la validación falla.
     */
    private void validateDna(String[] dna) {
        if (dna == null) {
            throw new InvalidDnaException("Secuencia de ADN inválida: el array no puede ser nulo");
        }
        if (dna.length == 0) {
            throw new InvalidDnaException("Secuencia de ADN inválida: el array no puede estar vacío");
        }
        int n = dna.length;
        if (n < 4) {
            throw new InvalidDnaException(String.format("Secuencia de ADN inválida: debe ser una matriz cuadrada NxN (mínimo 4x4). Tamaño actual: %d", n));
        }
        for (int i = 0; i < n; i++) {
            if (dna[i] == null) {
                throw new InvalidDnaException(String.format("Secuencia de ADN inválida: elemento nulo en índice %d", i));
            }
            if (dna[i].length() != n) {
                throw new InvalidDnaException(String.format("Secuencia de ADN inválida: debe ser una matriz cuadrada NxN. Se esperaba longitud %d pero se obtuvo %d en el índice %d", n, dna[i].length(), i));
            }
            for (char c : dna[i].toCharArray()) {
                if (!isValidBase(c)) {
                    throw new InvalidDnaException(String.format("Secuencia de ADN inválida: carácter '%c' no permitido. Solo se permiten A, T, C, G", c));
                }
            }
        }
    }

    /**
     * Verifica si un carácter es una base válida.
     */
    private boolean isValidBase(char c) {
        for (char b : VALID_BASES) {
            if (c == b) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calcula el hash SHA‑256 del ADN.
     */
    private String calculateHash(String[] dna) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String concatenated = String.join("", dna);
            byte[] hash = digest.digest(concatenated.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) sb.append('0');
                sb.append(hex);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algoritmo SHA-256 no disponible", e);
        }
    }

    /**
     * Detecta mutante usando terminación temprana y matriz de caracteres.
     */
    private boolean detectMutant(String[] dna) {
        int n = dna.length;
        // Convertir a char[][]
        char[][] matrix = new char[n][];
        for (int i = 0; i < n; i++) {
            matrix[i] = dna[i].toCharArray();
        }

        int sequenceCount = 0;

        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                // Búsqueda Horizontal (→)
                if (col <= n - SEQUENCE_LENGTH) {
                    if (checkHorizontal(matrix, row, col)) {
                        sequenceCount++;
                        if (sequenceCount > 1) return true;
                    }
                }

                // Búsqueda Vertical (↓)
                if (row <= n - SEQUENCE_LENGTH) {
                    if (checkVertical(matrix, row, col)) {
                        sequenceCount++;
                        if (sequenceCount > 1) return true;
                    }
                }

                // Búsqueda Diagonal Descendente (↘)
                if (row <= n - SEQUENCE_LENGTH && col <= n - SEQUENCE_LENGTH) {
                    if (checkDiagonalDescending(matrix, row, col)) {
                        sequenceCount++;
                        if (sequenceCount > 1) return true;
                    }
                }

                // Búsqueda Diagonal Ascendente (↗)
                if (row >= SEQUENCE_LENGTH - 1 && col <= n - SEQUENCE_LENGTH) {
                    if (checkDiagonalAscending(matrix, row, col)) {
                        sequenceCount++;
                        if (sequenceCount > 1) return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean checkHorizontal(char[][] matrix, int row, int col) {
        final char base = matrix[row][col];
        return matrix[row][col + 1] == base &&
               matrix[row][col + 2] == base &&
               matrix[row][col + 3] == base;
    }

    private boolean checkVertical(char[][] matrix, int row, int col) {
        final char base = matrix[row][col];
        return matrix[row + 1][col] == base &&
               matrix[row + 2][col] == base &&
               matrix[row + 3][col] == base;
    }

    private boolean checkDiagonalDescending(char[][] matrix, int row, int col) {
        final char base = matrix[row][col];
        return matrix[row + 1][col + 1] == base &&
               matrix[row + 2][col + 2] == base &&
               matrix[row + 3][col + 3] == base;
    }

    private boolean checkDiagonalAscending(char[][] matrix, int row, int col) {
        final char base = matrix[row][col];
        return matrix[row - 1][col + 1] == base &&
               matrix[row - 2][col + 2] == base &&
               matrix[row - 3][col + 3] == base;
    }
}
