package com.example.login_auth_api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {

    @NotNull(message = "O nome não pode ser nulo.")
    @NotEmpty(message = "O nome não pode estar vazio.")
    private String nome;

    private Boolean deleted; // Este campo é opcional; se não enviado, assume-se como false no controller

}
