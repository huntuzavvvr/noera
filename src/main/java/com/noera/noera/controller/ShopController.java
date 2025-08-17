package com.noera.noera.controller;

import com.noera.noera.model.Product;
import com.noera.noera.model.ProductSize;
import com.noera.noera.model.ProductColor;
import com.noera.noera.service.ShopService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping
public class ShopController {
    
    private final ShopService service;

    public ShopController(ShopService service) {
        this.service = service;
    }

    @GetMapping()
    public String getHtml(Model model) {
        List<Product> products = service.findAll();
        model.addAttribute("products", products);
        return "s";
    }

    @GetMapping("/gallery")
    public String getGallery() {
        return "redirect:/";
    }

    @GetMapping("/deliveryinfo")
    public String getDelivery() {
        return "redirect:/";
    }

    @GetMapping("/refund")
    public String getRefund() {
        return "redirect:/";
    }

    @GetMapping("/about")
    public String getAbout() {
        return "about";
    }

    @GetMapping("/products/{id}")
    public String getProductDescription(@PathVariable Integer id, 
                                      @RequestParam(required = false) String sizeName,
                                      @RequestParam(required = false) Integer colorId,
                                      Model model) {
        System.out.println(sizeName);
        Product product = service.findById(id);
        if (product == null) {
            return "redirect:/";
        }

        // Получаем все размеры товара
        List<ProductSize> sizes = product.getSizes();
        
        // Находим выбранный размер (или первый доступный)
        ProductSize selectedSize = sizes.stream()
                .filter(s -> sizeName == null || sizeName.equalsIgnoreCase(s.getSizeName()))
                .findFirst()
                .orElse(sizes.isEmpty() ? null : sizes.get(0));
        
        // Получаем все цвета для выбранного размера
        List<ProductColor> colors = selectedSize != null ? selectedSize.getColors() : Collections.emptyList();
        
        // Находим выбранный цвет (или первый доступный)
        ProductColor selectedColor = colors.stream()
                .filter(c -> colorId == null || colorId.equals(c.getId()))
                .findFirst()
                .orElse(colors.isEmpty() ? null : colors.get(0));

        // Получаем все уникальные цвета для всех размеров (для галереи)
        List<String> allProductImages = product.getSizes().stream()
                .flatMap(size -> size.getColors().stream())
                .map(ProductColor::getImageUrl)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        // Добавляем hover-изображения если они есть
        product.getSizes().stream()
                .flatMap(size -> size.getColors().stream())
                .map(ProductColor::getHoverImageUrl)
                .filter(Objects::nonNull)
                .forEach(allProductImages::add);
        System.out.println(selectedSize.getSizeName());
        System.out.println(selectedColor.getColorName());
        // System.out.println(selectedColor.getColorName());
        model.addAttribute("product", product);
        model.addAttribute("selectedSize", selectedSize);
        model.addAttribute("selectedColor", selectedColor);
        model.addAttribute("availableColors", colors);
        model.addAttribute("allProductImages", allProductImages);

        return "description";
    }

    // Вспомогательный метод для проверки доступности размера
    private boolean isSizeAvailable(ProductSize size) {
        return size != null && size.getColors().stream()
                .anyMatch(color -> color.getQuantity() > 0);
    }
}