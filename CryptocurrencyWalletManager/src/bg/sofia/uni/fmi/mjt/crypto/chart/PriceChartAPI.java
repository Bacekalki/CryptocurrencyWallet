package bg.sofia.uni.fmi.mjt.crypto.chart;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;

public interface PriceChartAPI {

    void updatePrices() throws URISyntaxException, IOException, InterruptedException;

    double getPrice(String assetId);

    Set<String> getAvailableCryptos();
}
