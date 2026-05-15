package br.com.ford.specradar.service;

import br.com.ford.specradar.domain.Veiculo;
import br.com.ford.specradar.domain.enums.MarcaVeiculo;
import br.com.ford.specradar.dto.request.VeiculoRequest;
import br.com.ford.specradar.dto.response.VeiculoResponse;
import br.com.ford.specradar.exception.ResourceNotFoundException;
import br.com.ford.specradar.repository.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VeiculoService {

    private final VeiculoRepository veiculoRepository;

    @Transactional(readOnly = true)
    public List<VeiculoResponse> listarTodos() {
        return veiculoRepository.findAll()
                .stream()
                .map(VeiculoResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VeiculoResponse> listarAtivos() {
        return veiculoRepository.findByAtivoTrue()
                .stream()
                .map(VeiculoResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VeiculoResponse> listarPorMarca(MarcaVeiculo marca) {
        return veiculoRepository.findByMarcaAndAtivoTrue(marca)
                .stream()
                .map(VeiculoResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public VeiculoResponse buscarPorId(Long id) {
        Veiculo veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veiculo", id));
        return VeiculoResponse.fromEntity(veiculo);
    }

    // Usado internamente pelo ConsultaService
    @Transactional(readOnly = true)
    public Veiculo buscarEntidadePorId(Long id) {
        return veiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veiculo", id));
    }

    @Transactional
    public VeiculoResponse criar(VeiculoRequest dto) {
        Veiculo veiculo = Veiculo.builder()
                .marca(dto.getMarca())
                .modelo(dto.getModelo())
                .versao(dto.getVersao())
                .ano(dto.getAno())
                .ativo(true)
                .build();

        return VeiculoResponse.fromEntity(veiculoRepository.save(veiculo));
    }

    @Transactional
    public VeiculoResponse atualizar(Long id, VeiculoRequest dto) {
        Veiculo veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veiculo", id));

        veiculo.setMarca(dto.getMarca());
        veiculo.setModelo(dto.getModelo());
        veiculo.setVersao(dto.getVersao());
        veiculo.setAno(dto.getAno());

        return VeiculoResponse.fromEntity(veiculoRepository.save(veiculo));
    }

    @Transactional
    public void desativar(Long id) {
        Veiculo veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veiculo", id));
        veiculo.setAtivo(false);
        veiculoRepository.save(veiculo);
    }

    @Transactional
    public VeiculoResponse reativar(Long id) {
        Veiculo veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veiculo", id));

        if (veiculo.getAtivo()) {
            throw new IllegalArgumentException("Veículo já está ativo");
        }

        veiculo.setAtivo(true);
        return VeiculoResponse.fromEntity(veiculoRepository.save(veiculo));
    }
}