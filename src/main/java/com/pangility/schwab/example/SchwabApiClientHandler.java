package com.pangility.schwab.example;

import com.pangility.schwab.api.client.marketdata.EnableSchwabMarketDataApi;
import com.pangility.schwab.api.client.marketdata.IndexNotFoundException;
import com.pangility.schwab.api.client.marketdata.SchwabMarketDataApiClient;
import com.pangility.schwab.api.client.marketdata.SymbolNotFoundException;
import com.pangility.schwab.api.client.marketdata.model.movers.MoversRequest;
import com.pangility.schwab.api.client.marketdata.model.movers.MoversResponse;
import com.pangility.schwab.api.client.marketdata.model.movers.Screener;
import com.pangility.schwab.api.client.marketdata.model.pricehistory.PriceHistoryRequest;
import com.pangility.schwab.api.client.marketdata.model.pricehistory.PriceHistoryResponse;
import com.pangility.schwab.api.client.marketdata.model.quotes.QuoteResponse;
import com.pangility.schwab.api.client.oauth2.SchwabAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@EnableSchwabMarketDataApi
@SuppressWarnings("unused")
public class SchwabApiClientHandler {
    @Autowired
    private SchwabMarketDataApiClient schwabMarketDataApiClient;

    @Value("${schwab-api.userid}")
    private String schwabUserId;
    @Value("${schwab-api.oauth2.refreshToken}")
    private String schwabRefreshToken;
    @Value("${schwab-api.oauth2.refreshExpiration}")
    private String schwabRefreshExpiration;

    public void init() {
        ClientTokenHandler clientTokenHandler = new ClientTokenHandler();
        SchwabAccount schwabAccount = new SchwabAccount();
        schwabAccount.setUserId(schwabUserId);
        // If you have saved your refresh token, pass it to the API client service.
        schwabAccount.setRefreshToken(schwabRefreshToken);
        schwabAccount.setRefreshExpiration(LocalDateTime.parse(schwabRefreshExpiration, DateTimeFormatter.ISO_DATE_TIME));
        schwabMarketDataApiClient.init(schwabAccount, clientTokenHandler);
    }

    @GetMapping("/quote")
    public Mono<Map<String, QuoteResponse>> callQuote(@RequestParam List<String> symbol) {
        Mono<Map<String, QuoteResponse>> quoteResponse = null;
        try {
            quoteResponse = schwabMarketDataApiClient.fetchQuotesToMono(symbol);
        } catch (Exception e) {
            // handle the exception
        }
        return quoteResponse;
    }

    @GetMapping("/movers/nyse")
    public Flux<Screener> callMovers() {
        Flux<Screener> moversResponse = null;
        MoversRequest moversRequest = MoversRequest.Builder.moversRequest()
                .withIndexSymbol(MoversRequest.IndexSymbol.NYSE)
                .build();
        try {
            moversResponse = schwabMarketDataApiClient.fetchMoversToFlux(moversRequest);
        } catch (Exception e) {
            // handle the exception
        }
        return moversResponse;
    }

    @GetMapping("/historicalPrices")
    public Mono<PriceHistoryResponse> callHistoricalPrices(String symbol, LocalDate startDate, LocalDate endDate) {
        Mono<PriceHistoryResponse> priceHistoryResponse = null;

        PriceHistoryRequest priceHistoryRequest = PriceHistoryRequest.Builder.
                priceHistReq().withSymbol(symbol).withStartDate(startDate).withEndDate(endDate).withNeedPreviousClose(true).build();
        try {
            priceHistoryResponse = schwabMarketDataApiClient.fetchPriceHistoryToMono(priceHistoryRequest);
        } catch (Exception e) {
            // handle the exception
        }
        return priceHistoryResponse;
    }
}
