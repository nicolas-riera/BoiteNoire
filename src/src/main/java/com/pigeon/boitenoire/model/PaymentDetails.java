package com.pigeon.boitenoire.model;

public record PaymentDetails(
    String provider,
    String cardLast4,
    String status
) {}