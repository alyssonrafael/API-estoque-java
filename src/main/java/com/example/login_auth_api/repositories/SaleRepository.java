package com.example.login_auth_api.repositories;

import com.example.login_auth_api.domain.sales.Sale;
import com.example.login_auth_api.dto.SaleDTO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, String> {
    List<Sale> findBySaleDateBetween(LocalDateTime startDate, LocalDateTime endDate);

}
