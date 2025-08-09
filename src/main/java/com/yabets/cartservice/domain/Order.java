package com.yabets.cartservice.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Data
public class Order {

    @Id
    private String id;
    private String customerSegment;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "order", orphanRemoval = true)
    private List<OrderItem> items;

    @Column(precision = 10, scale = 2)
    private BigDecimal finalPrice;

    @ElementCollection
    private List<String> appliedPromotions;

    private LocalDateTime orderDate;
}