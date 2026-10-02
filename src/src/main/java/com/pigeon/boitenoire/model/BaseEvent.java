package com.pigeon.boitenoire.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex; 
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import com.pigeon.boitenoire.enums.EventType;
import java.time.Instant;

@Document(collection = "events")
@CompoundIndex(name = "class_timestamp_idx", def = "{'_class': 1, 'timestamp': 1}")
public abstract class BaseEvent {

    @Id
    private String id;

    @Indexed
    private Instant timestamp;

    @Indexed
    private EventType type;

    @Indexed
    private String userId;

    public BaseEvent(EventType type) {
        this.type = type;
        this.timestamp = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}