# Expense Splitter
### SpendSmart

Java project that works out the fewest payments needed to settle shared expenses between a group of people

## How it works

Each person has a net balance. Debtors and creditors are held in two priority queues, ordered smallest amount first.
The algorithm repeatedly pays from the smallest debtor to the smallest creditor so every payment clears at least one person.
Group of n people needs at most n-1 payments to fully work out the amounts.

## Design decisions

- Money is stored as BigDecimal to avoid rounding errors.
- Balances must sum to zero. An IllegalArgumentException is thrown if they do not sum to zero.
- A person with a balance of zero is left out of the settlement.

Java 21, Maven, JUnit 5