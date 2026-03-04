package com.lipari.bank.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public sealed class Account permits CheckingAccount, SavingsAccount {

    private final String iban;
    private BigDecimal balance;
    private final Customer owner;
    private final List<Transaction> transactions;

    public Account(String iban, BigDecimal initialBalance, Customer owner) {
        if (iban == null || iban.isBlank()) {
            throw new IllegalArgumentException("IBAN non valido");
        }
        if (initialBalance == null || initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Il saldo iniziale non può essere negativo");
        }
        this.iban = iban;
        this.balance = initialBalance;
        this.owner = owner;
        this.transactions = new ArrayList<>();
    }

    public String getIban()                   { return iban; }
    public BigDecimal getBalance()            { return balance; }
    public Customer getOwner()                { return owner; }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    /**
     * Deposita un importo sul conto.
     * Sono ammessi importi >= 0 (zero non modifica il saldo ma è sintatticamente valido).
     */
    public void deposit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("L'importo del deposito non può essere negativo");
        }
        this.balance = this.balance.add(amount);
        transactions.add(new Transaction(
                TransactionType.DEPOSIT,
                amount,
                "Deposito",
                LocalDateTime.now()));
    }

    public void withdraw(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("L'importo del prelievo deve essere positivo");
        }
        if (balance.subtract(amount).compareTo(getMinBalance()) < 0) {
            throw new IllegalStateException(
                    "Fondi insufficienti. Saldo disponibile: " + balance.toPlainString() + "€");
        }
        this.balance = this.balance.subtract(amount);
        transactions.add(new Transaction(
                TransactionType.WITHDRAWAL,
                amount,
                "Prelievo",
                LocalDateTime.now()));
    }

    /** Soglia minima di saldo — override nelle sottoclassi. */
    protected BigDecimal getMinBalance() {
        return BigDecimal.ZERO;
    }

    @Override
    public String toString() {
        return String.format("IBAN: %-30s | Saldo: %10.2f€ | Titolare: %s",
                iban, balance, owner);
    }
}
