package br.com.ford.specradar.controller;

import br.com.ford.specradar.dto.request.EspecificacaoRequest;
import br.com.ford.specradar.dto.response.EspecificacaoResponse;
import br.com.ford.specradar.service.EspecificacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/veiculos/{veiculoId}/especificacoes")
@RequiredArgsConstructor
@Tag(name = "Especificações", description = "Gerenciamento de especificações técnicas dos veículos")
@SecurityRequirement(name = "bearerAuth")
public class EspecificacaoController {

    private final EspecificacaoService especificacaoService;

    @Operation(summary = "Listar especificações", description = "Lista todas as especificações de um veículo")
    @GetMapping
    public ResponseEntity<List<EspecificacaoResponse>> listar(
            @PathVariable Long veiculoId
    ) {
        return ResponseEntity.ok(especificacaoService.listarPorVeiculo(veiculoId));
    }

    @Operation(summary = "Buscar especificação", description = "Retorna uma especificação específica de um veículo")
    @GetMapping("/{specId}")
    public ResponseEntity<EspecificacaoResponse> buscarPorId(
            @PathVariable Long veiculoId,
            @PathVariable Long specId
    ) {
        return ResponseEntity.ok(especificacaoService.buscarPorId(veiculoId, specId));
    }

    @Operation(summary = "Cadastrar especificação", description = "Adiciona uma especificação ao veículo — apenas ADMIN")
    @PostMapping
    public ResponseEntity<EspecificacaoResponse> criar(
            @PathVariable Long veiculoId,
            @RequestBody @Valid EspecificacaoRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(especificacaoService.criar(veiculoId, request));
    }

    @Operation(summary = "Atualizar especificação", description = "Atualiza uma especificação — apenas ADMIN")
    @PutMapping("/{specId}")
    public ResponseEntity<EspecificacaoResponse> atualizar(
            @PathVariable Long veiculoId,
            @PathVariable Long specId,
            @RequestBody @Valid EspecificacaoRequest request
    ) {
        return ResponseEntity.ok(especificacaoService.atualizar(veiculoId, specId, request));
    }

    @Operation(summary = "Deletar especificação", description = "Remove uma especificação — apenas ADMIN")
    @DeleteMapping("/{specId}")
    public ResponseEntity<Void> deletar(
            @PathVariable Long veiculoId,
            @PathVariable Long specId
    ) {
        especificacaoService.deletar(veiculoId, specId);
        return ResponseEntity.noContent().build();
    }
}