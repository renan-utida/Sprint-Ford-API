package br.com.ford.specradar.dto.response;

import br.com.ford.specradar.domain.Especificacao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EspecificacaoResponse {

    private Long id;
    private Long veiculoId;
    private String atributo;
    private String valor;
    private String unidade;
    private Boolean disponivel;
    private LocalDateTime criadoEm;

    public static EspecificacaoResponse fromEntity(Especificacao especificacao) {
        return EspecificacaoResponse.builder()
                .id(especificacao.getId())
                .veiculoId(especificacao.getVeiculo().getId())
                .atributo(especificacao.getAtributo())
                .valor(especificacao.getValor())
                .unidade(especificacao.getUnidade())
                .disponivel(especificacao.getDisponivel())
                .criadoEm(especificacao.getCriadoEm())
                .build();
    }
}