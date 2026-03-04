package com.lipari.bank.cli;

import com.lipari.bank.model.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class BankConsole {

    private final List<Account> accounts = new ArrayList<>();
    private final Scanner scanner = new Scanner(System.in);

    // ─── Entry point ───────────────────────────────────────────────────────────

    public static void main(String[] args) {
        new BankConsole().run();
    }

    // ─── Main loop ─────────────────────────────────────────────────────────────

    private void run() {
        initializeSampleData();
        System.out.println("Benvenuto in LipariBank — " + accounts.size() + " conti caricati.");

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt();           // ← BUG #2 si annida qui

            switch (choice) {
                case 1 -> listAccounts();
                case 2 -> showBalance();
                case 3 -> makeDeposit();
                case 4 -> makeWithdrawal();
                case 5 -> showTransactions();
                case 6 -> applyInterestToSavings();
                case 7 -> processAccounts();  // ← BUG #3 si manifesta qui
                case 0 -> {
                    System.out.println("\nArrivederci da LipariBank!");
                    running = false;
                }
                default -> System.out.println("Scelta non valida, riprova.");
            }
        }
    }

    // ─── Menu ──────────────────────────────────────────────────────────────────

    private void printMenu() {
        System.out.println("""

                ╔══════════════════════════════════════╗
                ║         LIPARIBANK  CONSOLE          ║
                ╠══════════════════════════════════════╣
                ║  1. Lista conti                      ║
                ║  2. Visualizza saldo                 ║
                ║  3. Deposita                         ║
                ║  4. Preleva                          ║
                ║  5. Storico transazioni              ║
                ║  6. Applica interessi (risparmio)    ║
                ║  7. Processa e classifica conti      ║
                ║  0. Esci                             ║
                ╚══════════════════════════════════════╝""");
        System.out.print("  Scelta: ");
    }

    // ─── Input helper ──────────────────────────────────────────────────────────

    /**
     * Legge un intero da console.
     *
     * ⚠ BUG #2: quando l'utente digita testo non numerico, nextInt() lancia
     *   InputMismatchException. Il catch gestisce l'eccezione e ritorna -1,
     *   MA non consuma il token non valido dal buffer (manca scanner.nextLine()).
     *   Al giro successivo del while, nextInt() ritrova lo stesso token
     *   e rilancia l'eccezione → loop infinito di messaggi di errore.
     */
    private int readInt() {
        try {
            return scanner.nextInt();
        } catch (InputMismatchException e) {
            System.out.println("  ⚠ Input non valido! Inserisci un numero intero.");
            scanner.nextLine(); // BUG #2: manca scanner.nextLine() per consumare il token non valido
            return -1;
        }
    }

    // ─── Operazioni sui conti ──────────────────────────────────────────────────

    private void listAccounts() {
        System.out.println("\n─── LISTA CONTI ─────────────────────────────────────────");
        if (accounts.isEmpty()) {
            System.out.println("  Nessun conto presente.");
            return;
        }
        for (int i = 0; i < accounts.size(); i++) {
            System.out.printf("  %d. %s%n", i + 1, accounts.get(i));
        }
    }

    private void showBalance() {
        Account account = selectAccount("Seleziona il conto di cui vuoi vedere il saldo");
        if (account == null) return;
        System.out.printf("%n  Saldo attuale di [%s]: %.2f€%n",
                account.getIban(), account.getBalance());
    }

    private void makeDeposit() {
        Account account = selectAccount("Seleziona il conto su cui depositare");
        if (account == null) return;

        System.out.print("  Importo da depositare (es: 500.00): ");
        scanner.nextLine(); // consuma il newline rimasto dopo nextInt()
        String raw = scanner.nextLine().trim();
        try {
            BigDecimal amount = new BigDecimal(raw.replace(",", "."));
            account.deposit(amount);
            System.out.printf("  ✓ Depositati %.2f€. Nuovo saldo: %.2f€%n",
                    amount, account.getBalance());
        } catch (Exception e) {
            System.out.println("  Errore: " + e.getMessage());
        }
    }

    private void makeWithdrawal() {
        Account account = selectAccount("Seleziona il conto da cui prelevare");
        if (account == null) return;

        System.out.print("  Importo da prelevare (es: 200.00): ");
        scanner.nextLine(); // consuma il newline rimasto dopo nextInt()
        String raw = scanner.nextLine().trim();
        try {
            BigDecimal amount = new BigDecimal(raw.replace(",", "."));
            account.withdraw(amount);
            System.out.printf("  ✓ Prelevati %.2f€. Nuovo saldo: %.2f€%n",
                    amount, account.getBalance());
        } catch (Exception e) {
            System.out.println("  Errore: " + e.getMessage());
        }
    }

    private void showTransactions() {
        Account account = selectAccount("Seleziona il conto di cui vedere le transazioni");
        if (account == null) return;

        var txs = account.getTransactions();
        System.out.printf("%n─── TRANSAZIONI [%s] ────────────────────────────────%n",
                account.getIban());
        if (txs.isEmpty()) {
            System.out.println("  Nessuna transazione registrata.");
        } else {
            txs.forEach(t -> System.out.println("  " + t));
        }
    }

    private void applyInterestToSavings() {
        System.out.println("\n─── APPLICA INTERESSI ───────────────────────────────────");
        boolean found = false;
        for (Account account : accounts) {
            if (account instanceof SavingsAccount sa) {
                BigDecimal before = sa.getBalance();
                sa.applyInterest();                // ← chiama il metodo con BUG #1
                BigDecimal after = sa.getBalance();
                System.out.printf("  Conto [%s] | tasso %.2f%% | saldo prima: %.2f€ | saldo dopo: %.2f€%n",
                        sa.getIban(), sa.getInterestRate(), before, after);
                found = true;
            }
        }
        if (!found) {
            System.out.println("  Nessun conto di risparmio trovato.");
        }
    }

    /**
     * Classifica e stampa informazioni dettagliate su ogni conto.
     *
     * ⚠ BUG #3: getAccountTypeInfo() riceve un Object e controlla instanceof
     *   nell'ordine sbagliato: prima Account (superclasse) poi SavingsAccount
     *   e CheckingAccount (sottoclassi). Siccome ogni SavingsAccount o
     *   CheckingAccount È anche un Account, il primo ramo cattura sempre tutto
     *   e i rami specifici non vengono mai raggiunti.
     */
    private void processAccounts() {
        System.out.println("\n─── PROCESSING CONTI ────────────────────────────────────");
        for (Account account : accounts) {
            String info = getAccountTypeInfo(account);
            System.out.println("  " + info);
        }
    }

    /**
     * ⚠ BUG #3 — instanceof nell'ordine SBAGLIATO.
     *
     * Il parametro è Object affinché il compilatore non rilevi staticamente
     * l'irraggiungibilità dei rami. A runtime, qualsiasi Account (incluse le
     * sottoclassi) entra sempre nel primo ramo; i rami SavingsAccount e
     * CheckingAccount non vengono mai eseguiti.
     */
    private String getAccountTypeInfo(Object accountObj) {
        if (accountObj instanceof Account a) {
            // BUG #3: questo ramo cattura TUTTI gli Account, incluse le sottoclassi
            return String.format("Tipo: [GENERICO]   IBAN: %s — saldo: %.2f€",
                    a.getIban(), a.getBalance());

        } else if (accountObj instanceof SavingsAccount sa) {
            // Questo ramo non viene mai raggiunto!
            return String.format("Tipo: [RISPARMIO]  IBAN: %s — tasso: %.2f%% — saldo: %.2f€",
                    sa.getIban(), sa.getInterestRate(), sa.getBalance());

        } else if (accountObj instanceof CheckingAccount ca) {
            // Questo ramo non viene mai raggiunto!
            return String.format("Tipo: [CORRENTE]   IBAN: %s — scoperto: %.2f€ — saldo: %.2f€",
                    ca.getIban(), ca.getOverdraftLimit(), ca.getBalance());
        }
        return "Tipo: [SCONOSCIUTO]";
    }

    // ─── Utility ───────────────────────────────────────────────────────────────

    private Account selectAccount(String prompt) {
        System.out.println("\n  " + prompt + ":");
        listAccounts();
        System.out.print("  Numero conto: ");
        int idx = readInt();
        if (idx < 1 || idx > accounts.size()) {
            System.out.println("  Selezione non valida.");
            return null;
        }
        return accounts.get(idx - 1);
    }

    // ─── Dati di esempio ───────────────────────────────────────────────────────

    private void initializeSampleData() {
        Customer mario = new Customer(
                "RSSMRA85M01H501Z", "Mario", "Rossi", CustomerType.PRIVATE);
        Customer anna = new Customer(
                "BNCNNA90L50C351X", "Anna", "Bianchi", CustomerType.BUSINESS);

        CheckingAccount cc = new CheckingAccount(
                "IT60 X054 2811 1010 0000 0123 456",
                new BigDecimal("5000.00"),
                mario,
                new BigDecimal("1000.00"));

        SavingsAccount cs = new SavingsAccount(
                "IT60 X054 2811 1010 0000 0654 321",
                new BigDecimal("10000.00"),
                anna,
                2.5);   // 2.5% annuo

        // alcune transazioni iniziali
        cc.deposit(new BigDecimal("500.00"));
        cc.withdraw(new BigDecimal("200.00"));
        cs.deposit(new BigDecimal("1000.00"));

        accounts.add(cc);
        accounts.add(cs);
    }
}
