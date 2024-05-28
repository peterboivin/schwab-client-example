package com.pangility.schwab.example.strategy;

import com.pangility.schwab.api.client.accountsandtrading.SchwabAccountsAndTradingApiClient;
import com.pangility.schwab.api.client.marketdata.model.pricehistory.PriceHistoryResponse;
import com.pangility.schwab.example.SchwabApiClientHandler;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class MovingAverageCrossoverStrategy {

    @Autowired
    private SchwabApiClientHandler schwabApiClientHandler;

    @Autowired
    private SchwabAccountsAndTradingApiClient schwabAccountsAndTradingApiClient;

    public MovingAverageCrossoverStrategy(SchwabApiClientHandler schwabApiClientHandler, SchwabAccountsAndTradingApiClient schwabAccountsAndTradingApiClient) {
        this.schwabApiClientHandler = schwabApiClientHandler;
        this.schwabAccountsAndTradingApiClient = schwabAccountsAndTradingApiClient;
    }

    public static void main(String[] args) {
        MovingAverageCrossoverStrategy movingAverageCrossoverStrategy = new MovingAverageCrossoverStrategy(schwabApiClientHandler, schwabAccountsAndTradingApiClient);
        this.execute("AAPL");
    }

    public void execute(String symbol) throws IOException {
        // Fetch historical market data

        LocalDate startDate = LocalDate.of(2023, 01, 01);
        LocalDate endDate = LocalDate.now();
        Mono<PriceHistoryResponse> historicalPrices = schwabApiClientHandler.callHistoricalPrices("AAPL", startDate, endDate);

        // Calculate moving averages
        double shortMavg = calculateMovingAverage(historicalPrices, 40);
        double longMavg = calculateMovingAverage(historicalPrices, 100);

        // Generate trading signals
        if (shortMavg > longMavg) {
            System.out.println("Buy signal for " + symbol);
//            schwabAccountsAndTradingApiClient.placeOrder(schwabUserId, encryptedAccounts.get(0).getHashValue(), new Order());
            schwabAccountsAndTradingApiClient.placeOrder(symbol, 10, "BUY");  // Example: buying 10 shares
        } else if (shortMavg < longMavg) {
            System.out.println("Sell signal for " + symbol);
            schwabAccountsAndTradingApiClient.placeOrder(symbol, 10, "SELL");  // Example: selling 10 shares
        }
    }

    private double calculateMovingAverage(Mono<PriceHistoryResponse> prices, int windowSize) {

        AtomicReference<Double> sum = new AtomicReference<>((double) 0);
        AtomicInteger count = new AtomicInteger();

        prices.flux().toStream().forEach(
                response -> {
                    sum.updateAndGet(v -> new BigDecimal(v).add(response.getPreviousClose()).doubleValue());
                    count.getAndIncrement();
                }
        );

        return sum.get() / count.get();
    }
}

