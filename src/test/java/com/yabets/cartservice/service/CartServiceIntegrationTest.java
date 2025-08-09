package com.yabets.cartservice.service;

import com.yabets.cartservice.domain.Product;
import com.yabets.cartservice.domain.enums.PromotionType;
import com.yabets.cartservice.dto.CartConfirmRequest;
import com.yabets.cartservice.dto.ItemDto;
import com.yabets.cartservice.dto.PromotionDto;
import com.yabets.cartservice.repository.OrderRepository;
import com.yabets.cartservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class CartServiceIntegrationTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PromotionService promotionService;

    @Autowired
    private OrderRepository orderRepository;

    private UUID productId;

    @BeforeEach
    void setUp() {

        productRepository.deleteAll();

        promotionService.createPromotions(List.of(
                PromotionDto.builder()
                        .name("10% off Books")
                        .type(PromotionType.PERCENT_OFF_CATEGORY)
                        .rulesData(Map.of("category", "BOOKS", "percentage", "10"))
                        .build()
        ));

        Product product = new Product();
        product.setName("Java Book");
        product.setCategory(com.yabets.cartservice.domain.enums.ProductCategory.BOOKS);
        product.setPrice(new BigDecimal("50.00"));
        product.setStock(10);
        Product savedProduct = productRepository.save(product);
        productId = savedProduct.getId();
    }

    @Test
    @Transactional
    void confirmCart_shouldDecreaseStockAndCreateOrder() {

        // Given
        CartConfirmRequest request = new CartConfirmRequest();
        request.setItems(List.of(new ItemDto(productId, 2)));
        int initialStock = productRepository.findById(productId).orElseThrow().getStock();

        // When
        String orderId = cartService.confirmCart(request, null);

        // Then
        assertNotNull(orderId);
        assertEquals(1, orderRepository.count());
        assertEquals(initialStock - 2, productRepository.findById(productId).orElseThrow().getStock());
    }

    @Test
    @Transactional
    void confirmCart_shouldBeIdempotent_forSameKey() {

        // Given
        CartConfirmRequest request = new CartConfirmRequest();
        request.setItems(List.of(new ItemDto(productId, 2)));
        String idempotencyKey = UUID.randomUUID().toString();
        int initialStock = productRepository.findById(productId).orElseThrow().getStock();

        // When
        String orderId1 = cartService.confirmCart(request, idempotencyKey);
        String orderId2 = cartService.confirmCart(request, idempotencyKey);

        // Then
        assertEquals(orderId1, orderId2);
        assertEquals(1, orderRepository.count());
        assertEquals(initialStock - 2, productRepository.findById(productId).orElseThrow().getStock());
    }

    @Test
    void confirmCart_shouldHandleConcurrency_withOptimisticLocking() throws InterruptedException {

        // Given
        int stockToPurchase = 1;
        int numberOfThreads = 5;

        Product product = new Product();
        product.setName("Last Item");
        product.setCategory(com.yabets.cartservice.domain.enums.ProductCategory.ELECTRONICS);
        product.setPrice(new BigDecimal("100.00"));
        product.setStock(stockToPurchase);
        Product savedProduct = productRepository.save(product);
        UUID concurrentProductId = savedProduct.getId();

        CartConfirmRequest request = new CartConfirmRequest();
        request.setItems(List.of(new ItemDto(concurrentProductId, 1)));

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);

        // When
        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    latch.await();
                    cartService.confirmCart(request, null);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    // Expected to fail due to concurrency
                }
            });
        }

        latch.countDown();
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // Then
        assertEquals(1, successCount.get());
        assertEquals(0, productRepository.findById(concurrentProductId).orElseThrow().getStock());
    }
}