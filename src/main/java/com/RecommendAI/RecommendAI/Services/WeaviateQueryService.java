package com.RecommendAI.RecommendAI.Services;

import com.RecommendAI.RecommendAI.Config.WeaviateConfig;
import com.RecommendAI.RecommendAI.Model.ChildCategoryModel;
import com.RecommendAI.RecommendAI.Model.ProductDetailsModel;
import com.google.gson.*;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.filters.Operator;
import io.weaviate.client.v1.filters.WhereFilter;
import io.weaviate.client.v1.graphql.model.GraphQLResponse;
import io.weaviate.client.v1.graphql.query.argument.NearImageArgument;
import io.weaviate.client.v1.graphql.query.argument.WhereArgument;
import io.weaviate.client.v1.graphql.query.fields.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class WeaviateQueryService {

    @Autowired
    private WeaviateConfig weaviateConfig;

    public LinkedList<String> getListOfSkuIdsFromWeaviateDb(ProductDetailsModel productDetailsModel, WhereFilter[] whereFilters, boolean isSameBrand) {
        int limit = isSameBrand ? 15 : 50;
        WeaviateClient client = weaviateConfig.weaviateClientMethod();
        NearImageArgument base64Image = NearImageArgument.builder().image(productDetailsModel.base64Image).build();

        WhereFilter allFilters = WhereFilter.builder().operator(Operator.And).operands(whereFilters).build();
        WhereArgument whereArgument =  WhereArgument.builder().filter(allFilters).build();

        Result<GraphQLResponse> result = client.graphQL().get()
                .withClassName("TestImg16")
                .withNearImage(base64Image)
                .withWhere(whereArgument)
                .withFields(Field.builder().name("sku_id").build())
                .withLimit(limit)
                .run();

        String jsonString = new GsonBuilder()
                .setPrettyPrinting()
                .create()
                .toJson(result
                        .getResult()
                        .getData());

        if(jsonString.equalsIgnoreCase("null")){
            throw new RuntimeException(String.valueOf(result.getResult().getErrors()[0].getMessage()));
        }

        JsonArray testImgArray =  new JsonParser()
                .parse(jsonString)
                .getAsJsonObject()
                .getAsJsonObject("Get")
                .getAsJsonArray("TestImg16");

        LinkedList<String> skuList = new LinkedList<>();
        String substring = productDetailsModel.sku_id.substring(0, 4);

        for (int i = 0; i < testImgArray.size(); i++) {
            JsonObject item = testImgArray.get(i).getAsJsonObject();
            String skuId = item.get("sku_id").getAsString();
            if(skuId.equalsIgnoreCase(productDetailsModel.sku_id)){
                continue;
            }
            if(!isSameBrand){
                if(!skuId.toLowerCase().contains(substring.toLowerCase())) {
                    skuList.add(skuId);
                }
            }else {
                skuList.add(skuId);
            }
        }

        return skuList;
    }

    private WhereFilter whereFilterFactory (String path, String operator, String valueString){
        return   WhereFilter.builder()
                .path(path)
                .operator(operator)
                .valueString(valueString)
                .build();
    }

    private WhereFilter whereFilterFactory (String path, String operator, Set<ChildCategoryModel> valueString){
        ArrayList<String> a = new ArrayList<>();
        valueString.forEach(childCategoryModel -> {
            a.add(childCategoryModel.getLabel());
        });
        return   WhereFilter.builder()
                .path(new String[]{path})
                .operator(operator)
                .valueText(a.toString())
                .build();
    }

    public ArrayList<String> getListOfProductsForCompleteTheLook(ProductDetailsModel productDetails) {
        WeaviateClient client = weaviateConfig.weaviateClientMethod();
        NearImageArgument base64Image = NearImageArgument.builder().image(productDetails.base64Image).build();
        WhereFilter [] whereFilters = new WhereFilter[]{
                this.whereFilterFactory("parentCategory", "Equal", "Jewellery"),
        };
        WhereFilter allFilters = WhereFilter.builder().operator(Operator.And).operands(whereFilters).build();
        WhereArgument whereArgument =  WhereArgument.builder().filter(allFilters).build();

        Result<GraphQLResponse> result = client.graphQL().get()
                .withClassName("TestImg16")
                .withNearImage(base64Image)
                .withWhere(whereArgument)
                .withFields(Field.builder().name("sku_id").build())
                .withLimit(50)
                .run();

        String jsonString = new GsonBuilder()
                .setPrettyPrinting()
                .create()
                .toJson(result
                        .getResult()
                        .getData());

        if(jsonString.equalsIgnoreCase("null")){
            throw new RuntimeException(String.valueOf(result.getResult().getErrors()[0].getMessage()));
        }

        JsonArray testImgArray =  new JsonParser()
                .parse(jsonString)
                .getAsJsonObject()
                .getAsJsonObject("Get")
                .getAsJsonArray("TestImg16");

        ArrayList<String> skuList = new ArrayList<>();

        for (int i = 0; i < testImgArray.size(); i++) {
            JsonObject item = testImgArray.get(i).getAsJsonObject();
            String skuId = item.get("sku_id").getAsString();
            skuList.add(skuId);
        }

        return skuList;
    }

    public WhereFilter[] filterLevelOne(ProductDetailsModel productDetails, boolean isSameBrand){
        if (isSameBrand){
            return new WhereFilter[]{
                    this.whereFilterFactory("brand", Operator.Equal, productDetails.brand),
                    this.whereFilterFactory("color", Operator.Equal, productDetails.color),
                    this.whereFilterFactory("parentCategory", Operator.Equal, productDetails.parent_category),
                    this.whereFilterFactory("childCategories", Operator.Equal, productDetails.child_categories)
            };
        }
        return new WhereFilter[]{
                this.whereFilterFactory("color", Operator.Equal, productDetails.color),
                this.whereFilterFactory("parentCategory", Operator.Equal, productDetails.parent_category),
                this.whereFilterFactory("childCategories", Operator.Equal, productDetails.child_categories)
        };
    }

    public WhereFilter[] filterLevelTwo(ProductDetailsModel productDetails, boolean isSameBrand){
        if (isSameBrand){
            return new WhereFilter[]{
                    this.whereFilterFactory("brand", Operator.Equal, productDetails.brand),
                    this.whereFilterFactory("color", Operator.Equal, productDetails.color),
                    this.whereFilterFactory("parentCategory", Operator.Equal, productDetails.parent_category)
            };
        }
        return new WhereFilter[]{
                this.whereFilterFactory("parentCategory", Operator.Equal, productDetails.parent_category),
                this.whereFilterFactory("childCategories", Operator.Equal, productDetails.child_categories)
        };
    }

    public WhereFilter[] filterLevelThree(ProductDetailsModel productDetails, boolean isSameBrand){
        if(isSameBrand){
            return new WhereFilter[]{
                    this.whereFilterFactory("brand", Operator.Equal, productDetails.brand),
                    this.whereFilterFactory("parentCategory", Operator.Equal, productDetails.parent_category),
                    this.whereFilterFactory("childCategories", Operator.Equal, productDetails.child_categories)
            };
        }
        return new WhereFilter[]{
                this.whereFilterFactory("parentCategory", Operator.Equal, productDetails.parent_category),
        };
    }
}
