package com.example.ecommerce.controller;

import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.ProductRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Get all products, with optional category filter
    @GetMapping
    public List<Product> getAllProducts(@RequestParam(required = false) String category) {
        if (category != null && !category.isEmpty()) {
            return productRepository.findByCategory(category);
        } else {
            return productRepository.findAll();
        }
    }

    // ✅ Endpoint for fetching unique categories
    @GetMapping("/categories")
    public List<String> getAllCategories() {
        return productRepository.findDistinctCategories();
    }
}
