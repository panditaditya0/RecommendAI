package com.RecommendAI.RecommendAI.Model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

@Entity
@Table(name = "product_details")
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductDetailsModel {
    @Id
    public long entity_id;
    public String sku_id;
    public String product_id;
    public String title;
    public double discounted_price;
    public double region_sale_price;
    public String brand;
    public String image_link;
    public double discount;
    public String link;
    public String mad_id;
    public double sale_price;
    public String ontology;
    public double price;
    public String uuid;
    public String color;
    public String domain;
    public String parentCategory;
    @Column(name = "base_64_image")
    public String base64Image;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "kafka_payload_id", referencedColumnName = "entity_id")
    public Set<ChildCategoryModel> childCategories;
}
