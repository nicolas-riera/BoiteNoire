package com.pigeon.boitenoire.model;

import java.util.Map;

import com.pigeon.boitenoire.enums.EventType;

public class ApiRequestEvent extends BaseEvent {

    private String endpoint;
    private String httpMethod;
    private int statusCode;
    private long responseTimeMs;
    private Map<String, String> queryParameters;

    public ApiRequestEvent() {
        super(EventType.API_REQUEST);
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public Map<String, String> getQueryParameters() {
        return queryParameters;
    }

    public void setQueryParameters(Map<String, String> queryParameters) {
        this.queryParameters = queryParameters;
    }
}