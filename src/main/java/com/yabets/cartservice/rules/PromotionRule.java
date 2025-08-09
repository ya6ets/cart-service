package com.yabets.cartservice.rules;

import com.yabets.cartservice.domain.enums.PromotionType;
import com.yabets.cartservice.dto.CartQuoteRequest;
import com.yabets.cartservice.domain.Promotion;

import java.math.BigDecimal;
import java.util.Map;

public interface PromotionRule {

    String getName();

    PromotionType getType();

    BigDecimal apply(Map<String, BigDecimal> itemizedPrices, CartQuoteRequest request, Promotion promotion,
                     Map<String, com.yabets.cartservice.domain.Product> productsInCart);
}