package com.example.login_auth_api.dto;

import com.example.login_auth_api.domain.products.ProductSize;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductSizeDTO {

    @NotEmpty(message = "O id é obrigatório")
    private String id;

    @NotEmpty(message = "O tamanho é obrigatório")
    private String size;

    @NotNull(message = "A quantidade é obrigatória")
    private Integer quantity;

    // Método de conversão de ProductSize para ProductSizeDTO
    public static ProductSizeDTO fromEntity(ProductSize size) {
        ProductSizeDTO dto = new ProductSizeDTO();
        dto.setId(size.getId());  // Inclui o id na conversão
        dto.setSize(size.getSize());
        dto.setQuantity(size.getQuantity());
        return dto;
    }
}