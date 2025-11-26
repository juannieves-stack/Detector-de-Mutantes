package com.mutant.detector.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que representa un registro de ADN en la base de datos.
 * 
 * Almacena el hash de las secuencias de ADN para evitar re-analizar el mismo ADN.
 * El campo dnaHash está indexado para búsquedas rápidas.
 */
@Entity
@Table(name = "dna_records", indexes = {
    @Index(name = "idx_dna_hash", columnList = "dna_hash", unique = true),
    @Index(name = "idx_is_mutant", columnList = "is_mutant")
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
    @Column(name = "dna_hash", nullable = false, unique = true, length = 64)
    private String dnaHash;

    /**
     * Bandera que indica si el ADN pertenece a un mutante.
     */
    @Column(name = "is_mutant", nullable = false)
    private Boolean isMutant;

    /**
     * Fecha y hora de creación del registro.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Constructor sin ID (para crear nuevos registros).
     */
    public DnaRecord(String dnaHash, Boolean isMutant) {
        this.dnaHash = dnaHash;
        this.isMutant = isMutant;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
