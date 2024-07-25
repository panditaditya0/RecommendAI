package com.RecommendAI.RecommendAI.Services.impl;

import com.RecommendAI.RecommendAI.Dto.RequestDtos.RequestPayload;
import com.RecommendAI.RecommendAI.Dto.ResponsePayload;
import com.RecommendAI.RecommendAI.Dto.ResponseProductDetails;
import com.RecommendAI.RecommendAI.Exceptions.ImageNotInDbException;
import com.RecommendAI.RecommendAI.Exceptions.ProductNotInDbException;
import com.RecommendAI.RecommendAI.Model.*;
import com.RecommendAI.RecommendAI.Repo.ProductDetailsRepo;
import com.RecommendAI.RecommendAI.Repo.UsersRepo;
import com.RecommendAI.RecommendAI.Services.CacheService;
import com.RecommendAI.RecommendAI.Services.EventStreamingPlatformService;
import com.RecommendAI.RecommendAI.Services.SearchProductService;
import com.RecommendAI.RecommendAI.Services.VectorDatabaseService;
import io.weaviate.client.v1.filters.Operator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchProductServiceImpl implements SearchProductService {
    private final ProductDetailsRepo productDetailsRepo;
    private final UsersRepo usersRepo;
    private final VectorDatabaseService vectorDatabaseService;
    private final EventStreamingPlatformService kafkaService;
    private final CacheService cacheService;

    private final String SUCCESS = "success";
    private final String FAILED = "failed";
    private final String IMAGE_NOT_IN_DB = "Image not present in Database";
    private final String PRODUCT_NOT_IN_DB = "Product not present in Database";
    private final String FROM_CACHE = "From Cache";
    private final String FROM_VDB = "From vDb";
    private final int SKU_LENGTH_LIMIT = 100;
    private final int SKU_LENGTH_LIMIT_OTHER_BRAND = 50;
    private final List<String> JEWELLERY = new ArrayList<>(List.of("earrings", "cuffs", "bracelets", "necklaces", "rings", "bangles"
            , "pendants", "brooches", "hand harness", "earcuffs", "head pieces", "body chains", "arm bands", "anklets", "nose rings"
            , "maangtikas", "kaleeras", "cufflinks"));

    @Value("${PRICE_RANGE_PERCENTAGE}")
    private double PRICE_RANGE_PERCENTAGE;

    @Value("${KAFKA_DOWNLOAD_IMAGE_TOPIC}")
    private String KAFKA_DOWNLOAD_IMAGE_TOPIC;

    @Override
    public ResponsePayload getSimilarProductOfDifferentDesigner(RequestPayload requestPayload) throws ImageNotInDbException, ProductNotInDbException {
        LinkedHashSet<String> listOfSkuIdsFromRedis = cacheService.getListOfSkuIdsFromCache(requestPayload.skuId + "NO");
        if (!listOfSkuIdsFromRedis.isEmpty()) {
            return this.responseBuilder(prepareProductDetails(listOfSkuIdsFromRedis, requestPayload)
                    , FROM_CACHE
                    , SUCCESS);
        }
        ProductDetailsModel productDetails = this.getProductDetails(requestPayload.skuId);
        LinkedHashSet<String> listOfSkuIdsFromWeaviateDb = this.listOfSimilarSkuIdsFromWeaviateDb(productDetails, false);
        cacheService.SetListOfSkuIdsToCache(listOfSkuIdsFromWeaviateDb,requestPayload.skuId + "NO");
        LinkedHashSet<ResponseProductDetails> listOfAllSimilarProducts = prepareProductDetails(listOfSkuIdsFromWeaviateDb, requestPayload);
        return this.responseBuilder(listOfAllSimilarProducts
                , FROM_VDB
                , SUCCESS);

    }

//    private LinkedHashSet<ResponseProductDetails> sortProductsByPriceRange(ProductDetailsModel productDetails, LinkedHashSet<ResponseProductDetails> listOfAllSimilarProducts) {
//        double minPrice = productDetails.getPrice_in() * (1 - PRICE_RANGE_PERCENTAGE /100);
//        double maxPrice = productDetails.getPrice_in() *  (1 + PRICE_RANGE_PERCENTAGE/100);
//        String color = productDetails.getColor();
//        LinkedHashSet<ResponseProductDetails> sortedList = new LinkedHashSet<>();
//        LinkedHashSet<ResponseProductDetails> sortedListOutIfRange = new LinkedHashSet<>();
//        Iterator it = listOfAllSimilarProducts.iterator();
//        while (it.hasNext()) {
//            ResponseProductDetails productDetail = (ResponseProductDetails) it.next();
//            if(isWithinRange(productDetail.getPrice(), minPrice, maxPrice) && color == productDetail.getColor()){
//                sortedList.add(productDetail);
//            } else {
//                sortedListOutIfRange.add(productDetail);
//            }
//        }
//        sortedList.addAll(sortedListOutIfRange);
//        return sortedList;
//    }

    @Override
    public ResponsePayload getSimilarProductOfSameDesigner(RequestPayload requestPayload) throws ImageNotInDbException, ProductNotInDbException {
        LinkedHashSet<String> listOfSkuIdsFromRedis = cacheService.getListOfSkuIdsFromCache(requestPayload.skuId +"YES");
        if (listOfSkuIdsFromRedis.size() > 0) {
            return this.responseBuilder(prepareProductDetails(listOfSkuIdsFromRedis, requestPayload)
                    , FROM_CACHE
                    , SUCCESS);
        }
        ProductDetailsModel productDetails = this.getProductDetails(requestPayload.skuId);

        LinkedHashSet<String> listOfSkuIdsFromWeaviateDb = this.listOfSimilarSkuIdsFromWeaviateDb(productDetails, true);
        if (listOfSkuIdsFromWeaviateDb.contains(requestPayload.skuId)) {
            listOfSkuIdsFromWeaviateDb.remove(requestPayload.skuId);
        }
        if (listOfSkuIdsFromWeaviateDb.size() > 0) {
            cacheService.SetListOfSkuIdsToCache(listOfSkuIdsFromWeaviateDb,requestPayload.skuId + "YES");
        }
        return this.responseBuilder(prepareProductDetails(listOfSkuIdsFromWeaviateDb, requestPayload)
                , FROM_VDB
                , SUCCESS);
    }

    @Override
    public ResponsePayload getCompleteThelook(RequestPayload requestPayload) throws ImageNotInDbException, ProductNotInDbException {
        LinkedHashSet<String> listOfSkuIdsFromRedis = cacheService.getListOfSkuIdsFromCache(requestPayload.skuId + "COMPLETE");
        if (listOfSkuIdsFromRedis.size() > 0) {
            return this.responseBuilder(prepareProductDetails(listOfSkuIdsFromRedis, requestPayload)
                    , FROM_CACHE
                    , SUCCESS);
        }
        ProductDetailsModel productDetails = this.getProductDetails(requestPayload.skuId);
        ArrayList<String> randomFive = new ArrayList<>(JEWELLERY.subList(0, Math.min(5, JEWELLERY.size())));
        LinkedHashSet<String> listOfSkuIdsFromWeaviateDb = vectorDatabaseService.getListOfSkuIdsFromWeaviateDb(productDetails, vectorDatabaseService.filterCompleteTheLookForCloths(randomFive), false, 6, Operator.Or);
        listOfSkuIdsFromWeaviateDb.remove(requestPayload.skuId);
        if (listOfSkuIdsFromWeaviateDb.size() > 15) {
            listOfSkuIdsFromWeaviateDb = new LinkedHashSet<>(new ArrayList<>(listOfSkuIdsFromWeaviateDb).subList(0, SKU_LENGTH_LIMIT));
        }

        if (listOfSkuIdsFromWeaviateDb.isEmpty()){
           return responseBuilder(new LinkedHashSet<ResponseProductDetails>(), FROM_VDB, "No products found");
        }

        cacheService.SetListOfSkuIdsToCache(listOfSkuIdsFromWeaviateDb,requestPayload.skuId + "COMPLETE");

        return this.responseBuilder(prepareProductDetails(listOfSkuIdsFromWeaviateDb, requestPayload)
                , FROM_VDB
                , SUCCESS);
    }

    @Override
    public ResponsePayload getRecentlyViewed(RequestPayload requestPayload) {
        LinkedHashSet<String> listOfSkus = new LinkedHashSet<String>();
        if (requestPayload.user_id == "" && requestPayload.mad_uuid == "") {
            throw new RuntimeException("user_id/mad_uuid is mandatory");
        }
        if (requestPayload.user_id != "") {
            Users alreadyAUser = usersRepo.getUsinguserId(UUID.fromString(requestPayload.user_id));
            listOfSkus = new LinkedHashSet<>(Arrays.stream(alreadyAUser.getSkuIds().get(0).split(",")).toList());
        } else {
            Users alreadyAMadUser = usersRepo.getUsingMadId(UUID.fromString(requestPayload.mad_uuid));
            listOfSkus = new LinkedHashSet<>(alreadyAMadUser.getSkuIds());
            listOfSkus.remove(requestPayload.skuId);
        }

        return this.responseBuilder(prepareProductDetails(listOfSkus, requestPayload)
                , FROM_VDB
                , SUCCESS);
    }

    private LinkedHashSet<ResponseProductDetails> prepareProductDetails(LinkedHashSet<String> listOfSkuIdsFromRedis, RequestPayload requestPayload) {
        LinkedHashSet<String> requiredList = new LinkedHashSet<>();
        Iterator it = listOfSkuIdsFromRedis.iterator();
        while (it.hasNext() && requiredList.size() <= requestPayload.num_results.get(0)) {
            requiredList.add((String) it.next());
        }
        ArrayList<ProductDetailsModel> listOfProducts = productDetailsRepo.findByListOfIds(requiredList);
        LinkedHashSet<ResponseProductDetails> responsePayloads = new LinkedHashSet<>();

        for (String aSku : requiredList) {
            Optional a = listOfProducts.stream()
                    .filter(aProduct -> aProduct.getSku_id().equalsIgnoreCase(aSku))
                    .findFirst();

            if (a.isPresent()) {
                ProductDetailsModel aProductDetailsModel = (ProductDetailsModel) a.get();

                double discounted_price, region_sale_price, sale_price, price, discount;
                if (requestPayload.region.equalsIgnoreCase("ind")) {
                    price = aProductDetailsModel.price_in;
                    discount = aProductDetailsModel.discount_in;
                    sale_price = discount == 0.0 ? aProductDetailsModel.price_in : aProductDetailsModel.special_price_in;
                    region_sale_price = sale_price;
                    discounted_price = sale_price;
                } else if (requestPayload.region.equalsIgnoreCase("us")) {
                    price = aProductDetailsModel.price_us;
                    discount = aProductDetailsModel.discount_us;
                    sale_price = discount == 0.0 ? aProductDetailsModel.price_us : aProductDetailsModel.special_price_us;
                    region_sale_price = sale_price;
                    discounted_price = sale_price;
                } else {
                    price = aProductDetailsModel.price_row;
                    discount = aProductDetailsModel.discount_row;
                    sale_price = discount == 0.0 ? aProductDetailsModel.special_price_row : aProductDetailsModel.special_price_row;
                    region_sale_price = sale_price;
                    discounted_price = sale_price;
                }

                responsePayloads.add(ResponseProductDetails.builder()
                        .skuId(aProductDetailsModel.sku_id)
                        .productId(aProductDetailsModel.product_id)
                        .title(aProductDetailsModel.title)
                        .discountedPrice(discounted_price * requestPayload.extra_params.exchange_rate)
                        .regionSalePrice(region_sale_price * requestPayload.extra_params.exchange_rate)
                        .brand(aProductDetailsModel.brand)
                        .imageLink("https://img.perniaspopupshop.com/catalog/product" + aProductDetailsModel.image_link)
                        .discount(aProductDetailsModel.discount_in)
                        .link(aProductDetailsModel.link)
                        .salePrice(sale_price * requestPayload.extra_params.exchange_rate)
                        .price(price * requestPayload.extra_params.exchange_rate)
                        .color(aProductDetailsModel.color)
                        .build());
            }
        }
        return responsePayloads;
    }

    private static boolean isWithinRange(double price, double minPrice, double maxPrice) {
        return price >= minPrice && price <= maxPrice;
    }

    public void saveSkuIdToRecentlyViewed(RequestPayload requestPayload) {
        if (requestPayload.user_id == "" && requestPayload.mad_uuid == "") {
            throw new RuntimeException("user_id/mad_uuid is mandatory");
        }
        if (requestPayload.user_id != "") {
            Users alreadyAUser = usersRepo.getUsinguserId(UUID.fromString(requestPayload.user_id));
            List<String> listOfSkus  = Arrays.stream(alreadyAUser.getSkuIds().get(0).split(",")).toList();
            processRecentlyViewedSkus(alreadyAUser, requestPayload.skuId, listOfSkus);
        } else {
            Users userSkuDetails = usersRepo.getUsingMadId(UUID.fromString(requestPayload.mad_uuid));
            List<String> listOfSkus = new ArrayList<>();

            if (null != userSkuDetails) {
                processRecentlyViewedSkus(userSkuDetails, requestPayload.skuId, userSkuDetails.skuIds);
            } else {
                userSkuDetails = new Users();
                userSkuDetails.setMad_id(UUID.fromString(requestPayload.mad_uuid));
                processRecentlyViewedSkus(userSkuDetails, requestPayload.skuId, listOfSkus);
            }
        }
    }

    private LinkedHashSet<String> listOfSimilarSkuIdsFromWeaviateDb(ProductDetailsModel productDetails, boolean isSameBrand) {
        int limit = isSameBrand ? SKU_LENGTH_LIMIT : SKU_LENGTH_LIMIT_OTHER_BRAND;
        LinkedHashSet<String> listOfSkuIdsFromWeaviateDb = new LinkedHashSet<>();
        listOfSkuIdsFromWeaviateDb = vectorDatabaseService.getListOfSkuIdsFromWeaviateDb(productDetails, vectorDatabaseService.filterLevelOne(productDetails, isSameBrand), isSameBrand, limit, Operator.And);
        if(!isSameBrand && !listOfSkuIdsFromWeaviateDb.isEmpty()){
            listOfSkuIdsFromWeaviateDb = sortAsPrPrice(productDetails,listOfSkuIdsFromWeaviateDb);
        }
        if (listOfSkuIdsFromWeaviateDb.size() < SKU_LENGTH_LIMIT) {
            LinkedHashSet<String> listOfSkuIdsFromLevelTwoFilter = vectorDatabaseService.getListOfSkuIdsFromWeaviateDb(productDetails, vectorDatabaseService.filterLevelTwo(productDetails, isSameBrand), isSameBrand, limit, Operator.And);
            if(!isSameBrand && !listOfSkuIdsFromLevelTwoFilter.isEmpty()){
                listOfSkuIdsFromLevelTwoFilter = sortAsPrPrice(productDetails,listOfSkuIdsFromLevelTwoFilter);
            }
            listOfSkuIdsFromWeaviateDb = this.addSkuIdToExistingList(listOfSkuIdsFromWeaviateDb, listOfSkuIdsFromLevelTwoFilter);
        }
        if (listOfSkuIdsFromWeaviateDb.size() < SKU_LENGTH_LIMIT) {
            LinkedHashSet<String> listOfSkuIdsFromLevelThreeFilter = vectorDatabaseService.getListOfSkuIdsFromWeaviateDb(productDetails, vectorDatabaseService.filterLevelThree(productDetails, isSameBrand), isSameBrand, limit, Operator.And);
            if(!isSameBrand && !listOfSkuIdsFromLevelThreeFilter.isEmpty()){
                listOfSkuIdsFromLevelThreeFilter = sortAsPrPrice(productDetails,listOfSkuIdsFromLevelThreeFilter);
            }
            listOfSkuIdsFromWeaviateDb = this.addSkuIdToExistingList(listOfSkuIdsFromWeaviateDb, listOfSkuIdsFromLevelThreeFilter);
        }
        return this.getSkuIfOfLength(listOfSkuIdsFromWeaviateDb);
    }

    private LinkedHashSet<String> addSkuIdToExistingList(LinkedHashSet<String> listOfSkuIds, LinkedHashSet<String> listOfSkuIdsFromDb) {
        for (String skuId : listOfSkuIdsFromDb) {
            if (!listOfSkuIds.contains(skuId)) {
                listOfSkuIds.add(skuId);
            }
            if (listOfSkuIds.size() == SKU_LENGTH_LIMIT) {
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

    private void processRecentlyViewedSkus(Users user, String skuId, List<String> listOfSkus) {
        LinkedHashSet<String> finalListOfSku = new LinkedHashSet<String>();
        finalListOfSku.add(skuId);
        if (listOfSkus.size() == 0) {
            user.setSkuIds(finalListOfSku.stream().toList());
        } else {
            for (String skuToAdd : listOfSkus) {
                finalListOfSku.add(skuToAdd);
                if (finalListOfSku.size() == 15) {
                    break;
                }
            }
            user.setSkuIds(finalListOfSku.stream().toList());
        }
        usersRepo.save(user);
    }

    private ResponsePayload responseBuilder(LinkedHashSet<ResponseProductDetails> listOfProducts, String message, String status) {
        List<LinkedHashSet<ResponseProductDetails>> listOfProducts2 = new ArrayList<>();
        listOfProducts2.add(listOfProducts);
        return new ResponsePayload()
                .builder()
                .message(message)
                .data(listOfProducts2)
                .status(status)
                .build();
    }

    private ProductDetailsModel getProductDetails(String skuId) throws ProductNotInDbException, ImageNotInDbException {
        ProductDetailsModel productDetails = productDetailsRepo.findBySkuId(skuId);
        if (productDetails == null) {
            throw new ProductNotInDbException(PRODUCT_NOT_IN_DB);
        }
        if (productDetails.base64Image == null && productDetails.image_link != null) {
            kafkaService.sendMessage(KAFKA_DOWNLOAD_IMAGE_TOPIC, String.valueOf(productDetails.entity_id));
            throw new ImageNotInDbException(IMAGE_NOT_IN_DB);
        }
        return productDetails;
    }

    private LinkedHashSet<String> sortAsPrPrice(ProductDetailsModel productDetails, LinkedHashSet<String> skus){
        double minPrice = productDetails.getPrice_in() * (1 - PRICE_RANGE_PERCENTAGE /100);
        double maxPrice = productDetails.getPrice_in() *  (1 + PRICE_RANGE_PERCENTAGE/100);
        String color = productDetails.getColor();
        LinkedHashSet<String> sortedList = new LinkedHashSet<>();
        LinkedHashSet<String> sortedListOutIfRange = new LinkedHashSet<>();
        ArrayList<ProductDetailsModel> listOfProducts = productDetailsRepo.findByListOfIds(skus);
        Iterator it = listOfProducts.iterator();
        while (it.hasNext()) {
            ProductDetailsModel productDetail = (ProductDetailsModel) it.next();
            if(isWithinRange(productDetail.getPrice_in(), minPrice, maxPrice)){
                sortedList.add(productDetail.getSku_id());
            } else {
                sortedListOutIfRange.add(productDetail.getSku_id());
            }
        }
        sortedList.addAll(sortedListOutIfRange);
        return sortedList;
    }
}