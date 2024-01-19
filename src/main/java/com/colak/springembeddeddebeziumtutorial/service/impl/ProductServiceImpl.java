package com.colak.springembeddeddebeziumtutorial.service.impl;

import com.colak.springembeddeddebeziumtutorial.jpa.Product;
import com.colak.springembeddeddebeziumtutorial.service.ProductService;
import io.debezium.data.Envelope;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class ProductServiceImpl implements ProductService {

    @Override
    @Transactional
    public void handleEvent(String operation, String documentId, String collection, Product product) {

        // Check if the operation is either CREATE or READ
        if (Envelope.Operation.CREATE.code().equals(operation) || Envelope.Operation.READ.code().equals(operation)) {
            log.info("Product : {} is created or read", documentId);
//            // Set the MongoDB document ID to the product
//            product.setMongoId(documentId);
//            product.setSourceCollection(collection);
//            // Save the updated product information to the database
//            productRepository.save(product);

            // If the operation is DELETE
        } else if (Envelope.Operation.UPDATE.code().equals(operation)) {
            log.info("Product : {} is updated", documentId);
//            var productToUpdate = productRepository.findByMongoId(documentId);
//            product.setId(productToUpdate.getId());
//            product.setMongoId(documentId);
//            product.setSourceCollection(collection);
//            productRepository.save(product);
        }
        // If the operation is DELETE
        else if (Envelope.Operation.DELETE.code().equals(operation)) {
            // Remove the product from the database using the MongoDB document ID
            log.info("Product : {} is deleted", documentId);
//            productRepository.removeProductByMongoId(documentId);
        }
    }
}
