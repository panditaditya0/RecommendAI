package com.RecommendAI.RecommendAI.Controllers;

import com.RecommendAI.RecommendAI.Dto.ResponsePayload;
import com.RecommendAI.RecommendAI.Dto.RequestDtos.RequestPayload;
import com.RecommendAI.RecommendAI.Exceptions.ImageNotInDbException;
import com.RecommendAI.RecommendAI.Exceptions.ProductNotInDbException;
import com.RecommendAI.RecommendAI.Services.CacheService;
import com.RecommendAI.RecommendAI.Services.SearchProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ImageRecommendationController {
    private final SearchProductService searchProductService;
    private final CacheService cacheService;

    @CrossOrigin
    @PostMapping("/v1/fetch")
    public ResponseEntity getSimilarImageOfSameDesigner(@RequestBody RequestPayload requestPayload) {
        List<String> a = Arrays.asList(requestPayload.productId.split("-"));
        requestPayload.skuId = a.get(a.size() - 1).toUpperCase();

        ResponsePayload responsePayloads = new ResponsePayload();
        try {
            if (requestPayload.widget_list.size() > 0 && requestPayload.widget_list.get(0) == 0) {
                if (null != requestPayload.filters.get(0).type && requestPayload.filters.get(0).type.toString().contains("not-contains")) {
                    responsePayloads = searchProductService
                            .getSimilarProductOfDifferentDesigner(requestPayload);
                    searchProductService
                            .saveSkuIdToRecentlyViewed(requestPayload);
                } else {
                    responsePayloads = searchProductService
                            .getSimilarProductOfSameDesigner(requestPayload);
                }
            } else if (requestPayload.widget_list.size() > 0 && requestPayload.widget_list.get(0) == 8) {
                responsePayloads = searchProductService
                        .getCompleteThelook(requestPayload);

            } else if (requestPayload.widget_list.size() > 0 && requestPayload.widget_list.get(0) == 7) {
                responsePayloads = searchProductService
                        .getRecentlyViewed(requestPayload);
            }
            return new ResponseEntity<>(responsePayloads, HttpStatus.OK);
        } catch (ImageNotInDbException notInDbException) {
            return new ResponseEntity<>(notInDbException.getMessage(), HttpStatus.NOT_FOUND);
        } catch (ProductNotInDbException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.NO_CONTENT);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @CrossOrigin
    @GetMapping("/v1/clearSkuCache/{sku_id}")
    public ResponseEntity removeFromCache(@PathVariable String sku_id) {
        try {
            cacheService.clearSkuFromCache(sku_id);
            return new ResponseEntity<>("Success", HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @CrossOrigin
    @GetMapping("/v1/clearAllSku")
    public ResponseEntity clearAllSku() {
        try {
            cacheService.clearAllCache();
            return new ResponseEntity<>("Success", HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
