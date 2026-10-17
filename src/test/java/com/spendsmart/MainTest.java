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
}