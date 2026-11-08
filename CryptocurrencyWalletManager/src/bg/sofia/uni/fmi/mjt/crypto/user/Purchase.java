package bg.sofia.uni.fmi.mjt.crypto.user;

import java.io.Serializable;

public record Purchase(String assetId, double amountOfCrypto, double price) implements Serializable {
}
