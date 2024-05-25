package com.RecommendAI.RecommendAI.Exceptions;

public class ProductNotInDbException extends Exception{
    public ProductNotInDbException(String message){
        super(message);
    }
}