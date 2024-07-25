package com.RecommendAI.RecommendAI.Services;

import java.util.LinkedHashSet;

public interface CacheService {
    boolean clearSkuFromCache(String sku);
    boolean clearAllCache();
    LinkedHashSet<String> getListOfSkuIdsFromCache(String key);
    void SetListOfSkuIdsToCache(LinkedHashSet<String> skuIds,String key);
}