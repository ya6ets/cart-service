package com.yabets.cartservice.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "promotion_rules_data")
@Data
@NoArgsConstructor
public class PromotionRuleData {
    @Id
    @GeneratedValue
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    @Column(name = "rule_key")
    @NotBlank(message = "key is required")
    private String key;

    @Column(name = "rule_value")
    @NotBlank(message = "value is required")
    private String value;

    public PromotionRuleData(String key, String value) {
        this.key = key;
        this.value = value;
    }
}