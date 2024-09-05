package com.example.login_auth_api.dto;

import com.example.login_auth_api.domain.products.ProductSize;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductSizeDTO {

    @NotEmpty(message = "O id é obrigatorio")
    private String id;

    @NotEmpty(message = "O tamanho é obrigatorio")
    private String size;

    @NotNull(message = "A quantidade e obrigatoria")
    private Integer quantity;

    // Método de conversão de ProductSize para ProductSizeDTO
    public static ProductSizeDTO fromEntity(ProductSize size) {
        ProductSizeDTO dto = new ProductSizeDTO();
        dto.setSize(size.getSize());
        dto.setQuantity(size.getQuantity());
        return dto;
    }
}
