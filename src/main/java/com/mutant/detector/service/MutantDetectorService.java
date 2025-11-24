package com.mutant.detector.service;

import com.mutant.detector.entity.DnaRecord;
import com.mutant.detector.exception.InvalidDnaException;
import com.mutant.detector.repository.DnaRecordRepository;
import com.mutant.detector.dto.StatsResponse;
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
 * 1. Caché basado en hash: Calcula hash SHA-256 del ADN para evitar re-analizar
 * 2. Terminación temprana: Detiene la búsqueda después de encontrar 2 secuencias
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
     * @param dna Array de secuencias de ADN (matriz NxN)
     * @return true si es mutante, false si es humano
     * @throws InvalidDnaException si el ADN es inválido
     */
    @Transactional
    public boolean isMutant(String[] dna) {
        // Validar ADN
        validateDna(dna);
        
        // Calcular hash
        String dnaHash = calculateHash(dna);
        log.debug("Hash del ADN: {}", dnaHash);
        
        // Verificar caché
        Optional<DnaRecord> cachedRecord = dnaRecordRepository.findByDnaHash(dnaHash);
        if (cachedRecord.isPresent()) {
            log.info("ADN encontrado en caché, retornando resultado en caché");
            return cachedRecord.get().getIsMutant();
        }
        
        // Analizar ADN
        boolean isMutant = detectMutant(dna);
        log.info("Resultado del análisis de ADN: {}", isMutant ? "MUTANTE" : "HUMANO");
        
        // Guardar resultado
        DnaRecord record = new DnaRecord(dnaHash, isMutant);
        dnaRecordRepository.save(record);
        
        return isMutant;
    }

    /**
     * Obtiene estadísticas sobre las secuencias de ADN analizadas.
     * 
     * @return StatsResponse con conteos y ratio
     */
    public StatsResponse getStats() {
        long mutantCount = dnaRecordRepository.countByIsMutant(true);
        long humanCount = dnaRecordRepository.countByIsMutant(false);
        long total = mutantCount + humanCount;
        
        double ratio = total > 0 ? (double) mutantCount / total : 0.0;
        
        log.debug("Estadísticas - Mutantes: {}, Humanos: {}, Ratio: {}", mutantCount, humanCount, ratio);
        
        return new StatsResponse(mutantCount, humanCount, ratio);
    }

    /**
     * Valida la secuencia de ADN.
     * 
     * @param dna Array de ADN a validar
     * @throws InvalidDnaException si la validación falla
     */
    private void validateDna(String[] dna) {
        if (dna == null || dna.length == 0) {
            throw new InvalidDnaException("El array de ADN no puede ser nulo o vacío");
        }
        
        int n = dna.length;
        
        for (int i = 0; i < n; i++) {
            if (dna[i] == null) {
                throw new InvalidDnaException("La secuencia de ADN en el índice " + i + " es nula");
            }
            
            if (dna[i].length() != n) {
                throw new InvalidDnaException(
                    String.format("El ADN debe ser una matriz NxN. Se esperaba longitud %d pero se obtuvo %d en el índice %d", 
                                  n, dna[i].length(), i)
                );
            }
            
            // Validar caracteres
            for (char c : dna[i].toCharArray()) {
                if (!isValidBase(c)) {
                    throw new InvalidDnaException(
                        String.format("Base de ADN inválida '%c'. Solo se permiten A, T, C, G", c)
                    );
                }
            }
        }
    }

    /**
     * Verifica si un carácter es una base de ADN válida.
     */
    private boolean isValidBase(char c) {
        for (char valid : VALID_BASES) {
            if (c == valid) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calcula el hash SHA-256 del array de ADN.
     * 
     * @param dna Array de ADN
     * @return String hexadecimal del hash
     */
    private String calculateHash(String[] dna) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String dnaString = String.join("", dna);
            byte[] hashBytes = digest.digest(dnaString.getBytes(StandardCharsets.UTF_8));
            
            // Convertir a string hexadecimal
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algoritmo SHA-256 no disponible", e);
        }
    }

    /**
     * Detecta si el ADN pertenece a un mutante.
     * Usa terminación temprana: se detiene después de encontrar 2 secuencias.
     * 
     * @param dna Matriz de ADN
     * @return true si es mutante (se encontró más de una secuencia)
     */
    private boolean detectMutant(String[] dna) {
        int n = dna.length;
        int sequencesFound = 0;
        
        // Verificar secuencias horizontales
        sequencesFound += countHorizontalSequences(dna, n);
        if (sequencesFound > 1) {
            return true; // Terminación temprana
        }
        
        // Verificar secuencias verticales
        sequencesFound += countVerticalSequences(dna, n);
        if (sequencesFound > 1) {
            return true; // Terminación temprana
        }
        
        // Verificar secuencias diagonales (arriba-izquierda a abajo-derecha)
        sequencesFound += countDiagonalSequences(dna, n, true);
        if (sequencesFound > 1) {
            return true; // Terminación temprana
        }
        
        // Verificar secuencias diagonales (arriba-derecha a abajo-izquierda)
        sequencesFound += countDiagonalSequences(dna, n, false);
        
        return sequencesFound > 1;
    }

    /**
     * Cuenta secuencias horizontales.
     */
    private int countHorizontalSequences(String[] dna, int n) {
        int count = 0;
        for (int row = 0; row < n; row++) {
            for (int col = 0; col <= n - SEQUENCE_LENGTH; col++) {
                if (hasSequence(dna[row].charAt(col), 
                               dna[row].charAt(col + 1), 
                               dna[row].charAt(col + 2), 
                               dna[row].charAt(col + 3))) {
                    count++;
                    if (count > 1) {
                        return count; // Terminación temprana
                    }
                }
            }
        }
        return count;
    }

    /**
     * Cuenta secuencias verticales.
     */
    private int countVerticalSequences(String[] dna, int n) {
        int count = 0;
        for (int col = 0; col < n; col++) {
            for (int row = 0; row <= n - SEQUENCE_LENGTH; row++) {
                if (hasSequence(dna[row].charAt(col), 
                               dna[row + 1].charAt(col), 
                               dna[row + 2].charAt(col), 
                               dna[row + 3].charAt(col))) {
                    count++;
                    if (count > 1) {
                        return count; // Terminación temprana
                    }
                }
            }
        }
        return count;
    }

    /**
     * Cuenta secuencias diagonales.
     * 
     * @param dna Matriz de ADN
     * @param n Tamaño de la matriz
     * @param topLeftToBottomRight true para dirección \, false para dirección /
     */
    private int countDiagonalSequences(String[] dna, int n, boolean topLeftToBottomRight) {
        int count = 0;
        
        for (int row = 0; row <= n - SEQUENCE_LENGTH; row++) {
            for (int col = 0; col <= n - SEQUENCE_LENGTH; col++) {
                boolean hasSeq;
                
                if (topLeftToBottomRight) {
                    // Dirección diagonal \
                    hasSeq = hasSequence(
                        dna[row].charAt(col),
                        dna[row + 1].charAt(col + 1),
                        dna[row + 2].charAt(col + 2),
                        dna[row + 3].charAt(col + 3)
                    );
                } else {
                    // Dirección diagonal /
                    int startCol = col + SEQUENCE_LENGTH - 1;
                    if (startCol >= n) continue;
                    
                    hasSeq = hasSequence(
                        dna[row].charAt(startCol),
                        dna[row + 1].charAt(startCol - 1),
                        dna[row + 2].charAt(startCol - 2),
                        dna[row + 3].charAt(startCol - 3)
                    );
                }
                
                if (hasSeq) {
                    count++;
                    if (count > 1) {
                        return count; // Terminación temprana
                    }
                }
            }
        }
        
        return count;
    }

    /**
     * Verifica si 4 caracteres forman una secuencia válida (todos iguales).
     */
    private boolean hasSequence(char c1, char c2, char c3, char c4) {
        return c1 == c2 && c2 == c3 && c3 == c4;
    }
}
