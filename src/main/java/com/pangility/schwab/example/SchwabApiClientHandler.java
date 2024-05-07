package com.pangility.schwab.example;

import com.pangility.schwab.api.client.marketdata.EnableSchwabMarketDataApi;
import com.pangility.schwab.api.client.marketdata.SchwabMarketDataApiClient;
import com.pangility.schwab.api.client.marketdata.SymbolNotFoundException;
import com.pangility.schwab.api.client.marketdata.model.movers.MoversRequest;
import com.pangility.schwab.api.client.marketdata.model.movers.MoversResponse;
import com.pangility.schwab.api.client.marketdata.model.quotes.QuoteResponse;
import com.pangility.schwab.api.client.oauth2.SchwabAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Service
@EnableSchwabMarketDataApi
@SuppressWarnings("unused")
public class SchwabApiClientHandler {
    @Autowired
    private SchwabMarketDataApiClient schwabMarketDataApiClient;

    @Value("${schwab-api.userId}")
    private String schwabUserId;

    public void init() {
        ClientTokenHandler clientTokenHandler = new ClientTokenHandler();
        SchwabAccount schwabAccount = new SchwabAccount();
        schwabAccount.setUserId(schwabUserId);
        // If you have saved your refresh token, pass it to the API client service.
        // schwabAccount.setRefreshToken(schwabRefreshToken);
        // schwabAccount.setRefreshExpiration(schwabRefreshExpiration);
        schwabMarketDataApiClient.init(schwabAccount, clientTokenHandler);
    }

    @GetMapping("/quote")
    public QuoteResponse callQuote(@RequestParam String symbol) {
        QuoteResponse quoteResponse = null;
        try {
            quoteResponse = schwabMarketDataApiClient.fetchQuote(symbol);
        } catch (SymbolNotFoundException e) {
            // handle the exception
        }
        return quoteResponse;
    }

    @GetMapping("/movers/nyse")
    public MoversResponse callMovers() {
        MoversResponse moversResponse = null;
        MoversRequest moversRequest = MoversRequest.Builder.moversRequest()
                .withIndexSymbol(MoversRequest.IndexSymbol.NYSE)
                .build();
        try {
            moversResponse = schwabMarketDataApiClient.fetchMovers(moversRequest);
        } catch (SymbolNotFoundException e) {
            // handle the exception
        }
        return moversResponse;
    }
}
