package com.yabets.cartservice.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "idempotency_keys")
@Data
@NoArgsConstructor
public class IdempotencyKey {

    @Id
    private String keyId;
    private String orderId;
    private LocalDateTime createdDate;

    public IdempotencyKey(String keyId, String orderId) {
        this.keyId = keyId;
        this.orderId = orderId;
        this.createdDate = LocalDateTime.now();
    }
}