package com.example.login_auth_api.repositories;

import com.example.login_auth_api.domain.sales.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleRepository extends JpaRepository<Sale, String> {
}
