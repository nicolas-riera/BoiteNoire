package com.pigeon.boitenoire.model;

import java.util.List;

import com.pigeon.boitenoire.enums.EventType;

public class PaymentProcessedEvent extends BaseEvent {

    private String transactionId;
    private Double amount;
    private String currency;
    private PaymentDetails paymentDetails;
    private List<OrderItem> items;

    public PaymentProcessedEvent() {
        super(EventType.PAYMENT_PROCESSED);
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public PaymentDetails getPaymentDetails() {
        return paymentDetails;
    }

    public void setPaymentDetails(PaymentDetails paymentDetails) {
        this.paymentDetails = paymentDetails;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
}