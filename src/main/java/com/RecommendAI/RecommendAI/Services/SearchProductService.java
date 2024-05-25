package com.RecommendAI.RecommendAI.Services;

import com.RecommendAI.RecommendAI.Dto.RequestDtos.RequestPayload;
import com.RecommendAI.RecommendAI.Dto.ResponsePayload;
import com.RecommendAI.RecommendAI.Exceptions.ImageNotInDbException;
import com.RecommendAI.RecommendAI.Exceptions.ProductNotInDbException;

public interface SearchProductService {
    ResponsePayload getSimilarProductOfDifferentDesigner(RequestPayload requestPayload) throws ImageNotInDbException, ProductNotInDbException;
    ResponsePayload getSimilarProductOfSameDesigner(RequestPayload requestPayload) throws ImageNotInDbException, ProductNotInDbException;
    ResponsePayload getCompleteThelook(RequestPayload requestPayload) throws ImageNotInDbException, ProductNotInDbException;
    ResponsePayload getRecentlyViewed(RequestPayload requestPayload);
    void saveSkuIdToRecentlyViewed(RequestPayload requestPayload);
}
