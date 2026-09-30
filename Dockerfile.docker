From openjdk:17 as build
WORKDIR /IdentityCore
COPY src src
COPY pom.xml .

COPY mvnw .
COPY .mvn .mvn

RUN chmod +x ./mvnw
RUN ./mvnw clean package -DskipTests

From openjdk:17
VOLUME /tmp

COPY --from:build /IdentityCore/target/IdentityCore-0.0.1.jar IdentityCore.jar
ENTRYPOINT ["java","-jar","IdentityCore.jar"]
EXPOSE 8080
