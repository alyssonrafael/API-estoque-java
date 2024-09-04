package com.example.login_auth_api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductDTO {

    private String id;

    @NotEmpty(message = "O nome do produto não pode ser nulo ou vazio.")
    private String name;

    @NotNull(message = "A categoria do produto não pode ser nulo ou vazio. ")
    private String categoryId;

    @NotNull(message = "O custo do produto não pode ser nulo.")
    @DecimalMin(value = "0.0", inclusive = false, message = "O custo do produto deve ser positivo.")
    private BigDecimal cost;

    @NotNull(message = "O preço do produto não pode ser nulo.")
    @DecimalMin(value = "0.0", inclusive = false, message = "O preço do produto deve ser positivo.")
    private BigDecimal price;

    private Integer quantity;

    private List<ProductSizeDTO> sizes;
}
