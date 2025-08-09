package com.yabets.cartservice.repository;

import com.yabets.cartservice.domain.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, UUID> {

    @Query("SELECT p FROM Promotion p JOIN FETCH p.rulesData")
    List<Promotion> findAllWithRulesData();
}