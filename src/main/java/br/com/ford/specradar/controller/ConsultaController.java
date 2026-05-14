package br.com.ford.specradar.controller;

import br.com.ford.specradar.domain.Usuario;
import br.com.ford.specradar.domain.enums.RoleUsuario;
import br.com.ford.specradar.dto.response.ConsultaResponse;
import br.com.ford.specradar.service.ConsultaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consultas")
@RequiredArgsConstructor
@Tag(name = "Consultas", description = "Histórico de consultas realizadas")
@SecurityRequirement(name = "bearerAuth")
public class ConsultaController {

    private final ConsultaService consultaService;

    @Operation(
            summary = "Listar consultas",
            description = "ADMIN vê todas as consultas. ANALISTA vê apenas as próprias."
    )
    @GetMapping
    public ResponseEntity<List<ConsultaResponse>> listar(
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        if (usuarioLogado.getRole() == RoleUsuario.ADMIN) {
            return ResponseEntity.ok(consultaService.listarTodas());
        }
        return ResponseEntity.ok(
                consultaService.listarPorUsuario(usuarioLogado.getId())
        );
    }
}