package com.RecommendAI.RecommendAI.Services.impl;

import com.RecommendAI.RecommendAI.Services.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisCacheServiceImpl implements CacheService {
    private final RedisTemplate template;

    @Override
    public boolean clearSkuFromCache(String sku) {
        try{
            template.delete(sku + "YES");
            template.delete(sku + "NO");
            template.delete(sku + "COMPLETE");
        } catch (Exception ex){
        }
        return true;
    }

    @Override
    public boolean clearAllCache() {
        try{
            template.getConnectionFactory().getConnection().flushDb();
            return true;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public LinkedHashSet<String> getListOfSkuIdsFromCache(String key) {
        Object listOfSkus = template.opsForList().range(key, 0, -1);
        LinkedHashSet<String> listOfSkuIdsFromRedis = new LinkedHashSet<>();
        listOfSkuIdsFromRedis.addAll((ArrayList<String>) listOfSkus);
        return listOfSkuIdsFromRedis;
    }

    @Override
    public void SetListOfSkuIdsToCache(LinkedHashSet<String> listOfSkuIds, String key) {
        template.delete(key);
        template.opsForList().rightPushAll(key, listOfSkuIds.toArray(new String[0]));

    }
}