package com.transaction.service;

public class ParentNotFoundException extends RuntimeException {

    public ParentNotFoundException(String message) {
        super(message);
    }
}
