package com.Tulip_Tech.OrderService;


import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class OrderServiceConfig {


    public TestServiceInstanceListSupplier supplier() {
        return new TestServiceInstanceListSupplier();
    }
}
