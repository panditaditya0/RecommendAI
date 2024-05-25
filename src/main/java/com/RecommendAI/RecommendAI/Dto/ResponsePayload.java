package com.RecommendAI.RecommendAI.Dto;

import lombok.*;

import java.util.LinkedHashSet;
import java.util.List;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ResponsePayload {
    private String status;
    private List<LinkedHashSet<ResponseProductDetails>> data;
    private String message;
    private boolean trending = false;
}