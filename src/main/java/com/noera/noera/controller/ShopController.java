package com.noera.noera.controller;

import com.noera.noera.model.Product;
import com.noera.noera.model.ProductSize;
import com.noera.noera.repository.ShopRepository;
import com.noera.noera.model.ProductColor;
import com.noera.noera.service.ColorService;
import com.noera.noera.service.ShopService;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping
public class ShopController {
    private static final String UPLOAD_DIR = "src/main/resources/static/images/";
    
    private final ShopService service;
    private final ColorService colorService;
    private final ShopRepository repository;

    public ShopController(ShopService service, ColorService colorService, ShopRepository repository) {
        this.service = service;
        this.colorService = colorService;
        this.repository = repository;
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

    @GetMapping("/products/{id}/variant")
@ResponseBody
public Map<String, Object> getProductVariant(
        @PathVariable Integer id,
        @RequestParam(required = false) String sizeName,
        @RequestParam(required = false) Integer colorId) {

    Product product = service.findById(id);
    if (product == null) {
        return Map.of("error", "Product not found");
    }

    // Логика та же как в getProductDescription
    List<ProductSize> sizes = product.getSizes();

    ProductSize selectedSize = sizes.stream()
            .filter(s -> sizeName == null || sizeName.equalsIgnoreCase(s.getSizeName()))
            .findFirst()
            .orElse(sizes.isEmpty() ? null : sizes.get(0));

    List<ProductColor> colors = selectedSize != null ? selectedSize.getColors() : Collections.emptyList();

    // Если цвет не выбран, но есть доступные цвета, выбираем первый
    ProductColor selectedColor = null;
    if (colorId != null) {
        selectedColor = colors.stream()
                .filter(c -> colorId.equals(c.getId()))
                .findFirst()
                .orElse(null);
    }
    
    // Если цвет не выбран явно, но есть доступные цвета, берем первый
    if (selectedColor == null && !colors.isEmpty()) {
        selectedColor = colors.get(0);
    }

    return Map.of(
            "price", product.getPrice(),
            "sizeName", selectedSize != null ? selectedSize.getSizeName() : null,
            "colorId", selectedColor != null ? selectedColor.getId() : null,
            "colorName", selectedColor != null ? selectedColor.getColorName() : null,
            "quantity", selectedColor != null ? selectedColor.getQuantity() : 0,
            "imageUrl", selectedColor != null ? selectedColor.getImageUrl() : null,
            "hoverImageUrl", selectedColor != null ? selectedColor.getHoverImageUrl() : null
    );
}

    // Вспомогательный метод для проверки доступности размера
    private boolean isSizeAvailable(ProductSize size) {
        return size != null && size.getColors().stream()
                .anyMatch(color -> color.getQuantity() > 0);
    }

    @PostMapping("/products/{id}/description")
    @ResponseBody
    public Map<String, Object> updateDescription(@PathVariable Integer id,
                                                @RequestBody Map<String, String> payload) {
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        Product product = service.findById(id);
        if (product == null) return Map.of("success", false);

        String newDescription = payload.get("description");
        System.out.println(newDescription);
        product.setDescription(newDescription);
        service.save(product);

        return Map.of("success", true);
    }

    private String saveImage(MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                System.out.println("File is null or empty");
                return null;
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                System.out.println("Original filename is empty");
                return null;
            }

            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            Path path = Paths.get(UPLOAD_DIR + uniqueFileName);

            System.out.println("Saving file to: " + path.toAbsolutePath());
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            return "/images/" + uniqueFileName;
        } catch (IOException e) {
            System.err.println("Failed to save image: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @PostMapping("/products/{productId}/colors/{colorId}/images")
    @ResponseBody
    public ResponseEntity<String> handleProductUpload(
        @PathVariable Integer productId,
        @PathVariable Integer colorId,

        @RequestParam(value = "mainImage", required = false) MultipartFile imageFile,
        @RequestParam(value = "hoverImage", required = false) MultipartFile hoverImageFile, RedirectAttributes redirectAttributes) {
            System.out.println("ФОТООООООООООООООООООООООО");
    try {

        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("Основное изображение обязательно");
        }

        Product product = service.findById(productId);
        if (product == null) {
            throw new IllegalArgumentException("Товар не найден");
        }


        ProductColor productColor = colorService.findById(colorId);
        if (productColor == null) {
            throw new IllegalArgumentException("Цвет не найден");
        }

        String imageUrl = saveImage(imageFile);
        if (imageUrl == null) {
            throw new RuntimeException("Не удалось сохранить изображение");
        }
        productColor.setImageUrl(imageUrl);

        if (hoverImageFile != null && !hoverImageFile.isEmpty()) {
            String hoverImageUrl = saveImage(hoverImageFile);
            productColor.setHoverImageUrl(hoverImageUrl);
        }

        service.save(product);
        redirectAttributes.addFlashAttribute("success", "Изображения успешно обновлены");
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", "Ошибка: " + e.getMessage());
    }

    return ResponseEntity.ok("GOOD");
}



@GetMapping("/products/{id}/sizes/{sizeName}")
    public ResponseEntity<?> getBySize(@PathVariable Integer id, @PathVariable String sizeName) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Находим размер
        var size = product.getSizes().stream()
                .filter(s -> s.getSizeName().equalsIgnoreCase(sizeName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Size not found"));

        // Формируем ответ
        Map<String, Object> response = new HashMap<>();
        response.put("price", product.getPrice());
        response.put("quantity", size.getColors().stream().mapToInt(ProductColor::getQuantity).sum());

        List<Map<String, Object>> colors = size.getColors().stream()
        .map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("colorHex", c.getColorHex());
            map.put("colorName", c.getColorName());
            return map;
        })
        .toList();

        response.put("colors", colors);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/products/{id}/colors/{colorId}")
    public ResponseEntity<?> getByColor(@PathVariable Integer id, @PathVariable Integer colorId) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Находим цвет
        var color = product.getSizes().stream()
                .flatMap(s -> s.getColors().stream())
                .filter(c -> c.getId().equals(colorId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Color not found"));

        Map<String, Object> response = new HashMap<>();
        response.put("price", product.getPrice());
        response.put("quantity", color.getQuantity());
        response.put("imageUrl", color.getImageUrl());
        response.put("hoverImageUrl", color.getHoverImageUrl());

        return ResponseEntity.ok(response);
    }

}