package br.com.ford.specradar.service;

import br.com.ford.specradar.domain.Consulta;
import br.com.ford.specradar.domain.Usuario;
import br.com.ford.specradar.domain.Veiculo;
import br.com.ford.specradar.dto.response.ConsultaResponse;
import br.com.ford.specradar.exception.ResourceNotFoundException;
import br.com.ford.specradar.repository.ConsultaRepository;
import br.com.ford.specradar.repository.UsuarioRepository;
import br.com.ford.specradar.repository.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final UsuarioRepository usuarioRepository;
    private final VeiculoRepository veiculoRepository;

    public List<ConsultaResponse> listarTodas() {
        return consultaRepository.findAll()
                .stream()
                .map(ConsultaResponse::fromEntity)
                .toList();
    }

    public List<ConsultaResponse> listarPorUsuario(Long usuarioId) {
        return consultaRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(ConsultaResponse::fromEntity)
                .toList();
    }

    public ConsultaResponse registrar(Long usuarioId, Long veiculoId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", usuarioId));

        Veiculo veiculo = veiculoRepository.findById(veiculoId)
                .orElseThrow(() -> new ResourceNotFoundException("Veiculo", veiculoId));

        Consulta consulta = Consulta.builder()
                .usuario(usuario)
                .veiculo(veiculo)
                .build();

        return ConsultaResponse.fromEntity(consultaRepository.save(consulta));
    }
}