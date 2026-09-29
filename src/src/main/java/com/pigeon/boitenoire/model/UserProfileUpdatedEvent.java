package com.pigeon.boitenoire.model;

import java.util.List;

import com.pigeon.boitenoire.enums.EventType;

public class UserProfileUpdatedEvent extends BaseEvent {

    private String updatedByUserId;
    private List<String> modifiedFields;

    public UserProfileUpdatedEvent() {
        super(EventType.USER_PROFILE_UPDATED);
    }

    public String getUpdatedByUserId() {
        return updatedByUserId;
    }

    public void setUpdatedByUserId(String updatedByUserId) {
        this.updatedByUserId = updatedByUserId;
    }

    public List<String> getModifiedFields() {
        return modifiedFields;
    }

    public void setModifiedFields(List<String> modifiedFields) {
        this.modifiedFields = modifiedFields;
    }
}