package com.RecommendAI.RecommendAI.Model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RequestPayload {

    @JsonProperty("exchange_rate")
    public double exchangeRate;
    public RequestFilters filters;
    @JsonProperty("sku_id")
    public String skuId;
    public String region;
    @JsonProperty("product_id")
    public String productId;
}

