package br.com.ford.specradar.domain;

import br.com.ford.specradar.domain.enums.MarcaVeiculo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes - Especificacao (Domain)")
public class EspecificacaoTest {

    private Veiculo hilux;
    private Especificacao specMotor;
    private Especificacao specIndisponivel;

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
                .especificacoes(new ArrayList<>())
                .consultas(new ArrayList<>())
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

        specIndisponivel = Especificacao.builder()
                .id(2L)
                .veiculo(hilux)
                .atributo("Turbo Compressor")
                .valor("Não disponível")
                .unidade(null)
                .disponivel(false)
                .criadoEm(LocalDateTime.now())
                .build();
    }

    // Construtor e campos

    @Test
    @DisplayName("Deve criar especificacao com construtor vazio")
    public void testConstrutorVazio() {
        Especificacao e = new Especificacao();
        assertNotNull(e);
    }

    @Test
    @DisplayName("Deve criar especificacao com valores corretos via builder")
    public void testCriacaoViaBuilder() {
        assertEquals(1L, specMotor.getId());
        assertEquals("Motor", specMotor.getAtributo());
        assertEquals("2.8 Turbo Diesel", specMotor.getValor());
        assertNull(specMotor.getUnidade());
        assertTrue(specMotor.getDisponivel());
        assertNotNull(specMotor.getCriadoEm());
    }

    // Relacionamento com Veiculo

    @Test
    @DisplayName("Deve manter referencia correta ao veiculo")
    public void testReferenciaVeiculo() {
        assertNotNull(specMotor.getVeiculo());
        assertEquals(1L, specMotor.getVeiculo().getId());
        assertEquals(MarcaVeiculo.TOYOTA, specMotor.getVeiculo().getMarca());
        assertEquals("Hilux", specMotor.getVeiculo().getModelo());
    }

    // Campo disponivel

    @Test
    @DisplayName("Deve refletir disponivel true para spec disponivel")
    public void testDisponivelTrue() {
        assertTrue(specMotor.getDisponivel());
    }

    @Test
    @DisplayName("Deve refletir disponivel false para spec indisponivel")
    public void testDisponivelFalse() {
        assertFalse(specIndisponivel.getDisponivel());
    }

    @Test
    @DisplayName("Deve permitir alteracao do campo disponivel")
    public void testAlteracaoDisponivel() {
        specMotor.setDisponivel(false);
        assertFalse(specMotor.getDisponivel());

        specMotor.setDisponivel(true);
        assertTrue(specMotor.getDisponivel());
    }

    // Unidade opcional

    @Test
    @DisplayName("Deve aceitar unidade nula")
    public void testUnidadeNula() {
        assertNull(specMotor.getUnidade());
    }

    @Test
    @DisplayName("Deve aceitar unidade preenchida")
    public void testUnidadePreenchida() {
        Especificacao specComUnidade = Especificacao.builder()
                .id(3L)
                .veiculo(hilux)
                .atributo("Potencia")
                .valor("204")
                .unidade("cv")
                .disponivel(true)
                .criadoEm(LocalDateTime.now())
                .build();

        assertEquals("cv", specComUnidade.getUnidade());
    }

    // criadoEm

    @Test
    @DisplayName("Deve ter criadoEm preenchido")
    public void testCriadoEmPreenchido() {
        assertNotNull(specMotor.getCriadoEm());
        assertTrue(specMotor.getCriadoEm().isBefore(LocalDateTime.now().plusSeconds(1)));
    }
}