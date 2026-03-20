package com.sam.ecommerce.dto;

import lombok.Data;

@Data
public class CartItemDto {
    private Long productId;
    private String productName;
    private Integer quantity;
    private Double price;
    private Double totalPrice;
}