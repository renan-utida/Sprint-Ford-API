package br.com.ford.specradar.config;

import br.com.ford.specradar.security.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import java.time.LocalDateTime;
import java.util.Map;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Desabilita CSRF — API stateless não precisa
                .csrf(AbstractHttpConfigurer::disable)

                // Sem sessão — cada requisição é autenticada pelo JWT
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )

                // Regras de acesso
                .authorizeHttpRequests(auth -> auth

                        // Login publico
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()

                        // Register só ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/auth/register").hasRole("ADMIN")

                        // Swagger e H2 publicos
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/h2-console/**"
                        ).permitAll()

                        // Veículos — GET para ANALISTA e ADMIN, resto só ADMIN
                        .requestMatchers(HttpMethod.GET, "/api/veiculos/**").hasAnyRole("ANALISTA", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/veiculos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/veiculos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/veiculos/**").hasRole("ADMIN")

                        // Especificações — mesma lógica dos veículos
                        .requestMatchers(HttpMethod.GET, "/api/especificacoes/**").hasAnyRole("ANALISTA", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/especificacoes/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/especificacoes/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/especificacoes/**").hasRole("ADMIN")

                        // Consultas — ANALISTA e ADMIN
                        .requestMatchers("/api/consultas/**").hasAnyRole("ANALISTA", "ADMIN")

                        // Usuários — só ADMIN
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")

                        // Qualquer outra rota exige autenticação
                        .anyRequest().authenticated()
                )

                // Registra o JwtFilter antes do filtro padrão do Spring
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

                // Configura o provider de autenticação
                .authenticationProvider(authenticationProvider());

        return http.build();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            Map<String, Object> body = Map.of(
                    "status", 401,
                    "erro", "Não autenticado",
                    "mensagem", "É necessário realizar o login para acessar este recurso",
                    "path", request.getRequestURI(),
                    "timestamp", LocalDateTime.now().toString()
            );
            new ObjectMapper().writeValue(response.getOutputStream(), body);
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(403);
            response.setContentType("application/json;charset=UTF-8");
            Map<String, Object> body = Map.of(
                    "status", 403,
                    "erro", "Acesso negado",
                    "mensagem", "Você não tem permissão para acessar este recurso",
                    "path", request.getRequestURI(),
                    "timestamp", LocalDateTime.now().toString()
            );
            new ObjectMapper().writeValue(response.getOutputStream(), body);
        };
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}