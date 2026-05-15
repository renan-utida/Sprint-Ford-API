package br.com.ford.specradar.dto.response;

import br.com.ford.specradar.domain.Especificacao;
import br.com.ford.specradar.domain.enums.MarcaVeiculo;
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
    private VeiculoInfo veiculo;
    private String atributo;
    private String valor;
    private String unidade;
    private Boolean disponivel;
    private LocalDateTime criadoEm;

    public static EspecificacaoResponse fromEntity(Especificacao especificacao) {
        return EspecificacaoResponse.builder()
                .id(especificacao.getId())
                .veiculo(VeiculoInfo.fromEntity(especificacao))
                .atributo(especificacao.getAtributo())
                .valor(especificacao.getValor())
                .unidade(especificacao.getUnidade())
                .disponivel(especificacao.getDisponivel())
                .criadoEm(especificacao.getCriadoEm())
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VeiculoInfo {
        private Long id;
        private MarcaVeiculo marca;
        private String modelo;
        private String versao;
        private Integer ano;

        public static VeiculoInfo fromEntity(Especificacao especificacao) {
            return VeiculoInfo.builder()
                    .id(especificacao.getVeiculo().getId())
                    .marca(especificacao.getVeiculo().getMarca())
                    .modelo(especificacao.getVeiculo().getModelo())
                    .versao(especificacao.getVeiculo().getVersao())
                    .ano(especificacao.getVeiculo().getAno())
                    .build();
        }
    }
}