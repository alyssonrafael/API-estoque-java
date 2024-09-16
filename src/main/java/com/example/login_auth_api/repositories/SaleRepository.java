package com.example.login_auth_api.repositories;

import com.example.login_auth_api.domain.sales.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, String> {
    List<Sale> findBySaleDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    long countByIsGiftTrueAndSaleDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    long countByIsGiftFalseAndSaleDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<Sale> findTop5ByOrderBySaleDateDesc();
    // Busca por intervalo de datas e status isGift.
    List<Sale> findBySaleDateBetweenAndIsGift(LocalDateTime start, LocalDateTime end, Boolean isGift);
    // Busca todas as vendas com status isGift.
    List<Sale> findByIsGift(Boolean isGift);
    List<Sale> findAllByOrderBySaleDateDesc();

}
