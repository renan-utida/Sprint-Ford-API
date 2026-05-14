package br.com.ford.specradar.service;

import br.com.ford.specradar.domain.Especificacao;
import br.com.ford.specradar.domain.Veiculo;
import br.com.ford.specradar.dto.request.EspecificacaoRequest;
import br.com.ford.specradar.dto.response.EspecificacaoResponse;
import br.com.ford.specradar.exception.ResourceNotFoundException;
import br.com.ford.specradar.repository.EspecificacaoRepository;
import br.com.ford.specradar.repository.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EspecificacaoService {

    private final EspecificacaoRepository especificacaoRepository;
    private final VeiculoRepository veiculoRepository;

    public List<EspecificacaoResponse> listarPorVeiculo(Long veiculoId) {
        // Verifica se o veículo existe antes de listar
        if (!veiculoRepository.existsById(veiculoId)) {
            throw new ResourceNotFoundException("Veiculo", veiculoId);
        }

        return especificacaoRepository.findByVeiculoId(veiculoId)
                .stream()
                .map(EspecificacaoResponse::fromEntity)
                .toList();
    }

    public EspecificacaoResponse buscarPorId(Long veiculoId, Long specId) {
        Especificacao especificacao = especificacaoRepository.findById(specId)
                .orElseThrow(() -> new ResourceNotFoundException("Especificacao", specId));

        // Garante que a especificação pertence ao veículo informado
        if (!especificacao.getVeiculo().getId().equals(veiculoId)) {
            throw new ResourceNotFoundException("Especificacao", specId);
        }

        return EspecificacaoResponse.fromEntity(especificacao);
    }

    public EspecificacaoResponse criar(Long veiculoId, EspecificacaoRequest dto) {
        Veiculo veiculo = veiculoRepository.findById(veiculoId)
                .orElseThrow(() -> new ResourceNotFoundException("Veiculo", veiculoId));

        Especificacao especificacao = Especificacao.builder()
                .veiculo(veiculo)
                .atributo(dto.getAtributo())
                .valor(dto.getValor())
                .unidade(dto.getUnidade())
                .disponivel(dto.getDisponivel())
                .build();

        return EspecificacaoResponse.fromEntity(
                especificacaoRepository.save(especificacao)
        );
    }

    public EspecificacaoResponse atualizar(Long veiculoId, Long specId, EspecificacaoRequest dto) {
        Especificacao especificacao = especificacaoRepository.findById(specId)
                .orElseThrow(() -> new ResourceNotFoundException("Especificacao", specId));

        if (!especificacao.getVeiculo().getId().equals(veiculoId)) {
            throw new ResourceNotFoundException("Especificacao", specId);
        }

        especificacao.setAtributo(dto.getAtributo());
        especificacao.setValor(dto.getValor());
        especificacao.setUnidade(dto.getUnidade());
        especificacao.setDisponivel(dto.getDisponivel());

        return EspecificacaoResponse.fromEntity(
                especificacaoRepository.save(especificacao)
        );
    }

    public void deletar(Long veiculoId, Long specId) {
        Especificacao especificacao = especificacaoRepository.findById(specId)
                .orElseThrow(() -> new ResourceNotFoundException("Especificacao", specId));

        if (!especificacao.getVeiculo().getId().equals(veiculoId)) {
            throw new ResourceNotFoundException("Especificacao", specId);
        }

        especificacaoRepository.delete(especificacao);
    }
}