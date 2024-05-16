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
import java.util.List;
import java.util.Optional;

@Service
public class SearchProductService {
    private final Logger LOGGER = LoggerFactory.getLogger(SearchProductService.class);
    public ProductDetailsRepo productDetailsRepo;
    private final int SKU_LENGTH_LIMIT = 15;

    @Autowired
    private RedisTemplate template;

    @Autowired
    private WeaviateQueryService weaviateQueryService;

    public SearchProductService(ProductDetailsRepo productDetailsRepo){
        this.productDetailsRepo = productDetailsRepo;
    }

    public LinkedList<ResponsePayload> getSimilarProductOfDifferentDesigner(RequestPayload requestPayload) {
        LinkedList<String> listOfSkuIdsFromRedis = getListOfSkuIdsFromRedis(requestPayload.skuId+"NO");
        if (listOfSkuIdsFromRedis.size() >0) {
            return prepareProductDetails(listOfSkuIdsFromRedis);
        }
        ProductDetailsModel productDetail = productDetailsRepo.findBySkuId(requestPayload.skuId);
        LinkedList<String> listOfSkuIdsFromWeaviateDb = this.listOfSimilarSkuIdsFromWeaviateDb(productDetail, false);
        this.saveSkuIdsToRedis(listOfSkuIdsFromWeaviateDb, requestPayload.skuId+"NO");
        return prepareProductDetails(listOfSkuIdsFromWeaviateDb);
    }

    public LinkedList<ResponsePayload> getSimilarProductOfSameDesigner(RequestPayload requestPayload) {
        LinkedList<String> listOfSkuIdsFromRedis = getListOfSkuIdsFromRedis(requestPayload.skuId+"YES");
        if (listOfSkuIdsFromRedis.size() >0) {
            return prepareProductDetails(listOfSkuIdsFromRedis);
        }
        ProductDetailsModel productDetails = productDetailsRepo.findBySkuId(requestPayload.skuId);
        LinkedList<String> listOfSkuIdsFromWeaviateDb = new LinkedList<>();
        listOfSkuIdsFromWeaviateDb = this.listOfSimilarSkuIdsFromWeaviateDb(productDetails, true);
        if(listOfSkuIdsFromWeaviateDb.contains(requestPayload.skuId)){
            listOfSkuIdsFromWeaviateDb.remove(requestPayload.skuId);
        }
        if (listOfSkuIdsFromWeaviateDb.size() > 0) {
            this.saveSkuIdsToRedis(listOfSkuIdsFromWeaviateDb, requestPayload.skuId + "YES");
        }
        return prepareProductDetails(listOfSkuIdsFromWeaviateDb);
    }

    public LinkedList<ResponsePayload> getCompleteThelook(RequestPayload requestPayload){
        LinkedList<String> listOfSkuIdsFromRedis = getListOfSkuIdsFromRedis(requestPayload.skuId+"COMPLETE");
        if (listOfSkuIdsFromRedis.size() >0) {
            return prepareProductDetails(listOfSkuIdsFromRedis);
        }
        ProductDetailsModel productDetails = productDetailsRepo.findBySkuId(requestPayload.skuId);
        ArrayList<String>  listOfSkuIdsFromWeaviateDb = weaviateQueryService.getListOfProductsForCompleteTheLook(productDetails);
        listOfSkuIdsFromWeaviateDb.remove(requestPayload.skuId);
        LinkedList<String> finalListOfSku_Ids = new LinkedList<>(listOfSkuIdsFromWeaviateDb.subList(0,SKU_LENGTH_LIMIT));
        this.saveSkuIdsToRedis(finalListOfSku_Ids, requestPayload.skuId+"COMPLETE");
        return prepareProductDetails(finalListOfSku_Ids);
    }

    public void clearRedisForASkuId(String sku){
        template.delete(sku+"YES");
        template.delete(sku+"NO");
        template.delete(sku+"COMPLETE");
    }

