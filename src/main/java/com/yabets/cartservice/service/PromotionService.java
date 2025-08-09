package com.yabets.cartservice.service;

import com.yabets.cartservice.domain.Promotion;
import com.yabets.cartservice.domain.PromotionRuleData;
import com.yabets.cartservice.dto.PromotionDto;
import com.yabets.cartservice.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromotionService {
    private final PromotionRepository promotionRepository;

    public List<Promotion> createPromotions(List<PromotionDto> promotionDtos) {

        List<Promotion> promotions = promotionDtos.stream()
                .map(dto -> {

                    Promotion promotion = new Promotion();
                    promotion.setType(dto.getType());
                    promotion.setName(dto.getName());

                    List<PromotionRuleData> ruleDataList = dto.getRulesData().entrySet().stream()
                            .map(entry -> new PromotionRuleData(entry.getKey(), entry.getValue()))
                            .collect(Collectors.toList());

                    ruleDataList.forEach(ruleData -> ruleData.setPromotion(promotion));
                    promotion.setRulesData(ruleDataList);

                    return promotion;

                }).toList();

        return promotionRepository.saveAll(promotions);
    }

    public List<Promotion> getAllPromotions() {
        return promotionRepository.findAllWithRulesData();
    }
}