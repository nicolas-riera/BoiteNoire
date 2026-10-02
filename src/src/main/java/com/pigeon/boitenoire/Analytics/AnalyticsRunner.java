package com.pigeon.boitenoire.Analytics;

import com.pigeon.boitenoire.model.BaseEvent;
import org.bson.Document;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Component
@Profile("cli-analytics")
public class AnalyticsRunner implements CommandLineRunner {

    private final MongoTemplate mongoTemplate;

    public AnalyticsRunner(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(String... args) {
        Instant end = Instant.now();
        Instant start = end.minus(365, ChronoUnit.DAYS);

        printTop10Users(start, end);
        printErrorDistribution(start, end);
        printEndpointPerformance();
        printConversionFunnel(start, end);
    }

    private void printTop10Users(Instant start, Instant end) {
        System.out.println("--- 1. Top 10 most active users ---");

        Aggregation aggregation = newAggregation(
            match(Criteria.where(BaseEvent::getTimestamp).gte(start).lte(end)),
            group("userId").count().as("eventCount"),
            sort(Sort.Direction.DESC, "eventCount"),
            limit(10)
        );

        List<Document> results = mongoTemplate.aggregate(aggregation, BaseEvent.class, Document.class).getMappedResults();
        results.forEach(doc -> System.out.printf("User: %-15s | Events: %d%n", doc.getString("_id"), doc.getInteger("eventCount")));
        System.out.println();
    }

    private void printErrorDistribution(Instant start, Instant end) {
        System.out.println("--- 2. Error distribution by type and day ---");

        Aggregation aggregation = newAggregation(
            match(Criteria.where(BaseEvent::getTimestamp).gte(start).lte(end)
                    .and("_class").is("com.pigeon.boitenoire.model.ApplicationErrorEvent")),
            project("errorCode")
                .andExpression("dateToString('%Y-%m-%d', timestamp)").as("date"),
            group("date", "errorCode").count().as("count"),
            sort(Sort.Direction.ASC, "_id.date").and(Sort.Direction.DESC, "count")
        );

        List<Document> results = mongoTemplate.aggregate(aggregation, BaseEvent.class, Document.class).getMappedResults();
        results.forEach(doc -> {
            Document id = doc.get("_id", Document.class);
            System.out.printf("Date: %s | Error: %-25s | Count: %d%n", 
                id.getString("date"), id.getString("errorCode"), doc.getInteger("count"));
        });
        System.out.println();
    }

    private void printEndpointPerformance() {
        System.out.println("--- 3. Endpoint performance (average and P95) ---");

        AggregationOperation groupStep = context -> new Document("$group", new Document("_id", "$endpoint")
                .append("avgResponseTime", new Document("$avg", "$responseTimeMs"))
                .append("p95ResponseTime", new Document("$percentile", new Document("input", "$responseTimeMs")
                        .append("p", List.of(0.95))
                        .append("method", "approximate"))));

        Aggregation aggregation = newAggregation(
            match(Criteria.where("_class").is("com.pigeon.boitenoire.model.ApiRequestEvent")),
            groupStep,
            sort(Sort.Direction.DESC, "avgResponseTime")
        );

        List<Document> results = mongoTemplate.aggregate(aggregation, BaseEvent.class, Document.class).getMappedResults();
        
        results.forEach(doc -> {
            List<?> p95List = doc.get("p95ResponseTime", List.class);
            Object p95Val = (p95List != null && !p95List.isEmpty()) ? p95List.get(0) : 0;

            System.out.printf("Endpoint: %-30s | Avg: %.2f ms | P95: %s ms%n",
                doc.getString("_id"),
                doc.getDouble("avgResponseTime"),
                p95Val);
        });
        System.out.println();
    }

    private void printConversionFunnel(Instant start, Instant end) {
        System.out.println("--- 4. Conversion funnel ---"); // How many users have followed a given sequence of events, for example registration then first message then subscription.

        AggregationOperation userFlagsStep = context -> new Document("$group", new Document("_id", "$userId")
            .append("hasLogin", new Document("$max", new Document("$cond", List.of(
                new Document("$eq", List.of("$_class", "com.pigeon.boitenoire.model.UserLoginEvent")), 1, 0))))
            .append("hasApi", new Document("$max", new Document("$cond", List.of(
                new Document("$eq", List.of("$_class", "com.pigeon.boitenoire.model.ApiRequestEvent")), 1, 0))))
            .append("hasPay", new Document("$max", new Document("$cond", List.of(
                new Document("$eq", List.of("$_class", "com.pigeon.boitenoire.model.PaymentProcessedEvent")), 1, 0)))));

        AggregationOperation totalsStep = context -> new Document("$group", new Document("_id", null)
            .append("step1Users", new Document("$sum", "$hasLogin"))
            .append("step2Users", new Document("$sum", new Document("$cond", List.of(
                new Document("$and", List.of(
                    new Document("$eq", List.of("$hasLogin", 1)),
                    new Document("$eq", List.of("$hasApi", 1))
                )), 1, 0))))
            .append("step3Users", new Document("$sum", new Document("$cond", List.of(
                new Document("$and", List.of(
                    new Document("$eq", List.of("$hasLogin", 1)),
                    new Document("$eq", List.of("$hasApi", 1)),
                    new Document("$eq", List.of("$hasPay", 1))
                )), 1, 0)))));

        Aggregation aggregation = newAggregation(
            match(Criteria.where(BaseEvent::getTimestamp).gte(start).lte(end)),
            userFlagsStep,
            totalsStep,
            project("step1Users", "step2Users", "step3Users")
                .andExpression("cond(step1Users > 0, (step3Users / step1Users) * 100, 0)").as("conversionRate")
        );

        Document result = mongoTemplate.aggregate(aggregation, BaseEvent.class, Document.class).getUniqueMappedResult();

        if (result != null) {
            int step1 = result.getInteger("step1Users", 0);
            int step2 = result.getInteger("step2Users", 0);
            int step3 = result.getInteger("step3Users", 0);
            double rate = result.get("conversionRate") != null ? ((Number) result.get("conversionRate")).doubleValue() : 0.0;

            System.out.printf("Step 1 (Logins)            : %d users%n", step1);
            System.out.printf("Step 2 (Logins + APIs)     : %d users%n", step2);
            System.out.printf("Step 3 (Logins + APIs + Pay): %d users%n", step3);
            System.out.printf("Overall Conversion Rate    : %.2f %%%n", rate);
        } else {
            System.out.println("No data found for conversion funnel.");
        }
        System.out.println();
    }
}