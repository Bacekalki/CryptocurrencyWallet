package bg.sofia.uni.fmi.mjt.crypto.user;

import bg.sofia.uni.fmi.mjt.crypto.exception.MissingCryptoException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Wallet implements Serializable {
    private final Map<String, Double> investments;

    public Wallet() {
        investments = new HashMap<>();
    }

    public Wallet(Map<String, Double> investments) {
        this.investments = investments;
    }

    public double getAmount(String assetId) throws MissingCryptoException {
        if (assetId == null || assetId.isBlank()) {
            throw new IllegalArgumentException("AssetId can't be null");
        }

        if (!investments.containsKey(assetId)) {
            throw new MissingCryptoException("You don't have this crypto");
        }

        return investments.get(assetId);
    }

    public Map<String, Double> getInvestments() {
        return investments;
    }

    public void addInvestment(String assetId, double amount) {
        if (assetId == null || assetId.isBlank()) {
            throw new IllegalArgumentException("AssetId can't be null or blank");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("You can't add negative amount");
        }

        investments.merge(assetId, amount, Double::sum);
    }

    public void sell(String assetId) {
        if (assetId == null || assetId.isBlank()) {
            throw new IllegalArgumentException("AssetId can't be null or blank");
        }

        investments.remove(assetId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{").append(System.lineSeparator());

        for (Map.Entry<String, Double> entry : investments.entrySet()) {
            sb.append("  ")
                .append("AssetId: ").append(entry.getKey())
                .append(", Amount: ").append(entry.getValue())
                .append(System.lineSeparator());
        }

        sb.append("}");

        return sb.toString();
    }

}
