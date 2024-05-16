package com.RecommendAI.RecommendAI.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@Table(name = "child_category_2")
public class ChildCategoryModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public long id;

    @JoinColumn(name="entity_id")
    public long kafka_entity_id;
    public String label;
}
