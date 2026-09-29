package com.pigeon.boitenoire.model;

public record OrderItem(
    String productId,
    int quantity,
    Double unitPrice
) {}