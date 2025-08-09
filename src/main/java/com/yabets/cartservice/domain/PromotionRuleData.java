package com.yabets.cartservice.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "promotion_rules_data")
@Data
@NoArgsConstructor
public class PromotionRuleData {
    @Id
    @GeneratedValue
    private Long id;

    @JsonIgnore
    @ToString.Exclude // Exclude the parent reference from the toString() method
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    @Column(name = "rule_key")
    private String key;

    @Column(name = "rule_value")
    private String value;

    public PromotionRuleData(String key, String value) {
        this.key = key;
        this.value = value;
    }
}