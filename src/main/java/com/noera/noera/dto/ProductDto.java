package com.noera.noera.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class ProductDto {
    private Integer id;
    private String name;
    private BigDecimal price;
    private String description;
    private List<ProductSizeDto> sizes;
}