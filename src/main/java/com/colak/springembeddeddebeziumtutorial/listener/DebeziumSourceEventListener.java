package com.colak.springembeddeddebeziumtutorial.listener;

import com.colak.springembeddeddebeziumtutorial.jpa.Product;
import com.colak.springembeddeddebeziumtutorial.service.ProductService;
import com.colak.springembeddeddebeziumtutorial.util.HandlerUtils;
import io.debezium.config.Configuration;
import io.debezium.embedded.Connect;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.RecordChangeEvent;
import io.debezium.engine.format.ChangeEventFormat;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class DebeziumSourceEventListener {

    // This will be used to run the engine asynchronously
    private final Executor executor;

    // DebeziumEngine serves as an easy-to-use wrapper around any Debezium connector
    private final DebeziumEngine<RecordChangeEvent<SourceRecord>> debeziumEngine;

    // Inject product service
    private final ProductService productService;


    public DebeziumSourceEventListener(Configuration mongodbConnector, ProductService productService) {
        // Create a new single-threaded executor.
        this.executor = Executors.newSingleThreadExecutor();

        // Create a new DebeziumEngine instance.
        // This line is creating a new instance of the Debezium Engine.
        // The ChangeEventFormat.of(Connect.class) part specifies the format of the change events that the engine will produce.
        // In this case, it’s using the Connect class, which means it will produce events in Kafka Connect’s data format.
        this.debeziumEngine =
                DebeziumEngine.create(ChangeEventFormat.of(Connect.class))
                        // This line is configuring the engine with the properties of a MongoDB connector.
                        // The mongodbConnector.asProperties() method should return a Properties object that contains
                        // all the necessary configuration for connecting to a MongoDB database.
                        .using(mongodbConnector.asProperties())
                        // This line is specifying a method that will be called whenever a change event is produced by the connector.
                        .notifying(this::handleChangeEvent)
                        .build();

        // Set the product service.
        this.productService = productService;
    }

    private void handleChangeEvent(RecordChangeEvent<SourceRecord> sourceRecordRecordChangeEvent) {
        SourceRecord sourceRecord = sourceRecordRecordChangeEvent.record();
        Struct sourceRecordKey = (Struct) sourceRecord.key();
        Struct sourceRecordValue = (Struct) sourceRecord.value();
        if (sourceRecordValue != null) {
            try {

                String operation = HandlerUtils.getOperation(sourceRecordValue);

                String documentId = HandlerUtils.getDocumentId(sourceRecordKey);

                String collection = HandlerUtils.getCollection(sourceRecordValue);

                Product product = HandlerUtils.getData(sourceRecordValue);

                productService.handleEvent(operation, documentId, collection, product);

                log.info("Collection : {} , DocumentId : {} , Operation : {}", collection, documentId, operation);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

    }

    @PostConstruct
    private void start() {
        // start the debezium engine
        this.executor.execute(debeziumEngine);
    }

    @PreDestroy
    private void stop() throws IOException {
        // to stop it we need to close the engine.
        if (this.debeziumEngine != null) {
            this.debeziumEngine.close();
        }
    }
}
