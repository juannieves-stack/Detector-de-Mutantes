package com.mutant.detector.repository;

import com.mutant.detector.entity.DnaRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Interfaz de repositorio para la entidad DnaRecord.
 * 
 * Proporciona métodos para:
 * - Encontrar registros de ADN por hash (para caché)
 * - Contar registros de ADN mutantes y humanos (para estadísticas)
 */
@Repository
public interface DnaRecordRepository extends JpaRepository<DnaRecord, Long> {

    /**
     * Encuentra un registro de ADN por su hash.
     * Usado para verificar si una secuencia de ADN ya ha sido analizada.
     *
     * @param dnaHash Hash SHA-256 de la secuencia de ADN
     * @return Optional que contiene el DnaRecord si se encuentra
     */
    Optional<DnaRecord> findByDnaHash(String dnaHash);

    /**
     * Cuenta registros de ADN por estado de mutante.
     * Usado para generar estadísticas.
     *
     * @param isMutant true para contar mutantes, false para contar humanos
     * @return conteo de registros que coinciden con el criterio
     */
    long countByIsMutant(boolean isMutant);
}
