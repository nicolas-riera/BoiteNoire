package com.pigeon.boitenoire.repository;

import com.pigeon.boitenoire.enums.EventType;
import com.pigeon.boitenoire.model.BaseEvent;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface EventRepository extends MongoRepository<BaseEvent, String> {

    List<BaseEvent> findByUserId(String userId);

    List<BaseEvent> findByType(EventType type);
}