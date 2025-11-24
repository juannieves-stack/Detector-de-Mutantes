# Dockerfile Multi-Stage para API de Detección de Mutantes
# Optimizado para producción con tamaño de imagen mínimo

# ============================================
# Etapa 1: Etapa de Construcción
# ============================================
FROM gradle:8.5-jdk17-alpine AS builder

# Establecer directorio de trabajo
WORKDIR /app

# Copiar Gradle wrapper y archivos de construcción
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Descargar dependencias (capa cacheada)
RUN gradle dependencies --no-daemon

# Copiar código fuente
COPY src src

# Construir la aplicación
# Omitir tests para builds más rápidos (ejecutar tests en pipeline CI/CD)
RUN gradle bootJar --no-daemon -x test

# ============================================
# Etapa 2: Etapa de Ejecución
# ============================================
FROM eclipse-temurin:17-jre-alpine

# Metadatos
LABEL maintainer="mutant-detector-team@example.com"
LABEL description="API de Detección de Mutantes - Servicio de análisis de secuencias de ADN"
LABEL version="1.0.0"

# Crear usuario no-root para seguridad
RUN addgroup -S spring && adduser -S spring -G spring

# Establecer directorio de trabajo
WORKDIR /app

# Copiar JAR desde la etapa de construcción
COPY --from=builder /app/build/libs/*.jar app.jar

# Cambiar propiedad al usuario no-root
RUN chown -R spring:spring /app

# Cambiar a usuario no-root
USER spring:spring

# Exponer puerto de la aplicación
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Flags de optimización de JVM
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -XX:+OptimizeStringConcat \
               -Djava.security.egd=file:/dev/./urandom"

# Ejecutar la aplicación
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
