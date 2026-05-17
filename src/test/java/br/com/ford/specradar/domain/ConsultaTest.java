package br.com.ford.specradar.domain;

import br.com.ford.specradar.domain.enums.MarcaVeiculo;
import br.com.ford.specradar.domain.enums.RoleUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes - Consulta (Domain)")
public class ConsultaTest {

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
                .especificacoes(new ArrayList<>())
                .consultas(new ArrayList<>())
                .build();

        amarok = Veiculo.builder()
                .id(2L)
                .marca(MarcaVeiculo.VOLKSWAGEN)
                .modelo("Amarok")
                .versao("V6 Extreme")
                .ano(2025)
                .ativo(true)
                .criadoEm(LocalDateTime.now())
                .especificacoes(new ArrayList<>())
                .consultas(new ArrayList<>())
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

    // Construtor e campos

    @Test
    @DisplayName("Deve criar consulta com construtor vazio")
    public void testConstrutorVazio() {
        Consulta c = new Consulta();
        assertNotNull(c);
    }

    @Test
    @DisplayName("Deve criar consulta com valores corretos via builder")
    public void testCriacaoViaBuilder() {
        assertEquals(1L, consultaAdmin.getId());
        assertNotNull(consultaAdmin.getUsuario());
        assertNotNull(consultaAdmin.getVeiculo());
        assertNotNull(consultaAdmin.getRealizadaEm());
    }

    // Relacionamento com Usuario

    @Test
    @DisplayName("Deve manter referencia correta ao usuario")
    public void testReferenciaUsuario() {
        assertEquals(1L, consultaAdmin.getUsuario().getId());
        assertEquals("admin@specradar.com", consultaAdmin.getUsuario().getEmail());
        assertEquals(RoleUsuario.ADMIN, consultaAdmin.getUsuario().getRole());
    }

    // Relacionamento com Veiculo

    @Test
    @DisplayName("Deve manter referencia correta ao veiculo")
    public void testReferenciaVeiculo() {
        assertEquals(1L, consultaAdmin.getVeiculo().getId());
        assertEquals(MarcaVeiculo.TOYOTA, consultaAdmin.getVeiculo().getMarca());
        assertEquals("Hilux", consultaAdmin.getVeiculo().getModelo());
    }

    // Isolamento entre usuários

    @Test
    @DisplayName("Deve registrar consultas de usuarios diferentes corretamente")
    public void testIsolamentoEntreUsuarios() {
        assertNotEquals(
                consultaAdmin.getUsuario().getEmail(),
                consultaAnalista.getUsuario().getEmail()
        );
        assertNotEquals(
                consultaAdmin.getVeiculo().getMarca(),
                consultaAnalista.getVeiculo().getMarca()
        );
    }

    @Test
    @DisplayName("Admin consultou Hilux e Analista consultou Amarok")
    public void testConsultasDistintas() {
        assertEquals(MarcaVeiculo.TOYOTA, consultaAdmin.getVeiculo().getMarca());
        assertEquals(MarcaVeiculo.VOLKSWAGEN, consultaAnalista.getVeiculo().getMarca());
        assertEquals("Hilux", consultaAdmin.getVeiculo().getModelo());
        assertEquals("Amarok", consultaAnalista.getVeiculo().getModelo());
    }

    // realizadaEm

    @Test
    @DisplayName("Deve ter realizadaEm preenchido")
    public void testRealizadaEmPreenchido() {
        assertNotNull(consultaAdmin.getRealizadaEm());
        assertTrue(consultaAdmin.getRealizadaEm()
                .isBefore(LocalDateTime.now().plusSeconds(1)));
    }
}