package com.yabets.cartservice.dto;

import com.yabets.cartservice.domain.enums.PromotionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.Map;

@Data
public class PromotionDto {

    @NotNull
    private PromotionType type;

    @NotBlank
    private String name;

    private Map<String, String> rulesData;
}