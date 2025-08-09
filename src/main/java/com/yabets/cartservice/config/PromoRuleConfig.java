package com.yabets.cartservice.config;

import com.yabets.cartservice.rules.BuyXGetYRule;
import com.yabets.cartservice.rules.PercentOffCategoryRule;
import com.yabets.cartservice.rules.PromotionRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PromoRuleConfig {

    @Bean
    public PromotionRule percentOffCategoryRule() {
        return new PercentOffCategoryRule();
    }

    @Bean
    public PromotionRule buyXGetYRule() {
        return new BuyXGetYRule();
    }
}