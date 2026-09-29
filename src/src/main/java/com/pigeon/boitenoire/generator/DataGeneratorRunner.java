package com.pigeon.boitenoire.generator;

import com.pigeon.boitenoire.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Component
@Profile("generate")
public class DataGeneratorRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataGeneratorRunner.class);
    private static final int TOTAL_EVENTS = 100_000;
    private static final int BATCH_SIZE = 10_000;
    private static final int TOTAL_USERS = 1_000;

    private static final String[] ENDPOINTS = {
        "/api/v1/messages/send",
        "/api/v1/channels/history",
        "/api/v1/webhooks/deliver",
        "/api/v1/users/status",
        "/api/v1/auth/token"
    };

    private static final int[] HOURLY_WEIGHTS = {
        1, 1, 1, 1, 1, 2,
        4, 7, 10, 12, 11, 13,
        15, 12, 10, 11, 13, 15,
        18, 16, 12, 8, 4, 2
    };

    private final MongoTemplate mongoTemplate;

    public DataGeneratorRunner(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(String... args) {
        log.info("Starting event generation for Pigeon platform: {} events...", TOTAL_EVENTS);
        long startTime = System.currentTimeMillis();

        Random random = new Random();
        List<BaseEvent> batch = new ArrayList<>(BATCH_SIZE);
        int totalInserted = 0;

        for (int i = 0; i < TOTAL_EVENTS; i++) {
            String userId = generateUserId(random);
            Instant timestamp = generateTimestamp(random);
            BaseEvent event = createRandomEvent(random, userId, timestamp);

            batch.add(event);

            if (batch.size() == BATCH_SIZE) {
                mongoTemplate.insert(batch, BaseEvent.class);
                totalInserted += batch.size();
                log.info("Progress: {} / {} events inserted", totalInserted, TOTAL_EVENTS);
                batch.clear();
            }
        }

        if (!batch.isEmpty()) {
            mongoTemplate.insert(batch, BaseEvent.class);
            totalInserted += batch.size();
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("Success! {} Pigeon events generated and inserted in {} ms.", totalInserted, duration);
    }

    private String generateUserId(Random random) {
        double r = random.nextDouble();
        int userIndex = (int) (Math.pow(r, 3) * TOTAL_USERS) + 1;
        return "user_" + userIndex;
    }

    private Instant generateTimestamp(Random random) {
        int dayOfYear = random.nextInt(365);
        int hour = pickWeightedHour(random);
        int minute = random.nextInt(60);
        int second = random.nextInt(60);

        return LocalDateTime.now()
                .minusDays(dayOfYear)
                .withHour(hour)
                .withMinute(minute)
                .withSecond(second)
                .toInstant(ZoneOffset.UTC);
    }

    private int pickWeightedHour(Random random) {
        int totalWeight = Arrays.stream(HOURLY_WEIGHTS).sum();
        int r = random.nextInt(totalWeight);
        int current = 0;
        for (int h = 0; h < 24; h++) {
            current += HOURLY_WEIGHTS[h];
            if (r < current) {
                return h;
            }
        }
        return 12;
    }

    private BaseEvent createRandomEvent(Random random, String userId, Instant timestamp) {
        int roll = random.nextInt(100);
        BaseEvent event;

        if (roll < 45) {
            ApiRequestEvent api = new ApiRequestEvent();
            api.setEndpoint(ENDPOINTS[random.nextInt(ENDPOINTS.length)]);
            api.setHttpMethod(roll < 35 ? "GET" : "POST");
            api.setStatusCode(random.nextInt(100) < 92 ? 200 : (random.nextBoolean() ? 429 : 504));
            api.setResponseTimeMs(12 + random.nextInt(320));
            api.setQueryParameters(Map.of("channel_id", "chn_" + random.nextInt(100), "limit", "50"));
            event = api;
        } else if (roll < 70) {
            UserLoginEvent login = new UserLoginEvent();
            login.setIpAddress("10.0." + random.nextInt(255) + "." + (random.nextInt(254) + 1));
            login.setUserAgent("PigeonDesktopClient/2.4.1 (Windows NT 10.0)");
            login.setSuccess(random.nextInt(100) < 96);
            event = login;
        } else if (roll < 85) {
            PaymentProcessedEvent payment = new PaymentProcessedEvent();
            payment.setTransactionId("sub_tx_" + UUID.randomUUID().toString().substring(0, 8));
            int seats = random.nextInt(20) + 1;
            double amount = seats * 12.0;
            payment.setAmount(amount);
            payment.setCurrency("EUR");
            payment.setPaymentDetails(new PaymentDetails("STRIPE", "4242", "SUCCESS"));
            payment.setItems(List.of(
                new OrderItem("pigeon_pro_seat", seats, 12.0),
                new OrderItem("addon_history_retention_1y", 1, 29.0)
            ));
            event = payment;
        } else if (roll < 95) {
            ApplicationErrorEvent error = new ApplicationErrorEvent();
            error.setServiceName("messaging-worker-service");
            error.setErrorCode("ERR_PUBSUB_DISCONNECT");
            error.setErrorMessage("WebSocket connection dropped under high throughput");
            error.setStackTrace("com.pigeon.messaging.exception.SocketTimeoutException: Redis Pub/Sub timed out at Worker.java:88");
            event = error;
        } else {
            UserProfileUpdatedEvent profile = new UserProfileUpdatedEvent();
            profile.setUpdatedByUserId("admin_workspace");
            profile.setModifiedFields(List.of("status_message", "notification_preferences"));
            event = profile;
        }

        event.setUserId(userId);
        event.setTimestamp(timestamp);
        return event;
    }
}