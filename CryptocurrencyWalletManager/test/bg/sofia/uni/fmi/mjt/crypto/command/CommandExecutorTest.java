package bg.sofia.uni.fmi.mjt.crypto.command;

import bg.sofia.uni.fmi.mjt.crypto.chart.PriceChart;
import bg.sofia.uni.fmi.mjt.crypto.chart.PriceChartAPI;
import bg.sofia.uni.fmi.mjt.crypto.datamanager.DataManager;
import bg.sofia.uni.fmi.mjt.crypto.datamanager.DataManagerAPI;
import bg.sofia.uni.fmi.mjt.crypto.exception.InsufficientBalanceException;
import bg.sofia.uni.fmi.mjt.crypto.exception.MissingCryptoException;
import bg.sofia.uni.fmi.mjt.crypto.exception.NegativeMoneyException;
import bg.sofia.uni.fmi.mjt.crypto.user.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CommandExecutorTest {

    private static PriceChartAPI priceChart = mock();
    private static DataManagerAPI manager = mock();
    private static CommandExecutor commandExecutor = new CommandExecutor(priceChart, null, manager);
    private static CommandExecutorAPI mockedExecutor;
    private static final String INVALID_NUMBER_OF_ARGUMENTS_MESSAGE = "Wrong number of arguments";
    private static final String INVALID_ASSET_ID_MESSAGE = "Invalid assetId";
    private static final String INVALID_NUMBER_FORMAT_MESSAGE = "Amount of money must be a number";
    private static final String INVALID_PASSWORD_MESSAGE =
        "Your password must be at least 7 characters long and must contain at least 1 uppercase letter, " +
            "1 lowercase and 1 digit";
    private static final String VALID_PASSWORD = "Password1";
    private static final String SUCCESSFUL_BUY_MESSAGE = "Bought successfully";
    private static final String SUCCESSFUL_SELL_MESSAGE = "Sold successfully";
    private static final String SUCCESSFUL_LOGIN_MESSAGE = "Logged in successfully";
    private static final String SUCCESSFUL_DEPOSIT_MESSAGE = "Deposited successfully";
    private static final String SUCCESSFUL_REGISTRATION_MESSAGE = "Registration successful";
    private static final String SUCCESSFUL_LOGOUT_MESSAGE = "Logged out successfully";
    private static final String NOT_LOGGED_IN_MESSAGE = "You are not logged in.";
    private static final String USER_ALREADY_LOGGED_MESSAGE = "User already logged";
    private static final String USER_NOT_FOUND_MESSAGE = "User not found";
    private static final String LOGIN_FAILED_MESSAGE = "Wrong username or password";
    private static final String USER_ALREADY_EXISTS_MESSAGE = "Such user already exists";
    private static final String BUY = "buy";
    private static final String SELL = "sell";
    private static final String LIST_OFFERINGS = "list-offerings";
    private static final String DEPOSIT = "deposit";
    private static final String WALLET_SUMMARY = "wallet-summary";
    private static final String WALLET_OVERALL_SUMMARY = "wallet-overall-summary";
    private static final String LOGIN = "login";
    private static final String LOGOUT = "logout";
    private static final String REGISTER = "register";
    private static final String QUIT = "quit";
    private static final String expectedToStringWalletOverallSummary = "Your total winnings are : " + "{" + System.lineSeparator() +
        "  AssetId: BTC, MoneyWon: 1000.0" + System.lineSeparator() +
        "  AssetId: SPEX, MoneyWon: -160.0" + System.lineSeparator() +
        "  AssetId: BNB, MoneyWon: 0.0" + System.lineSeparator() +
        "}";

    @Test
    void testBuyStockInvalidArgumentsCount() {
        String[] args = {"fake"};
        Command cmd = new Command(BUY, args);
        assertEquals(INVALID_NUMBER_OF_ARGUMENTS_MESSAGE, commandExecutor.execute("", cmd));
    }

    @Test
    void testBuyStockInvalidAssetId() {
        String[] args = {"BTC", "20"};
        Command cmd = new Command(BUY, args);
        mockedExecutor = new CommandExecutor(priceChart, null, manager);
        when(priceChart.getAvailableCryptos()).thenReturn(Set.of());
        assertEquals(INVALID_ASSET_ID_MESSAGE, mockedExecutor.execute("", cmd));
    }

    @Test
    void testBuyStockInvalidNumberFormat() {
        String[] args = {"BTC", "BTC"};
        Command cmd = new Command(BUY, args);
        mockedExecutor = new CommandExecutor(priceChart, null, manager);
        when(priceChart.getAvailableCryptos()).thenReturn(Set.of("BTC"));
        assertEquals(INVALID_NUMBER_FORMAT_MESSAGE, mockedExecutor.execute("", cmd));
    }

    @Test
    void testBuyStockSuccessful() throws NegativeMoneyException {
        String[] args = {"BTC", "20"};
        Command cmd = new Command(BUY, args);
        User jim = new User("Jim", "pass");
        jim.deposit(30);
        Map<String, User> map = Map.of(jim.getUsername(), jim);
        mockedExecutor = new CommandExecutor(priceChart, map, manager);
        when(priceChart.getAvailableCryptos()).thenReturn(Set.of("BTC"));
        when(priceChart.getPrice("BTC")).thenReturn(20.0);
        assertEquals(SUCCESSFUL_BUY_MESSAGE, mockedExecutor.execute("Jim", cmd));
    }

    @Test
    void testBuyStockUserNotLoggedIn() {
        String[] args = {"BTC", "20"};
        Command cmd = new Command(BUY, args);
        mockedExecutor = new CommandExecutor(priceChart, Map.of(), manager);
        when(priceChart.getAvailableCryptos()).thenReturn(Set.of("BTC"));
        assertEquals(NOT_LOGGED_IN_MESSAGE, mockedExecutor.execute("Jim", cmd));
    }

    @Test
    void testSellStockWithInvalidNumberOfArguments() {
        String[] args = {};
        Command cmd = new Command(SELL, args);
        assertEquals(INVALID_NUMBER_OF_ARGUMENTS_MESSAGE, commandExecutor.execute("", cmd));
    }

    @Test
    void testSellStockInvalidAssetId() {
        String[] args = {"BTC"};
        Command cmd = new Command(SELL, args);
        mockedExecutor = new CommandExecutor(priceChart, null, manager);
        when(priceChart.getAvailableCryptos()).thenReturn(Set.of());
        assertEquals(INVALID_ASSET_ID_MESSAGE, mockedExecutor.execute("", cmd));
    }

    @Test
    void testSellStockMissingCrypto() {
        String[] args = {"BTC"};
        Command cmd = new Command(SELL, args);
        when(priceChart.getPrice("BTC")).thenReturn(20.0);
        when(priceChart.getAvailableCryptos()).thenReturn(Set.of("BTC"));
        mockedExecutor = new CommandExecutor(priceChart, Map.of("jim", new User("jim", "jks")), manager);
        assertEquals("You don't have that crypto", mockedExecutor.execute("jim", cmd));
    }

    @Test
    void testSellStockSuccessful() throws NegativeMoneyException, InsufficientBalanceException {
        String[] args = {"BTC"};
        Command cmd = new Command(SELL, args);
        User jim = new User("Jim", "pass");
        jim.deposit(30);
        jim.buyStock("BTC", 10, 2);
        Map<String, User> map = Map.of("Jim", jim);
        mockedExecutor = new CommandExecutor(priceChart, map, manager);
        when(priceChart.getAvailableCryptos()).thenReturn(Set.of("BTC"));
        assertEquals(SUCCESSFUL_SELL_MESSAGE, mockedExecutor.execute("Jim", cmd));
    }

    @Test
    void testSellStockUserNotLoggedIn() throws NegativeMoneyException {
        String[] args = {"BTC"};
        Command cmd = new Command(SELL, args);
        User jim = new User("Jim", "pass");
        jim.deposit(30);
        mockedExecutor = new CommandExecutor(priceChart, Map.of(), manager);
        when(priceChart.getAvailableCryptos()).thenReturn(Set.of("BTC"));
        assertEquals(NOT_LOGGED_IN_MESSAGE, mockedExecutor.execute("dummy", cmd));
    }

    @Test
    void testLoginWithInvalidArgumentsCount() {
        String[] args = {};
        Command cmd = new Command(LOGIN, args);
        assertEquals(INVALID_NUMBER_OF_ARGUMENTS_MESSAGE, commandExecutor.execute("", cmd));
    }

    @Test
    void testLoginWithAlreadyLoggedUser() {
        String[] args = {"Jim", "pass"};
        Command cmd = new Command(LOGIN, args);
        mockedExecutor = new CommandExecutor(priceChart, Map.of("Jim", new User("Jim", "fde")), manager);
        assertEquals(USER_ALREADY_LOGGED_MESSAGE, mockedExecutor.execute("dummy", cmd));
    }

    @Test
    void testLoginWithNonExistingUser() {
        String[] args = {"Jim", "pass"};
        Command cmd = new Command(LOGIN, args);
        mockedExecutor = new CommandExecutor(priceChart, Map.of(), manager);
        when(manager.getData(any(Path.class))).thenReturn(null);
        assertEquals(USER_NOT_FOUND_MESSAGE, mockedExecutor.execute("dummy", cmd));
    }

    @Test
    void testLoginWithInvalidUsernameOrPassword() {
        String[] args = {"Jim", "pass"};
        Command cmd = new Command(LOGIN, args);
        mockedExecutor = new CommandExecutor(priceChart, Map.of(), manager);
        when(manager.getData(any(Path.class))).thenReturn(new User("Jim", "pass1"));
        assertEquals(LOGIN_FAILED_MESSAGE, mockedExecutor.execute("dummy", cmd));
    }

    @Test
    void testLoginSuccessful() {
        String[] args = {"Jim", "pass"};
        Command cmd = new Command(LOGIN, args);
        mockedExecutor = new CommandExecutor(priceChart, new HashMap<>(), manager);
        when(manager.getData(any(Path.class))).thenReturn(new User("Jim", "pass"));
        assertEquals(SUCCESSFUL_LOGIN_MESSAGE, mockedExecutor.execute("dummy", cmd));
    }

    @Test
    void testRegisterWithInvalidArgumentsCount() {
        String[] args = {};
        Command cmd = new Command(REGISTER, args);
        assertEquals(INVALID_NUMBER_OF_ARGUMENTS_MESSAGE, commandExecutor.execute("", cmd));
    }

    @Test
    void testRegisterWithInvalidPassword() {
        String[] args = {"Jim", "Password"};
        Command cmd = new Command(REGISTER, args);
        assertEquals(INVALID_PASSWORD_MESSAGE, commandExecutor.execute("", cmd));
    }

    @Test
    void testRegisterWithAlreadyLoggedUser() {
        String[] args = {"Jim", VALID_PASSWORD};
        Command cmd = new Command(REGISTER, args);
        mockedExecutor = new CommandExecutor(priceChart, Map.of("Jim", new User("Jim", "fde")), manager);
        assertEquals(USER_ALREADY_LOGGED_MESSAGE, mockedExecutor.execute("dummy", cmd));
    }

    @Test
    void testRegisterWithExistingUser() {
        String[] args = {"Jim", VALID_PASSWORD};
        Command cmd = new Command(REGISTER, args);
        mockedExecutor = new CommandExecutor(priceChart, Map.of(), manager);
        when(manager.exists(any(Path.class))).thenReturn(true);
        assertEquals(USER_ALREADY_EXISTS_MESSAGE, mockedExecutor.execute("dummy", cmd));
    }

    @Test
    void testRegistrationSuccessful() {
        String[] args = {"Jim", VALID_PASSWORD};
        Command cmd = new Command(REGISTER, args);
        mockedExecutor = new CommandExecutor(priceChart, new HashMap<>(), manager);
        when(manager.exists(any(Path.class))).thenReturn(false);
        assertEquals(SUCCESSFUL_REGISTRATION_MESSAGE, mockedExecutor.execute("dummy", cmd));
    }

    @Test
    void testListOfferingsWithInvalidArgumentsCount() {
        String[] args = {"dummy"};
        Command cmd = new Command(LIST_OFFERINGS, args);
        assertEquals(INVALID_NUMBER_OF_ARGUMENTS_MESSAGE, commandExecutor.execute("", cmd));
    }

    @Test
    void testDepositMoneyWithInvalidArgumentsCount() {
        String[] args = {};
        Command cmd = new Command(DEPOSIT, args);
        assertEquals(INVALID_NUMBER_OF_ARGUMENTS_MESSAGE, commandExecutor.execute("", cmd));
    }

    @Test
    void testDepositMoneyWithInvalidNumberFormatMoney() {
        String[] args = {"abc"};
        Command cmd = new Command(DEPOSIT, args);
        assertEquals(INVALID_NUMBER_FORMAT_MESSAGE, commandExecutor.execute("", cmd));
    }

    @Test
    void testSuccessfulDeposit() {
        String[] args = {"19.5"};
        Command cmd = new Command(DEPOSIT, args);
        Map<String, User> map = Map.of("Jim", new User("Jim", "12345"));
        mockedExecutor = new CommandExecutor(priceChart, map, manager);
        assertEquals(SUCCESSFUL_DEPOSIT_MESSAGE, mockedExecutor.execute("Jim", cmd));
    }

    @Test
    void testDepositNotLoggedIn() {
        String[] args = {"19.5"};
        Command cmd = new Command(DEPOSIT, args);
        mockedExecutor = new CommandExecutor(priceChart, Map.of(), manager);
        assertEquals(NOT_LOGGED_IN_MESSAGE, mockedExecutor.execute("Jim", cmd));
    }

    @Test
    void testWalletSummaryWithInvalidArgumentsCount() {
        String[] args = {"dummy"};
        Command cmd = new Command(WALLET_SUMMARY, args);
        assertEquals(INVALID_NUMBER_OF_ARGUMENTS_MESSAGE, commandExecutor.execute("", cmd));
    }

    @Test
    void testGetWalletSummaryNotLoggedIn() {
        String[] args = {};
        Command cmd = new Command(WALLET_SUMMARY, args);
        mockedExecutor = new CommandExecutor(priceChart, Map.of(), manager);
        assertEquals(NOT_LOGGED_IN_MESSAGE, mockedExecutor.execute("Jim", cmd));
    }

    @Test
    void testGetOverallWalletSummaryWithInvalidArgumentsCount() {
        String[] args = {"dummy"};
        Command cmd = new Command(WALLET_OVERALL_SUMMARY, args);
        assertEquals(INVALID_NUMBER_OF_ARGUMENTS_MESSAGE, commandExecutor.execute("", cmd));
    }

    @Test
    void testGetOverallWalletSummarySuccessful() throws NegativeMoneyException, InsufficientBalanceException {
        String[] args = {};
        Command cmd = new Command(WALLET_OVERALL_SUMMARY, args);
        User jim = new User("Jim", "pass");
        jim.deposit(2000);
        jim.buyStock("BTC", 1000, 10);
        jim.buyStock("SPEX", 200, 5);
        jim.buyStock("BNB", 300, 15);
        when(priceChart.getPrice("BTC")).thenReturn(20.0);
        when(priceChart.getPrice("SPEX")).thenReturn(1.0);
        when(priceChart.getPrice("BNB")).thenReturn(15.0);
        mockedExecutor = new CommandExecutor(priceChart, Map.of("Jim", jim), manager);
        assertEquals(expectedToStringWalletOverallSummary, mockedExecutor.execute("Jim", cmd));
    }

    @Test
    void testGetOverallWalletSummaryNotLoggedIn() {
        String[] args = {};
        Command cmd = new Command(WALLET_OVERALL_SUMMARY, args);
        mockedExecutor = new CommandExecutor(priceChart, Map.of(), manager);
        assertEquals(NOT_LOGGED_IN_MESSAGE, mockedExecutor.execute("Jim", cmd));
    }

    @Test
    void testLogoutWithInvalidArgumentsCount() {
        String[] args = {"dummy"};
        Command cmd = new Command(LOGOUT, args);
        assertEquals(INVALID_NUMBER_OF_ARGUMENTS_MESSAGE, commandExecutor.execute("", cmd));
    }

    @Test
    void testLogoutSuccessFul() {
        String[] args = {};
        Command cmd = new Command(LOGOUT, args);
        Mockito.doNothing().when(manager).serializeData(any(User.class));
        Map<String, User> map = new HashMap<>();
        map.put("Jim", new User("Jim", "pass"));
        mockedExecutor = new CommandExecutor(priceChart, map, manager);
        assertEquals(SUCCESSFUL_LOGOUT_MESSAGE, mockedExecutor.execute("Jim", cmd));
        assertEquals(0, map.size());
    }

    @Test
    void testLogoutNotLoggedIn() {
        String[] args = {};
        Command cmd = new Command(LOGOUT, args);
        mockedExecutor = new CommandExecutor(priceChart, Map.of(), manager);
        assertEquals(NOT_LOGGED_IN_MESSAGE, mockedExecutor.execute("Jim", cmd));
    }

    @Test
    void testExecuteWithUnknownCommand() {
        String[] args = {};
        Command cmd = new Command("abc", args);
        assertEquals("Unknown command", commandExecutor.execute("", cmd));
    }

    @Test
    void testUpdatePricesError() throws URISyntaxException, IOException, InterruptedException {
        doThrow(IOException.class).when(priceChart).updatePrices();
        mockedExecutor = new CommandExecutor(priceChart, null, manager);
        assertThrows(RuntimeException.class, () -> mockedExecutor.updatePrices());
    }

    @Test
    void testQuitWithInvalidArgumentsCount() {
        String[] args = {"dummy"};
        Command cmd = new Command(QUIT, args);
        assertEquals(INVALID_NUMBER_OF_ARGUMENTS_MESSAGE, commandExecutor.execute("", cmd));
    }
}
