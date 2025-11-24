package com.mutant.detector.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Validador para la anotación @ValidDna.
 * 
 * Realiza validación integral de secuencias de ADN:
 * 1. Verifica si el array es nulo o vacío
 * 2. Valida que la matriz sea NxN (cuadrada)
 * 3. Asegura que todos los caracteres sean bases de ADN válidas (A, T, C, G)
 */
public class DnaValidator implements ConstraintValidator<ValidDna, String[]> {

    private static final Set<Character> VALID_BASES = new HashSet<>(Arrays.asList('A', 'T', 'C', 'G'));
    private static final int MIN_SIZE = 4;

    @Override
    public void initialize(ValidDna constraintAnnotation) {
        // No se necesita inicialización
    }

    @Override
    public boolean isValid(String[] dna, ConstraintValidatorContext context) {
        // Deshabilitar violación de restricción predeterminada
        context.disableDefaultConstraintViolation();

        // Verificar si el array es nulo o vacío
        if (dna == null || dna.length == 0) {
            context.buildConstraintViolationWithTemplate("El array de ADN no puede ser nulo o vacío")
                   .addConstraintViolation();
            return false;
        }

        // Verificar tamaño mínimo
        if (dna.length < MIN_SIZE) {
            context.buildConstraintViolationWithTemplate(
                String.format("La matriz de ADN debe ser al menos de %dx%d", MIN_SIZE, MIN_SIZE))
                   .addConstraintViolation();
            return false;
        }

        int n = dna.length;

        // Validar cada fila
        for (int i = 0; i < n; i++) {
            String row = dna[i];

            // Verificar fila nula
            if (row == null) {
                context.buildConstraintViolationWithTemplate(
                    String.format("La secuencia de ADN en el índice %d es nula", i))
                       .addConstraintViolation();
                return false;
            }

            // Verificar si la matriz es cuadrada (NxN)
            if (row.length() != n) {
                context.buildConstraintViolationWithTemplate(
                    String.format("El ADN debe ser una matriz NxN. Se esperaba longitud %d pero se obtuvo %d en el índice %d", 
                                  n, row.length(), i))
                       .addConstraintViolation();
                return false;
            }

            // Validar caracteres
            for (int j = 0; j < row.length(); j++) {
                char base = row.charAt(j);
                if (!VALID_BASES.contains(base)) {
                    context.buildConstraintViolationWithTemplate(
                        String.format("Base de ADN inválida '%c' en la posición [%d,%d]. Solo se permiten A, T, C, G", 
                                      base, i, j))
                           .addConstraintViolation();
                    return false;
                }
            }
        }

        return true;
    }
}
