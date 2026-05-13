package br.com.ford.specradar.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SpecRadar API")
                        .description("""
                                API de Inteligência Competitiva Automotiva — Ford FIAP 2026.
                                
                                Permite gerenciar veículos concorrentes e suas especificações técnicas,
                                com autenticação JWT e controle de acesso por perfil (ADMIN / ANALISTA).
                                
                                Para testar os endpoints protegidos:
                                1. Use POST /api/auth/login com suas credenciais
                                2. Copie o token retornado
                                3. Clique em Authorize e cole o token
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Grupo SpecRadar — FIAP 3ESPW")
                                .email("admin@specradar.com")
                        )
                )
                // Adiciona o esquema Bearer JWT globalmente
                .addSecurityItem(new SecurityRequirement()
                        .addList(SECURITY_SCHEME_NAME)
                )
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Insira o token JWT obtido no endpoint /api/auth/login")
                        )
                );
    }
}