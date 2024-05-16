package com.RecommendAI.RecommendAI.Exceptions;

public class NotEnoughProductsException extends Exception {
    public NotEnoughProductsException(String errorMessage,Throwable err) {
        super(errorMessage, err);
    }
    public NotEnoughProductsException(String errorMessage) {
        super(errorMessage);
    }
}
