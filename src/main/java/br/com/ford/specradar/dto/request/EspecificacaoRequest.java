package br.com.ford.specradar.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EspecificacaoRequest {

    @NotBlank(message = "Atributo é obrigatório")
    @Size(min = 1, max = 100, message = "Atributo deve ter no máximo 100 caracteres")
    private String atributo;

    @NotBlank(message = "Valor é obrigatório")
    @Size(min = 1, max = 255, message = "Valor deve ter no máximo 255 caracteres")
    private String valor;

    @Size(max = 50, message = "Unidade deve ter no máximo 50 caracteres")
    private String unidade;

    @NotNull(message = "Disponível é obrigatório")
    private Boolean disponivel;
}