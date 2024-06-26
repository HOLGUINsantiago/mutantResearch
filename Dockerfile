# Usa una imagen base de OpenJDK para Java 11 (o la versión que estés usando)
FROM openjdk:17-oracle

# Etiqueta que describe el mantenimiento del Dockerfile
LABEL maintainer="holguinsanty@gmail.com"

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia el archivo JAR construido de tu proyecto al contenedor
COPY build/libs/neuroPsi-0.0.1.jar /app/app.jar

COPY src/main/resources /app/src/main/resources


# Expone el puerto en el que tu aplicación Spring Boot escucha (ajústalo según tu configuración)
EXPOSE 8080

# Comando para ejecutar tu aplicación Spring Boot cuando el contenedor se inicie
CMD ["java", "-jar", "app.jar"]

