package br.com.ford.specradar.service;

import br.com.ford.specradar.domain.Veiculo;
import br.com.ford.specradar.domain.enums.MarcaVeiculo;
import br.com.ford.specradar.dto.request.VeiculoRequest;
import br.com.ford.specradar.dto.response.VeiculoResponse;
import br.com.ford.specradar.exception.ResourceNotFoundException;
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
@DisplayName("Testes - VeiculoService")
public class VeiculoServiceTest {

    @Mock
    private VeiculoRepository veiculoRepository;

    @InjectMocks
    private VeiculoService veiculoService;

    private Veiculo hilux;
    private Veiculo amarok;
    private Veiculo veiculoInativo;
    private VeiculoRequest requestPadrao;

    @BeforeEach
    public void setUp() {
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

        veiculoInativo = Veiculo.builder()
                .id(3L)
                .marca(MarcaVeiculo.JEEP)
                .modelo("Compass")
                .versao("Limited")
                .ano(2024)
                .ativo(false)
                .criadoEm(LocalDateTime.now())
                .build();

        requestPadrao = new VeiculoRequest();
        requestPadrao.setMarca(MarcaVeiculo.JEEP);
        requestPadrao.setModelo("Compass");
        requestPadrao.setVersao("Limited");
        requestPadrao.setAno(2025);
    }

    // Listar

    @Test
    @DisplayName("Deve listar apenas veículos ativos")
    public void testListarAtivos() {
        when(veiculoRepository.findByAtivoTrue())
                .thenReturn(List.of(hilux, amarok));

        List<VeiculoResponse> resultado = veiculoService.listarAtivos();

        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(VeiculoResponse::getAtivo));
        verify(veiculoRepository, times(1)).findByAtivoTrue();
    }

    @Test
    @DisplayName("Deve listar todos os veículos incluindo inativos")
    public void testListarTodos() {
        when(veiculoRepository.findAll())
                .thenReturn(List.of(hilux, amarok, veiculoInativo));

        List<VeiculoResponse> resultado = veiculoService.listarTodos();

        assertEquals(3, resultado.size());
        verify(veiculoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve listar veículos por marca corretamente")
    public void testListarPorMarca() {
        when(veiculoRepository.findByMarcaAndAtivoTrue(MarcaVeiculo.TOYOTA))
                .thenReturn(List.of(hilux));

        List<VeiculoResponse> resultado = veiculoService.listarPorMarca(MarcaVeiculo.TOYOTA);

        assertEquals(1, resultado.size());
        assertEquals(MarcaVeiculo.TOYOTA, resultado.get(0).getMarca());
        verify(veiculoRepository, times(1)).findByMarcaAndAtivoTrue(MarcaVeiculo.TOYOTA);
    }

    // Buscar por ID

    @Test
    @DisplayName("Deve buscar veículo por ID existente com sucesso")
    public void testBuscarPorIdExistente() {
        when(veiculoRepository.findById(1L))
                .thenReturn(Optional.of(hilux));

        VeiculoResponse resultado = veiculoService.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(MarcaVeiculo.TOYOTA, resultado.getMarca());
        assertEquals("Hilux", resultado.getModelo());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao buscar ID inexistente")
    public void testBuscarPorIdInexistente() {
        when(veiculoRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> veiculoService.buscarPorId(99L));
    }

    // Criar

    @Test
    @DisplayName("Deve criar veículo com sucesso")
    public void testCriarComSucesso() {
        when(veiculoRepository.save(any(Veiculo.class)))
                .thenAnswer(invocation -> {
                    Veiculo v = invocation.getArgument(0);
                    v.setId(4L);
                    v.setCriadoEm(LocalDateTime.now());
                    return v;
                });

        VeiculoResponse resultado = veiculoService.criar(requestPadrao);

        assertNotNull(resultado);
        assertEquals(MarcaVeiculo.JEEP, resultado.getMarca());
        assertEquals("Compass", resultado.getModelo());
        assertTrue(resultado.getAtivo());
        verify(veiculoRepository, times(1)).save(any(Veiculo.class));
    }

    // Atualizar

    @Test
    @DisplayName("Deve atualizar veículo com sucesso")
    public void testAtualizarComSucesso() {
        when(veiculoRepository.findById(1L))
                .thenReturn(Optional.of(hilux));
        when(veiculoRepository.save(any(Veiculo.class)))
                .thenReturn(hilux);

        VeiculoResponse resultado = veiculoService.atualizar(1L, requestPadrao);

        assertNotNull(resultado);
        verify(veiculoRepository, times(1)).findById(1L);
        verify(veiculoRepository, times(1)).save(any(Veiculo.class));
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao atualizar ID inexistente")
    public void testAtualizarIdInexistente() {
        when(veiculoRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> veiculoService.atualizar(99L, requestPadrao));

        verify(veiculoRepository, never()).save(any());
    }

    // Desativar

    @Test
    @DisplayName("Deve desativar veículo com sucesso")
    public void testDesativarComSucesso() {
        when(veiculoRepository.findById(1L))
                .thenReturn(Optional.of(hilux));
        when(veiculoRepository.save(any(Veiculo.class)))
                .thenReturn(hilux);

        veiculoService.desativar(1L);

        assertFalse(hilux.getAtivo());
        verify(veiculoRepository, times(1)).save(hilux);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao desativar ID inexistente")
    public void testDesativarIdInexistente() {
        when(veiculoRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> veiculoService.desativar(99L));

        verify(veiculoRepository, never()).save(any());
    }

    // Reativar

    @Test
    @DisplayName("Deve reativar veículo desativado com sucesso")
    public void testReativarComSucesso() {
        when(veiculoRepository.findById(3L))
                .thenReturn(Optional.of(veiculoInativo));
        when(veiculoRepository.save(any(Veiculo.class)))
                .thenReturn(veiculoInativo);

        VeiculoResponse resultado = veiculoService.reativar(3L);

        assertNotNull(resultado);
        assertTrue(veiculoInativo.getAtivo());
        verify(veiculoRepository, times(1)).save(veiculoInativo);
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException ao reativar veículo já ativo")
    public void testReativarJaAtivo() {
        when(veiculoRepository.findById(1L))
                .thenReturn(Optional.of(hilux));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> veiculoService.reativar(1L));

        assertTrue(ex.getMessage().contains("já está ativo"));
        verify(veiculoRepository, never()).save(any());
    }
}