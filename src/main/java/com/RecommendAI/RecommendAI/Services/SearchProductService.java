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

    public ArrayList<ResponsePayload> getSimilarProduct(RequestPayload requestPayload, boolean fromSameBrand) {
        LinkedList<String> listOfSkuIdsFromRedis = getListOfSkuIdsFromRedis(requestPayload.skuId, fromSameBrand, false);
        if (listOfSkuIdsFromRedis.size() >0) {
            return prepareProductDetails(listOfSkuIdsFromRedis);
        }
        ProductDetailsModel productDetails = productDetailsRepo.findBySkuId(requestPayload.skuId);
        ArrayList<String> listOfSkuIdsFromWeaviateDb = new ArrayList<>();
        if(fromSameBrand){
            listOfSkuIdsFromWeaviateDb = weaviateQueryService.getListOfSkuIdsFromWeaviateDb(productDetails);
        } else {
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
        }
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
        this.saveToRedis(finalListOfSku_Ids, requestPayload.skuId, fromSameBrand);


        return prepareProductDetails(finalListOfSku_Ids);
    }

    private void saveToRedis(LinkedList<String> listOfSkuIdsFromWeaviateDb, String skuId, boolean fromSameBrand) {
        String a = "NO";
        if (fromSameBrand){
            a = "YES";
        }
        template.delete(skuId+a);
        template.opsForList().rightPushAll(skuId+a, listOfSkuIdsFromWeaviateDb.toArray(new String[0]));
    }

    private ArrayList<ResponsePayload> prepareProductDetails(LinkedList<String> listOfSkuIdsFromRedis) {
        ArrayList<ProductDetailsModel> listOfProducts =  productDetailsRepo.findByListOfIds(listOfSkuIdsFromRedis);
        ArrayList<ResponsePayload> responsePayloads = new ArrayList<>();
        for(ProductDetailsModel aProductDetailsModel : listOfProducts){
            responsePayloads.add(ResponsePayload.builder()
                            .skuId(aProductDetailsModel.sku_id)
                            .productId(aProductDetailsModel.product_id)
                            .title(aProductDetailsModel.title)
                            .discountedPrice(aProductDetailsModel.discounted_price)
                            .regionSalePrice(aProductDetailsModel.region_sale_price)
                            .brand(aProductDetailsModel.brand)
                            .imageLink(aProductDetailsModel.image_link)
                            .discount(aProductDetailsModel.discount)
                            .link(aProductDetailsModel.link)
                            .salePrice(aProductDetailsModel.sale_price)
                            .price(aProductDetailsModel.price)
                    .build());
        }
        return responsePayloads;
    }

    public LinkedList<String> getListOfSkuIdsFromRedis(String skuId, boolean fromSameBrand, boolean isCompleteTheLook){
        String b = "NO";
        if(fromSameBrand){
            b="YES";
        }
        if(isCompleteTheLook){
            b="COMPLETE";
        }
        Object listOfSkus = template.opsForList().range(skuId+b, 0, -1);
        LinkedList listOfSkuIdsFromRedis = new LinkedList<>();
        for(String a : (ArrayList<String>) listOfSkus){
            listOfSkuIdsFromRedis.add(a);
        }
        return (LinkedList<String>) listOfSkuIdsFromRedis;
    }

    public ArrayList<ResponsePayload> getCompletethelook(RequestPayload requestPayload, boolean fromSameBrand){
        LinkedList<String> listOfSkuIdsFromRedis = getListOfSkuIdsFromRedis(requestPayload.skuId, fromSameBrand, true);
        if (listOfSkuIdsFromRedis.size() >0) {
            return prepareProductDetails(listOfSkuIdsFromRedis);
        }
        ProductDetailsModel productDetails = productDetailsRepo.findBySkuId(requestPayload.skuId);
        ArrayList<String>  listOfSkuIdsFromWeaviateDb = weaviateQueryService.getListOfProductsForCompleteTheLook(productDetails);
        listOfSkuIdsFromWeaviateDb.remove(requestPayload.skuId);
        LinkedList<String> finalListOfSku_Ids = new LinkedList<>(listOfSkuIdsFromWeaviateDb);
        if(finalListOfSku_Ids.size() >15) {
            int size = finalListOfSku_Ids.size();
            for(int i =15 ;i<size;i++){
                finalListOfSku_Ids.remove(i);
            }
        }
        this.saveToRedis(finalListOfSku_Ids, requestPayload.skuId, fromSameBrand);
        return prepareProductDetails(finalListOfSku_Ids);
    }

    public void clearRedisForASkuId(String sku){
        template.delete(sku+"YES");
        template.delete(sku+"NO");
        template.delete(sku+"COMPLETE");
    }
}
