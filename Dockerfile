FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY . /app

# Da permisos de ejecución al wrapper de Maven
RUN chmod +x mvnw

# Compilar sin tests (más rápido para producción)
RUN ./mvnw package -DskipTests

# Ejecuta el JAR generado
CMD ["java", "-jar", "target/tfg-0.0.1-SNAPSHOT.jar"]
