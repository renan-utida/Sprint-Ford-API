package br.com.ford.specradar.dto.response;

import br.com.ford.specradar.domain.Consulta;
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
public class ConsultaResponse {

    private Long id;
    private String nomeUsuario;
    private String emailUsuario;
    private MarcaVeiculo marcaVeiculo;
    private String modeloVeiculo;
    private String versaoVeiculo;
    private LocalDateTime realizadaEm;

    public static ConsultaResponse fromEntity(Consulta consulta) {
        return ConsultaResponse.builder()
                .id(consulta.getId())
                .nomeUsuario(consulta.getUsuario().getNome())
                .emailUsuario(consulta.getUsuario().getEmail())
                .marcaVeiculo(consulta.getVeiculo().getMarca())
                .modeloVeiculo(consulta.getVeiculo().getModelo())
                .versaoVeiculo(consulta.getVeiculo().getVersao())
                .realizadaEm(consulta.getRealizadaEm())
                .build();
    }
}