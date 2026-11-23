FROM gradle:8.14-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle --no-daemon clean installDist

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/install/KtorUsersApiAdvanced/ ./
EXPOSE 8080
ENV PORT=8080
CMD ["bin/KtorUsersApiAdvanced"]
