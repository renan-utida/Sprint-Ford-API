package br.com.ford.specradar.service;

import br.com.ford.specradar.domain.Consulta;
import br.com.ford.specradar.domain.Usuario;
import br.com.ford.specradar.domain.Veiculo;
import br.com.ford.specradar.domain.enums.MarcaVeiculo;
import br.com.ford.specradar.domain.enums.RoleUsuario;
import br.com.ford.specradar.dto.response.ConsultaResponse;
import br.com.ford.specradar.exception.ResourceNotFoundException;
import br.com.ford.specradar.repository.ConsultaRepository;
import br.com.ford.specradar.repository.UsuarioRepository;
import br.com.ford.specradar.repository.VeiculoRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes — ConsultaService")
public class ConsultaServiceTest {

    @Mock
    private ConsultaRepository consultaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private VeiculoRepository veiculoRepository;

    @InjectMocks
    private ConsultaService consultaService;

    private Usuario admin;
    private Usuario analista;
    private Veiculo hilux;
    private Veiculo amarok;
    private Consulta consultaAdmin;
    private Consulta consultaAnalista;

    @BeforeEach
    public void setUp() {
        admin = Usuario.builder()
                .id(1L)
                .nome("Administrador SpecRadar")
                .email("admin@specradar.com")
                .senha("$2a$12$hash")
                .role(RoleUsuario.ADMIN)
                .ativo(true)
                .criadoEm(LocalDateTime.now())
                .build();

        analista = Usuario.builder()
                .id(2L)
                .nome("Analista Ford")
                .email("analista@specradar.com")
                .senha("$2a$12$hash")
                .role(RoleUsuario.ANALISTA)
                .ativo(true)
                .criadoEm(LocalDateTime.now())
                .build();

        hilux = Veiculo.builder()
                .id(1L)
                .marca(MarcaVeiculo.TOYOTA)
                .modelo("Hilux")
                .versao("GR-Sport")
                .ano(2025)
                .ativo(true)
                .criadoEm(LocalDateTime.now())
                .build();

        amarok = Veiculo.builder()
                .id(2L)
                .marca(MarcaVeiculo.VOLKSWAGEN)
                .modelo("Amarok")
                .versao("V6 Extreme")
                .ano(2025)
                .ativo(true)
                .criadoEm(LocalDateTime.now())
                .build();

        consultaAdmin = Consulta.builder()
                .id(1L)
                .usuario(admin)
                .veiculo(hilux)
                .realizadaEm(LocalDateTime.now())
                .build();

        consultaAnalista = Consulta.builder()
                .id(2L)
                .usuario(analista)
                .veiculo(amarok)
                .realizadaEm(LocalDateTime.now())
                .build();
    }

    // Listar todas

    @Test
    @DisplayName("Deve listar todas as consultas para ADMIN")
    public void testListarTodas() {
        when(consultaRepository.findAll())
                .thenReturn(List.of(consultaAdmin, consultaAnalista));

        List<ConsultaResponse> resultado = consultaService.listarTodas();

        assertEquals(2, resultado.size());
        assertEquals("admin@specradar.com", resultado.get(0).getEmailUsuario());
        assertEquals("analista@specradar.com", resultado.get(1).getEmailUsuario());
        verify(consultaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há consultas")
    public void testListarTodasVazia() {
        when(consultaRepository.findAll()).thenReturn(List.of());

        List<ConsultaResponse> resultado = consultaService.listarTodas();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    // Listar por usuário

    @Test
    @DisplayName("Deve listar apenas as consultas do analista")
    public void testListarPorUsuario() {
        when(consultaRepository.findByUsuarioId(2L))
                .thenReturn(List.of(consultaAnalista));

        List<ConsultaResponse> resultado = consultaService.listarPorUsuario(2L);

        assertEquals(1, resultado.size());
        assertEquals("analista@specradar.com", resultado.get(0).getEmailUsuario());
        assertEquals(MarcaVeiculo.VOLKSWAGEN, resultado.get(0).getMarcaVeiculo());
        verify(consultaRepository, times(1)).findByUsuarioId(2L);
    }

    @Test
    @DisplayName("Deve retornar lista vazia para usuário sem consultas")
    public void testListarPorUsuarioSemConsultas() {
        when(consultaRepository.findByUsuarioId(99L))
                .thenReturn(List.of());

        List<ConsultaResponse> resultado = consultaService.listarPorUsuario(99L);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    // Registrar

    @Test
    @DisplayName("Deve registrar consulta com sucesso")
    public void testRegistrarComSucesso() {
        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(admin));
        when(veiculoRepository.findById(1L))
                .thenReturn(Optional.of(hilux));
        when(consultaRepository.save(any(Consulta.class)))
                .thenAnswer(invocation -> {
                    Consulta c = invocation.getArgument(0);
                    c.setId(3L);
                    c.setRealizadaEm(LocalDateTime.now());
                    return c;
                });

        ConsultaResponse resultado = consultaService.registrar(1L, 1L);

        assertNotNull(resultado);
        assertEquals("admin@specradar.com", resultado.getEmailUsuario());
        assertEquals(MarcaVeiculo.TOYOTA, resultado.getMarcaVeiculo());
        assertEquals("Hilux", resultado.getModeloVeiculo());
        verify(consultaRepository, times(1)).save(any(Consulta.class));
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao registrar com usuário inexistente")
    public void testRegistrarUsuarioInexistente() {
        when(usuarioRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> consultaService.registrar(99L, 1L));

        verify(consultaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao registrar com veículo inexistente")
    public void testRegistrarVeiculoInexistente() {
        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(admin));
        when(veiculoRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> consultaService.registrar(1L, 99L));

        verify(consultaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve verificar isolamento — admin e analista têm consultas separadas")
    public void testIsolamentoConsultas() {
        when(consultaRepository.findByUsuarioId(1L))
                .thenReturn(List.of(consultaAdmin));
        when(consultaRepository.findByUsuarioId(2L))
                .thenReturn(List.of(consultaAnalista));

        List<ConsultaResponse> consultasAdmin    = consultaService.listarPorUsuario(1L);
        List<ConsultaResponse> consultasAnalista = consultaService.listarPorUsuario(2L);

        assertEquals(1, consultasAdmin.size());
        assertEquals(1, consultasAnalista.size());
        assertNotEquals(
                consultasAdmin.get(0).getEmailUsuario(),
                consultasAnalista.get(0).getEmailUsuario()
        );
    }
}