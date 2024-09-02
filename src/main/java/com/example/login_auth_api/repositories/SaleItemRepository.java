package com.example.login_auth_api.repositories;

import com.example.login_auth_api.domain.sales.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleItemRepository extends JpaRepository<SaleItem, String> {
}