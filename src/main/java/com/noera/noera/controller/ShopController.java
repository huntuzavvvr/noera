package com.noera.noera.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.noera.noera.model.Product;
import com.noera.noera.service.ShopService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@Controller
@RequestMapping
public class ShopController {
    
    private ShopService service;

    public ShopController(ShopService service){
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

    @GetMapping("/description/{id}")
    public String getDescription(@PathVariable Integer id, Model model) {
        Product product = service.findById(id);
        model.addAttribute("product", product);
        return "description";
    }
    
    @GetMapping("/product/{name}/color/{color}")
    @ResponseBody
    public Product getProductByNameAndColor(@PathVariable String name, @PathVariable String color) {
        return service.findByNameAndColor(name, color);
    }
    
    // @GetMapping()
    // public String getMethodName(@RequestParam String param) {
    //     return new String();
    // }
    
}
