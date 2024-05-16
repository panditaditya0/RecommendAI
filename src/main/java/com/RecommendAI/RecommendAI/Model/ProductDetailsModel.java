package com.RecommendAI.RecommendAI.Model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "product_details_2")
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
    public String brand;
    public String image_link;
    public double discount;
    public String link;
    public String color;
    public String domain;
    public String parent_category;
    public double price_in;
    public double discount_in;
    public double special_price_in;
    public double price_us;
    public double discount_us;
    public double special_price_us;
    public double price_row;
    public double discount_row;
    public double special_price_row;
    public String uuid;
    public LocalDateTime updated_at;
    @Column(name = "base_64_image")
    public String base64Image;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "kafka_entity_id", referencedColumnName = "entity_id")
    public Set<ChildCategoryModel> child_categories;
}