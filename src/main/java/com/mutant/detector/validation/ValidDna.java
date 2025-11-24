package com.mutant.detector.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Anotación de validación personalizada para secuencias de ADN.
 * 
 * Valida que:
 * - El array no sea nulo o vacío
 * - La matriz sea NxN (cuadrada)
 * - Todos los caracteres sean bases de ADN válidas (A, T, C, G)
 * 
 * Uso:
 * <pre>
 * {@code
 * @ValidDna
 * private String[] dna;
 * }
 * </pre>
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DnaValidator.class)
@Documented
public @interface ValidDna {

    /**
     * Mensaje de error cuando falla la validación.
     */
    String message() default "Secuencia de ADN inválida";

    /**
     * Grupos de validación.
     */
    Class<?>[] groups() default {};

    /**
     * Payload para clientes.
     */
    Class<? extends Payload>[] payload() default {};
}
