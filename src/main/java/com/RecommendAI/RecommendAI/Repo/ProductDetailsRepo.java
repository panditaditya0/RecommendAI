package com.RecommendAI.RecommendAI.Repo;

import com.RecommendAI.RecommendAI.Model.ProductDetailsModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.ArrayList;
import java.util.List;

public interface ProductDetailsRepo extends JpaRepository<ProductDetailsModel, Long> {

    @Query(value ="SELECT * FROM product_details_2 WHERE sku_id = ?1", nativeQuery = true)
    ProductDetailsModel findBySkuId(String skuId);


    @Query(value = "SELECT null as base_64_image, \n" +
            "       Max(t1.entity_id)         AS entity_id,\n" +
            "        Max(t1.sku_id) AS sku_id,\n" +
            "        Max(t1.product_id)  AS product_id,\n" +
            "        Max(t1.title)             AS title,\n" +
            "        Max(t1.brand)        AS brand,\n" +
            "        Max(t1.image_link)         AS image_link,\n" +
            "        Max(t1.discount)             AS discount,\n" +
            "        Max(t1.link)             AS link,\n" +
            "        Max(t1.color)            AS color,\n" +
            "        Max(t1.domain)              AS domain,\n" +
            "        Max(t1.parent_category)   AS parent_category,\n" +
            "        Max(t1.price_in)        AS price_in,\n" +
            "        Max(t1.discount_in)            AS discount_in,\n" +
            "        Max(t1.special_price_in)             AS special_price_in,\n" +
            "        Max(t1.price_us)              AS price_us,\n" +
            "        Max(t1.discount_us)              AS discount_us,\n" +
            "        Max(t1.special_price_us)        AS special_price_us,\n" +
            "        Max(t1.price_row)        AS price_row,\n" +
            "        Max(t1.discount_row)        AS discount_row,\n" +
            "        Max(t1.special_price_row)        AS special_price_row,\n" +
            "        Max(t1.uuid)        AS uuid,\n" +
            "        Max(t1.updated_at)        AS updated_at,\n" +
            "        t2.kafka_payload_id as id,\n" +
            "        String_agg(t2.label, ',') AS label\n" +
            "        from product_details_2 t1 JOIN child_category_2 t2\n" +
            "        ON t1.entity_id = t2.kafka_payload_id\n" +
            "        WHERE  t1.sku_id in (?1)\n" +
            "       GROUP  BY t2.kafka_payload_id;", nativeQuery = true)
    ArrayList<ProductDetailsModel> findByListOfIds(List<String> skuIds);


    @Query(value = "select sku_id from product_details_2 pd where color = ?1 \n" +
            "and brand = ?2 \n" +
            "and sku_id not IN (?3) limit ?4 ", nativeQuery = true )
    ArrayList<String> findAllSimilarProductQuery(String color, String brand, ArrayList<String> skuIds, int limit);
}
