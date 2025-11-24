package com.mutant.detector.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa un registro de ADN en la base de datos.
 * 
 * Almacena el hash de las secuencias de ADN para evitar re-analizar el mismo ADN.
 * El campo dnaHash está indexado para búsquedas rápidas.
 */
@Entity
@Table(name = "dna_record", indexes = {
    @Index(name = "idx_dna_hash", columnList = "dnaHash", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DnaRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Hash SHA-256 de la secuencia de ADN.
     * Usado como identificador único para prevenir análisis duplicados.
     */
    @Column(nullable = false, unique = true, length = 64)
    private String dnaHash;

    /**
     * Bandera que indica si el ADN pertenece a un mutante.
     */
    @Column(nullable = false)
    private Boolean isMutant;

    /**
     * Constructor sin ID (para crear nuevos registros).
     */
    public DnaRecord(String dnaHash, Boolean isMutant) {
        this.dnaHash = dnaHash;
        this.isMutant = isMutant;
    }
}
