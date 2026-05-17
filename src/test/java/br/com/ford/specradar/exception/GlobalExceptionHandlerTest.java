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

@DisplayName("Testes — GlobalExceptionHandler")
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
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Recurso não encontrado", response.getBody().getErro());
        assertTrue(response.getBody().getMensagem().contains("99"));
        assertEquals("/api/veiculos", response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    @DisplayName("Deve retornar 404 para ResourceNotFoundException com mensagem customizada")
    public void testHandleResourceNotFoundMensagemCustom() {
        ResourceNotFoundException ex =
                new ResourceNotFoundException("Recurso não encontrado com critério especial");

        ResponseEntity<GlobalExceptionHandler.ErroResponse> response =
                handler.handleResourceNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Recurso não encontrado com critério especial",
                response.getBody().getMensagem());
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

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Erro de validação", response.getBody().getErro());
        assertEquals("Um ou mais campos são inválidos",
                response.getBody().getMensagem());
        assertNotNull(response.getBody().getCampos());
        assertTrue(response.getBody().getCampos().containsKey("modelo"));
        assertEquals("Modelo é obrigatório",
                response.getBody().getCampos().get("modelo"));
    }

    // 400 IllegalArgumentException

    @Test
    @DisplayName("Deve retornar 400 para IllegalArgumentException com mensagem")
    public void testHandleIllegalArgument() {
        IllegalArgumentException ex =
                new IllegalArgumentException("Email já cadastrado: admin@specradar.com");

        ResponseEntity<GlobalExceptionHandler.ErroResponse> response =
                handler.handleIllegalArgument(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Requisição inválida", response.getBody().getErro());
        assertEquals("Email já cadastrado: admin@specradar.com",
                response.getBody().getMensagem());
    }

    @Test
    @DisplayName("Deve retornar 400 para veiculo já ativo")
    public void testHandleVeiculoJaAtivo() {
        IllegalArgumentException ex =
                new IllegalArgumentException("Veículo já está ativo");

        ResponseEntity<GlobalExceptionHandler.ErroResponse> response =
                handler.handleIllegalArgument(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().getMensagem().contains("já está ativo"));
    }

    // 403 AccessDeniedException

    @Test
    @DisplayName("Deve retornar 403 para AccessDeniedException")
    public void testHandleAccessDenied() {
        ResponseEntity<GlobalExceptionHandler.ErroResponse> response =
                handler.handleAccessDenied(request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(403, response.getBody().getStatus());
        assertEquals("Acesso negado", response.getBody().getErro());
        assertEquals("Você não tem permissão para acessar este recurso",
                response.getBody().getMensagem());
        assertEquals("/api/veiculos", response.getBody().getPath());
    }

    // 401 AuthenticationException

    @Test
    @DisplayName("Deve retornar 401 para AuthenticationException")
    public void testHandleAuthentication() {
        ResponseEntity<GlobalExceptionHandler.ErroResponse> response =
                handler.handleAuthentication(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(401, response.getBody().getStatus());
        assertEquals("Não autenticado", response.getBody().getErro());
        assertEquals("Credenciais inválidas ou token ausente",
                response.getBody().getMensagem());
    }

    // 500 Exception genérica

    @Test
    @DisplayName("Deve retornar 500 para Exception generica sem expor stack trace")
    public void testHandleGenerico() {
        Exception ex = new RuntimeException("Erro interno detalhado - não deve aparecer");

        ResponseEntity<GlobalExceptionHandler.ErroResponse> response =
                handler.handleGenerico(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Erro interno do servidor", response.getBody().getErro());

        // Garante que o detalhe interno NÃO aparece na resposta
        assertFalse(response.getBody().getMensagem()
                .contains("Erro interno detalhado"));
        assertEquals("Ocorreu um erro inesperado. Tente novamente mais tarde.",
                response.getBody().getMensagem());
    }

    // ErroResponse

    @Test
    @DisplayName("Deve preencher timestamp automaticamente no ErroResponse")
    public void testErroResponseTimestamp() {
        ResourceNotFoundException ex =
                new ResourceNotFoundException("Usuario", 1L);

        ResponseEntity<GlobalExceptionHandler.ErroResponse> response =
                handler.handleResourceNotFound(ex, request);

        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    @DisplayName("Deve preencher path corretamente no ErroResponse")
    public void testErroResponsePath() {
        request.setRequestURI("/api/usuarios/99");

        ResourceNotFoundException ex =
                new ResourceNotFoundException("Usuario", 99L);

        ResponseEntity<GlobalExceptionHandler.ErroResponse> response =
                handler.handleResourceNotFound(ex, request);

        assertEquals("/api/usuarios/99", response.getBody().getPath());
    }
}