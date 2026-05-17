package br.com.ford.specradar.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Testes - GlobalExceptionHandler")
public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    public void setUp() {
        handler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/veiculos");
    }

    // 404 ResourceNotFoundException

    @Test
    @DisplayName("Deve retornar 404 para ResourceNotFoundException com mensagem")
    public void testHandleResourceNotFound() {
        ResourceNotFoundException ex =
                new ResourceNotFoundException("Veiculo", 99L);

        ResponseEntity<GlobalExceptionHandler.ErroResponse> response =
                handler.handleResourceNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        var body = response.getBody();
        assertNotNull(body);
        assertEquals(404, body.getStatus());
        assertEquals("Recurso não encontrado", body.getErro());
        assertTrue(body.getMensagem().contains("99"));
        assertEquals("/api/veiculos", body.getPath());
        assertNotNull(body.getTimestamp());
    }

    @Test
    @DisplayName("Deve retornar 404 para ResourceNotFoundException com mensagem customizada")
    public void testHandleResourceNotFoundMensagemCustom() {
        ResourceNotFoundException ex =
                new ResourceNotFoundException("Recurso não encontrado com critério especial");

        ResponseEntity<GlobalExceptionHandler.ErroResponse> response =
                handler.handleResourceNotFound(ex, request);

        var body = response.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Recurso não encontrado com critério especial", body.getMensagem());
    }

    // 400 MethodArgumentNotValidException

    @Test
    @DisplayName("Deve retornar 400 com lista de campos inválidos")
    public void testHandleValidacao() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError(
                "veiculoRequest", "modelo", "Modelo é obrigatório"
        );
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException ex =
                mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<GlobalExceptionHandler.ErroResponse> response =
                handler.handleValidacao(ex, request);

        var body = response.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, body.getStatus());
        assertEquals("Erro de validação", body.getErro());
        assertEquals("Um ou mais campos são inválidos", body.getMensagem());
        assertNotNull(body.getCampos());
        assertTrue(body.getCampos().containsKey("modelo"));
        assertEquals("Modelo é obrigatório", body.getCampos().get("modelo"));
    }

    // 400 IllegalArgumentException

    @Test
    @DisplayName("Deve retornar 400 para IllegalArgumentException com mensagem")
    public void testHandleIllegalArgument() {
        IllegalArgumentException ex =
                new IllegalArgumentException("Email já cadastrado: admin@specradar.com");

        ResponseEntity<GlobalExceptionHandler.ErroResponse> response =
                handler.handleIllegalArgument(ex, request);

        var body = response.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, body.getStatus());
        assertEquals("Requisição inválida", body.getErro());
        assertEquals("Email já cadastrado: admin@specradar.com", body.getMensagem());
    }

    @Test
    @DisplayName("Deve retornar 400 para veiculo já ativo")
    public void testHandleVeiculoJaAtivo() {
        IllegalArgumentException ex =
                new IllegalArgumentException("Veículo já está ativo");

        ResponseEntity<GlobalExceptionHandler.ErroResponse> response =
                handler.handleIllegalArgument(ex, request);

        var body = response.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(body.getMensagem().contains("já está ativo"));
    }

    // 403 AccessDeniedException

    @Test
    @DisplayName("Deve retornar 403 para AccessDeniedException")
    public void testHandleAccessDenied() {
        ResponseEntity<GlobalExceptionHandler.ErroResponse> response =
                handler.handleAccessDenied(request);

        var body = response.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(403, body.getStatus());
        assertEquals("Acesso negado", body.getErro());
        assertEquals("Você não tem permissão para acessar este recurso", body.getMensagem());
        assertEquals("/api/veiculos", body.getPath());
    }

    // 401 AuthenticationException

    @Test
    @DisplayName("Deve retornar 401 para AuthenticationException")
    public void testHandleAuthentication() {
        ResponseEntity<GlobalExceptionHandler.ErroResponse> response =
                handler.handleAuthentication(request);

        var body = response.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(401, body.getStatus());
        assertEquals("Não autenticado", body.getErro());
        assertEquals("Credenciais inválidas ou token ausente", body.getMensagem());
    }

    // 500 Exception genérica

    @Test
    @DisplayName("Deve retornar 500 sem expor detalhes internos")
    public void testHandleGenerico() {
        Exception ex = new RuntimeException("Erro interno detalhado - não deve aparecer");

        ResponseEntity<GlobalExceptionHandler.ErroResponse> response =
                handler.handleGenerico(ex, request);

        var body = response.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, body.getStatus());
        assertEquals("Erro interno do servidor", body.getErro());
        assertFalse(body.getMensagem().contains("Erro interno detalhado"));
        assertEquals(
                "Ocorreu um erro inesperado. Tente novamente mais tarde.",
                body.getMensagem()
        );
    }

    // ErroResponse

    @Test
    @DisplayName("Deve preencher timestamp automaticamente no ErroResponse")
    public void testErroResponseTimestamp() {
        ResourceNotFoundException ex =
                new ResourceNotFoundException("Usuario", 1L);

        ResponseEntity<GlobalExceptionHandler.ErroResponse> response =
                handler.handleResourceNotFound(ex, request);

        var body = response.getBody();
        assertNotNull(body);
        assertNotNull(body.getTimestamp());
    }

    @Test
    @DisplayName("Deve preencher path corretamente no ErroResponse")
    public void testErroResponsePath() {
        request.setRequestURI("/api/usuarios/99");

        ResourceNotFoundException ex =
                new ResourceNotFoundException("Usuario", 99L);

        ResponseEntity<GlobalExceptionHandler.ErroResponse> response =
                handler.handleResourceNotFound(ex, request);

        var body = response.getBody();
        assertNotNull(body);
        assertEquals("/api/usuarios/99", body.getPath());
    }
}