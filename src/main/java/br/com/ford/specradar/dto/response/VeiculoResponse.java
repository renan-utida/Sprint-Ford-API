package br.com.ford.specradar.dto.response;

import br.com.ford.specradar.domain.Veiculo;
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
public class VeiculoResponse {

    private Long id;
    private MarcaVeiculo marca;
    private String modelo;
    private String versao;
    private Integer ano;
    private Boolean ativo;
    private LocalDateTime criadoEm;

    public static VeiculoResponse fromEntity(Veiculo veiculo) {
        return VeiculoResponse.builder()
                .id(veiculo.getId())
                .marca(veiculo.getMarca())
                .modelo(veiculo.getModelo())
                .versao(veiculo.getVersao())
                .ano(veiculo.getAno())
                .ativo(veiculo.getAtivo())
                .criadoEm(veiculo.getCriadoEm())
                .build();
    }
}