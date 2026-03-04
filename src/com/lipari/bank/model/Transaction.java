package com.lipari.bank.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record Transaction(
        TransactionType type,
        BigDecimal amount,
        String description,
        LocalDateTime timestamp) {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public Transaction {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("L'importo non può essere negativo");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("La descrizione è obbligatoria");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("Il timestamp è obbligatorio");
        }
    }

    @Override
    public String toString() {
        return String.format("[%s] %-10s %10.2f€  %s",
                timestamp.format(FMT),
                type.getLabel(),
                amount,
                description);
    }
}
