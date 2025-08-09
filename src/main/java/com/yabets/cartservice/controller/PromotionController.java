package com.yabets.cartservice.controller;

import com.yabets.cartservice.domain.Promotion;
import com.yabets.cartservice.dto.PromotionDto;
import com.yabets.cartservice.service.PromotionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public List<Promotion> createPromotions(@RequestBody @Valid List<PromotionDto> promotions) {

        return promotionService.createPromotions(promotions);
    }
}