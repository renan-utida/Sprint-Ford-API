package br.com.ford.specradar.service;

import br.com.ford.specradar.domain.Usuario;
import br.com.ford.specradar.domain.enums.RoleUsuario;
import br.com.ford.specradar.dto.request.UsuarioRequest;
import br.com.ford.specradar.dto.response.UsuarioResponse;
import br.com.ford.specradar.exception.ResourceNotFoundException;
import br.com.ford.specradar.repository.UsuarioRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes - UsuarioService")
public class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario adminPadrao;
    private Usuario analistaPadrao;
    private Usuario usuarioDesativado;
    private UsuarioRequest requestPadrao;

    @BeforeEach
    public void setUp() {
        adminPadrao = Usuario.builder()
                .id(1L)
                .nome("Administrador SpecRadar")
                .email("admin@specradar.com")
                .senha("$2a$12$hash")
                .role(RoleUsuario.ADMIN)
                .ativo(true)
                .criadoEm(LocalDateTime.now())
                .build();

        analistaPadrao = Usuario.builder()
                .id(2L)
                .nome("Analista Ford")
                .email("analista@specradar.com")
                .senha("$2a$12$hash")
                .role(RoleUsuario.ANALISTA)
                .ativo(true)
                .criadoEm(LocalDateTime.now())
                .build();

        usuarioDesativado = Usuario.builder()
                .id(3L)
                .nome("Usuario Desativado")
                .email("desativado@specradar.com")
                .senha("$2a$12$hash")
                .role(RoleUsuario.ANALISTA)
                .ativo(false)
                .criadoEm(LocalDateTime.now())
                .build();

        requestPadrao = new UsuarioRequest();
        requestPadrao.setNome("Novo Usuario");
        requestPadrao.setEmail("novo@specradar.com");
        requestPadrao.setSenha("Novo@2026");
        requestPadrao.setRole(RoleUsuario.ANALISTA);
    }

    // Listar

    @Test
    @DisplayName("Deve listar todos os usuários corretamente")
    public void testListar() {
        when(usuarioRepository.findAll())
                .thenReturn(List.of(adminPadrao, analistaPadrao));

        List<UsuarioResponse> resultado = usuarioService.listar();

        assertEquals(2, resultado.size());
        assertEquals("admin@specradar.com", resultado.get(0).getEmail());
        assertEquals("analista@specradar.com", resultado.get(1).getEmail());
        verify(usuarioRepository, times(1)).findAll();
    }

    // Buscar por ID

    @Test
    @DisplayName("Deve buscar usuário por ID existente com sucesso")
    public void testBuscarPorIdExistente() {
        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(adminPadrao));

        UsuarioResponse resultado = usuarioService.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("admin@specradar.com", resultado.getEmail());
        assertEquals(RoleUsuario.ADMIN, resultado.getRole());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao buscar ID inexistente")
    public void testBuscarPorIdInexistente() {
        when(usuarioRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> usuarioService.buscarPorId(99L));
    }

    // Criar

    @Test
    @DisplayName("Deve criar usuário com sucesso e senha encodada")
    public void testCriarComSucesso() {
        when(usuarioRepository.existsByEmail("novo@specradar.com"))
                .thenReturn(false);
        when(passwordEncoder.encode("Novo@2026"))
                .thenReturn("$2a$12$hashNovo");
        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> {
                    Usuario u = invocation.getArgument(0);
                    u.setId(3L);
                    u.setCriadoEm(LocalDateTime.now());
                    return u;
                });

        UsuarioResponse resultado = usuarioService.criar(requestPadrao);

        assertNotNull(resultado);
        assertEquals("novo@specradar.com", resultado.getEmail());
        assertEquals(RoleUsuario.ANALISTA, resultado.getRole());
        assertTrue(resultado.getAtivo());
        verify(passwordEncoder, times(1)).encode("Novo@2026");
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException ao criar com email duplicado")
    public void testCriarEmailDuplicado() {
        when(usuarioRepository.existsByEmail("novo@specradar.com"))
                .thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> usuarioService.criar(requestPadrao));

        assertTrue(ex.getMessage().contains("Email já cadastrado"));
        verify(usuarioRepository, never()).save(any());
    }

    // Atualizar

    @Test
    @DisplayName("Deve atualizar usuário com sucesso")
    public void testAtualizarComSucesso() {
        when(usuarioRepository.findById(2L))
                .thenReturn(Optional.of(analistaPadrao));
        when(usuarioRepository.existsByEmail("novo@specradar.com"))
                .thenReturn(false);
        when(passwordEncoder.encode(anyString()))
                .thenReturn("$2a$12$hashAtualizado");
        when(usuarioRepository.save(any(Usuario.class)))
                .thenReturn(analistaPadrao);

        UsuarioResponse resultado = usuarioService.atualizar(2L, requestPadrao);

        assertNotNull(resultado);
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
        verify(passwordEncoder, times(1)).encode(anyString());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException ao atualizar com email de outro usuário")
    public void testAtualizarEmailDeOutroUsuario() {
        when(usuarioRepository.findById(2L))
                .thenReturn(Optional.of(analistaPadrao));
        when(usuarioRepository.existsByEmail("novo@specradar.com"))
                .thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> usuarioService.atualizar(2L, requestPadrao));

        assertTrue(ex.getMessage().contains("Email já cadastrado"));
        verify(usuarioRepository, never()).save(any());
    }

    // Desativar

    @Test
    @DisplayName("Deve desativar usuário com sucesso")
    public void testDesativarComSucesso() {
        when(usuarioRepository.findById(2L))
                .thenReturn(Optional.of(analistaPadrao));
        when(usuarioRepository.save(any(Usuario.class)))
                .thenReturn(analistaPadrao);

        usuarioService.desativar(2L);

        assertFalse(analistaPadrao.getAtivo());
        verify(usuarioRepository, times(1)).save(analistaPadrao);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao desativar ID inexistente")
    public void testDesativarIdInexistente() {
        when(usuarioRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> usuarioService.desativar(99L));

        verify(usuarioRepository, never()).save(any());
    }

    // Reativar

    @Test
    @DisplayName("Deve reativar usuário desativado com sucesso")
    public void testReativarComSucesso() {
        analistaPadrao.setAtivo(false);

        when(usuarioRepository.findById(2L))
                .thenReturn(Optional.of(analistaPadrao));
        when(usuarioRepository.save(any(Usuario.class)))
                .thenReturn(analistaPadrao);

        UsuarioResponse resultado = usuarioService.reativar(2L);

        assertNotNull(resultado);
        assertTrue(analistaPadrao.getAtivo());
        verify(usuarioRepository, times(1)).save(analistaPadrao);
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException ao reativar usuário já ativo")
    public void testReativarJaAtivo() {
        when(usuarioRepository.findById(2L))
                .thenReturn(Optional.of(analistaPadrao));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> usuarioService.reativar(2L));

        assertTrue(ex.getMessage().contains("já está ativo"));
        verify(usuarioRepository, never()).save(any());
    }

    // Anonimizar

    @Test
    @DisplayName("Deve anonimizar usuário desativado com sucesso")
    public void testAnonimizarComSucesso() {
        when(usuarioRepository.findById(3L))
                .thenReturn(Optional.of(usuarioDesativado));
        when(passwordEncoder.encode(anyString()))
                .thenReturn("$2a$12$hashAleatorio");
        when(usuarioRepository.save(any(Usuario.class)))
                .thenReturn(usuarioDesativado);

        usuarioService.anonimizar(3L);

        assertEquals("Usuário Removido", usuarioDesativado.getNome());
        assertEquals("anonimizado_3@specradar.com", usuarioDesativado.getEmail());
        verify(passwordEncoder, times(1)).encode(anyString());
        verify(usuarioRepository, times(1)).save(usuarioDesativado);
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException ao anonimizar usuário ainda ativo")
    public void testAnonimizarUsuarioAtivo() {
        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(adminPadrao));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> usuarioService.anonimizar(1L));

        assertTrue(ex.getMessage().contains("desativados"));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao anonimizar ID inexistente")
    public void testAnonimizarIdInexistente() {
        when(usuarioRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> usuarioService.anonimizar(99L));

        verify(usuarioRepository, never()).save(any());
    }
}