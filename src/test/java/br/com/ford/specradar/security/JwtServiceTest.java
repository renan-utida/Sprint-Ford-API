package br.com.ford.specradar.security;

import br.com.ford.specradar.domain.Usuario;
import br.com.ford.specradar.domain.enums.RoleUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes - JwtService")
public class JwtServiceTest {

    private JwtService jwtService;

    private Usuario admin;
    private Usuario analista;

    // Chave com pelo menos 64 bytes para HS256
    private static final String SECRET =
            "5A7234753778214125442A472D4B6150645367566B59703373367639792F423F" +
                    "5A7234753778214125442A472D4B6150645367566B59703373367639792F423F";

    private static final long EXPIRATION_MS = 28800000L; // 8 horas

    @BeforeEach
    public void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "expiration", EXPIRATION_MS);

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
    }

    // Gerar token

    @Test
    @DisplayName("Deve gerar token JWT não nulo para usuario admin")
    public void testGerarTokenAdmin() {
        String token = jwtService.gerarToken(admin);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        // Token JWT tem 3 partes separadas por ponto
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    @DisplayName("Deve gerar token JWT não nulo para usuario analista")
    public void testGerarTokenAnalista() {
        String token = jwtService.gerarToken(analista);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    @DisplayName("Deve gerar tokens diferentes para usuarios diferentes")
    public void testTokensDiferentesParaUsuariosDiferentes() {
        String tokenAdmin    = jwtService.gerarToken(admin);
        String tokenAnalista = jwtService.gerarToken(analista);

        assertNotEquals(tokenAdmin, tokenAnalista);
    }

    // Extrair email

    @Test
    @DisplayName("Deve extrair email correto do token do admin")
    public void testExtrairEmailAdmin() {
        String token = jwtService.gerarToken(admin);
        String email = jwtService.extrairEmail(token);

        assertEquals("admin@specradar.com", email);
    }

    @Test
    @DisplayName("Deve extrair email correto do token do analista")
    public void testExtrairEmailAnalista() {
        String token = jwtService.gerarToken(analista);
        String email = jwtService.extrairEmail(token);

        assertEquals("analista@specradar.com", email);
    }

    // Extrair role

    @Test
    @DisplayName("Deve extrair role ADMIN do token")
    public void testExtrairRoleAdmin() {
        String token = jwtService.gerarToken(admin);
        String role  = jwtService.extrairRole(token);

        assertEquals("ADMIN", role);
    }

    @Test
    @DisplayName("Deve extrair role ANALISTA do token")
    public void testExtrairRoleAnalista() {
        String token = jwtService.gerarToken(analista);
        String role  = jwtService.extrairRole(token);

        assertEquals("ANALISTA", role);
    }

    // Extrair expiração

    @Test
    @DisplayName("Deve extrair data de expiracao futura")
    public void testExtrairExpiracao() {
        String token     = jwtService.gerarToken(admin);
        Date   expiracao = jwtService.extrairExpiracao(token);

        assertNotNull(expiracao);
        assertTrue(expiracao.after(new Date()));
    }

    @Test
    @DisplayName("Deve ter expiracao de aproximadamente 8 horas")
    public void testExpiracaoOitoHoras() {
        String token     = jwtService.gerarToken(admin);
        Date   expiracao = jwtService.extrairExpiracao(token);
        Date   agora     = new Date();

        long diferencaMs = expiracao.getTime() - agora.getTime();

        // Verifica que a expiração está entre 7h59 e 8h01 (margem de 1 minuto)
        assertTrue(diferencaMs > 28740000L); // > 7h59min
        assertTrue(diferencaMs < 28860000L); // < 8h01min
    }

    // Validar token

    @Test
    @DisplayName("Deve validar token correto como valido")
    public void testIsTokenValidoCorreto() {
        String token   = jwtService.gerarToken(admin);
        boolean valido = jwtService.isTokenValido(token, admin);

        assertTrue(valido);
    }

    @Test
    @DisplayName("Deve invalidar token de outro usuario")
    public void testIsTokenValidoOutroUsuario() {
        // Token gerado para admin mas validado com analista
        String  token  = jwtService.gerarToken(admin);
        boolean valido = jwtService.isTokenValido(token, analista);

        assertFalse(valido);
    }

    @Test
    @DisplayName("Deve lançar excecao ao validar token malformado")
    public void testTokenMalformado() {
        assertThrows(Exception.class,
                () -> jwtService.extrairEmail("tokeninvalido123"));
    }

    @Test
    @DisplayName("Deve lançar excecao ao validar token com assinatura incorreta")
    public void testTokenAssinaturaIncorreta() {
        // Token gerado com secret diferente — assinatura não bate
        String tokenFalso = "eyJhbGciOiJIUzI1NiJ9" +
                ".eyJzdWIiOiJhZG1pbkBzcGVjcmFkYXIuY29tIn0" +
                ".assinatura_invalida_aqui";

        assertThrows(Exception.class,
                () -> jwtService.extrairEmail(tokenFalso));
    }
}