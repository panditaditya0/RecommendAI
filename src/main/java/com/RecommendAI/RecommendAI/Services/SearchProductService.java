package com.RecommendAI.RecommendAI.Services;

import com.RecommendAI.RecommendAI.Model.ProductDetailsModel;
import com.RecommendAI.RecommendAI.Model.RequestPayload;
import com.RecommendAI.RecommendAI.Model.ResponsePayload;
import com.RecommendAI.RecommendAI.Repo.ProductDetailsRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.LinkedList;

@Service
public class SearchProductService {
    private final Logger LOGGER = LoggerFactory.getLogger(SearchProductService.class);
    public ProductDetailsRepo productDetailsRepo;

    @Autowired
    private RedisTemplate template;

    @Autowired
    private WeaviateQueryService weaviateQueryService;

    public SearchProductService(ProductDetailsRepo productDetailsRepo){
        this.productDetailsRepo = productDetailsRepo;
    }

    public ArrayList<ResponsePayload> getSimilarProductOfSameDesigner(RequestPayload requestPayload, boolean fromSameBrand) {
        LinkedList<String> listOfSkuIdsFromRedis = getListOfSkuIdsFromRedis(requestPayload.skuId+"YES");
//        if (listOfSkuIdsFromRedis.size() >0) {
//            return prepareProductDetails(listOfSkuIdsFromRedis);
//        }
        ProductDetailsModel productDetails = productDetailsRepo.findBySkuId(requestPayload.skuId);
        ArrayList<String> listOfSkuIdsFromWeaviateDb = new ArrayList<>();
        listOfSkuIdsFromWeaviateDb = weaviateQueryService.getListOfSkuIdsFromWeaviateDb(productDetails);
        listOfSkuIdsFromWeaviateDb.remove(requestPayload.skuId);
        LinkedList<String> finalListOfSku_Ids = new LinkedList<>();
        finalListOfSku_Ids.addAll(listOfSkuIdsFromWeaviateDb);
//        if(listOfSkuIdsFromWeaviateDb.size()<15){
//            if(listOfSkuIdsFromWeaviateDb.size() == 0){
//                listOfSkuIdsFromWeaviateDb.add("null");
//            }
//           ArrayList<String> fromDb =  productDetailsRepo.findAllSimilarProductQuery(productDetails.color, productDetails.brand, listOfSkuIdsFromWeaviateDb, 15-listOfSkuIdsFromWeaviateDb.size());
//            finalListOfSku_Ids.addAll(fromDb);
//        }
        this.saveSkuIdsToRedis(finalListOfSku_Ids, requestPayload.skuId + "YES");
        return prepareProductDetails(finalListOfSku_Ids);
    }

    public ArrayList<ResponsePayload> getCompletethelook(RequestPayload requestPayload){
        LinkedList<String> listOfSkuIdsFromRedis = getListOfSkuIdsFromRedis(requestPayload.skuId+"COMPLETE");
        if (listOfSkuIdsFromRedis.size() >0) {
            return prepareProductDetails(listOfSkuIdsFromRedis);
        }
        ProductDetailsModel productDetails = productDetailsRepo.findBySkuId(requestPayload.skuId);
        ArrayList<String>  listOfSkuIdsFromWeaviateDb = weaviateQueryService.getListOfProductsForCompleteTheLook(productDetails);
        listOfSkuIdsFromWeaviateDb.remove(requestPayload.skuId);
        LinkedList<String> finalListOfSku_Ids = new LinkedList<>(listOfSkuIdsFromWeaviateDb.subList(0,15));
        this.saveSkuIdsToRedis(finalListOfSku_Ids, requestPayload.skuId+"COMPLETE");
        return prepareProductDetails(finalListOfSku_Ids);
    }

    public void clearRedisForASkuId(String sku){
        template.delete(sku+"YES");
        template.delete(sku+"NO");
        template.delete(sku+"COMPLETE");
    }

    public ArrayList<ResponsePayload> getSimilarProductOfDifferentDesigner(RequestPayload requestPayload) {
        LinkedList<String> listOfSkuIdsFromRedis = getListOfSkuIdsFromRedis(requestPayload.skuId+"NO");
        if (listOfSkuIdsFromRedis.size() >0) {
            return prepareProductDetails(listOfSkuIdsFromRedis);
        }
        ProductDetailsModel productDetails = productDetailsRepo.findBySkuId(requestPayload.skuId);
        ArrayList<String> listOfSkuIdsFromWeaviateDb = new ArrayList<>();
        ArrayList<String> finalNotSameBrand = new ArrayList<>();
        finalNotSameBrand = weaviateQueryService.getSimilarProductsFromOtherDesigner(productDetails);
        String substring = requestPayload.skuId.substring(0, 4);
        for (String skuId : finalNotSameBrand) {
            if(!skuId.toLowerCase().contains(substring.toLowerCase())){
                listOfSkuIdsFromWeaviateDb.add(skuId);
                if(listOfSkuIdsFromWeaviateDb.size() == 15){
                    break;
                }
            }
        }
        listOfSkuIdsFromWeaviateDb.remove(requestPayload.skuId);
        LinkedList<String> finalListOfSku_Ids = new LinkedList<>();
        finalListOfSku_Ids.addAll(listOfSkuIdsFromWeaviateDb);
        this.saveSkuIdsToRedis(finalListOfSku_Ids, requestPayload.skuId+"NO");
        return prepareProductDetails(finalListOfSku_Ids);
    }

    private void saveSkuIdsToRedis(LinkedList<String> listOfSkuIds, String key ) {
        template.delete(key);
        template.opsForList().rightPushAll(key, listOfSkuIds.toArray(new String[0]));
    }

    private ArrayList<ResponsePayload> prepareProductDetails(LinkedList<String> listOfSkuIdsFromRedis) {
        ArrayList<ProductDetailsModel> listOfProducts =  productDetailsRepo.findByListOfIds(listOfSkuIdsFromRedis);
        ArrayList<ResponsePayload> responsePayloads = new ArrayList<>();
        for(ProductDetailsModel aProductDetailsModel : listOfProducts){
            responsePayloads.add(ResponsePayload.builder()
                    .skuId(aProductDetailsModel.sku_id)
                    .productId(aProductDetailsModel.product_id)
                    .title(aProductDetailsModel.title)
                    .discountedPrice(aProductDetailsModel.discount_in)
                    .regionSalePrice(aProductDetailsModel.price_in)
                    .brand(aProductDetailsModel.brand)
                    .imageLink(aProductDetailsModel.image_link)
                    .discount(aProductDetailsModel.discount_in)
                    .link(aProductDetailsModel.link)
                    .salePrice(aProductDetailsModel.special_price_in)
                    .price(aProductDetailsModel.price_in)
                    .build());
        }
        return responsePayloads;
    }

    private LinkedList<String> getListOfSkuIdsFromRedis(String key) {
        Object listOfSkus = template.opsForList().range(key, 0, -1);
        LinkedList<String> listOfSkuIdsFromRedis = new LinkedList<>();
        listOfSkuIdsFromRedis.addAll((ArrayList<String>) listOfSkus);
        return listOfSkuIdsFromRedis;
    }
}
