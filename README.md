# BoiteNoire

## Description
BoiteNoire is an event ingestion and analytics service built with Spring Boot and MongoDB for the Pigeon messaging platform. It ingests and analyzes application events—such as user logins, API calls, subscription payments, system errors, and profile updates—to help understand product usage, identify recurring errors, monitor API latency, and track user conversion.

## Project Setup
Make sure MongoDB is installed and running

### Run Generator
```bash
cd src
mvn.cmd spring-boot:run "-Dspring-boot.run.profiles=generate"
```

### Run Analytics
```bash
cd src
mvn.cmd spring-boot:run "-Dspring-boot.run.profiles=cli-analytics"
```
## Database
MongoDB database running on `localhost:27017` under `/boitenoire/events`.

## Authors
This project was made by: [Nicolas](https://github.com/nicolas-riera) and [Gabriel](https://github.com/Gabriel-SEMPERE)