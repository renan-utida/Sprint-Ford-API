package br.com.ford.specradar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class SpecradarApplication {

	public static void main(String[] args) {

		ConfigurableApplicationContext context =
				SpringApplication.run(SpecradarApplication.class, args);

		Environment env = context.getEnvironment();

		String port    = env.getProperty("server.port", "8080");
		String profile = env.getProperty("spring.profiles.active", "dev");
		boolean isDev  = "dev".equalsIgnoreCase(profile);
		boolean sslOn  = Boolean.parseBoolean(
				env.getProperty("server.ssl.enabled", "false")
		);

		String protocolo = sslOn ? "https" : "http";

		System.out.println("\n\n========================================================");
		System.out.println("			SpecRadar API — Ford FIAP 2026				");
		System.out.println("		  Inteligência Competitiva Automotiva			");
		System.out.println("========================================================");
		System.out.println("  Perfil ativo : " + profile.toUpperCase());
		System.out.println("  Protocolo    : " + protocolo.toUpperCase());

		if (isDev) {
			System.out.println("  Banco        : H2 (em memória)");
		} else {
			System.out.println("  Banco        : Oracle FIAP");
			System.out.println("  Usuário      : " + env.getProperty("spring.datasource.username"));
		}

		System.out.println("--------------------------------------------------------");
		System.out.println("  LINKS ÚTEIS\n");
		System.out.println("  Swagger UI : " + protocolo + "://localhost:" + port + "/swagger-ui.html");
		System.out.println("  API Docs   : " + protocolo + "://localhost:" + port + "/v3/api-docs");

		if (isDev) {
			System.out.println("  H2 Console : http://localhost:" + port + "/h2-console");
			System.out.println("               JDBC URL: jdbc:h2:mem:specradar");
			System.out.println("               User: Ford | Password: Fiap2026");
		} else {
			System.out.println("\n  AVISO SSL: Certificado self-signed — o navegador exibirá");
			System.out.println("  aviso de segurança. Clique em 'Avançar assim mesmo'.");
		}

		System.out.println("--------------------------------------------------------");
		System.out.println("  CREDENCIAIS DE TESTE\n");
		System.out.println("  ADMIN    : admin@specradar.com    / Admin@2026");
		System.out.println("  ANALISTA : analista@specradar.com / Analista@2026");
		System.out.println("========================================================\n\n");
	}
}
