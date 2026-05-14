package br.com.ford.specradar.controller;

import br.com.ford.specradar.domain.Usuario;
import br.com.ford.specradar.domain.enums.MarcaVeiculo;
import br.com.ford.specradar.dto.request.VeiculoRequest;
import br.com.ford.specradar.dto.response.VeiculoResponse;
import br.com.ford.specradar.service.ConsultaService;
import br.com.ford.specradar.service.VeiculoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/veiculos")
@RequiredArgsConstructor
@Tag(name = "Veículos", description = "Gerenciamento de veículos concorrentes")
@SecurityRequirement(name = "bearerAuth")
public class VeiculoController {

    private final VeiculoService veiculoService;
    private final ConsultaService consultaService;

    @Operation(summary = "Listar veículos", description = "Lista todos os veículos ativos")
    @GetMapping
    public ResponseEntity<List<VeiculoResponse>> listar(
            @RequestParam(required = false) MarcaVeiculo marca
    ) {
        if (marca != null) {
            return ResponseEntity.ok(veiculoService.listarPorMarca(marca));
        }
        return ResponseEntity.ok(veiculoService.listar());
    }

    @Operation(summary = "Buscar veículo por ID", description = "Retorna um veículo e registra a consulta")
    @GetMapping("/{id}")
    public ResponseEntity<VeiculoResponse> buscarPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        VeiculoResponse veiculo = veiculoService.buscarPorId(id);

        // Registra a consulta automaticamente nos bastidores
        consultaService.registrar(usuarioLogado.getId(), id);

        return ResponseEntity.ok(veiculo);
    }

    @Operation(summary = "Cadastrar veículo", description = "Cria um novo veículo — apenas ADMIN")
    @PostMapping
    public ResponseEntity<VeiculoResponse> criar(@RequestBody @Valid VeiculoRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(veiculoService.criar(request));
    }

    @Operation(summary = "Atualizar veículo", description = "Atualiza os dados de um veículo — apenas ADMIN")
    @PutMapping("/{id}")
    public ResponseEntity<VeiculoResponse> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid VeiculoRequest request
    ) {
        return ResponseEntity.ok(veiculoService.atualizar(id, request));
    }

    @Operation(summary = "Desativar veículo", description = "Desativa um veículo — apenas ADMIN")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        veiculoService.desativar(id);
        return ResponseEntity.noContent().build();
    }
}