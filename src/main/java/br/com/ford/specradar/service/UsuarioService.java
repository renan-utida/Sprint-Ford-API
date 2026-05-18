package br.com.ford.specradar.service;

import br.com.ford.specradar.domain.Usuario;
import br.com.ford.specradar.dto.request.UsuarioRequest;
import br.com.ford.specradar.dto.response.UsuarioResponse;
import br.com.ford.specradar.exception.ResourceNotFoundException;
import br.com.ford.specradar.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll()
                .stream()
                .map(UsuarioResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
        return UsuarioResponse.fromEntity(usuario);
    }

    @Transactional
    public UsuarioResponse criar(UsuarioRequest dto) {
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email já cadastrado: " + dto.getEmail());
        }

        Usuario usuario = Usuario.builder()
                .nome(dto.getNome())
                .email(dto.getEmail())
                .senha(passwordEncoder.encode(dto.getSenha()))
                .role(dto.getRole())
                .ativo(true)
                .build();

        UsuarioResponse response = UsuarioResponse.fromEntity(usuarioRepository.save(usuario));
        log.info("[AUDITORIA] Usuário criado — id: {} email: {} role: {}",
                response.getId(), response.getEmail(), response.getRole());
        return response;
    }

    @Transactional
    public UsuarioResponse atualizar(Long id, UsuarioRequest dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));

        // Verifica se o novo email já pertence a outro usuário
        if (!usuario.getEmail().equals(dto.getEmail())
                && usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email já cadastrado: " + dto.getEmail());
        }

        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        usuario.setRole(dto.getRole());

        UsuarioResponse response = UsuarioResponse.fromEntity(usuarioRepository.save(usuario));
        log.info("[AUDITORIA] Usuário atualizado — id: {} email: {} role: {}",
                response.getId(), response.getEmail(), response.getRole());
        return response;
    }

    @Transactional
    public void desativar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
        log.info("[AUDITORIA] Usuário desativado — id: {} email: {}",
                id, usuario.getEmail());
    }

    @Transactional
    public UsuarioResponse reativar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));

        if (usuario.getAtivo()) {
            throw new IllegalArgumentException("Usuário já está ativo");
        }

        usuario.setAtivo(true);
        UsuarioResponse response = UsuarioResponse.fromEntity(usuarioRepository.save(usuario));
        log.info("[AUDITORIA] Usuário reativado — id: {} email: {}",
                id, usuario.getEmail());
        return response;
    }

    @Transactional
    public void anonimizar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));

        if (usuario.getAtivo()) {
            throw new IllegalArgumentException(
                    "Apenas usuários desativados podem ser anonimizados"
            );
        }

        String emailOriginal = usuario.getEmail();
        usuario.setNome("Usuário Removido");
        usuario.setEmail("anonimizado_" + id + "@specradar.com");
        usuario.setSenha(passwordEncoder.encode(UUID.randomUUID().toString()));
        usuarioRepository.save(usuario);
        log.info("[AUDITORIA] Usuário anonimizado — id: {} email original: {}",
                id, emailOriginal);
    }
}