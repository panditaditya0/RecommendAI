package com.RecommendAI.RecommendAI.Dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;

@Getter
@Setter
@ToString
public class Filter {
    public String field;
    public String type;
    public ArrayList<String> value;
}