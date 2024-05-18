package com.RecommendAI.RecommendAI.Model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Users {
    @Id
    public UUID mad_id;
    public UUID user_id;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "sku_Ids", columnDefinition = "text[]")
    public List<String> skuIds;
}
