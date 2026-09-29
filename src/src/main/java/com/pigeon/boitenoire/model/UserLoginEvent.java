package com.pigeon.boitenoire.model;

import com.pigeon.boitenoire.enums.EventType;

public class UserLoginEvent extends BaseEvent {

    private String ipAddress;
    private String userAgent;
    private boolean success;

    public UserLoginEvent() {
        super(EventType.USER_LOGIN);
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}