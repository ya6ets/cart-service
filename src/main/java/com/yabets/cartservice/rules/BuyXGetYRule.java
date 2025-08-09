package com.yabets.cartservice.rules;

import com.yabets.cartservice.domain.Promotion;
import com.yabets.cartservice.domain.PromotionRuleData;
import com.yabets.cartservice.domain.enums.PromotionType;
import com.yabets.cartservice.dto.CartQuoteRequest;
import com.yabets.cartservice.domain.Product;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class BuyXGetYRule implements PromotionRule {

    @Override
    public String getName() {
        return "Buy X Get Y Free";
    }

    @Override
    public PromotionType getType() {
        return PromotionType.BUY_X_GET_Y;
    }

    @Override
    public BigDecimal apply(Map<String, BigDecimal> itemizedPrices, CartQuoteRequest request, Promotion promotion,
                            Map<String, Product> productsInCart) {

        String productIdString = promotion.getRulesData().stream()
                .filter(ruleData -> "productId".equals(ruleData.getKey()))
                .map(PromotionRuleData::getValue)
                .findFirst().orElse(null);

        String buyCountString = promotion.getRulesData().stream()
                .filter(ruleData -> "buyCount".equals(ruleData.getKey()))
                .map(PromotionRuleData::getValue)
                .findFirst().orElse(null);

        String getCountString = promotion.getRulesData().stream()
                .filter(ruleData -> "getCount".equals(ruleData.getKey()))
                .map(PromotionRuleData::getValue)
                .findFirst().orElse(null);

        if (productIdString == null || buyCountString == null || getCountString == null) {
            log.error("Missing rules data for promotion: {}", promotion.getId());
            return BigDecimal.ZERO;
        }

        UUID productId = UUID.fromString(productIdString);
        int buyCount = Integer.parseInt(buyCountString);
        int getCount = Integer.parseInt(getCountString);
        BigDecimal discount = BigDecimal.ZERO;

        for (var item : request.getItems()) {

            if (item.getProductId().equals(productId)) {

                Product product = productsInCart.get(item.getProductId().toString());

                if (product != null) {

                    int freeItems = (item.getQty() / buyCount) * getCount;
                    BigDecimal freeItemCost = product.getPrice().multiply(BigDecimal.valueOf(freeItems));

                    itemizedPrices.computeIfPresent(item.getProductId().toString(), (key, value) -> {
                        BigDecimal newPrice = value.subtract(freeItemCost);
                        return newPrice.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : newPrice;
                    });

                    discount = discount.add(freeItemCost);
                }
            }
        }

        return discount;
    }
}