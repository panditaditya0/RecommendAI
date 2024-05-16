package com.RecommendAI.RecommendAI.Services;

import com.RecommendAI.RecommendAI.Model.ChildCategoryModel;
import com.RecommendAI.RecommendAI.Model.ProductDetailsModel;
import com.RecommendAI.RecommendAI.Model.RequestPayload;
import com.RecommendAI.RecommendAI.Model.ResponsePayload;
import com.RecommendAI.RecommendAI.Repo.ProductDetailsRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SearchProductService {
    private final Logger LOGGER = LoggerFactory.getLogger(SearchProductService.class);
    public ProductDetailsRepo productDetailsRepo;
    private final int SKU_LENGTH_LIMIT = 15;
    private final int SKU_LENGTH_LIMIT_OTHER_BRAND = 100;
    private final List<String> JEWELLERY = new ArrayList<>(List.of("earrings","cuffs", "bracelets", "necklaces", "rings","bangles"
    ,"pendants", "brooches", "hand harness", "earcuffs", "head pieces", "body chains", "arm bands", "anklets", "nose rings"
    ,"maangtikas", "kaleeras", "cufflinks"));


    @Autowired
    private RedisTemplate template;

    @Autowired
    private WeaviateQueryService weaviateQueryService;

    public SearchProductService(ProductDetailsRepo productDetailsRepo){
        this.productDetailsRepo = productDetailsRepo;
    }

    public LinkedHashSet<ResponsePayload> getSimilarProductOfDifferentDesigner(RequestPayload requestPayload) {
        LinkedHashSet<String> listOfSkuIdsFromRedis = getListOfSkuIdsFromRedis(requestPayload.skuId+"NO");
        if (listOfSkuIdsFromRedis.size() >0) {
            return prepareProductDetails(listOfSkuIdsFromRedis);
        }
        ProductDetailsModel productDetail = productDetailsRepo.findBySkuId(requestPayload.skuId);
        LinkedHashSet<String> listOfSkuIdsFromWeaviateDb = this.listOfSimilarSkuIdsFromWeaviateDb(productDetail, false);
        this.saveSkuIdsToRedis(listOfSkuIdsFromWeaviateDb, requestPayload.skuId+"NO");
        return prepareProductDetails(listOfSkuIdsFromWeaviateDb);
    }

    public LinkedHashSet<ResponsePayload> getSimilarProductOfSameDesigner(RequestPayload requestPayload) {
        LinkedHashSet<String> listOfSkuIdsFromRedis = getListOfSkuIdsFromRedis(requestPayload.skuId+"YES");
        if (listOfSkuIdsFromRedis.size() >0) {
            return prepareProductDetails(listOfSkuIdsFromRedis);
        }
        ProductDetailsModel productDetails = productDetailsRepo.findBySkuId(requestPayload.skuId);
        LinkedHashSet<String> listOfSkuIdsFromWeaviateDb = new LinkedHashSet<>();
        listOfSkuIdsFromWeaviateDb = this.listOfSimilarSkuIdsFromWeaviateDb(productDetails, true);
        if(listOfSkuIdsFromWeaviateDb.contains(requestPayload.skuId)){
            listOfSkuIdsFromWeaviateDb.remove(requestPayload.skuId);
        }
        if (listOfSkuIdsFromWeaviateDb.size() > 0) {
            this.saveSkuIdsToRedis(listOfSkuIdsFromWeaviateDb, requestPayload.skuId + "YES");
        }
        return prepareProductDetails(listOfSkuIdsFromWeaviateDb);
    }

    public LinkedHashSet<ResponsePayload> getCompleteThelook(RequestPayload requestPayload){
        LinkedHashSet<String> listOfSkuIdsFromRedis = getListOfSkuIdsFromRedis(requestPayload.skuId+"COMPLETE");
        if (listOfSkuIdsFromRedis.size() >0) {
            return prepareProductDetails(listOfSkuIdsFromRedis);
        }
        ProductDetailsModel productDetails = productDetailsRepo.findBySkuId(requestPayload.skuId);
        List<String> randomFive = JEWELLERY.subList(0, Math.min(5, JEWELLERY.size()));
        LinkedList<String>  listOfSkuIdsFromWeaviateDb = new LinkedList<>();
        for(String childCategory : randomFive){

            LinkedHashSet<String> temp = weaviateQueryService.getListOfSkuIdsFromWeaviateDb(productDetails,weaviateQueryService.filterCompleteTheLookForCloths("clothing", childCategory),false, 3);
            if(temp.size()>0){
                listOfSkuIdsFromWeaviateDb.addAll(temp);
            }
        }
        listOfSkuIdsFromWeaviateDb.remove(requestPayload.skuId);
        LinkedHashSet<String> finalListOfSku_Ids = new LinkedHashSet<>(listOfSkuIdsFromWeaviateDb.subList(0,SKU_LENGTH_LIMIT));
        this.saveSkuIdsToRedis(finalListOfSku_Ids, requestPayload.skuId+"COMPLETE");
        return prepareProductDetails(finalListOfSku_Ids);
    }

    public void clearRedisForASkuId(String sku){
        template.delete(sku+"YES");
        template.delete(sku+"NO");
        template.delete(sku+"COMPLETE");
    }

    private LinkedHashSet<ResponsePayload> prepareProductDetails(LinkedHashSet<String> listOfSkuIdsFromRedis) {
        ArrayList<ProductDetailsModel> listOfProducts =  productDetailsRepo.findByListOfIds(listOfSkuIdsFromRedis);
        LinkedHashSet<ResponsePayload> responsePayloads = new LinkedHashSet<>();

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
    private LinkedHashSet<String> getListOfSkuIdsFromRedis(String key) {
        Object listOfSkus = template.opsForList().range(key, 0, -1);
        LinkedHashSet<String> listOfSkuIdsFromRedis = new LinkedHashSet<>();
        listOfSkuIdsFromRedis.addAll((ArrayList<String>) listOfSkus);
        return listOfSkuIdsFromRedis;
    }
    private LinkedHashSet<String> listOfSimilarSkuIdsFromWeaviateDb(ProductDetailsModel productDetails, boolean isSameBrand) {
        int limit =  isSameBrand? SKU_LENGTH_LIMIT:SKU_LENGTH_LIMIT_OTHER_BRAND;
        LinkedHashSet<String> listOfSkuIdsFromWeaviateDb = new LinkedHashSet<>();
        listOfSkuIdsFromWeaviateDb = weaviateQueryService.getListOfSkuIdsFromWeaviateDb(productDetails, weaviateQueryService.filterLevelOne(productDetails,productDetails.child_categories, isSameBrand),isSameBrand, limit);
        for (ChildCategoryModel aChildCategory : productDetails.child_categories){
            Set<ChildCategoryModel> temp = new HashSet<>(){
                {
                    add(aChildCategory);
                }
            };
            LinkedHashSet<String> listOhChildCategories = weaviateQueryService.getListOfSkuIdsFromWeaviateDb(productDetails, weaviateQueryService.filterLevelOne(productDetails, temp,isSameBrand),isSameBrand,limit);
            listOfSkuIdsFromWeaviateDb =this.addSkuIdToExistingList(listOfSkuIdsFromWeaviateDb, listOhChildCategories);
        }
        if(listOfSkuIdsFromWeaviateDb.size() < SKU_LENGTH_LIMIT) {
            LinkedHashSet<String> listOfSkuIdsFromLevelTwoFilter = weaviateQueryService.getListOfSkuIdsFromWeaviateDb(productDetails, weaviateQueryService.filterLevelTwo(productDetails, isSameBrand),isSameBrand,limit);
            listOfSkuIdsFromWeaviateDb =this.addSkuIdToExistingList(listOfSkuIdsFromWeaviateDb, listOfSkuIdsFromLevelTwoFilter);
        }
        if(listOfSkuIdsFromWeaviateDb.size() < SKU_LENGTH_LIMIT) {
            LinkedHashSet<String> listOfSkuIdsFromLevelThreeFilter = weaviateQueryService.getListOfSkuIdsFromWeaviateDb(productDetails, weaviateQueryService.filterLevelThree(productDetails, isSameBrand),isSameBrand,limit);
            listOfSkuIdsFromWeaviateDb = this.addSkuIdToExistingList(listOfSkuIdsFromWeaviateDb, listOfSkuIdsFromLevelThreeFilter);
        }
        return this.getSkuIfOfLength(listOfSkuIdsFromWeaviateDb);
    }
    private void saveSkuIdsToRedis(LinkedHashSet<String> listOfSkuIds, String key ) {
        template.delete(key);
        template.opsForList().rightPushAll(key, listOfSkuIds.toArray(new String[0]));
    }
    private LinkedHashSet<String> addSkuIdToExistingList(LinkedHashSet<String> listOfSkuIds, LinkedHashSet<String> listOfSkuIdsFromDb) {
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
    private LinkedHashSet getSkuIfOfLength(LinkedHashSet<String> listOfSkuIds) {
        LinkedHashSet<String> listOfSkuIdsnew = new LinkedHashSet<>();
        for (String skuId : listOfSkuIds) {
            if (listOfSkuIdsnew.size() < SKU_LENGTH_LIMIT) {
                listOfSkuIdsnew.add(skuId);
            }
        }
        return listOfSkuIdsnew;
    }
}