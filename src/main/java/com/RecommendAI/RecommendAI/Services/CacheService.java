package com.RecommendAI.RecommendAI.Services;

import java.util.LinkedHashSet;

public interface CacheService {
    void clearSkuFromCache(String sku);
    LinkedHashSet<String> getListOfSkuIdsFromCache(String key);
    void SetListOfSkuIdsToCache(LinkedHashSet<String> skuIds,String key);
}