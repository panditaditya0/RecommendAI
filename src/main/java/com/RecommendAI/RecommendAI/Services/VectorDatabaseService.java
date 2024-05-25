package com.RecommendAI.RecommendAI.Services;

import com.RecommendAI.RecommendAI.Model.ProductDetailsModel;
import io.weaviate.client.v1.filters.WhereFilter;

import java.util.ArrayList;
import java.util.LinkedHashSet;

public interface VectorDatabaseService {
    LinkedHashSet<String> getListOfSkuIdsFromWeaviateDb(ProductDetailsModel productDetailsModel, WhereFilter[] whereFilters, boolean isSameBrand, int limit, String operator);
    ArrayList<String> getListOfProductsForCompleteTheLook(ProductDetailsModel productDetails);
    WhereFilter[] filterLevelOne(ProductDetailsModel productDetails, boolean isSameBrand);
    WhereFilter[] filterLevelTwo(ProductDetailsModel productDetails, boolean isSameBrand);
    WhereFilter[] filterLevelThree(ProductDetailsModel productDetails, boolean isSameBrand);
    WhereFilter[] filterCompleteTheLookForCloths( ArrayList<String> childCategories);
}