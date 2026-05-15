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

		System.out.println("\n\n\n========================================================");
		System.out.println("			SpecRadar API — Ford FIAP 2026				");
		System.out.println("		  Inteligência Competitiva Automotiva			");
		System.out.println("========================================================");
		System.out.println("Perfil ativo:  										 " + profile);
		System.out.println("Banco:         							 Oracle FIAP / H2");
		System.out.println("--------------------------------------------------------");
		System.out.println("					LINKS ÚTEIS				    		");
		System.out.println();
		System.out.println("Swagger UI:		   http://localhost:" + port + "/swagger-ui.html");
		System.out.println("API Docs:			   http://localhost:" + port + "/v3/api-docs");
		System.out.println("H2 Console: 			http://localhost:" + port + "/h2-console");
		System.out.println("						(Somente no profile DEV)		");
		System.out.println("--------------------------------------------------------");
		System.out.println("				CREDENCIAIS DE TESTE			   		");
		System.out.println();
		System.out.println("Login (ANALISTA): analista@specradar.com / Analista@2026");
		System.out.println("Login (ADMIN):    	  admin@specradar.com   / Admin@2026");
		System.out.println("========================================================\n\n\n");
	}
}
