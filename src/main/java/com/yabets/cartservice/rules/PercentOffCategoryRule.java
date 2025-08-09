package com.yabets.cartservice.rules;

import com.yabets.cartservice.domain.Promotion;
import com.yabets.cartservice.domain.PromotionRuleData;
import com.yabets.cartservice.domain.enums.ProductCategory;
import com.yabets.cartservice.dto.CartQuoteRequest;
import com.yabets.cartservice.domain.Product;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Slf4j
public class PercentOffCategoryRule implements PromotionRule {

    @Override
    public String getName() {
        return "Category Percentage Off";
    }

    @Override
    public String getType() {
        return "PERCENT_OFF_CATEGORY";
    }

    @Override
    public BigDecimal apply(Map<String, BigDecimal> itemizedPrices, CartQuoteRequest request, Promotion promotion,
                            Map<String, Product> productsInCart) {

        String categoryString = promotion.getRulesData().stream()
                .filter(ruleData -> "category".equals(ruleData.getKey()))
                .map(PromotionRuleData::getValue)
                .findFirst().orElse(null);

        String percentageString = promotion.getRulesData().stream()
                .filter(ruleData -> "percentage".equals(ruleData.getKey()))
                .map(PromotionRuleData::getValue)
                .findFirst().orElse(null);

        if (categoryString == null || percentageString == null) {

            log.error("Missing rules data for promotion: {}", promotion.getId());
            return BigDecimal.ZERO;
        }

        ProductCategory category = ProductCategory.valueOf(categoryString);
        int percentage = Integer.parseInt(percentageString);
        BigDecimal discount = BigDecimal.ZERO;

        for (var item : request.getItems()) {

            Product product = productsInCart.get(item.getProductId().toString());

            if (product != null && product.getCategory().equals(category)) {

                BigDecimal originalPrice = product.getPrice().multiply(BigDecimal.valueOf(item.getQty()));
                BigDecimal itemDiscount = originalPrice.multiply(BigDecimal.valueOf(percentage))
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

                itemizedPrices.computeIfPresent(item.getProductId().toString(), (key, value) -> {
                    BigDecimal newPrice = value.subtract(itemDiscount);
                    return newPrice.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : newPrice;
                });

                discount = discount.add(itemDiscount);
            }
        }

        return discount;
    }
}