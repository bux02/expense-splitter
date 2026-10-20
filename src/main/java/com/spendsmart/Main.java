package com.spendsmart;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class Main {
    record Balance(String name, BigDecimal amount) {}
    record Payment(String debtor, String creditor, BigDecimal amount) {}
    record Expense(String paidBy, BigDecimal amount, List<String> splitBetween) {}

    public static void main(String[] args) {


        Map<String, BigDecimal> balances = new HashMap<>();
        balances.put("Dev", new BigDecimal("50"));
        balances.put("Fay", new BigDecimal("-20"));
        balances.put("Gus", new BigDecimal("-30"));

        System.out.println(settle(balances));

        Map<String, BigDecimal> unbalanced = new HashMap<>();
        unbalanced.put("Dev", new BigDecimal("50"));
        unbalanced.put("Fay", new BigDecimal("-20"));

        try {
            settle(unbalanced);
        } catch (IllegalArgumentException e) {
            System.out.println("Caught: " + e.getMessage());
        }
    }

    public static Map<String, BigDecimal> calculateBalances(List<Expense> expenses) {
        Map<String, BigDecimal> balances = new HashMap<>();
        for (Expense expense : expenses) {
            //Credit
            BigDecimal current = balances.getOrDefault(expense.paidBy(), new BigDecimal("0.00"));
            balances.put(expense.paidBy(), current.add(expense.amount()));

            //Debt
            List<BigDecimal> shares = splitEvenly(expense.amount(),
                    expense.splitBetween());
            for (int i = 0; i < expense.splitBetween().size(); i++) {
                String person = expense.splitBetween().get(i);
                BigDecimal personBalance = balances.getOrDefault(person, new BigDecimal("0.00"));
                balances.put(person, personBalance.subtract(shares.get(i)));
            }
        }
        return balances;
    }

    //Splits the balance evenly between all people
    //If it doesnt divide evenly the leftover amount goes to the first person
    public static List<BigDecimal> splitEvenly(BigDecimal total, List<String> people) {

        if (people.isEmpty()) {
            throw new IllegalArgumentException("There must be at least one person");
        }

        BigDecimal totalPerPerson = total.divide(BigDecimal.valueOf(people.size()), 2,
                RoundingMode.DOWN);

        BigDecimal fullTotal = totalPerPerson.multiply(BigDecimal.valueOf(people.size()));

        BigDecimal remainder = total.subtract(fullTotal);

        List<BigDecimal> shares = new ArrayList<>();

        for (int i = 0; i < people.size(); i++) {
            if (i != 0) {
                shares.add(totalPerPerson);
            }
            else {
                shares.add(totalPerPerson.add(remainder));
            }
        }

        return shares;
    }

    public static List<Payment> settle(Map<String, BigDecimal> balances) {
        BigDecimal total = BigDecimal.ZERO;

        PriorityQueue<Balance> debtors =
                new PriorityQueue<>(Comparator.comparing(Balance::amount).reversed());

        PriorityQueue<Balance> creditors =
                new PriorityQueue<>(Comparator.comparing(Balance::amount));

        ArrayList<Payment> payments = new ArrayList<>();

        for (Map.Entry<String, BigDecimal> entry : balances.entrySet()) {
            total = total.add(entry.getValue());
        }

        //if balance is negative put it in debtors, if its positive put it in creditors
        for (Map.Entry<String, BigDecimal> entry : balances.entrySet()) {
            if (entry.getValue().signum() < 0) {
                debtors.add(new Balance(entry.getKey(), entry.getValue()));
            }
            else if (entry.getValue().signum() > 0) {
                creditors.add(new Balance(entry.getKey(), entry.getValue()));
            }
        }

        if (total.signum() != 0) {
            throw new IllegalArgumentException("Balances must sum to zero, but currently " +
                    "sum to " + total);
        }

        //loop for paying creditors from the debtors
        //flips the debt to a positive number and stores it, then adds it to the debt
        //and subtracts from the credit to cancel them out
        //if a debt or credit is left over it adds the entry back to the queue
        while (!debtors.isEmpty() && !creditors.isEmpty()) {
            Balance debtor = debtors.poll();
            Balance creditor = creditors.poll();

            BigDecimal paid = debtor.amount().negate().min(creditor.amount());

            payments.add(new Payment(debtor.name(), creditor.name(), paid));

            BigDecimal debtorLeft = debtor.amount().add(paid);
            BigDecimal creditorLeft = creditor.amount().subtract(paid);

            if (debtorLeft.signum() != 0) {
                debtors.add(new Balance(debtor.name(), debtorLeft));
            }

            if (creditorLeft.signum() != 0) {
                creditors.add(new Balance(creditor.name(), creditorLeft));
            }
        }
        return payments;
    }
}