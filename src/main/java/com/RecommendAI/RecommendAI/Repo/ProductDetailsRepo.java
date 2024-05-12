package com.RecommendAI.RecommendAI.Repo;

import com.RecommendAI.RecommendAI.Model.ProductDetailsModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.ArrayList;
import java.util.List;

public interface ProductDetailsRepo extends JpaRepository<ProductDetailsModel, Long> {

    @Query(value ="SELECT * FROM product_details WHERE sku_id = ?1", nativeQuery = true)
    ProductDetailsModel findBySkuId(String skuId);


    @Query(value = "SELECT null as base_64_image, " +
            "       Max(t1.discount)          AS discount,\n" +
            "       Max(t1.discounted_price)  AS discounted_price,\n" +
            "       Max(t1.price)             AS price,\n" +
            "       Max(t1.region_sale_price) AS region_sale_price,\n" +
            "       Max(t1.sale_price)        AS sale_price,\n" +
            "       Max(t1.entity_id)         AS entity_id,\n" +
            "       Max(t1.brand)             AS brand,\n" +
            "       Max(t1.color)             AS color,\n" +
            "       Max(t1.domain)            AS domain,\n" +
            "       Max(t1.link)              AS link,\n" +
            "       Max(t1.mad_id)            AS mad_id,\n" +
            "       Max(t1.ontology)          AS ontology,\n" +
            "       Max(t1.parent_category)   AS parent_category,\n" +
            "       Max(t1.product_id)        AS product_id,\n" +
            "       Max(t1.sku_id)            AS sku_id,\n" +
            "       Max(t1.title)             AS title,\n" +
            "       Max(t1.uuid)              AS uuid,\n" +
            "       Max(t1.image_link)        AS image_link,\n" +
            "       t2.kafka_payload_id as id,\n" +
            "       String_agg(t2.label, ',') AS label\n" +
            "FROM   product_details t1\n" +
            "       JOIN child_category t2\n" +
            "         ON t1.entity_id = t2.kafka_payload_id\n" +
            "WHERE  t1.sku_id in (?1)\n" +
            "GROUP  BY t2.kafka_payload_id; ", nativeQuery = true)
    ArrayList<ProductDetailsModel> findByListOfIds(List<String> skuIds);


    @Query(value = "select sku_id from product_details pd where color = ?1 \n" +
            "and brand = ?2 \n" +
            "and sku_id not IN (?3) limit ?4 ", nativeQuery = true )
    ArrayList<String> findAllSimilarProductQuery(String color, String brand, ArrayList<String> skuIds, int limit);
}
