package br.com.ford.specradar.controller;

import br.com.ford.specradar.dto.request.UsuarioRequest;
import br.com.ford.specradar.dto.response.UsuarioResponse;
import br.com.ford.specradar.service.UsuarioService;
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
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Gerenciamento de usuários — apenas ADMIN")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @Operation(summary = "Listar usuários", description = "Lista todos os usuários cadastrados")
    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar() {
        return ResponseEntity.ok(usuarioService.listar());
    }

    @Operation(summary = "Buscar usuário por ID")
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @Operation(summary = "Registrar usuário", description = "Cria um novo usuário — apenas ADMIN")
    @PostMapping
    public ResponseEntity<UsuarioResponse> criar(@RequestBody @Valid UsuarioRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(usuarioService.criar(request));
    }

    @Operation(summary = "Atualizar usuário")
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid UsuarioRequest request
    ) {
        return ResponseEntity.ok(usuarioService.atualizar(id, request));
    }

    @Operation(summary = "Desativar usuário")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        usuarioService.desativar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reativar usuário",
            description = "Reativa um usuário desativado — apenas ADMIN")
    @PatchMapping("/{id}/reativar")
    public ResponseEntity<UsuarioResponse> reativar(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.reativar(id));
    }

    @Operation(summary = "Anonimizar usuário",
            description = "Anonimiza dados pessoais de usuário desativado — apenas ADMIN")
    @PatchMapping("/{id}/anonimizar")
    public ResponseEntity<Void> anonimizar(@PathVariable Long id) {
        usuarioService.anonimizar(id);
        return ResponseEntity.noContent().build();
    }
}