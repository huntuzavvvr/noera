package com.noera.noera.dto;

import java.util.List;

import lombok.Data;

@Data
public class ProductSizeDto {
    private Integer id;
    private String sizeName;
    private List<ProductColorDto> colors;
}
