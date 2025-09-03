package com.noera.noera.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class ProductColorDto {
    private Integer id;
    private String colorName;
    private String colorHex;
    private Integer quantity;

    // Для загрузки файлов
    private MultipartFile imageFile;
    private MultipartFile hoverImageFile;

    // Для сохранения ссылок в базу
    private String imageUrl;
    private String hoverImageUrl;
}
