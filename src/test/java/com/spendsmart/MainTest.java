package com.spendsmart;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @Test
    void balancedGroupGivesCorrectPayments() {
        Map<String, BigDecimal> balances = new HashMap<>();
        balances.put("Dev", new BigDecimal("50"));
        balances.put("Fay", new BigDecimal("-20"));
        balances.put("Gus", new BigDecimal("-30"));

        List<Main.Payment> result = Main.settle(balances);

        assertEquals(2, result.size());
        assertEquals(
                new Main.Payment("Fay", "Dev", new BigDecimal("20")),
                result.get(0));
        assertEquals(
                new Main.Payment("Gus", "Dev", new BigDecimal("30")),
                result.get(1));
    }

    @Test
    void unbalancedGroupThrows() {
        Map<String, BigDecimal> balances = new HashMap<>();
        balances.put("Dev", new BigDecimal("50"));
        balances.put("Fay", new BigDecimal("-20"));

        assertThrows(IllegalArgumentException.class, () -> Main.settle(balances));
    }

    @Test
    void zeroBalanceIsIgnored() {
        Map<String, BigDecimal> balances = new HashMap<>();
        balances.put("Dev", new BigDecimal("50"));
        balances.put("Fay", new BigDecimal("-20"));
        balances.put("Gus", new BigDecimal("-30"));
        balances.put("Hana", new BigDecimal("0"));

        List<Main.Payment> result = Main.settle(balances);

        for (Main.Payment p : result) {
            assertNotEquals("Hana", p.debtor());
            assertNotEquals("Hana", p.creditor());
        }
    }

    @Test
    void sevenPersonCase() {
        List<BigDecimal> shares = Main.splitEvenly(new BigDecimal("10.00"),
                List.of("A", "B", "C", "D", "E", "F", "G"));

        assertEquals(7, shares.size());
        assertEquals(new BigDecimal("1.48"), shares.get(0));
        assertEquals(new BigDecimal("1.42"), shares.get(1));
    }

    @Test
    void sharesAddUpToTotal() {
        List<BigDecimal> shares = Main.splitEvenly(new BigDecimal("10.00"),
                List.of("A", "B", "C", "D", "E", "F", "G"));

        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal p : shares) {
            sum = sum.add(p);
        }

        assertEquals(new BigDecimal("10.00"), sum);
    }

    @Test
    void emptyListThrows() {
        assertThrows(IllegalArgumentException.class, () -> Main.splitEvenly(new BigDecimal("10.00"),
                new ArrayList<>()));
    }

    @Test
    void calculateBalancesForTwoExpenses() {
        List<String> everyone = List.of("Ana", "Ben", "Cara");
        List<Main.Expense> expenses = List.of(
                new Main.Expense("Ana", new BigDecimal("30.00"), everyone),
                new Main.Expense("Ben", new BigDecimal("60.00"), everyone));

        Map<String, BigDecimal> balances = Main.calculateBalances(expenses);

        assertEquals(new BigDecimal("0.00"), balances.get("Ana"));
        assertEquals(new BigDecimal("30.00"), balances.get("Ben"));
        assertEquals(new BigDecimal("-30.00"), balances.get("Cara"));
    }

    @Test
    void unevenExpenseStillSumsToZero() {
        List<Main.Expense> expenses = List.of(
                new Main.Expense("Cara", new BigDecimal("10.00"),
                        List.of("Ana", "Ben", "Cara")));

        Map<String, BigDecimal> balances = Main.calculateBalances(expenses);

        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal p : balances.values()) {
            sum = sum.add(p);
        }

        assertEquals(new BigDecimal("0.00"), sum);
    }

    @Test
    void expensesToPaymentsPipeline() {
        List<String> everyone = List.of("Ana", "Ben", "Cara");
        List<Main.Expense> expenses = List.of(
                new Main.Expense("Ana", new BigDecimal("30.00"), everyone),
                new Main.Expense("Ben", new BigDecimal("60.00"), everyone));

        Map<String, BigDecimal> balances = Main.calculateBalances(expenses);
        List<Main.Payment> payments = Main.settle(balances);

        assertEquals(1, payments.size());
        assertEquals(new Main.Payment("Cara", "Ben", new BigDecimal("30.00")),
                payments.get(0));
    }
}