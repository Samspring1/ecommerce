package com.sam.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EcommerceApplication {
    public static void main(String[] args) {
        SpringApplication.run(EcommerceApplication.class, args);
        System.out.println("🚀 E-commerce Application Started Successfully!");
        System.out.println("📝 API Documentation: http://localhost:8080/api");
    }
}