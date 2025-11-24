package com.mutant.detector.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de Swagger/OpenAPI 3.0.
 * 
 * Proporciona documentación completa de la API accesible en:
 * - Swagger UI: http://localhost:8080/swagger-ui.html
 * - OpenAPI JSON: http://localhost:8080/v3/api-docs
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI mutantDetectorOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Detección de Mutantes")
                        .description("""
                                # API de Detección de Mutantes
                                
                                API REST para detectar mutantes basada en secuencias de ADN.
                                
                                ## Funcionalidades
                                
                                - Detección de Mutantes: Analiza secuencias de ADN para identificar patrones mutantes
                                - Estadísticas: Proporciona métricas sobre las secuencias analizadas
                                - Caché Inteligente: Utiliza hash SHA-256 para evitar re-análisis
                                - Optimización: Early termination para máximo rendimiento
                                
                                ## Criterio de Detección
                                
                                Un humano es mutante si se encuentra más de una secuencia de 4 letras iguales 
                                (A, T, C, G) en cualquier dirección:
                                - Horizontal
                                - Vertical
                                - Diagonal (ambas direcciones)
                                
                                ## Validaciones
                                
                                - La matriz debe ser NxN (cuadrada)
                                - Solo se permiten caracteres: A, T, C, G
                                - El array no puede ser nulo o vacío
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipo Detector de Mutantes")
                                .email("support@mutantdetector.com")
                                .url("https://github.com/mutant-detector"))
                        .license(new License()
                                .name("Licencia MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .externalDocs(new ExternalDocumentation()
                        .description("Documentación completa del proyecto")
                        .url("https://github.com/mutant-detector/docs"))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor de Desarrollo"),
                        new Server()
                                .url("https://api.mutantdetector.com")
                                .description("Servidor de Producción")
                ));
    }
}
