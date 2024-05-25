package com.RecommendAI.RecommendAI.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public class ResponsePayload {
    @JsonProperty("sku_id")
    public String skuId;
    @JsonProperty("product_id")
    public String productId;
    @JsonProperty("title")
    public String title;
    @JsonProperty("discounted_price")
    public double discountedPrice;
    @JsonProperty("region_sale_price")
    public double regionSalePrice;
    @JsonProperty("brand")
    public String brand;
    @JsonProperty("image_link")
    public String imageLink;
    @JsonProperty("discount")
    public double discount;
    @JsonProperty("link")
    public String link;
    @JsonProperty("sale_price")
    public double salePrice;
    @JsonProperty("price")
    public double price;
}
