package com.img.imgbackend.repository;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.Collection;
import java.util.Collections;

@Configuration
@EnableMongoRepositories(basePackages = "com.img.imgbackend")
public class MongoConfig extends AbstractMongoClientConfiguration {
    private final String database = "image";
    private static final Logger log = LogManager.getLogger(MongoConfig.class);


    @Override
    protected String getDatabaseName() {
        return database;
    }

    @Bean
    public MongoTemplate mongoTemplate() throws Exception {
        return new MongoTemplate(mongoClient(), database);
    }

    @Override
    public MongoClient mongoClient() {
//        ConnectionString connectionString = new ConnectionString("mongodb://root:example@sm_db:27017/" + database + "?authSource=admin");
        ConnectionString connectionString = new ConnectionString("mongodb://localhost:27017/" + database);
        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
            .applyConnectionString(connectionString)
            .build();

        return MongoClients.create(mongoClientSettings);
    }

    @Override
    public Collection<String> getMappingBasePackages() {
        return Collections.singleton("com.img.imgbackend");
    }

    @Override
    protected boolean autoIndexCreation() {
        return true;
    }
}
