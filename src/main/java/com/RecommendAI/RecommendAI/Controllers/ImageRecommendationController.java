package com.RecommendAI.RecommendAI.Controllers;

import com.RecommendAI.RecommendAI.Model.RequestPayload;
import com.RecommendAI.RecommendAI.Model.ResponsePayload;
import com.RecommendAI.RecommendAI.Services.SearchProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.logging.Logger;

@RestController
public class ImageRecommendationController {
    private static final Logger logger = Logger.getLogger(ImageRecommendationController.class.getName());

    @Autowired
    public SearchProductService searchProductService;

    @CrossOrigin
    @PostMapping("/fetch")
    public ResponseEntity getSimilarImageOfSameDesigner(@RequestBody RequestPayload payload) {
        try {
            ArrayList<ResponsePayload> responsePayloads = searchProductService
                    .getSimilarProductOfSameDesigner(payload, true);
            return new ResponseEntity<>(responsePayloads, HttpStatus.FOUND);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage() , HttpStatus.BAD_REQUEST);
        }
    }

    @CrossOrigin
    @PostMapping("/fetch2")
    public ResponseEntity getSimilarProductsOfDifferentDesigner(@RequestBody RequestPayload payload) {
        try {
            ArrayList<ResponsePayload> responsePayloads = searchProductService
                    .getSimilarProductOfSameDesigner(payload, false);
            return new ResponseEntity<>(responsePayloads, HttpStatus.FOUND);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage() , HttpStatus.BAD_REQUEST);
        }
    }

    @CrossOrigin
    @PostMapping("/fetch3")
    public ResponseEntity getCompleteTheLook(@RequestBody RequestPayload payload) {
        try {
            ArrayList<ResponsePayload> responsePayloads = searchProductService
                    .getCompletethelook(payload);
            return new ResponseEntity<>(responsePayloads, HttpStatus.FOUND);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage() , HttpStatus.BAD_REQUEST);
        }
    }

    @CrossOrigin
    @PostMapping("/clearRedis/{sku}")
    public ResponseEntity pushToWeaviate(@PathVariable String sku){
        try {
            searchProductService
                    .clearRedisForASkuId(sku);
            return new ResponseEntity<>("RedisClearedForSku", HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage() , HttpStatus.BAD_REQUEST);
        }
    }




}
