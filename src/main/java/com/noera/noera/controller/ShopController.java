package com.noera.noera.controller;

import com.noera.noera.model.Product;
import com.noera.noera.model.ProductVariant;
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
        return "redirect:/";
    }

    @GetMapping("/products/{id}")
    public String getDescription(@PathVariable Integer id, 
                               @RequestParam(required = false) String color,
                               @RequestParam(required = false) String size,
                               Model model) {
        Product product = service.findById(id);
        if (product == null) {
            return "redirect:/";
        }

        // Get all variants for this product
        List<ProductVariant> variants = product.getVariants();
        
        // Find selected variant based on color parameter
        ProductVariant selectedVariant = variants.stream()
                .filter(v -> color == null || color.equalsIgnoreCase(v.getColor()))
                .findFirst()
                .orElse(variants.isEmpty() ? null : variants.get(0));

        // Get all unique colors from variants
        Set<String> availableColors = variants.stream()
                .map(ProductVariant::getColor)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Get all unique sizes from variants (assuming size is stored somewhere)
        // This is just an example - you'll need to adjust based on your actual size storage
        Set<String> availableSizes = variants.stream()
                .filter(v -> v.getQuantity() > 0) // Only show sizes that are available
                .map(v -> "S") // Replace with actual size property if you have one
                .collect(Collectors.toSet());

        // Get all image URLs for the gallery
        List<String> allProductImages = variants.stream()
                .flatMap(v -> Arrays.stream(new String[]{v.getImageUrl(), v.getHoverImageUrl()}))
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        model.addAttribute("product", product);
        model.addAttribute("selectedVariant", selectedVariant);
        model.addAttribute("selectedSize", size);
        model.addAttribute("availableColors", availableColors);
        model.addAttribute("availableSizes", availableSizes);
        model.addAttribute("allProductImages", allProductImages);

        return "description";
    }

    // Utility method to check if size is available
    private boolean isSizeAvailable(String size, List<ProductVariant> variants) {
        // Implement your size availability logic here
        // This is just a placeholder - adjust based on your actual implementation
        return variants.stream()
                .anyMatch(v -> v.getQuantity() > 0 && size.equals("S")); // Replace with actual size check
    }
}