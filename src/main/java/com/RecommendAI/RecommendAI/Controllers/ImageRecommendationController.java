package com.RecommendAI.RecommendAI.Controllers;

import com.RecommendAI.RecommendAI.Dto.DataTo;
import com.RecommendAI.RecommendAI.Dto.RequestPayload;
import com.RecommendAI.RecommendAI.Dto.ResponsePayload;
import com.RecommendAI.RecommendAI.Services.SearchProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ImageRecommendationController {
    private final SearchProductService searchProductService;

    @CrossOrigin
    @PostMapping("/v1/fetch")
    public ResponseEntity getSimilarImageOfSameDesigner(@RequestBody RequestPayload requestPayload) {
        List<String> a = Arrays.asList(requestPayload.productId.split("-"));
        requestPayload.skuId = a.get(a.size() - 1).toUpperCase();

        LinkedHashSet<ResponsePayload> responsePayloads = new LinkedHashSet<>();
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
            List<LinkedHashSet<ResponsePayload>> cc = new ArrayList<>();
            cc.add(responsePayloads);
            DataTo data = new DataTo();
            data.status = "success";
            data.data = cc;
            data.message = "";
            return new ResponseEntity<>(data, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @CrossOrigin
    @GetMapping("/v1/clearSkuCache/{sku_id}")
    public ResponseEntity removeFromCache(@PathVariable String sku_id) {
        searchProductService.clearRedisForASkuId(sku_id);
        return new ResponseEntity<>("Success", HttpStatus.OK);
    }
}
