# Multi-stage build for μPACS
FROM eclipse-temurin:17-jre-jammy as builder
WORKDIR /app
COPY build/libs/*.jar application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy extracted layers
COPY --from=builder /app/dependencies/ ./
COPY --from=builder /app/spring-boot-loader/ ./
COPY --from=builder /app/snapshot-dependencies/ ./
COPY --from=builder /app/application/ ./

# Expose HTTP port (Spring Boot)
EXPOSE 8080

# Expose DICOM port
EXPOSE 8104

# Set the entry point
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
