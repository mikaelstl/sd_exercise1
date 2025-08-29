# Exemplo usando a imagem do Maven
FROM maven:3.8-openjdk-17 as builder
WORKDIR /app

# Copie o arquivo pom.xml e o código fonte para o contêiner
COPY pom.xml .
COPY src ./src

# Execute o comando Maven para compilar e empacotar o projeto
RUN mvn clean package -DskipTests

# Use uma imagem mais leve para a etapa final (opcional)
FROM openjdk:17-jdk-slim
WORKDIR .
COPY --from=builder /app/target/mapper-1.0-SNAPSHOT.jar /app/app.jar

# Comando para executar o arquivo JAR
# ENTRYPOINT ["java", "-jar", "/usr/src/app/app.jar"]
ENTRYPOINT ["java", "-jar", "/app/app.jar"]