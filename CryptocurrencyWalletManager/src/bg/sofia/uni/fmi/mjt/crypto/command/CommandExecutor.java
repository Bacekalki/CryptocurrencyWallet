package bg.sofia.uni.fmi.mjt.crypto.command;

import bg.sofia.uni.fmi.mjt.crypto.chart.PriceChartAPI;
import bg.sofia.uni.fmi.mjt.crypto.datamanager.DataManager;
import bg.sofia.uni.fmi.mjt.crypto.datamanager.DataManagerAPI;
import bg.sofia.uni.fmi.mjt.crypto.exception.InsufficientBalanceException;
import bg.sofia.uni.fmi.mjt.crypto.exception.MissingCryptoException;
import bg.sofia.uni.fmi.mjt.crypto.exception.NegativeMoneyException;
import bg.sofia.uni.fmi.mjt.crypto.hasher.PasswordHasher;
import bg.sofia.uni.fmi.mjt.crypto.chart.PriceChart;
import bg.sofia.uni.fmi.mjt.crypto.logger.ErrorLogger;
import bg.sofia.uni.fmi.mjt.crypto.user.Purchase;
import bg.sofia.uni.fmi.mjt.crypto.user.User;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class CommandExecutor implements CommandExecutorAPI {
    private final PriceChartAPI priceChart;
    private final Map<String, User> users;
    private final PasswordHasher hasher;
    private final DataManagerAPI manager;
    private static final String LOG_PATH = "logs.txt";
    private final ErrorLogger errorLogger = new ErrorLogger(LOG_PATH);
    private static final String INVALID_NUMBER_OF_ARGUMENTS_MESSAGE = "Wrong number of arguments";
    private static final String INVALID_ASSET_ID_MESSAGE = "Invalid assetId";
    private static final String INVALID_NUMBER_FORMAT_MESSAGE = "Amount of money must be a number";
    private static final String INVALID_PASSWORD_MESSAGE =
        "Your password must be at least 7 characters long and must contain at least 1 uppercase letter, " +
            "1 lowercase and 1 digit";
    private static final String SUCCESSFUL_BUY_MESSAGE = "Bought successfully";
    private static final String SUCCESSFUL_SELL_MESSAGE = "Sold successfully";
    private static final String SUCCESSFUL_LOGIN_MESSAGE = "Logged in successfully";
    private static final String SUCCESSFUL_DEPOSIT_MESSAGE = "Deposited successfully";
    private static final String SUCCESSFUL_REGISTRATION_MESSAGE = "Registration successful";
    private static final String SUCCESSFUL_LOGOUT_MESSAGE = "Logged out successfully";
    private static final String SUCCESSFUL_QUIT_MESSAGE = "Quited successfully";
    private static final String HELP_MESSAGE =
        "Hello! " +
            "The commands that this app supports are " +
            "<login>, <register>, <deposit>, <buy>, <sell>, <list-offerings>, <wallet-summary>, " +
            "<wallet-overall-summary>, <logout>, <quit>, <help>. " +
            "Please note that you have to logout before you quit, otherwise any made changes won't be saved";
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
    private static final String HELP = "help";
    private static final int MIN_PASSWORD_CHARACTERS = 7;

    public CommandExecutor() {
        priceChart = new PriceChart();
        users = new HashMap<>();
        hasher = new PasswordHasher();
        manager = new DataManager();
    }

    public CommandExecutor(PriceChartAPI priceChart, Map<String, User> users, DataManagerAPI manager) {
        this.priceChart = priceChart;
        this.users = users;
        this.manager = manager;
        hasher = new PasswordHasher();
    }

    public String execute(String attachment, Command cmd) {
        return switch (cmd.command()) {
            case BUY -> buyStock(attachment, cmd.arguments());
            case SELL -> sellStock(attachment, cmd.arguments());
            case LIST_OFFERINGS -> listOfferings(cmd.arguments());
            case WALLET_SUMMARY -> getWalletSummary(attachment, cmd.arguments());
            case WALLET_OVERALL_SUMMARY -> getOverallWalletSummary(attachment, cmd.arguments());
            case LOGIN -> login(cmd.arguments());
            case REGISTER -> register(cmd.arguments());
            case DEPOSIT -> depositMoney(attachment, cmd.arguments());
            case LOGOUT -> logout(attachment, cmd.arguments());
            case QUIT -> quit(cmd.arguments());
            case HELP -> help(cmd.arguments());
            default -> "Unknown command";
        };
    }

    public void updatePrices() {
        try {
            priceChart.updatePrices();
        } catch (IOException | InterruptedException | URISyntaxException e) {
            errorLogger.logError(e.getMessage());
            throw new RuntimeException("There was an error updating prices", e);
        }
    }

    private String winLossMapToString(Map<String, Double> winLossMap) {
        StringBuilder sb = new StringBuilder();
        sb.append("{").append(System.lineSeparator());

        for (Map.Entry<String, Double> entry : winLossMap.entrySet()) {
            sb.append("  ")
                .append("AssetId: ").append(entry.getKey())
                .append(", MoneyWon: ").append(entry.getValue())
                .append(System.lineSeparator());
        }

        sb.append("}");

        return sb.toString();
    }

    private boolean isValidAssetId(String assetId) {
        return priceChart.getAvailableCryptos().contains(assetId);
    }

    private String buyStock(String username, String... arguments) {
        if (arguments.length != 2) {
            return INVALID_NUMBER_OF_ARGUMENTS_MESSAGE;
        }

        String assetId = arguments[0];
        double amount;

        if (!isValidAssetId(assetId)) {
            return INVALID_ASSET_ID_MESSAGE;
        }
        try {
            amount = Double.parseDouble(arguments[1]);
        } catch (NumberFormatException e) {
            return INVALID_NUMBER_FORMAT_MESSAGE;
        }

        User user = users.get(username);
        if (user == null) {
            return NOT_LOGGED_IN_MESSAGE;
        }

        try {
            user.buyStock(assetId, amount, priceChart.getPrice(assetId));
        } catch (NegativeMoneyException | InsufficientBalanceException e) {
            return e.getMessage();
        }

        return SUCCESSFUL_BUY_MESSAGE;
    }

    private String sellStock(String username, String... arguments) {
        if (arguments.length != 1) {
            return INVALID_NUMBER_OF_ARGUMENTS_MESSAGE;
        }

        String assetId = arguments[0];

        if (!isValidAssetId(assetId)) {
            return INVALID_ASSET_ID_MESSAGE;
        }

        User user = users.get(username);
        if (user == null) {
            return NOT_LOGGED_IN_MESSAGE;
        }

        try {
            user.sellStock(assetId, priceChart.getPrice(assetId));
        } catch (MissingCryptoException | NegativeMoneyException e) {
            return e.getMessage();
        }

        return SUCCESSFUL_SELL_MESSAGE;
    }

    private String login(String... arguments) {
        if (arguments.length != 2) {
            return INVALID_NUMBER_OF_ARGUMENTS_MESSAGE;
        }

        String username = arguments[0];
        String password = arguments[1];
        String hashedPassword = hasher.hashPassword(password);

        if (users.containsKey(username)) {
            return USER_ALREADY_LOGGED_MESSAGE;
        }

        Path userFile = Path.of(username + ".txt");
        User us = manager.getData(userFile);
        if (us == null) {
            return USER_NOT_FOUND_MESSAGE;
        }

        if (!us.getHashedPassword().equals(hashedPassword)) {
            return LOGIN_FAILED_MESSAGE;
        }

        users.put(us.getUsername(), us);
        return SUCCESSFUL_LOGIN_MESSAGE;
    }

    private String register(String... arguments) {
        if (arguments.length != 2) {
            return INVALID_NUMBER_OF_ARGUMENTS_MESSAGE;
        }

        String username = arguments[0];
        String password = arguments[1];

        if (!isPasswordValid(password)) {
            return INVALID_PASSWORD_MESSAGE;
        }

        User testUser = new User(username, password);
        if (users.containsValue(testUser)) {
            return USER_ALREADY_LOGGED_MESSAGE;
        }

        if (manager.exists(Path.of(username + ".txt"))) {
            return USER_ALREADY_EXISTS_MESSAGE;
        }

        users.put(username, testUser);
        return SUCCESSFUL_REGISTRATION_MESSAGE;
    }

    private String listOfferings(String... arguments) {
        if (arguments.length != 0) {
            return INVALID_NUMBER_OF_ARGUMENTS_MESSAGE;
        }

        return priceChart.toString();
    }

    private String depositMoney(String username, String... arguments) {
        if (arguments.length != 1) {
            return INVALID_NUMBER_OF_ARGUMENTS_MESSAGE;
        }

        double amount;

        try {
            amount = Double.parseDouble(arguments[0]);
        } catch (NumberFormatException e) {
            return INVALID_NUMBER_FORMAT_MESSAGE;
        }

        User user = users.get(username);
        if (user == null) {
            return NOT_LOGGED_IN_MESSAGE;
        }

        try {
            user.deposit(amount);
        } catch (NegativeMoneyException e) {
            return e.getMessage();
        }

        return SUCCESSFUL_DEPOSIT_MESSAGE;
    }

    private String getWalletSummary(String username, String... arguments) {
        if (arguments.length != 0) {
            return INVALID_NUMBER_OF_ARGUMENTS_MESSAGE;
        }

        User user = users.get(username);
        if (user == null) {
            return NOT_LOGGED_IN_MESSAGE;
        }

        return user.getWallet().toString() + System.lineSeparator() + "Your current balance is :  " + user.getBalance();
    }

    private String getOverallWalletSummary(String username, String... arguments) {
        if (arguments.length != 0) {
            return INVALID_NUMBER_OF_ARGUMENTS_MESSAGE;
        }

        Map<String, Double> winLossMap = new HashMap<>();
        User user = users.get(username);

        if (user == null) {
            return NOT_LOGGED_IN_MESSAGE;
        }

        for (Purchase p : user.getHistory()) {
            winLossMap.merge(p.assetId(), p.amountOfCrypto() * (priceChart.getPrice(p.assetId()) - p.price()),
                Double::sum);
        }

        return "Your total winnings are : " + winLossMapToString(winLossMap);
    }

    private String logout(String username, String... arguments) {
        if (arguments.length != 0) {
            return INVALID_NUMBER_OF_ARGUMENTS_MESSAGE;
        }

        User user = users.get(username);
        if (user == null) {
            return NOT_LOGGED_IN_MESSAGE;
        }

        manager.serializeData(user);
        users.remove(username);
        return SUCCESSFUL_LOGOUT_MESSAGE;
    }

    private String quit(String... args) {
        if (args.length != 0) {
            return INVALID_NUMBER_OF_ARGUMENTS_MESSAGE;
        }

        return SUCCESSFUL_QUIT_MESSAGE;
    }

    private String help(String... args) {
        return HELP_MESSAGE;
    }

    private boolean isPasswordValid(String password) {
        if (password.length() < MIN_PASSWORD_CHARACTERS) {
            return false;
        }
        boolean containsUppercase = false;
        boolean containsLowercase = false;
        boolean containsDigit = false;
        char[] pass = password.toCharArray();
        for (char c : pass) {
            if (Character.isDigit(c)) {
                containsDigit = true;
            }
            if (Character.isUpperCase(c)) {
                containsUppercase = true;
            }
            if (Character.isLowerCase(c)) {
                containsLowercase = true;
            }
            if (containsDigit && containsLowercase && containsUppercase) {
                return true;
            }
        }
        return false;
    }
}
