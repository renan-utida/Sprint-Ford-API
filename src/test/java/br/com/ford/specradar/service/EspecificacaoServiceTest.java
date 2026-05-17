package br.com.ford.specradar.service;

import br.com.ford.specradar.domain.Especificacao;
import br.com.ford.specradar.domain.Veiculo;
import br.com.ford.specradar.domain.enums.MarcaVeiculo;
import br.com.ford.specradar.dto.request.EspecificacaoRequest;
import br.com.ford.specradar.dto.response.EspecificacaoResponse;
import br.com.ford.specradar.exception.ResourceNotFoundException;
import br.com.ford.specradar.repository.EspecificacaoRepository;
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
@DisplayName("Testes - EspecificacaoService")
public class EspecificacaoServiceTest {

    @Mock
    private EspecificacaoRepository especificacaoRepository;

    @Mock
    private VeiculoRepository veiculoRepository;

    @InjectMocks
    private EspecificacaoService especificacaoService;

    private Veiculo hilux;
    private Veiculo amarok;
    private Especificacao specMotor;
    private Especificacao specPotencia;
    private EspecificacaoRequest requestPadrao;

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

        specMotor = Especificacao.builder()
                .id(1L)
                .veiculo(hilux)
                .atributo("Motor")
                .valor("2.8 Turbo Diesel")
                .unidade(null)
                .disponivel(true)
                .criadoEm(LocalDateTime.now())
                .build();

        specPotencia = Especificacao.builder()
                .id(2L)
                .veiculo(hilux)
                .atributo("Potencia")
                .valor("204")
                .unidade("cv")
                .disponivel(true)
                .criadoEm(LocalDateTime.now())
                .build();

        requestPadrao = new EspecificacaoRequest();
        requestPadrao.setAtributo("Tracao");
        requestPadrao.setValor("4WD");
        requestPadrao.setUnidade(null);
        requestPadrao.setDisponivel(true);
    }

    // Listar por veículo

    @Test
    @DisplayName("Deve listar especificações de veículo existente")
    public void testListarPorVeiculoExistente() {
        when(veiculoRepository.existsById(1L)).thenReturn(true);
        when(especificacaoRepository.findByVeiculoId(1L))
                .thenReturn(List.of(specMotor, specPotencia));

        List<EspecificacaoResponse> resultado = especificacaoService.listarPorVeiculo(1L);

        assertEquals(2, resultado.size());
        assertEquals("Motor", resultado.get(0).getAtributo());
        assertEquals("Potencia", resultado.get(1).getAtributo());
        verify(veiculoRepository, times(1)).existsById(1L);
        verify(especificacaoRepository, times(1)).findByVeiculoId(1L);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao listar specs de veículo inexistente")
    public void testListarPorVeiculoInexistente() {
        when(veiculoRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> especificacaoService.listarPorVeiculo(99L));

        verify(especificacaoRepository, never()).findByVeiculoId(any());
    }

    // Buscar por ID

    @Test
    @DisplayName("Deve buscar especificação do veículo correto com sucesso")
    public void testBuscarPorIdVeiculoCorreto() {
        when(especificacaoRepository.findById(1L))
                .thenReturn(Optional.of(specMotor));

        EspecificacaoResponse resultado = especificacaoService.buscarPorId(1L, 1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Motor", resultado.getAtributo());
        assertEquals(MarcaVeiculo.TOYOTA, resultado.getVeiculo().getMarca());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao buscar spec com veículo errado")
    public void testBuscarPorIdVeiculoErrado() {
        when(especificacaoRepository.findById(1L))
                .thenReturn(Optional.of(specMotor));

        // specMotor pertence ao veículo 1 (hilux), mas passamos veículo 2 (amarok)
        assertThrows(ResourceNotFoundException.class,
                () -> especificacaoService.buscarPorId(2L, 1L));
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao buscar spec inexistente")
    public void testBuscarPorIdInexistente() {
        when(especificacaoRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> especificacaoService.buscarPorId(1L, 99L));
    }

    // Criar

    @Test
    @DisplayName("Deve criar especificação em veículo existente com sucesso")
    public void testCriarComSucesso() {
        when(veiculoRepository.findById(1L))
                .thenReturn(Optional.of(hilux));
        when(especificacaoRepository.save(any(Especificacao.class)))
                .thenAnswer(invocation -> {
                    Especificacao e = invocation.getArgument(0);
                    e.setId(3L);
                    e.setCriadoEm(LocalDateTime.now());
                    return e;
                });

        EspecificacaoResponse resultado = especificacaoService.criar(1L, requestPadrao);

        assertNotNull(resultado);
        assertEquals("Tracao", resultado.getAtributo());
        assertEquals("4WD", resultado.getValor());
        assertTrue(resultado.getDisponivel());
        assertEquals(MarcaVeiculo.TOYOTA, resultado.getVeiculo().getMarca());
        verify(especificacaoRepository, times(1)).save(any(Especificacao.class));
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao criar spec em veículo inexistente")
    public void testCriarVeiculoInexistente() {
        when(veiculoRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> especificacaoService.criar(99L, requestPadrao));

        verify(especificacaoRepository, never()).save(any());
    }

    // Atualizar

    @Test
    @DisplayName("Deve atualizar especificação com sucesso")
    public void testAtualizarComSucesso() {
        when(especificacaoRepository.findById(1L))
                .thenReturn(Optional.of(specMotor));
        when(especificacaoRepository.save(any(Especificacao.class)))
                .thenReturn(specMotor);

        EspecificacaoResponse resultado = especificacaoService.atualizar(1L, 1L, requestPadrao);

        assertNotNull(resultado);
        assertEquals("Tracao", specMotor.getAtributo());
        assertEquals("4WD", specMotor.getValor());
        verify(especificacaoRepository, times(1)).save(specMotor);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao atualizar spec com veículo errado")
    public void testAtualizarVeiculoErrado() {
        when(especificacaoRepository.findById(1L))
                .thenReturn(Optional.of(specMotor));

        // specMotor pertence ao veículo 1, mas passamos veículo 2
        assertThrows(ResourceNotFoundException.class,
                () -> especificacaoService.atualizar(2L, 1L, requestPadrao));

        verify(especificacaoRepository, never()).save(any());
    }

    // Deletar

    @Test
    @DisplayName("Deve deletar especificação com sucesso")
    public void testDeletarComSucesso() {
        when(especificacaoRepository.findById(1L))
                .thenReturn(Optional.of(specMotor));

        especificacaoService.deletar(1L, 1L);

        verify(especificacaoRepository, times(1)).delete(specMotor);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao deletar spec com veículo errado")
    public void testDeletarVeiculoErrado() {
        when(especificacaoRepository.findById(1L))
                .thenReturn(Optional.of(specMotor));

        // specMotor pertence ao veículo 1, mas passamos veículo 2
        assertThrows(ResourceNotFoundException.class,
                () -> especificacaoService.deletar(2L, 1L));

        verify(especificacaoRepository, never()).delete(any());
    }
}