package com.noera.noera.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.service.annotation.DeleteExchange;

import com.noera.noera.model.Product;
import com.noera.noera.model.ProductVariant;
import com.noera.noera.service.ShopService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@Controller
@RequestMapping("/admin")
// @RequiredArgsConstructor
public class AdminController {
    private static final String UPLOAD_DIR = "src/main/resources/static/images/";
    @Autowired
    private ShopService service;

    @GetMapping("/products")
    public String getMethodName(Model model) {
        List<Product> products = service.findAll();
        model.addAttribute("products", products);
        model.addAttribute("product", new Product());
        return "ad";
    }

    // @PostMapping("/products")
    // public String addProduct(@ModelAttribute Product product) {

    //     service.save(product);
    //     return "redirect:/admin/products";
    // }
    
    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Integer id){
        service.delete(id);
        return "redirect:/admin/products";
    }

    // @PostMapping("/products")
    // public String postMethodName(@ModelAttribute Product product, @RequestParam("image") MultipartFile file) {
    //     System.out.println("FADLF");
    //     if (!file.isEmpty()){
    //         try{
    //             System.out.println("FADLF");
    //             Path path = Paths.get("src/main/resources/static/images/" + file.getOriginalFilename());
    //             // Files.createDirectories(path.getParent());
    //             Files.write(path, file.getBytes());
    //         }
    //         catch (IOException e){
    //             e.printStackTrace();
    //         }
    //     }
    //     // product.setImageUrl("/images/" + file.getOriginalFilename());
    //     service.save(product);
    //     return "redirect:/admin/products";
    // }
    @PostMapping("/products")
    public String addProduct(
        @ModelAttribute Product product,
        MultipartHttpServletRequest request) {

        int variantCount = 0;
        List<ProductVariant> variants = new ArrayList<>();
        
        // Собираем все индексы вариантов
        Map<String, String[]> parameterMap = request.getParameterMap();
        Set<Integer> variantIndices = new TreeSet<>();
        
        for (String paramName : parameterMap.keySet()) {
            if (paramName.matches("variants\\[\\d+\\]\\.price")) {
                int index = Integer.parseInt(paramName.replaceAll(".*\\[(\\d+)\\].*", "$1"));
                variantIndices.add(index);
            }
        }
        
        // Обрабатываем каждый вариант
        for (int index : variantIndices) {
            ProductVariant variant = new ProductVariant();
            
            // Обработка цены
            String priceStr = request.getParameter("variants[" + index + "].price");
            if (priceStr != null && !priceStr.isEmpty()) {
                try {
                    // Заменяем запятые на точки для корректного парсинга
                    priceStr = priceStr.replace(',', '.');
                    variant.setPrice(new BigDecimal(priceStr));
                } catch (NumberFormatException e) {
                    variant.setPrice(BigDecimal.ZERO);
                }
            }
            
            // Обработка цвета
            String color = request.getParameter("variants[" + index + "].color");
            variant.setColor(color != null ? color : "");
            System.out.println(color);
            // Обработка количества
            String quantityStr = request.getParameter("variants[" + index + "].quantity");
            if (quantityStr != null && !quantityStr.isEmpty()) {
                try {
                    variant.setQuantity(Integer.parseInt(quantityStr));
                } catch (NumberFormatException e) {
                    variant.setQuantity(0);
                }
            }
            
            // Обработка изображений
            MultipartFile imageFile = request.getFile("variantImages[" + index + "]");
            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = saveImage(imageFile);
                variant.setImageUrl(imageUrl);
            }
            
            MultipartFile hoverImageFile = request.getFile("variantHoverImages[" + index + "]");
            if (hoverImageFile != null && !hoverImageFile.isEmpty()) {
                String hoverImageUrl = saveImage(hoverImageFile);
                variant.setHoverImageUrl(hoverImageUrl);
            }
            if (variant.getPrice() != null){
                variant.setProduct(product);
                variants.add(variant);
        }
            
        }
        
        product.setVariants(variants);
        service.save(product);
        return "redirect:/admin/products";
    }

    private String saveImage(MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            
            Path path = Paths.get(UPLOAD_DIR + uniqueFileName);
            Files.write(path, file.getBytes());
            
            return "/images/" + uniqueFileName;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
