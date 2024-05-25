package com.RecommendAI.RecommendAI.Dto;

import lombok.*;
import org.checkerframework.checker.units.qual.N;

import java.util.LinkedHashSet;
import java.util.List;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ResponsePayload {
    public String status;
    public List<LinkedHashSet<ResponseProductDetails>> data;
    public String message;
    public boolean trending = false;
}