    private LinkedList<ResponsePayload> prepareProductDetails(LinkedList<String> listOfSkuIdsFromRedis) {
        ArrayList<ProductDetailsModel> listOfProducts =  productDetailsRepo.findByListOfIds(listOfSkuIdsFromRedis);
        LinkedList<ResponsePayload> responsePayloads = new LinkedList<>();

        for (String aSku : listOfSkuIdsFromRedis){
            Optional a = listOfProducts.stream()
                    .filter(aProduct -> aProduct.getSku_id().equalsIgnoreCase(aSku))
                    .findFirst();

            if(a.isPresent()){
                ProductDetailsModel aProductDetailsModel = (ProductDetailsModel) a.get();
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
        }
        return responsePayloads;
    }

    private LinkedList<String> getListOfSkuIdsFromRedis(String key) {
        Object listOfSkus = template.opsForList().range(key, 0, -1);
        LinkedList<String> listOfSkuIdsFromRedis = new LinkedList<>();
        listOfSkuIdsFromRedis.addAll((ArrayList<String>) listOfSkus);
        return listOfSkuIdsFromRedis;
    }

    private LinkedList<String> listOfSimilarSkuIdsFromWeaviateDb(ProductDetailsModel productDetails, boolean isSameBrand) {
        LinkedList<String> listOfSkuIdsFromWeaviateDb = new LinkedList<>();
        listOfSkuIdsFromWeaviateDb = weaviateQueryService.getListOfSkuIdsFromWeaviateDb(productDetails, weaviateQueryService.filterLevelOne(productDetails, isSameBrand),isSameBrand);
        if(listOfSkuIdsFromWeaviateDb.size() < SKU_LENGTH_LIMIT) {
            LinkedList<String> listOfSkuIdsFromLevelTwoFilter = weaviateQueryService.getListOfSkuIdsFromWeaviateDb(productDetails, weaviateQueryService.filterLevelTwo(productDetails, isSameBrand),isSameBrand);
            listOfSkuIdsFromWeaviateDb =this.addSkuIdToExistingList(listOfSkuIdsFromWeaviateDb, listOfSkuIdsFromLevelTwoFilter);
        }
        if(listOfSkuIdsFromWeaviateDb.size() < SKU_LENGTH_LIMIT) {
            LinkedList<String> listOfSkuIdsFromLevelThreeFilter = weaviateQueryService.getListOfSkuIdsFromWeaviateDb(productDetails, weaviateQueryService.filterLevelThree(productDetails, isSameBrand),isSameBrand);
            listOfSkuIdsFromWeaviateDb = this.addSkuIdToExistingList(listOfSkuIdsFromWeaviateDb, listOfSkuIdsFromLevelThreeFilter);
        }
        return this.getSkuIfOfLength(listOfSkuIdsFromWeaviateDb);
    }

    private void saveSkuIdsToRedis(LinkedList<String> listOfSkuIds, String key ) {
        template.delete(key);
        template.opsForList().rightPushAll(key, listOfSkuIds.toArray(new String[0]));
    }
    private LinkedList<String> addSkuIdToExistingList(LinkedList<String> listOfSkuIds, List<String> listOfSkuIdsFromDb) {
        for (String skuId : listOfSkuIdsFromDb) {
            if(!listOfSkuIds.contains(skuId)){
                listOfSkuIds.add(skuId);
            }
            if(listOfSkuIds.size() ==SKU_LENGTH_LIMIT){
                return listOfSkuIds;
            }
        }
        return listOfSkuIds;
    }

    private LinkedList getSkuIfOfLength(LinkedList<String> listOfSkuIds) {
        LinkedList<String> listOfSkuIdsnew = new LinkedList<>();
        for (String skuId : listOfSkuIds) {
            if (listOfSkuIdsnew.size() < SKU_LENGTH_LIMIT) {
                listOfSkuIdsnew.add(skuId);
            }
        }
        return listOfSkuIdsnew;
    }
}