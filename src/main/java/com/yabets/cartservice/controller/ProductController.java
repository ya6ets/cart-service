package com.yabets.cartservice.controller;

import com.yabets.cartservice.domain.Product;
import com.yabets.cartservice.dto.ProductDto;
import com.yabets.cartservice.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public List<Product> createProducts(@RequestBody @Valid List<ProductDto> products) {

        return productService.createProducts(products);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Product> getProducts() {

        return productService.getAllProducts();
    }
}