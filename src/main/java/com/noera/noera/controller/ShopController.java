package com.noera.noera.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.noera.noera.model.Product;
import com.noera.noera.service.ShopService;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/description")
    public String getDescription() {
        
        return "description";
    }
    
    
    // @GetMapping()
    // public String getMethodName(@RequestParam String param) {
    //     return new String();
    // }
    
}
