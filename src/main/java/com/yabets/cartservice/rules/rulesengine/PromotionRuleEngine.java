package com.yabets.cartservice.rules.rulesengine;

import com.yabets.cartservice.domain.Product;
import com.yabets.cartservice.domain.Promotion;
import com.yabets.cartservice.dto.CartQuoteRequest;
import com.yabets.cartservice.rules.PromotionRule;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Chain of Responsibility / Pipeline for promotions
@Component
public class PromotionRuleEngine {

    private final List<PromotionRule> promotionRules;
    private final Map<String, PromotionRule> ruleMap;

    public PromotionRuleEngine(List<PromotionRule> promotionRules) {
        this.promotionRules = promotionRules;
        this.ruleMap = promotionRules.stream()
                .collect(Collectors.toMap(PromotionRule::getType, rule -> rule));
    }

    public List<String> applyRules(List<Promotion> promotions, Map<String, BigDecimal> itemizedPrices, CartQuoteRequest request, Map<String, Product> productsInCart) {
        List<String> appliedPromotions = new ArrayList<>();

        // This is where we can enforce a specific order if needed.
        // For now, we apply them in the order they are provided.
        for (Promotion promotion : promotions) {
            PromotionRule rule = ruleMap.get(promotion.getType());
            if (rule != null) {
                BigDecimal discount = rule.apply(itemizedPrices, request, promotion, productsInCart);
                if (discount.compareTo(BigDecimal.ZERO) > 0) {
                    appliedPromotions.add(promotion.getName());
                }
            }
        }
        return appliedPromotions;
    }
}