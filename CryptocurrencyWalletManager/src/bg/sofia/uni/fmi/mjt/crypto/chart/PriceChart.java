package bg.sofia.uni.fmi.mjt.crypto.chart;

import bg.sofia.uni.fmi.mjt.crypto.exception.ClientRequestException;
import bg.sofia.uni.fmi.mjt.crypto.exception.ServerException;
import bg.sofia.uni.fmi.mjt.crypto.logger.ErrorLogger;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PriceChart implements PriceChartAPI {
    private static Map<String, Double> priceChart;
    private static HttpRequest request;
    private final HttpClient client;
    private static final String LOG_PATH = "logs.txt";
    private static final ErrorLogger LOGGER = new ErrorLogger(LOG_PATH);
    private static final int CRYPTOS_COUNT = 100;
    private static final int MIN_PRICE = 1;
    private static final int MAX_PRICE = 100_000;
    private static final int CLIENT_ERROR_CODE = 400;
    private static final int SERVER_ERROR_CODE = 500;
    private static final String TYPE = "type_is_crypto";
    private static final String PRICE = "price_usd";
    private static final String ASSETID = "asset_id";
    private static String key = System.getenv("API_KEY");
    private static final String URI = "https://rest.coinapi.io/v1/assets";
    private static final int DECIMAL_PLACES = 4;

    public PriceChart() {
        priceChart = new HashMap<>();
        client = HttpClient.newHttpClient();
        buildRequest();
    }

    public PriceChart(HttpClient client, String apiKey) {
        priceChart = new HashMap<>();
        this.client = client;
        key = apiKey;
        buildRequest();
    }

    public void updatePrices() throws IOException, InterruptedException {
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        checkResponseCode(response.statusCode());
        JsonArray hits = JsonParser.parseString(response.body()).getAsJsonArray();

        int counter = 0;
        for (int i = 0; i < hits.size(); i++) {
            JsonObject current = hits.get(i).getAsJsonObject();

            if (current.get(TYPE).getAsInt() == 1 && current.has(PRICE)) {
                if (current.get(PRICE).getAsDouble() > MIN_PRICE && current.get(PRICE).getAsDouble() < MAX_PRICE) {
                    priceChart.put(current.get(ASSETID).getAsString(), current.get(PRICE).getAsDouble());
                    counter++;
                }
            }

            if (counter == CRYPTOS_COUNT) {
                break;
            }
        }
    }

    public double getPrice(String assetId) {
        return priceChart.get(assetId);
    }

    public Set<String> getAvailableCryptos() {
        return priceChart.keySet();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(System.lineSeparator());

        String pattern = "#." + "0".repeat(DECIMAL_PLACES);
        DecimalFormat decimalFormat = new DecimalFormat(pattern);

        for (HashMap.Entry<String, Double> entry : priceChart.entrySet()) {
            String roundedValue = decimalFormat.format(entry.getValue());
            stringBuilder.append(entry.getKey()).append(": ").append(roundedValue).append(System.lineSeparator());
        }

        return stringBuilder.toString();
    }

    private void checkResponseCode(int responseCode) {
        if (responseCode >= CLIENT_ERROR_CODE && responseCode < SERVER_ERROR_CODE) {
            throw new ClientRequestException("There was an error fetching prices with code : " + responseCode);
        }
        if (responseCode >= SERVER_ERROR_CODE) {
            throw new ServerException("There was an error with coinAPI and prices couldn't be fetched");
        }
    }

    private void buildRequest() {
        try {
            request = HttpRequest.newBuilder(new URI(URI))
                .header("Accept", "application/json")
                .header("X-CoinAPI-Key", key)
                .build();
        } catch (URISyntaxException e) {
            LOGGER.logError(e.toString() + " " + e.getInput());
            throw new RuntimeException("There was an error in URI syntax", e);
        }
    }

}
