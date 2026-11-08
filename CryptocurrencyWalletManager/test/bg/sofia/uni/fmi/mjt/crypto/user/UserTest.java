package bg.sofia.uni.fmi.mjt.crypto.user;

import bg.sofia.uni.fmi.mjt.crypto.exception.InsufficientBalanceException;
import bg.sofia.uni.fmi.mjt.crypto.exception.MissingCryptoException;
import bg.sofia.uni.fmi.mjt.crypto.exception.NegativeMoneyException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserTest {
    private static final User testUser = new User("Username", "password");
    private static final String expectedToString = "User{username='dummy', balance=30.0, wallet={" + System.lineSeparator() +
        "  AssetId: BTC, Amount: 7.0" + System.lineSeparator() +
        "}, hashedPassword='b5a2c96250612366ea272ffac6d9744aaf4b45aacd96aa7cfcb931ee3b558259', history=[Purchase[assetId=BTC, amountOfCrypto=7.0, price=10.0]]}";

    @Test
    void testDepositWithNegativeAmount() {
        assertThrows(NegativeMoneyException.class, () -> testUser.deposit(-90));
    }

    @Test
    void testDeposit() throws NegativeMoneyException {
        testUser.deposit(50);
        assertEquals(50, testUser.getBalance());
    }

    @Test
    void testBuyStockWithNegativeAmount() {
        assertThrows(NegativeMoneyException.class, () -> testUser.buyStock("BTC", -90, 9));
    }

    @Test
    void testBuyStockWithNegativePrice() {
        assertThrows(NegativeMoneyException.class, () -> testUser.buyStock("BTC", 90, -9));
    }

    @Test
    void testBuyStockWithInsufficientBalance() {
        assertThrows(InsufficientBalanceException.class, () -> testUser.buyStock("BTC", 90, 9));
    }

    @Test
    void testBuyStock() throws NegativeMoneyException, InsufficientBalanceException, MissingCryptoException {
        User user = new User("dummy", "dummy");
        user.deposit(100);
        user.buyStock("BTC", 70, 10);
        assertEquals(30, user.getBalance());
        assertEquals(7, user.getWallet().getAmount("BTC"));
        assertEquals(1, user.getHistory().size());
    }

    @Test
    void testSellStockWithNegativePrice() {
        assertThrows(NegativeMoneyException.class, () -> testUser.sellStock("BTC", -90));
    }

    @Test
    void testSellStockWithMissingCrypto() {
        assertThrows(MissingCryptoException.class, () -> testUser.sellStock("BTC", 90));
    }

    @Test
    void testSellStock() throws NegativeMoneyException, InsufficientBalanceException, MissingCryptoException {
        User user = new User("dummy", "dummy");
        user.deposit(100);
        user.buyStock("BTC", 70, 10);
        user.sellStock("BTC", 5);
        assertEquals(0, user.getHistory().size());
        assertThrows(MissingCryptoException.class, () -> user.getWallet().getAmount("BTC"));
        assertEquals(65, user.getBalance());
    }

    @Test
    void testToString() throws NegativeMoneyException, InsufficientBalanceException {
        User user = new User("dummy", "dummy");
        user.deposit(100);
        user.buyStock("BTC", 70, 10);
        assertEquals(expectedToString, user.toString());
    }

    @Test
    void testEquals() {
        User user = new User("dummy", "dummy");
        User user2 = new User("dummy", "dummyTRY");
        assertTrue(user.equals(user2));
    }

}
