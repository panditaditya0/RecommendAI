package com.RecommendAI.RecommendAI.Repo;

import com.RecommendAI.RecommendAI.Model.ProductDetailsModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.ArrayList;
import java.util.LinkedHashSet;

public interface ProductDetailsRepo extends JpaRepository<ProductDetailsModel, Long> {

    @Query(value ="SELECT * FROM product_details_3 WHERE sku_id = ?1", nativeQuery = true)
    ProductDetailsModel findBySkuId(String skuId);


    @Query(value = "SELECT NULL AS base_64_image,\n" +
            " NULL AS base64image_original,\n" +
            "       discount,\n" +
            "       discount_in,\n" +
            "       discount_row,\n" +
            "       discount_us,\n" +
            "       price_in,\n" +
            "       price_row,\n" +
            "       price_us,\n" +
            "       special_price_in,\n" +
            "       special_price_row,\n" +
            "       special_price_us,\n" +
            "       entity_id,\n" +
            "       updated_at,\n" +
            "       brand,\n" +
            "       color,\n" +
            "       domain,\n" +
            "       image_link,\n" +
            "       in_stock,\n" +
            "       link,\n" +
            "       parent_category,\n" +
            "       product_id,\n" +
            "       sku_id,\n" +
            "       title,\n" +
            "       uuid,\n" +
            "       categories,\n" +
            "       child_categories,\n" +
            "       parent_categories\n" +
            "FROM   product_details_3 pd \n" +
            "WHERE  sku_id in (?1) ", nativeQuery = true)
    ArrayList<ProductDetailsModel> findByListOfIds(LinkedHashSet<String> skuIds);


    @Query(value = "select sku_id from product_details_3 pd where color = ?1 \n" +
            "and brand = ?2 \n" +
            "and sku_id not IN (?3) limit ?4 ", nativeQuery = true )
    ArrayList<String> findAllSimilarProductQuery(String color, String brand, ArrayList<String> skuIds, int limit);
}
