package com.noera.noera.controller;

import com.noera.noera.model.Product;
import com.noera.noera.model.ProductColor;
import com.noera.noera.model.ProductSize;
import com.noera.noera.service.ColorService;
import com.noera.noera.service.ShopService;
import com.noera.noera.service.SizeService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;


@Controller
@RequestMapping("/admin")
public class AdminController {
    private static final String UPLOAD_DIR = "src/main/resources/static/images/";
    
    private final ShopService service;
    private final SizeService sizeService;
    private final ColorService colorService;

    public AdminController(ShopService service, SizeService sizeService, ColorService colorService) {
        this.service = service;
        this.sizeService = sizeService;
        this.colorService = colorService;
    }

    @GetMapping("/products")
    public String getProducts(Model model) {
        List<Product> products = service.findAll();
        model.addAttribute("products", products);
        model.addAttribute("product", new Product());
        return "ad";
    }

    @PostMapping("/products")
    public String addProduct(
        @ModelAttribute Product product,
        MultipartHttpServletRequest request) {
        
        request.getParameterMap().forEach((key, value) -> {
            System.out.println("Param: " + key + " = " + Arrays.toString(value));
        });
        
        // Debug logging to see all files
        request.getFileMap().forEach((key, value) -> {
            System.out.println("File: " + key + " = " + (value != null ? value.getOriginalFilename() : "null"));
        });

        List<ProductSize> sizes = new ArrayList<>();
        Map<String, String[]> parameterMap = request.getParameterMap();
        
        // Создаем директорию для загрузки, если её нет
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "redirect:/admin/products?error=upload_dir";
        }

        // Обработка размеров и цветов
        Set<Integer> sizeIndices = new TreeSet<>();
        for (String paramName : parameterMap.keySet()) {
            if (paramName.matches("sizes\\[\\d+\\]\\.sizeName")) {
                int index = Integer.parseInt(paramName.replaceAll(".*\\[(\\d+)\\].*", "$1"));
                sizeIndices.add(index);
            }
        }

        for (int sizeIndex : sizeIndices) {
            ProductSize size = new ProductSize();
            String sizeName = getLastNonEmptyValue(request.getParameterValues("sizes[" + sizeIndex + "].sizeName"));
            size.setSizeName(sizeName);

            // Обработка цветов
            Set<Integer> colorIndices = new TreeSet<>();
            for (String paramName : parameterMap.keySet()) {
                if (paramName.matches("sizes\\[" + sizeIndex + "\\]\\.colors\\[\\d+\\]\\.colorName")) {
                    int colorIndex = Integer.parseInt(paramName.replaceAll(".*\\[(\\d+)\\].*", "$1"));
                    colorIndices.add(colorIndex);
                }
            }

            List<ProductColor> colors = new ArrayList<>();
            for (int colorIndex : colorIndices) {
                ProductColor color = new ProductColor();
                color.setColorName(getLastNonEmptyValue(
                    request.getParameterValues("sizes[" + sizeIndex + "].colors[" + colorIndex + "].colorName")));

                String quantityStr = getLastNonEmptyValue(
                    request.getParameterValues("sizes[" + sizeIndex + "].colors[" + colorIndex + "].quantity"));
                color.setQuantity(quantityStr != null ? Integer.parseInt(quantityStr) : 0);

                // Обработка изображений
                MultipartFile imageFile = request.getFile("sizeColorImages[" + sizeIndex + "]" + "[" + colorIndex + "]");
                if (imageFile != null && !imageFile.isEmpty()) {
                    System.out.println("Found image file: " + imageFile.getOriginalFilename() + ", size: " + imageFile.getSize());
                    String imageUrl = saveImage(imageFile);
                    color.setImageUrl(imageUrl);
                } else {
                    System.out.println("No image file found for size " + sizeIndex + " color " + colorIndex);
                }

                MultipartFile hoverImageFile = request.getFile("sizeColorHoverImages[" + sizeIndex + "]" + "[" + colorIndex + "]");
                if (hoverImageFile != null && !hoverImageFile.isEmpty()) {
                    String hoverImageUrl = saveImage(hoverImageFile);
                    color.setHoverImageUrl(hoverImageUrl);
                }

                color.setProductSize(size);
                colors.add(color);
            }

            size.setColors(colors);
            size.setProduct(product);
            sizes.add(size);
        }

        product.setSizes(sizes);
        service.save(product);
        return "redirect:/admin/products";
    }

    private String getLastNonEmptyValue(String[] values) {
        if (values == null || values.length == 0) return null;
        for (int i = values.length - 1; i >= 0; i--) {
            if (values[i] != null && !values[i].trim().isEmpty()) {
                return values[i].trim();
            }
        }
        return null;
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

    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Integer id) {
        service.delete(id);
        return "redirect:/admin/products";
    }
    @PostMapping("/products/image")
    public String handleProductUpload(
            @RequestParam String name,
            @RequestParam String size,
            @RequestParam String color,
            @RequestParam("image") MultipartFile imageFile,
            @RequestParam(value = "hoverImage", required = false) MultipartFile hoverImageFile) {

        // ProductColor color = new ProductColor();
        System.out.println(name + " " + size + " " + color);
        Product product = service.findOneByName(name);
        ProductSize productSize = sizeService.findByProductIdAndSizeName(product.getId(), size);
        ProductColor productColor = colorService.findBySizeIdAndColorName(productSize.getId(), color);
        System.out.println(productColor.getId());
        // productColor.setImageUrl()
        // service.save(product);
        if (imageFile != null && !imageFile.isEmpty()) {
                System.out.println("Found image file: " + imageFile.getOriginalFilename() + ", size: " + imageFile.getSize());
                String imageUrl = saveImage(imageFile);
                productColor.setImageUrl(imageUrl);
            } else {
                System.out.println("No image file found for size ");
            }

            
            if (hoverImageFile != null && !hoverImageFile.isEmpty()) {
                String hoverImageUrl = saveImage(hoverImageFile);
                productColor.setHoverImageUrl(hoverImageUrl);
            }
        service.save(product);
        return "redirect:/admin/products"; // страница успешного добавления
    }
    
   @PostMapping("/products/{productId}/sizes/{sizeId}/delete")
    public String deleteSize(@PathVariable Integer productId, @PathVariable Integer sizeId) {
        sizeService.deleteSize(sizeId);
        return "redirect:/admin/products";
    }

    @PostMapping("/products/{productId}/colors/{colorId}/delete")
    public String deleteColor(@PathVariable Integer productId, @PathVariable Integer colorId) {
        colorService.deleteColor(colorId);
        return "redirect:/admin/products";
    }
}