package com.yabets.cartservice.service;

import com.yabets.cartservice.domain.*;
import com.yabets.cartservice.dto.CartConfirmRequest;
import com.yabets.cartservice.dto.CartQuoteRequest;
import com.yabets.cartservice.dto.ItemDto;
import com.yabets.cartservice.dto.QuoteResponse;
import com.yabets.cartservice.exception.ResourceNotFoundException;
import com.yabets.cartservice.exception.StockConflictException;
import com.yabets.cartservice.repository.IdempotencyKeyRepository;
import com.yabets.cartservice.repository.OrderRepository;
import com.yabets.cartservice.repository.ProductRepository;
import com.yabets.cartservice.rules.rulesengine.PromotionRuleEngine;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {
    private final ProductRepository productRepository;
    private final PromotionService promotionService;
    private final PromotionRuleEngine promotionRuleEngine;
    private final OrderRepository orderRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;

    public QuoteResponse getCartQuote(CartQuoteRequest request) {

        List<UUID> productIds = request.getItems().stream()
                .map(ItemDto::getProductId)
                .toList();

        Map<String, Product> productsInCart = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(p -> p.getId().toString(), p -> p));

        if (productsInCart.size() != productIds.size()) {

            throw new ResourceNotFoundException("One or more products not found.");
        }

        Map<String, BigDecimal> itemizedPrices = calculateBasePrices(request, productsInCart);
        List<Promotion> promotions = promotionService.getAllPromotions();
        List<String> appliedPromotions = promotionRuleEngine.applyRules(promotions, itemizedPrices, request, productsInCart);

        BigDecimal totalAmount = itemizedPrices.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        return QuoteResponse.builder()
                .itemizedPrices(itemizedPrices)
                .totalAmount(totalAmount)
                .appliedPromotions(appliedPromotions)
                .build();
    }

    @Transactional
    public String confirmCart(CartConfirmRequest request, String idempotencyKeyHeader) {

        if (idempotencyKeyHeader != null) {

            Optional<IdempotencyKey> existingKey = idempotencyKeyRepository.findById(idempotencyKeyHeader);

            if (existingKey.isPresent()) {

                return existingKey.get().getOrderId();
            }
        }

        List<UUID> productIds = request.getItems().stream()
                .map(ItemDto::getProductId)
                .toList();

        // Fetch products and acquire a pessimistic lock
        List<Product> products = request.getItems().stream()
                .map(item -> productRepository.findByIdWithPessimisticLock(item.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + item.getProductId())))
                .toList();

        Map<String, Product> productsInCart = products.stream()
                .collect(Collectors.toMap(p -> p.getId().toString(), p -> p));

        if (productsInCart.size() != productIds.size()) {

            throw new ResourceNotFoundException("One or more products not found.");
        }

        // Check stock availability first before any updates
        for (ItemDto item : request.getItems()) {

            Product product = productsInCart.get(item.getProductId().toString());

            if (product.getStock() < item.getQty()) {

                throw new StockConflictException("Insufficient stock for product " + product.getName());
            }
        }

        // Recalculate price with promotions just before confirmation
        QuoteResponse quoteResponse = getCartQuote(new CartQuoteRequest(request.getItems(), request.getCustomerSegment()));

        // Decrement stock
        for (ItemDto item : request.getItems()) {

            Product product = productsInCart.get(item.getProductId().toString());
            product.setStock(product.getStock() - item.getQty());

            productRepository.save(product); // This will trigger optimistic locking check
        }

        // Create and save the order
        Order newOrder = new Order();
        newOrder.setId(UUID.randomUUID().toString());
        newOrder.setCustomerSegment(request.getCustomerSegment());
        newOrder.setFinalPrice(quoteResponse.getTotalAmount());
        newOrder.setAppliedPromotions(quoteResponse.getAppliedPromotions());
        newOrder.setOrderDate(LocalDateTime.now());

        List<OrderItem> orderItems = request.getItems().stream()
                .map(itemDto -> {

                    Product product = productsInCart.get(itemDto.getProductId().toString());
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(newOrder);
                    orderItem.setProductId(product.getId());
                    orderItem.setProductName(product.getName());
                    orderItem.setQuantity(itemDto.getQty());
                    orderItem.setItemPrice(product.getPrice());

                    return orderItem;

                }).toList();

        newOrder.setItems(orderItems);
        orderRepository.save(newOrder);

        // Save idempotency key if provided
        if (idempotencyKeyHeader != null) {

            idempotencyKeyRepository.save(new IdempotencyKey(idempotencyKeyHeader, newOrder.getId()));
        }

        return newOrder.getId();
    }

    private Map<String, BigDecimal> calculateBasePrices(CartQuoteRequest request, Map<String, Product> productsInCart) {

        Map<String, BigDecimal> itemizedPrices = new HashMap<>();

        for (ItemDto item : request.getItems()) {

            Product product = productsInCart.get(item.getProductId().toString());

            BigDecimal itemTotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(item.getQty()))
                    .setScale(2, RoundingMode.HALF_UP);

            itemizedPrices.put(product.getId().toString(), itemTotal);
        }

        return itemizedPrices;
    }
}