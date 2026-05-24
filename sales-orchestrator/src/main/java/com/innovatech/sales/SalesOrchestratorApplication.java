package com.innovatech.sales;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class SalesOrchestratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(SalesOrchestratorApplication.class, args);
    }

    // Bean inyectable de RestTemplate para consumir síncronamente el inventory-service
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
