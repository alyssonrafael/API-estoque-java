package com.example.login_auth_api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductSizeDTO {

    @NotEmpty(message = "id is required")
    private String id;

    @NotEmpty(message = "Size is required")
    private String size;

    @NotNull(message = "Quantity is required")
    private Integer quantity;
}
