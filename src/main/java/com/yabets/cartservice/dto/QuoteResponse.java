package com.yabets.cartservice.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class QuoteResponse {
    private Map<String, BigDecimal> itemizedPrices;
    private BigDecimal totalAmount;
    private List<String> appliedPromotions;
}