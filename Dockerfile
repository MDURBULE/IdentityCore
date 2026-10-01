FROM eclipse-temurin:17-jdk AS build
WORKDIR /IdentityCore
COPY src src
COPY pom.xml .

COPY mvnw .
COPY .mvn .mvn

RUN chmod +x ./mvnw
RUN ./mvnw clean package -DskipTests

From eclipse-temurin:17-jdk
VOLUME /tmp

COPY --from=build /IdentityCore/target/IdentityCore-0.0.1.jar IdentityCore.jar
ENTRYPOINT ["java","-jar","IdentityCore.jar"]
EXPOSE 8080
