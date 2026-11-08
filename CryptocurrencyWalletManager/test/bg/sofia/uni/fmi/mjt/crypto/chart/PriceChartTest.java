package bg.sofia.uni.fmi.mjt.crypto.chart;

import bg.sofia.uni.fmi.mjt.crypto.exception.ClientRequestException;
import bg.sofia.uni.fmi.mjt.crypto.exception.ServerException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PriceChartTest {
    private static HttpClient mockedClient = mock();
    private PriceChart priceChart = new PriceChart(mockedClient, "dummyKey");
    private static HttpResponse<String> mockedResponse = mock();
    private static Map<String, Double> expectedMap;
    private static String mockedResponseBody = "[\n" +
        "  {\n" +
        "    \"asset_id\": \"BTC\",\n" +
        "    \"type_is_crypto\": 1,\n" +
        "    \"price_usd\": 42805.822172988985\n" +
        "  },\n" +
        "  {\n" +
        "    \"asset_id\": \"ETH\",\n" +
        "    \"type_is_crypto\": 1,\n" +
        "    \"price_usd\": 2298.281646572329\n" +
        "  },\n" +
        "  {\n" +
        "    \"asset_id\": \"BNB\",\n" +
        "    \"type_is_crypto\": 1,\n" +
        "    \"price_usd\": 305.9146094791575\n" +
        "  }\n" +
        "]";

    private static String expectedToString = System.lineSeparator() +
        "BTC: 42805.8222" + System.lineSeparator() +
        "BNB: 305.9146" + System.lineSeparator() +
        "ETH: 2298.2816" + System.lineSeparator();

    @BeforeAll
    static void setUp() throws IOException, InterruptedException {
        when(mockedClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockedResponse);
        when(mockedResponse.body()).thenReturn(mockedResponseBody);
        expectedMap = new HashMap<>();
        expectedMap.put("BTC", 42805.822172988985);
        expectedMap.put("ETH", 2298.281646572329);
        expectedMap.put("BNB", 305.9146094791575);
    }

    @Test
    void testUpdatePrice() throws URISyntaxException, IOException, InterruptedException {
        when(mockedResponse.statusCode()).thenReturn(200);
        priceChart.updatePrices();
        assertEquals(expectedMap.get("BTC"),priceChart.getPrice("BTC"));
        assertEquals(expectedMap.get("ETH"),priceChart.getPrice("ETH"));
        assertEquals(expectedMap.get("BNB"),priceChart.getPrice("BNB"));
    }

    @Test
    void testUpdatePricesResponseCode404() {
        when(mockedResponse.statusCode()).thenReturn(404);
        assertThrows(ClientRequestException.class, () -> priceChart.updatePrices());
    }

    @Test
    void testUpdatePricesResponseCode500() {
        when(mockedResponse.statusCode()).thenReturn(500);
        assertThrows(ServerException.class, () -> priceChart.updatePrices());
    }

    @Test
    void testToString() throws URISyntaxException, IOException, InterruptedException {
        priceChart.updatePrices();
        assertEquals(expectedToString, priceChart.toString());
    }
}
