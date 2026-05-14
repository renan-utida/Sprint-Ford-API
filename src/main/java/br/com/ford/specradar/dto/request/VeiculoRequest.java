package br.com.ford.specradar.dto.request;

import br.com.ford.specradar.domain.enums.MarcaVeiculo;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VeiculoRequest {

    @NotNull(message = "Marca é obrigatória")
    private MarcaVeiculo marca;

    @NotBlank(message = "Modelo é obrigatório")
    @Size(min = 1, max = 100, message = "Modelo deve ter no máximo 100 caracteres")
    private String modelo;

    @NotBlank(message = "Versão é obrigatória")
    @Size(min = 1, max = 100, message = "Versão deve ter no máximo 100 caracteres")
    private String versao;

    @NotNull(message = "Ano é obrigatório")
    @Min(value = 1900, message = "Ano inválido")
    @Max(value = 2100, message = "Ano inválido")
    private Integer ano;
}