package br.com.ford.specradar.domain;

import br.com.ford.specradar.domain.enums.MarcaVeiculo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes - Veiculo (Domain)")
public class VeiculoTest {

    private Veiculo hilux;
    private Veiculo veiculoInativo;

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

        veiculoInativo = Veiculo.builder()
                .id(2L)
                .marca(MarcaVeiculo.JEEP)
                .modelo("Compass")
                .versao("Limited")
                .ano(2024)
                .ativo(false)
                .criadoEm(LocalDateTime.now())
                .especificacoes(new ArrayList<>())
                .consultas(new ArrayList<>())
                .build();
    }

    // Construtor e campos

    @Test
    @DisplayName("Deve criar veiculo com construtor vazio")
    public void testConstrutorVazio() {
        Veiculo v = new Veiculo();
        assertNotNull(v);
    }

    @Test
    @DisplayName("Deve criar veiculo com valores corretos via builder")
    public void testCriacaoViaBuilder() {
        assertEquals(1L, hilux.getId());
        assertEquals(MarcaVeiculo.TOYOTA, hilux.getMarca());
        assertEquals("Hilux", hilux.getModelo());
        assertEquals("GR-Sport", hilux.getVersao());
        assertEquals(2025, hilux.getAno());
        assertTrue(hilux.getAtivo());
        assertNotNull(hilux.getCriadoEm());
    }

    // Estado ativo/inativo

    @Test
    @DisplayName("Deve refletir estado ativo corretamente")
    public void testEstadoAtivo() {
        assertTrue(hilux.getAtivo());
        assertFalse(veiculoInativo.getAtivo());
    }

    @Test
    @DisplayName("Deve permitir alteração do estado ativo")
    public void testAlteracaoEstadoAtivo() {
        hilux.setAtivo(false);
        assertFalse(hilux.getAtivo());

        hilux.setAtivo(true);
        assertTrue(hilux.getAtivo());
    }

    // Marca

    @Test
    @DisplayName("Deve aceitar todas as marcas do enum MarcaVeiculo")
    public void testTodasAsMarcas() {
        for (MarcaVeiculo marca : MarcaVeiculo.values()) {
            Veiculo v = Veiculo.builder()
                    .id(99L)
                    .marca(marca)
                    .modelo("Modelo Teste")
                    .versao("Versao Teste")
                    .ano(2025)
                    .ativo(true)
                    .criadoEm(LocalDateTime.now())
                    .build();
            assertEquals(marca, v.getMarca());
        }
    }

    // ── Relacionamentos

    @Test
    @DisplayName("Deve inicializar listas de especificacoes e consultas vazias")
    public void testListasVazias() {
        assertNotNull(hilux.getEspecificacoes());
        assertNotNull(hilux.getConsultas());
        assertTrue(hilux.getEspecificacoes().isEmpty());
        assertTrue(hilux.getConsultas().isEmpty());
    }

    // criadoEm

    @Test
    @DisplayName("Deve ter criadoEm preenchido")
    public void testCriadoEmPreenchido() {
        assertNotNull(hilux.getCriadoEm());
        assertTrue(hilux.getCriadoEm().isBefore(LocalDateTime.now().plusSeconds(1)));
    }
}