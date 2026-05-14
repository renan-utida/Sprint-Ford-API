package br.com.ford.specradar.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ConsultaRequest {

    @NotNull(message = "ID do veículo é obrigatório")
    private Long veiculoId;
}