package br.com.ford.specradar.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Pega o header Authorization
        String authHeader = request.getHeader("Authorization");

        // 2. Se não tem Bearer token, deixa passar
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extrai o token (remove "Bearer ")
        String token = authHeader.substring(7);

        try {
            // 4. Extrai o email do token
            String email = jwtService.extrairEmail(token);

            // 5. Se tem email e não há autenticação ativa no contexto
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 6. Carrega o usuário do banco
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // 7. Valida o token
                if (jwtService.isTokenValido(token, userDetails)) {

                    // 8. Cria o objeto de autenticação e injeta no contexto
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

        } catch (Exception e) {
            log.warn("[SEGURANÇA] Token JWT inválido na requisição {} — IP: {} — motivo: {}",
                    request.getRequestURI(),
                    request.getRemoteAddr(),
                    e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}