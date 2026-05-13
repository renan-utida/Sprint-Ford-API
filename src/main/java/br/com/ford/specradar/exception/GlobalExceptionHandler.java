package br.com.ford.specradar.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404 — Recurso não encontrado
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErroResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErroResponse.of(
                        HttpStatus.NOT_FOUND.value(),
                        "Recurso não encontrado",
                        ex.getMessage(),
                        request.getRequestURI()
                ));
    }

    // 400 — Validação de campos
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> handleValidacao(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> campos = new HashMap<>();

        ex.getBindingResult()
                .getAllErrors()
                .forEach(error -> {
                    String campo = ((FieldError) error).getField();
                    String mensagem = error.getDefaultMessage();
                    campos.put(campo, mensagem);
                });

        ErroResponse erro = ErroResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Erro de validação",
                "Um ou mais campos são inválidos",
                request.getRequestURI()
        );
        erro.setCampos(campos);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(erro);
    }

    // 403 — Acesso negado
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErroResponse> handleAccessDenied(
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ErroResponse.of(
                        HttpStatus.FORBIDDEN.value(),
                        "Acesso negado",
                        "Você não tem permissão para acessar este recurso",
                        request.getRequestURI()
                ));
    }

    // 401 — Não autenticado
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErroResponse> handleAuthentication(
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ErroResponse.of(
                        HttpStatus.UNAUTHORIZED.value(),
                        "Não autenticado",
                        "Credenciais inválidas ou token ausente",
                        request.getRequestURI()
                ));
    }

    // 500 — Erro genérico
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> handleGenerico(
            Exception ex,
            HttpServletRequest request
    ) {
        // Loga internamente sem expor para o cliente
        System.err.println("[ERRO INTERNO] " + ex.getClass().getName() + ": " + ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErroResponse.of(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Erro interno do servidor",
                        "Ocorreu um erro inesperado. Tente novamente mais tarde.",
                        request.getRequestURI()
                ));
    }

    // Classe interna do response de erro
    @Data
    @Builder
    public static class ErroResponse {

        private int status;
        private String erro;
        private String mensagem;
        private String path;
        private LocalDateTime timestamp;
        private Map<String, String> campos;

        public static ErroResponse of(int status, String erro, String mensagem, String path) {
            return ErroResponse.builder()
                    .status(status)
                    .erro(erro)
                    .mensagem(mensagem)
                    .path(path)
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }
}