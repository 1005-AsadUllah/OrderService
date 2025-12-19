package com.Tulip_Tech.OrderService;

import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

public class TestServiceInstanceListSupplier implements ServiceInstanceListSupplier {
    @Override
    public String getServiceId() {
        return "";
    }

    @Override
    public Flux<List<ServiceInstance>> get() {
        List<ServiceInstance> instances = new ArrayList<>();
        instances.add(new DefaultServiceInstance(
                "payment-service-svc",
                "payment-service-svc",
                "localhost",
                8080,false
        ));

        instances.add(new DefaultServiceInstance(
                "product-service-svc",
                "product-service-svc",
                "localhost",
                8080,false
        ));

        return Flux.just(instances);
    }
}
