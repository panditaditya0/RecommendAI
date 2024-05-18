package com.RecommendAI.RecommendAI.Controllers;

import com.RecommendAI.RecommendAI.Model.DataTo;
import com.RecommendAI.RecommendAI.Model.RequestPayload;
import com.RecommendAI.RecommendAI.Model.ResponsePayload;
import com.RecommendAI.RecommendAI.Services.SearchProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Logger;

@RestController
public class ImageRecommendationController {
    private static final Logger logger = Logger.getLogger(ImageRecommendationController.class.getName());

    @Autowired
    public SearchProductService searchProductService;

    @CrossOrigin
    @PostMapping("/fetch")
    public ResponseEntity getSimilarImageOfSameDesigner(@RequestBody RequestPayload requestPayload) {
        if (!requestPayload.mad_uuid.equalsIgnoreCase("i_am_a_bot")){
            List<String> a = Arrays.asList(requestPayload.productId.split("-"));
            requestPayload.skuId = a.get(a.size()-1).toUpperCase();
        }

        LinkedHashSet<ResponsePayload> responsePayloads = new LinkedHashSet<>();
        try {
            if(requestPayload.widget_list.size() >0 && requestPayload.widget_list.get(0) == 0) {
                if (null != requestPayload.filters.get(0).type && requestPayload.filters.get(0).type.toString().contains("not-contains")) {
                    responsePayloads = searchProductService
                            .getSimilarProductOfDifferentDesigner(requestPayload);
                    if(!requestPayload.mad_uuid.equalsIgnoreCase("i_am_a_bot")){
                        searchProductService
                                .saveSkuIdToRecentlyViewed(requestPayload);
                    }
                } else {
                   responsePayloads = searchProductService
                            .getSimilarProductOfSameDesigner(requestPayload);
                }
            } else if (requestPayload.widget_list.size() >0 && requestPayload.widget_list.get(0) == 8){
                responsePayloads = searchProductService
                        .getCompleteThelook(requestPayload);

            } else if((!requestPayload.mad_uuid.equalsIgnoreCase("i_am_a_bot")) && requestPayload.widget_list.size() >0 && requestPayload.widget_list.get(0) == 7){
                responsePayloads =   searchProductService
                        .getRecentlyViewed(requestPayload);
            }

            List<LinkedHashSet<ResponsePayload>> cc = new ArrayList<>();
            cc.add(responsePayloads);
            DataTo data = new DataTo();
            data.status="success";
            data.data = cc;
            data.message="";
            return new ResponseEntity<>(data, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage() , HttpStatus.BAD_REQUEST);
        }
    }
}
