package com.colak.springembeddeddebeziumtutorial.service;

import com.colak.springembeddeddebeziumtutorial.jpa.Product;

public interface ProductService {
    void handleEvent(String operation, String documentId, String collection, Product product);
}
