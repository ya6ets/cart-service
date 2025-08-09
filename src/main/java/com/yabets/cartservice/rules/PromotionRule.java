package com.yabets.cartservice.rules;

import com.yabets.cartservice.dto.CartQuoteRequest;
import com.yabets.cartservice.domain.Promotion;

import java.math.BigDecimal;
import java.util.Map;

public interface PromotionRule {

    String getName();

    String getType();

    BigDecimal apply(Map<String, BigDecimal> itemizedPrices, CartQuoteRequest request, Promotion promotion,
                     Map<String, com.yabets.cartservice.domain.Product> productsInCart);
}