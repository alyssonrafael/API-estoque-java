package com.example.login_auth_api.dto;

import com.example.login_auth_api.domain.products.Product;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ProductDTO {
    private String id;
    private boolean deleted;

    @NotEmpty(message = "O nome do produto não pode ser nulo ou vazio.")
    private String name;

    @NotNull(message = "A categoria do produto não pode ser nulo ou vazio. ")
    private String categoryId;

    private String categoryName;

    @NotNull(message = "O custo do produto não pode ser nulo.")
    @DecimalMin(value = "0.0", inclusive = false, message = "O custo do produto deve ser positivo.")
    private BigDecimal cost;

    @NotNull(message = "O preço do produto não pode ser nulo.")
    @DecimalMin(value = "0.0", inclusive = false, message = "O preço do produto deve ser positivo.")
    private BigDecimal price;

    private Integer quantity;

    private Integer quantitySold;

    private List<ProductSizeDTO> sizes;

    // Método de conversão de Product para ProductDTO
    public static ProductDTO fromEntity(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId().toString());
        dto.setName(product.getName());
        dto.setCategoryName(product.getCategory().getNome());
        dto.setCost(product.getCost());
        dto.setPrice(product.getPrice());
        dto.setQuantity(product.getQuantity());
        dto.setDeleted(product.isDeleted());
        dto.setQuantitySold(product.getQuantitySold());

        // Converter os tamanhos, assumindo que ProductSizeDTO também possui um método fromEntity
        dto.setSizes(product.getSizes().stream()
                .map(ProductSizeDTO::fromEntity) // Converte cada tamanho usando o método do ProductSizeDTO
                .collect(Collectors.toList()));

        return dto;
    }
}
