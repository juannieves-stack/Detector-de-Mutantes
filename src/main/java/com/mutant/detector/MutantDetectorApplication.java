package com.mutant.detector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de la aplicación API de Detección de Mutantes.
 * 
 * Esta aplicación Spring Boot proporciona endpoints REST para:
 * - Detectar si una secuencia de ADN pertenece a un mutante
 * - Obtener estadísticas sobre las secuencias de ADN analizadas
 */
@SpringBootApplication
public class MutantDetectorApplication {

    public static void main(String[] args) {
        SpringApplication.run(MutantDetectorApplication.class, args);
    }
}
