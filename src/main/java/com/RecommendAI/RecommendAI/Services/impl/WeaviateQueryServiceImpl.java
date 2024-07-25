package com.RecommendAI.RecommendAI.Services.impl;

import com.RecommendAI.RecommendAI.Config.WeaviateConfig;
import com.RecommendAI.RecommendAI.Model.ProductDetailsModel;
import com.RecommendAI.RecommendAI.Services.VectorDatabaseService;
import com.google.gson.*;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.filters.Operator;
import io.weaviate.client.v1.filters.WhereFilter;
import io.weaviate.client.v1.graphql.model.GraphQLResponse;
import io.weaviate.client.v1.graphql.query.argument.NearImageArgument;
import io.weaviate.client.v1.graphql.query.argument.NearObjectArgument;
import io.weaviate.client.v1.graphql.query.argument.WhereArgument;
import io.weaviate.client.v1.graphql.query.fields.Field;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WeaviateQueryServiceImpl implements VectorDatabaseService {
    private final WeaviateConfig weaviateConfig;

    @Override
    public LinkedHashSet<String> getListOfSkuIdsFromWeaviateDb(ProductDetailsModel productDetailsModel, WhereFilter[] whereFilters, boolean isSameBrand, int limit, String operator) {
            WeaviateClient client = weaviateConfig.weaviateClientMethod();
//        NearImageArgument base64Image = NearImageArgument.builder().image(productDetailsModel.base64Image_original).build();
        NearObjectArgument nearObject = client.graphQL().arguments().nearObjectArgBuilder()
                .id(productDetailsModel.getUuid())
                .build();
        WhereFilter allFilters = WhereFilter.builder()
                .operator(operator)
                .operands(whereFilters)
                .build();
        WhereArgument whereArgument = WhereArgument
                .builder()
                .filter(allFilters)
                .build();

        Result<GraphQLResponse> result = client.graphQL().get()
                .withClassName("TestImg19")
                .withNearObject(nearObject)
                .withWhere(whereArgument)
                .withFields(Field.builder().name("sku_id").build())
                .withLimit(limit+1)
                .run();
        String jsonString = new GsonBuilder()
                .setPrettyPrinting()
                .create()
                .toJson(result
                        .getResult()
                        .getData());

        if (jsonString.equalsIgnoreCase("null")) {
            throw new RuntimeException(String.valueOf(result.getResult().getErrors()[0].getMessage()));
        }

        JsonArray testImgArray = new JsonParser()
                .parse(jsonString)
                .getAsJsonObject()
                .getAsJsonObject("Get")
                .getAsJsonArray("TestImg19");

        LinkedHashSet<String> skuList = new LinkedHashSet<>();
        String substring = productDetailsModel.sku_id.substring(0, 4);

        for (int i = 0; i < testImgArray.size(); i++) {
            JsonObject item = testImgArray.get(i).getAsJsonObject();
            String skuId = item.get("sku_id").getAsString();
            if (skuId.equalsIgnoreCase(productDetailsModel.sku_id)) {
                continue;
            }
            if (!isSameBrand) {
                if (!skuId.toLowerCase().contains(substring.toLowerCase())) {
                    skuList.add(skuId);
                }
            } else {
                skuList.add(skuId);
            }
        }

        return skuList;
    }

    private WhereFilter whereFilterFactory(String path, String operator, String valueString) {
        return WhereFilter.builder()
                .path(path)
                .operator(operator)
                .valueString(valueString)
                .build();
    }

    private WhereFilter whereFilterFactory(String path, String operator, List<String> valueString) {
        String[] arrayOfValueString = valueString.toArray(new String[0]);
        return WhereFilter.builder()
                .path(path)
                .operator(operator)
                .valueString(arrayOfValueString)
                .build();

    }

    @Override
    public WhereFilter[] filterLevelOne(ProductDetailsModel productDetails, boolean isSameBrand) {
        List<WhereFilter> filters = new ArrayList<>();

        if (isSameBrand) {
            filters.add(this.whereFilterFactory("brand", Operator.Equal, productDetails.brand));
        }

        filters.add(this.whereFilterFactory("color", Operator.Equal, productDetails.color));
        filters.add(this.whereFilterFactory("parent_categories", Operator.ContainsAny, productDetails.parent_categories));

        if (!productDetails.getChild_categories().isEmpty()) {
            filters.add(this.whereFilterFactory("child_categories", Operator.ContainsAny, productDetails.child_categories));
        }

        return filters.toArray(new WhereFilter[0]);
    }

    @Override
    public WhereFilter[] filterLevelTwo(ProductDetailsModel productDetails, boolean isSameBrand) {
        List<WhereFilter> filters = new ArrayList<>();

        if (isSameBrand) {
            filters.add(this.whereFilterFactory("brand", Operator.Equal, productDetails.brand));
            filters.add(this.whereFilterFactory("color", Operator.Equal, productDetails.color));
        } else {
            if (productDetails.getChild_categories().isEmpty()) {
                filters.add(this.whereFilterFactory("parentCategory", Operator.Equal, productDetails.parent_category));
            }
        }

        filters.add(this.whereFilterFactory("parent_categories", Operator.ContainsAny, productDetails.parent_categories));

        if (!isSameBrand && !productDetails.getChild_categories().isEmpty()) {
            filters.add(this.whereFilterFactory("child_categories", Operator.ContainsAny, productDetails.child_categories));
        }

        return filters.toArray(new WhereFilter[0]);
    }

    @Override
    public WhereFilter[] filterLevelThree(ProductDetailsModel productDetails, boolean isSameBrand) {
        List<WhereFilter> filters = new ArrayList<>();
        if (isSameBrand) {
            filters.add(this.whereFilterFactory("brand", Operator.Equal, productDetails.brand));
        }

        filters.add(this.whereFilterFactory("parent_categories", Operator.ContainsAny, productDetails.parent_categories));

        if (isSameBrand && !productDetails.getChild_categories().isEmpty()) {
            filters.add(this.whereFilterFactory("child_categories", Operator.ContainsAny, productDetails.child_categories));
        }

        return filters.toArray(new WhereFilter[0]);
    }

    @Override
    public WhereFilter[] filterCompleteTheLookForCloths( ArrayList<String> childCategories) {
        return new WhereFilter[]{this.whereFilterFactory("child_categories", Operator.ContainsAny, childCategories)
        };
    }
}