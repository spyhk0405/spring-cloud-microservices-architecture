package com.davidbadell.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user_service", r -> r.path("/api/users/**")
                        .uri("lb://USER-SERVICE"))
                .route("product_service", r -> r.path("/api/products/**")
                        .uri("lb://PRODUCT-SERVICE"))
                .route("order_service", r -> r.path("/api/orders/**")
                        .uri("lb://ORDER-SERVICE"))
                .build();
    }
}
