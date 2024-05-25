package com.RecommendAI.RecommendAI.Services;

import com.RecommendAI.RecommendAI.Dto.RequestPayload;
import com.RecommendAI.RecommendAI.Dto.ResponsePayload;

import java.util.LinkedHashSet;

public interface SearchProductService {
    LinkedHashSet<ResponsePayload> getSimilarProductOfDifferentDesigner(RequestPayload requestPayload);
    LinkedHashSet<ResponsePayload> getSimilarProductOfSameDesigner(RequestPayload requestPayload);
    LinkedHashSet<ResponsePayload> getCompleteThelook(RequestPayload requestPayload);
    LinkedHashSet<ResponsePayload> getRecentlyViewed(RequestPayload requestPayload);
    void clearRedisForASkuId(String skuId);
    void saveSkuIdToRecentlyViewed(RequestPayload requestPayload);
}
