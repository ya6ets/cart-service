package com.yabets.cartservice.service;

import com.yabets.cartservice.domain.Product;
import com.yabets.cartservice.dto.ProductDto;
import com.yabets.cartservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public List<Product> createProducts(List<ProductDto> productDtos) {

        List<Product> products = productDtos.stream()
                .map(dto -> {

                    Product product = new Product();
                    product.setName(dto.getName());
                    product.setCategory(dto.getCategory());
                    product.setPrice(dto.getPrice());
                    product.setStock(dto.getStock());

                    return product;

                }).toList();

        return productRepository.saveAll(products);
    }

    public List<Product> getAllProducts() {

        return productRepository.findAll();
    }
}