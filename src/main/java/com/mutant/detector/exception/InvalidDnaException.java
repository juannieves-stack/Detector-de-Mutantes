package com.mutant.detector.exception;

/**
 * Excepción personalizada lanzada cuando falla la validación de ADN.
 * 
 * Esta excepción se lanza cuando:
 * - La matriz de ADN no es NxN (no es cuadrada)
 * - El ADN contiene caracteres inválidos (no son A, T, C, G)
 * - La matriz de ADN está vacía o es nula
 */
public class InvalidDnaException extends RuntimeException {

    public InvalidDnaException(String message) {
        super(message);
    }
}
