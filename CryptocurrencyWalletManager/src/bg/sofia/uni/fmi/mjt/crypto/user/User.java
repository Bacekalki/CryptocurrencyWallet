package bg.sofia.uni.fmi.mjt.crypto.user;

import bg.sofia.uni.fmi.mjt.crypto.exception.InsufficientBalanceException;
import bg.sofia.uni.fmi.mjt.crypto.exception.MissingCryptoException;
import bg.sofia.uni.fmi.mjt.crypto.exception.NegativeMoneyException;
import bg.sofia.uni.fmi.mjt.crypto.hasher.PasswordHasher;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class User implements Serializable {
    @Serial
    private static final long serialVersionUID = 4109019801707400433L;
    private final String username;
    private double balance;
    private final Wallet wallet;
    private final String hashedPassword;
    private static final PasswordHasher hasher = new PasswordHasher();
    private final List<Purchase> history;

    public User(String username, String password) {
        this.username = username;
        this.wallet = new Wallet();
        this.balance = 0;
        this.hashedPassword = hasher.hashPassword(password);
        history = new ArrayList<>();
    }

    public String getUsername() {
        return username;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public double getBalance() {
        return balance;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public List<Purchase> getHistory() {
        return history;
    }

    public void deposit(double amount) throws NegativeMoneyException {
        if (amount < 0) {
            throw new NegativeMoneyException("You can't deposit negative money");
        }

        balance += amount;
    }

    public void buyStock(String assetId, double amount, double price)
        throws NegativeMoneyException, InsufficientBalanceException {
        if (amount < 0) {
            throw new NegativeMoneyException("You can't buy crypto with negative money");
        }
        if (price <= 0) {
            throw new NegativeMoneyException("Price can't be negative");
        }
        if (amount > balance) {
            throw new InsufficientBalanceException("You don't have enough money for this transaction");
        }

        double amountOfCrypto = amount / price;
        wallet.addInvestment(assetId, amountOfCrypto);
        history.add((new Purchase(assetId, amountOfCrypto, price)));
        balance -= amount;
    }

    public void sellStock(String assetId, double price) throws MissingCryptoException, NegativeMoneyException {
        if (price < 0) {
            throw new NegativeMoneyException("Price can't be negative");
        }
        if (!wallet.getInvestments().containsKey(assetId)) {
            throw new MissingCryptoException("You don't have that crypto");
        }

        history.removeIf(purchase -> purchase.assetId().equals(assetId));
        double amount = wallet.getAmount(assetId);
        wallet.sell(assetId);
        balance += (amount * price);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("User{")
            .append("username='").append(username).append('\'')
            .append(", balance=").append(balance)
            .append(", wallet=").append(wallet)
            .append(", hashedPassword='").append(hashedPassword).append('\'')
            .append(", history=[");

        for (Purchase purchase : history) {
            stringBuilder.append(purchase).append(", ");
        }

        if (!history.isEmpty()) {
            stringBuilder.setLength(stringBuilder.length() - 2);
        }

        stringBuilder.append("]}");
        return stringBuilder.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (this.getClass() != other.getClass()) {
            return false;
        }

        return username.equals(((User) other).getUsername());
    }

}
