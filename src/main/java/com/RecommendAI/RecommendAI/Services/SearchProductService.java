package com.RecommendAI.RecommendAI.Services;

import com.RecommendAI.RecommendAI.Dto.RequestDtos.RequestPayload;
import com.RecommendAI.RecommendAI.Dto.ResponsePayload;

public interface SearchProductService {
    ResponsePayload getSimilarProductOfDifferentDesigner(RequestPayload requestPayload);
    ResponsePayload getSimilarProductOfSameDesigner(RequestPayload requestPayload);
    ResponsePayload getCompleteThelook(RequestPayload requestPayload);
    ResponsePayload getRecentlyViewed(RequestPayload requestPayload);
    void clearRedisForASkuId(String skuId);
    void saveSkuIdToRecentlyViewed(RequestPayload requestPayload);
}
