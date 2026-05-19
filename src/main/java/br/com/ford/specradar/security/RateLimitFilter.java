package br.com.ford.specradar.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Filtro de rate limiting por IP usando Bucket4j.
 *
 * Limites aplicados a TODOS os endpoints da aplicação:
 * - 20 requisições por minuto por IP (janela deslizante)
 * - IPs que excedem o limite recebem 429 Too Many Requests
 * - Tentativas bloqueadas são logadas com IP para monitoramento
 *
 * Aplicado globalmente para prevenir brute force, scraping e DoS
 * em qualquer endpoint, incluindo login e Swagger.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    // Mapa de buckets por IP - cada IP tem seu próprio contador
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    // Limite: 20 requisições por minuto por IP
    private static final int MAX_REQUESTS_PER_MINUTE = 20;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String ip = extrairIp(request);
        Bucket bucket = buckets.computeIfAbsent(ip, this::criarNovoBucket);

        // Tenta consumir 1 token - se houver saldo, deixa passar
        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Sem saldo - bloqueia e loga
        log.warn("[SEGURANÇA] Rate limit excedido - IP: {} endpoint: {} método: {}",
                ip, request.getRequestURI(), request.getMethod());

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> body = Map.of(
                "status", 429,
                "erro", "Muitas requisições",
                "mensagem", "Limite de requisições excedido. Tente novamente em 1 minuto.",
                "path", request.getRequestURI(),
                "timestamp", LocalDateTime.now().toString()
        );

        new ObjectMapper().writeValue(response.getOutputStream(), body);
    }

    /**
     * Cria um novo bucket para o IP com limite de 20 req/min.
     * Usa janela deslizante (greedy refill) — tokens são repostos
     * gradualmente ao longo do minuto, não todos de uma vez.
     * API atual do Bucket4j 8.x - Bandwidth.builder().
     */
    private Bucket criarNovoBucket(String ip) {
        Bandwidth limite = Bandwidth.builder()
                .capacity(MAX_REQUESTS_PER_MINUTE)
                .refillGreedy(MAX_REQUESTS_PER_MINUTE, Duration.ofMinutes(1))
                .build();

        return Bucket.builder()
                .addLimit(limite)
                .build();
    }

    /**
     * Extrai o IP real do cliente, considerando proxies reversos.
     * X-Forwarded-For é o header padrão de proxies (nginx, AWS ALB, etc.)
     */
    private String extrairIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            // X-Forwarded-For pode ter múltiplos IPs: "ip1, ip2, ip3"
            // O primeiro é sempre o IP real do cliente
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}