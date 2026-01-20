package com.ecommerce.searchservice;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.netflix.discovery.EurekaClient;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Lazy;

import java.io.IOException;

@SpringBootApplication
public class SearchServiceApplication {
    @Autowired
    @Lazy
    private EurekaClient eurekaClient;
    @Autowired
    private ElasticsearchClient es;

    public static void main(String[] args) {
        SpringApplication.run(SearchServiceApplication.class, args);
    }
    @PostConstruct
    public void logEsVersion() throws IOException {
        var info = es.info();
        System.out.println("🔥 Elasticsearch SERVER version = " + info.version().number());
    }
}
