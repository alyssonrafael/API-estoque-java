package com.example.login_auth_api.dto;

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
}
