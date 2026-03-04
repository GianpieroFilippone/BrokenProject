package com.lipari.bank.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class SavingsAccount extends Account {

    /** Tasso di interesse annuo in percentuale intera, es: 2 per il 2%, 3.5 per il 3.5% */
    private double interestRate;

    public SavingsAccount(String iban, BigDecimal initialBalance,
                          Customer owner, double interestRate) {
        super(iban, initialBalance, owner);
        this.interestRate = interestRate;
    }

    public double getInterestRate()               { return interestRate; }
    public void setInterestRate(double rate)      { this.interestRate = rate; }

    /**
     * Applica gli interessi annui al saldo corrente.
     *
     * ⚠ BUG #1: il calcolo degli interessi è corretto (interestRate / 100),
     *   ma la chiamata a deposit() usa BigDecimal.ZERO come argomento invece
     *   della variabile 'interest'. Risultato: il saldo non cambia mai.
     */
    public void applyInterest() {
        BigDecimal interest = getBalance()
                .multiply(BigDecimal.valueOf(interestRate / 100))
                .setScale(2, RoundingMode.HALF_UP);

        if (interest.compareTo(BigDecimal.ZERO) > 0) {
            deposit(interest);   // BUG #1: dovrebbe essere deposit(interest)
        }
    }

    @Override
    public String toString() {
        return super.toString()
                + String.format(" | Conto Risparmio | Tasso: %.2f%%", interestRate);
    }
}
