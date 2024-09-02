package com.example.login_auth_api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.example.login_auth_api.domain.sales.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SaleDTO {
    private String id;
    private LocalDateTime saleDate;
    private BigDecimal totalAmount;
    private List<SaleItemDTO> items;
    private String observation;
    private PaymentMethod paymentMethod;
    private BigDecimal discount;
    private BigDecimal subtotal;
    private String userId;
    private String userName;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SaleItemDTO {
        private String id;
        private String productId;
        private String productName;
        private String sizeId;
        private String sizeName;
        private Integer quantity;

    }


}
