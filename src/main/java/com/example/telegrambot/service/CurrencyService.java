package com.example.telegrambot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

@Service
public class CurrencyService {

    @Value("${currency.api.key}")
    private String apiKey;

    @Value("${currency.api.url}")
    private String apiUrl;
    public String getExchangeRateAsString() throws IOException {
        String exchangeRatesString = "Exchange rates:\n";
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(new URL(apiUrl));
        JsonNode conversionRatesNode = rootNode.get("conversion_rates");
        for (Iterator<Map.Entry<String, JsonNode>> it = conversionRatesNode.fields(); it.hasNext();) {
            Map.Entry<String, JsonNode> entry = it.next();
            String currencyCode = entry.getKey();
            double exchangeRate = entry.getValue().asDouble();
            exchangeRatesString += String.format("%s: %.2f\n", currencyCode, exchangeRate);
        }
        return exchangeRatesString;
    }


    public double getRUBToKZTExchangeRate() throws IOException {
        var kzt = getExchangeRate("KZT");
        var rub = getExchangeRate("RUB");
        return kzt / rub;
    }
    public double getExchangeRate(String currencyCode) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(new URL(apiUrl));
        return rootNode.get("conversion_rates").get(currencyCode).asDouble();
    }
}