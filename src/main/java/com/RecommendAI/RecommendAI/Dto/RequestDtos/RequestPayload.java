package com.RecommendAI.RecommendAI.Dto.RequestDtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class RequestPayload{
    public ArrayList<Long> num_results;
    public ArrayList<Long> widget_list;
    public String mad_uuid;
    public String user_id;
    @JsonProperty("product_id")
    public String productId;
    public boolean details;
    public ArrayList<String> fields;
    public ExtraParams extra_params;
    public String region;
    public ArrayList<Filter> filters;
    @JsonProperty("sku_id")
    public String skuId;
}

