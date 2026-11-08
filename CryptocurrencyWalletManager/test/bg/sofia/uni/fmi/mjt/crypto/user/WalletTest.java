package bg.sofia.uni.fmi.mjt.crypto.user;

import bg.sofia.uni.fmi.mjt.crypto.exception.MissingCryptoException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class WalletTest {
    private static Map<String, Double> map = new HashMap<>();
    private static Wallet wallet;
    private static final String expectedToString = "{" + System.lineSeparator() +
        "  AssetId: BTC, Amount: 5.0" + System.lineSeparator() +
        "  AssetId: BNB, Amount: 7.0" + System.lineSeparator() +
        "}";

    @BeforeAll
    static void setUp() {
        map.put("BTC", 7.0);
        map.put("ETH", 6.0);
        map.put("BNB", 5.0);
        wallet = new Wallet(map);
    }

    @Test
    void testGetAmountWithNullAssetId() {
        assertThrows(IllegalArgumentException.class, () -> wallet.getAmount(null));
    }

    @Test
    void testGetAmountWithMissingCrypto() {
        assertThrows(MissingCryptoException.class, () -> wallet.getAmount("ABC"));
    }

    @Test
    void testGetAmount() throws MissingCryptoException {
        assertEquals(6.0 ,wallet.getAmount("ETH"));
    }

    @Test
    void testAddInvestmentWithBlankAssetId() {
        assertThrows(IllegalArgumentException.class, () -> wallet.addInvestment("  ", 3.0));
    }

    @Test
    void testAddInvestmentWithNegativeAmount() {
        assertThrows(IllegalArgumentException.class, () -> wallet.addInvestment("BTC", -3.0));
    }

    @Test
    void testAddInvestment() throws MissingCryptoException {
        wallet.addInvestment("BTC", 2.0);
        assertEquals(9.0, wallet.getAmount("BTC"));
    }

    @Test
    void testSellWithNullAssetId() {
        assertThrows(IllegalArgumentException.class, () -> wallet.sell(null));
    }

    @Test
    void testSell() {
        wallet.sell("BNB");
        assertEquals(2, map.entrySet().size());
    }

    @Test
    void testToString() {
        Wallet w = new Wallet();
        w.addInvestment("BTC", 5.0);
        w.addInvestment("BNB", 7.0);
        assertEquals(expectedToString, w.toString());
    }

}
