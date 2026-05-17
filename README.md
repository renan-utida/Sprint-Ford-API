# SpecRadar API

**Inteligência Competitiva Automotiva - Ford FIAP 2026**

API REST para gerenciamento de veículos concorrentes e suas especificações técnicas, desenvolvida como parte da Sprint 1 da parceria Ford × FIAP. Permite que analistas da Ford consultem e comparem especificações de veículos concorrentes de forma padronizada, com autenticação JWT e controle de acesso por perfil.

---

## Sumário

- [Contexto](#contexto)
- [Stack Tecnológica](#stack-tecnológica)
- [Arquitetura](#arquitetura)
- [Decisões Técnicas](#decisões-técnicas)
- [Estrutura de Pacotes](#estrutura-de-pacotes)
- [Como Rodar](#como-rodar)
- [Configuração do Projeto](#configuração-do-projeto)
- [Banco de Dados](#banco-de-dados)
- [Endpoints](#endpoints)
- [Autenticação](#autenticação)
- [Perfis e Permissões](#perfis-e-permissões)
- [Segurança](#segurança)
- [Credenciais de Teste](#credenciais-de-teste)
- [Documentação Swagger](#documentação-swagger)
- [Validação dos Endpoints](#validação-dos-endpoints)
- [Testes](#testes)

---

## Contexto

O Desafio 01 da Ford propõe automatizar a coleta de especificações técnicas de veículos concorrentes - processo que hoje consome aproximadamente **1 hora por versão** de forma manual, com alta probabilidade de imprecisão.

O SpecRadar resolve esse problema oferecendo uma API estruturada onde analistas podem cadastrar, consultar e comparar especificações de veículos concorrentes em formato padronizado, com histórico de consultas e controle de acesso por perfil.

---

## Stack Tecnológica

| Tecnologia | Versão | Uso |
|---|---|---|
| Java | 21 | Linguagem principal |
| Spring Boot | 3.5.14 | Framework base |
| Spring Security | 6.x | Autenticação e autorização |
| Spring Data JPA | 3.x | Persistência |
| Flyway | 11.8.2 | Migrações de banco |
| JWT (jjwt) | 0.12.6 | Tokens de autenticação |
| Springdoc OpenAPI | 2.8.9 | Documentação Swagger |
| Bucket4j | 8.10.1 | Rate limiting |
| Lombok | 1.18.38 | Redução de boilerplate |
| H2 | - | Banco em memória (dev) |
| Oracle 19c | - | Banco relacional (prod) |

---

## Arquitetura

O projeto segue **Arquitetura em Camadas (Layered Architecture)**:

```
┌──────────────────────────────────────────────────┐
│               Presentation Layer                 │
│                                                  │
│  AuthController      VeiculoController           │
│  EspecificacaoController                         │
│  ConsultaController  UsuarioController           │
│                                                  │
│  Recebe as requisições HTTP e delega             │
│  para a camada de negócio                        │
├──────────────────────────────────────────────────┤
│                Business Layer                    │
│                                                  │
│  UsuarioService      VeiculoService              │
│  EspecificacaoService                            │
│  ConsultaService                                 │
│                                                  │
│  Contém todas as regras de negócio               │
│  e orquestra as operações                        │
├──────────────────────────────────────────────────┤
│                  Data Layer                      │
│                                                  │
│  UsuarioRepository   VeiculoRepository           │
│  EspecificacaoRepository                         │
│  ConsultaRepository                              │
│                                                  │
│  Abstrai o acesso ao banco de dados              │
│  via Spring Data JPA                             │
├──────────────────────────────────────────────────┤
│                 Domain Layer                     │
│                                                  │
│  Usuario    Veiculo    Especificacao             │
│  Consulta   MarcaVeiculo(E)   RoleUsuario(E)     │
│                                                  │
│  Entidades JPA e enums que representam           │
│  o modelo de negócio da solução                  │
└──────────────────────────────────────────────────┘
(E) = Enum
```
Camadas transversais que suportam todas as outras:

| Camada | Responsabilidade |
|---|---|
| `config/` | Configuração de segurança, Swagger e CORS |
| `security/` | Filtro JWT, geração e validação de tokens |
| `exception/` | Tratamento centralizado de erros - nunca expõe stack trace |
| `dto/` | Objetos de entrada e saída da API - separa domínio da apresentação |

---

## Decisões Técnicas

### Arquitetura em Camadas em vez de Hexagonal ou Microsserviços

A Arquitetura em Camadas foi escolhida por ser o padrão ensinado em sala e por atender plenamente aos critérios de SOA da Sprint - separação clara entre apresentação, serviço e dados. Hexagonal adicionaria complexidade de ports e adapters desnecessária para o escopo. Microsserviços estaria completamente fora de escopo para uma entrega com prazo curto.

### JWT em vez de Session

A API é stateless por design - cada requisição carrega sua própria autenticação no header `Authorization: Bearer`. Isso elimina a necessidade de armazenar estado de sessão no servidor, facilita o consumo por clientes mobile (React Native) e é o padrão para APIs REST modernas. O token carrega o email e o role do usuário, permitindo que o app mobile leia as permissões sem requisição extra.

### Enum para Marca e Role

Usar `MarcaVeiculo` e `RoleUsuario` como enums resolve automaticamente dois problemas: normalização de entrada (`"toyota"`, `"Toyota"` e `"TOYOTA"` são tratados como o mesmo valor com `accept-case-insensitive-enums=true`) e validação implícita (valores fora do enum retornam 400 sem nenhum código extra). O professor de Cybersecurity apontou isso explicitamente como boa prática para o Desafio 1.

### Soft Delete em vez de Hard Delete

Veículos e usuários nunca são deletados fisicamente - apenas marcados com `ativo = false`. Isso preserva o histórico de consultas: se um veículo fosse deletado do banco, todas as consultas vinculadas a ele perderiam a referência por causa das Foreign Keys. O soft delete garante integridade referencial e auditoria completa.

### Flyway para Migrações

O controle de schema via Flyway garante que qualquer desenvolvedor que clonar o projeto terá o banco idêntico ao de produção após o primeiro startup - sem necessidade de scripts manuais. Cada migration é versionada e imutável, tornando o histórico do schema rastreável pelo Git.

### spring-dotenv para Variáveis de Ambiente

Em vez de configurar variáveis de ambiente manualmente no sistema operacional ou no IntelliJ, o `spring-dotenv` lê o arquivo `.env` automaticamente na inicialização. Isso simplifica o onboarding de novos desenvolvedores - basta copiar o `.env.example`, preencher as credenciais e rodar o projeto.

---

## Estrutura de Pacotes

```
br.com.ford.specradar
├── config/
│   ├── SecurityConfig.java
│   ├── SwaggerConfig.java
│   └── CorsConfig.java
├── controller/
│   ├── AuthController.java
│   ├── VeiculoController.java
│   ├── EspecificacaoController.java
│   ├── ConsultaController.java
│   └── UsuarioController.java
├── domain/
│   ├── enums/
│   │   ├── MarcaVeiculo.java
│   │   └── RoleUsuario.java
│   ├── Usuario.java
│   ├── Veiculo.java
│   ├── Especificacao.java
│   └── Consulta.java
├── dto/
│   ├── request/
│   │   ├── LoginRequest.java
│   │   ├── UsuarioRequest.java
│   │   ├── VeiculoRequest.java
│   │   ├── EspecificacaoRequest.java
│   │   └── ConsultaRequest.java
│   └── response/
│       ├── TokenResponse.java
│       ├── UsuarioResponse.java
│       ├── VeiculoResponse.java
│       ├── EspecificacaoResponse.java
│       └── ConsultaResponse.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   └── ResourceNotFoundException.java
├── repository/
│   ├── UsuarioRepository.java
│   ├── VeiculoRepository.java
│   ├── EspecificacaoRepository.java
│   └── ConsultaRepository.java
├── security/
│   ├── JwtService.java
│   ├── JwtFilter.java
│   └── UserDetailsServiceImpl.java
├── service/
│   ├── UsuarioService.java
│   ├── VeiculoService.java
│   ├── EspecificacaoService.java
│   └── ConsultaService.java
└── SpecradarApplication.java
```

---

## Como Rodar

### Pré-requisitos

- Java 21+
- Maven 3.8+
- IntelliJ IDEA (recomendado)

### Perfil dev (H2 em memória)

1. Clone o repositório:
```bash
git clone https://github.com/renan-utida/Sprint-Ford-API.git
cd Sprint-Ford-API
```

2. Crie o arquivo `.env` a partir do exemplo:
```bash
cp .env.example .env
```

3. Preencha o `.env` com as variáveis necessárias (veja a seção abaixo).

4. O projeto usa **spring-dotenv** - o arquivo `.env` é lido automaticamente pelo Spring na inicialização. Não é necessário configurar variáveis de ambiente manualmente no IntelliJ ou no sistema operacional.

   Para trocar de perfil, basta editar o `.env`:
```env
   # Dev (H2 em memória)
   SPRING_PROFILE=dev

   # Prod (Oracle FIAP)
   SPRING_PROFILE=prod
```

5. Rode a aplicação - o perfil `dev` é o padrão:
```bash
mvn spring-boot:run
```

6. Acesse o Swagger:
```
http://localhost:8080/swagger-ui.html
```

### Perfil prod (Oracle FIAP)

1. No `.env`, muda o perfil:
```env
SPRING_PROFILE=prod
```

2. Preencha as credenciais Oracle no `.env`.

3. Rode a aplicação - o Flyway cria as tabelas automaticamente no Oracle na primeira execução.

---

## Configuração do Projeto

### Variáveis de Ambiente

O projeto usa **spring-dotenv** — o arquivo `.env` é lido automaticamente pelo Spring na inicialização. Não é necessário configurar variáveis de ambiente manualmente no IntelliJ ou no sistema operacional.

**1. Copie o arquivo de exemplo (`.env.example`) para `.env`:**
```bash
cp .env.example .env
```

**2. Preencha o `.env` com suas credenciais:**

```env
# Projeto SpecRadar — Variáveis de Ambiente
# Copie este arquivo para .env e preencha os valores

# Perfil ativo: dev ou prod
SPRING_PROFILE=dev

# Oracle (necessário em prod)
ORACLE_URL=jdbc:oracle:thin:@oracle.fiap.com.br:1521:orcl
ORACLE_USER=seu_rm_aqui
ORACLE_PASSWORD=sua_senha_aqui

# JWT — gere com: openssl rand -base64 64
JWT_SECRET=
JWT_EXPIRATION=28800000

# Servidor
SERVER_PORT=8080

# CORS — separar múltiplas origens por vírgula
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:8081,exp://localhost:8081
```

**3. Gere o JWT_SECRET:**

O `JWT_SECRET` precisa ter no mínimo 256 bits (32 bytes) para o algoritmo HS256. Gere uma chave segura com o comando:

```bash
openssl rand -base64 64
```

Cole o resultado no campo `JWT_SECRET` do `.env`:
```env
JWT_SECRET=sua_chave_gerada_aqui
```

> O arquivo `.env` está no `.gitignore` e **nunca deve ser commitado**. Apenas o `.env.example` vai para o repositório.

---

### application.properties

Configurações globais compartilhadas entre todos os perfis:

```properties
# APLICACAO
spring.application.name=specradar
server.port=${SERVER_PORT:8080}

# Profile ativo (trocar para prod ao subir no Oracle)
spring.profiles.active=${SPRING_PROFILE:dev}

# JWT
# Chave secreta - mínimo 256 bits para HS256
jwt.secret=${JWT_SECRET}
jwt.expiration=${JWT_EXPIRATION:28800000}

# SWAGGER / OPENAPI
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.display-request-duration=true
springdoc.swagger-ui.operationsSorter=alpha
springdoc.swagger-ui.tagsSorter=alpha
springdoc.api-docs.path=/v3/api-docs

# FLYWAY
spring.flyway.enabled=true

# CORS
cors.allowed-origins=${CORS_ALLOWED_ORIGINS:http://localhost:3000,http://localhost:8081}

# Jackson
spring.jackson.deserialization.fail-on-unknown-properties=false
spring.jackson.mapper.accept-case-insensitive-enums=true

# JPA
spring.jpa.open-in-view=false
```

---

### application-dev.properties

Perfil de desenvolvimento com **H2 em memória** — banco zerado a cada restart:

```properties
# BANCO H2 (DEV)
spring.datasource.url=jdbc:h2:mem:specradar;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;NON_KEYWORDS=VALUE
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=Ford
spring.datasource.password=Fiap2026

# JPA
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# H2 CONSOLE — acessível em http://localhost:8080/h2-console
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# FLYWAY
spring.flyway.url=jdbc:h2:mem:specradar;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;NON_KEYWORDS=VALUE
spring.flyway.user=Ford
spring.flyway.password=Fiap2026
spring.flyway.locations=classpath:db/migration
```

---

### application-prod.properties

Perfil de produção com **Oracle FIAP** — dados persistidos entre sessões:

```properties
# BANCO ORACLE (PROD)
spring.datasource.url=${ORACLE_URL}
spring.datasource.username=${ORACLE_USER}
spring.datasource.password=${ORACLE_PASSWORD}
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

# JPA
spring.jpa.database-platform=org.hibernate.dialect.OracleDialect
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false

# FLYWAY
spring.flyway.url=${ORACLE_URL}
spring.flyway.user=${ORACLE_USER}
spring.flyway.password=${ORACLE_PASSWORD}
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=0
spring.flyway.out-of-order=true
```

---

### pom.xml — Configuração Maven

```xml
   <properties>
		<java.version>21</java.version>
		<jjwt.version>0.12.6</jjwt.version>
		<springdoc.version>2.8.9</springdoc.version>
		<bucket4j.version>8.10.1</bucket4j.version>
		<flyway.version>11.8.2</flyway.version>
		<lombok.version>1.18.38</lombok.version>
	</properties>

	<dependencies>

		<!-- WEB / REST -->
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>

		<!-- SEGURANÇA -->
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-security</artifactId>
		</dependency>

		<!-- JPA / BANCO -->
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-jpa</artifactId>
		</dependency>

		<!-- VALIDAÇÃO -->
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-validation</artifactId>
		</dependency>

		<!-- DEV TOOLS -->
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-devtools</artifactId>
			<scope>runtime</scope>
			<optional>true</optional>
		</dependency>

		<!-- BANCO DE DADOS -->
		<dependency>
			<groupId>com.oracle.database.jdbc</groupId>
			<artifactId>ojdbc11</artifactId>
			<scope>runtime</scope>
		</dependency>

		<dependency>
			<groupId>com.h2database</groupId>
			<artifactId>h2</artifactId>
			<scope>runtime</scope>
		</dependency>

		<!-- FLYWAY (core + Oracle) -->
		<dependency>
			<groupId>org.flywaydb</groupId>
			<artifactId>flyway-core</artifactId>
			<version>${flyway.version}</version>
		</dependency>
		<dependency>
			<groupId>org.flywaydb</groupId>
			<artifactId>flyway-database-oracle</artifactId>
			<version>${flyway.version}</version>
		</dependency>

		<!-- JWT -->
		<dependency>
			<groupId>io.jsonwebtoken</groupId>
			<artifactId>jjwt-api</artifactId>
			<version>${jjwt.version}</version>
		</dependency>
		<dependency>
			<groupId>io.jsonwebtoken</groupId>
			<artifactId>jjwt-impl</artifactId>
			<version>${jjwt.version}</version>
			<scope>runtime</scope>
		</dependency>
		<dependency>
			<groupId>io.jsonwebtoken</groupId>
			<artifactId>jjwt-jackson</artifactId>
			<version>${jjwt.version}</version>
			<scope>runtime</scope>
		</dependency>

		<!-- SWAGGER / OPENAPI -->
		<dependency>
			<groupId>org.springdoc</groupId>
			<artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
			<version>${springdoc.version}</version>
		</dependency>

		<!-- RATE LIMITING -->
		<dependency>
			<groupId>com.bucket4j</groupId>
			<artifactId>bucket4j-core</artifactId>
			<version>${bucket4j.version}</version>
		</dependency>

		<!-- LOMBOK -->
		<dependency>
			<groupId>org.projectlombok</groupId>
			<artifactId>lombok</artifactId>
			<optional>true</optional>
		</dependency>

		<!-- TESTES -->
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.springframework.security</groupId>
			<artifactId>spring-security-test</artifactId>
			<scope>test</scope>
		</dependency>

		<!-- DOTENV — lê o .env automaticamente -->
		<dependency>
			<groupId>me.paulschwarz</groupId>
			<artifactId>spring-dotenv</artifactId>
			<version>4.0.0</version>
		</dependency>

		<!-- Suite de Testes -->
		<dependency>
			<groupId>org.junit.platform</groupId>
			<artifactId>junit-platform-suite</artifactId>
			<scope>test</scope>
		</dependency>

	</dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
				<configuration>
					<excludes>
						<exclude>
							<groupId>org.projectlombok</groupId>
							<artifactId>lombok</artifactId>
						</exclude>
					</excludes>
				</configuration>
			</plugin>

			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-compiler-plugin</artifactId>
				<configuration>
					<source>21</source>
					<target>21</target>
					<annotationProcessorPaths>
						<path>
							<groupId>org.projectlombok</groupId>
							<artifactId>lombok</artifactId>
							<version>${lombok.version}</version>
						</path>
					</annotationProcessorPaths>
				</configuration>
			</plugin>

			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-surefire-plugin</artifactId>
				<configuration>
					<argLine>
						-XX:+EnableDynamicAgentLoading
						-Xshare:off
					</argLine>
				</configuration>
			</plugin>
		</plugins>
	</build>
```

---

## Banco de Dados

### Migrações Flyway

As tabelas são criadas e populadas automaticamente pelo Flyway na inicialização:

| Migration | Descrição |
|---|---|
| `V1__create_usuarios.sql` | Tabela `ford_usuarios` |
| `V2__create_veiculos.sql` | Tabela `ford_veiculos` |
| `V3__create_especificacoes.sql` | Tabela `ford_especificacoes` |
| `V4__create_consultas.sql` | Tabela `ford_consultas` |
| `V5__insert_dados_iniciais.sql` | Dados iniciais: 2 usuários, 3 veículos concorrentes com specs |
| `V6__insert_ranger_raptor.sql` | Ford Ranger Raptor 2025 com 14 especificações técnicas completas |

### Marcas suportadas

`FORD`, `TOYOTA`, `VOLKSWAGEN`, `CHEVROLET`, `FIAT`, `HYUNDAI`, `NISSAN`, `MITSUBISHI`, `JEEP`, `RAM`, `MERCEDES`

### Diagrama de Relacionamentos

```
ford_usuarios (1) ──── (N) ford_consultas
ford_veiculos (1) ──── (N) ford_consultas
ford_veiculos (1) ──── (N) ford_especificacoes
```

---

## Endpoints

### Autenticação

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| POST | `/api/auth/login` | Autentica e retorna JWT | Público |

### Usuários

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| GET | `/api/usuarios` | Lista todos os usuários | ADMIN |
| GET | `/api/usuarios/{id}` | Busca usuário por ID | ADMIN |
| POST | `/api/usuarios` | Cria novo usuário | ADMIN |
| PUT | `/api/usuarios/{id}` | Atualiza usuário | ADMIN |
| DELETE | `/api/usuarios/{id}` | Desativa usuário | ADMIN |
| PATCH | `/api/usuarios/{id}/reativar` | Reativa usuário desativado | ADMIN |

### Veículos

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| GET | `/api/veiculos` | Lista veículos ativos (filtro por marca opcional) | ANALISTA, ADMIN |
| GET | `/api/veiculos/todos` | Lista todos incluindo inativos | ADMIN |
| GET | `/api/veiculos/{id}` | Busca por ID e registra consulta | ANALISTA, ADMIN |
| POST | `/api/veiculos` | Cadastra veículo | ADMIN |
| PUT | `/api/veiculos/{id}` | Atualiza veículo | ADMIN |
| DELETE | `/api/veiculos/{id}` | Desativa veículo | ADMIN |
| PATCH | `/api/veiculos/{id}/reativar` | Reativa veículo desativado | ADMIN |

### Especificações

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| GET | `/api/veiculos/{id}/especificacoes` | Lista specs do veículo | ANALISTA, ADMIN |
| GET | `/api/veiculos/{id}/especificacoes/{specId}` | Busca spec específica | ANALISTA, ADMIN |
| POST | `/api/veiculos/{id}/especificacoes` | Cadastra spec | ADMIN |
| PUT | `/api/veiculos/{id}/especificacoes/{specId}` | Atualiza spec | ADMIN |
| DELETE | `/api/veiculos/{id}/especificacoes/{specId}` | Remove spec | ADMIN |

### Consultas

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| GET | `/api/consultas` | ADMIN vê todas; ANALISTA vê só as próprias | ANALISTA, ADMIN |

---

## Autenticação

A API usa **JWT (JSON Web Token)** com expiração de 8 horas.

### Como autenticar

1. Faça login em `POST /api/auth/login`:
```json
{
  "email": "admin@specradar.com",
  "senha": "Admin@2026"
}
```

2. Copie o token da resposta:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer",
  "expiraEm": "2026-05-15T21:00:00"
}
```

3. Use o token no header de todas as requisições protegidas:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### No Swagger

Clique em **Authorize** (cadeado), cole o token e clique em **Authorize**. Todos os endpoints protegidos passarão a funcionar automaticamente.

### No Insomnia

Importe a coleção pela URL:
```
http://localhost:8080/v3/api-docs
```
O Insomnia importa todos os endpoints automaticamente com autenticação Bearer configurada.

---

## Perfis e Permissões (RBAC)

| Ação | ANALISTA | ADMIN |
|---|---|---|
| Login | ✅ | ✅ |
| Listar veículos ativos | ✅ | ✅ |
| Buscar veículo por ID | ✅ | ✅ |
| Listar especificações | ✅ | ✅ |
| Ver próprias consultas | ✅ | ✅ |
| Ver todas as consultas | ❌ | ✅ |
| Listar todos os veículos (inativos) | ❌ | ✅ |
| Cadastrar / editar / desativar veículo | ❌ | ✅ |
| Cadastrar / editar / deletar especificação | ❌ | ✅ |
| Gerenciar usuários | ❌ | ✅ |

---

## Segurança

| Requisito | Implementação |
|---|---|
| Autenticação | JWT com expiração de 8 horas e assinatura HS256 |
| Autorização | RBAC com roles `ADMIN` e `ANALISTA` |
| Senhas | BCrypt com custo 12 - nunca armazenadas em texto plano |
| Validação de entrada | Bean Validation (`@NotBlank`, `@Size`, `@Email`, `@Min`, `@Max`) |
| Normalização de parâmetros | Enums para `marca` e `role` - valores inválidos retornam 400 |
| Proteção contra payloads grandes | `@Size` com limite máximo em todos os campos String |
| Erros seguros | `GlobalExceptionHandler` - nunca expõe stack trace ou tecnologia |
| CORS | Origens configuradas via variável de ambiente - nunca `*` |
| Rate limiting | Bucket4j por IP |
| HTTPS | Configurado via variável de ambiente em prod |
| Isolamento de dados | ANALISTA acessa apenas as próprias consultas |
| Auditoria | Toda consulta a veículo por ID é registrada com usuário e timestamp |

### Padrão de erros

Todos os erros retornam JSON padronizado, nunca stack trace:

```json
{
  "status": 404,
  "erro": "Recurso não encontrado",
  "mensagem": "Veiculo não encontrado com id: 99",
  "path": "/api/veiculos/99",
  "timestamp": "2026-05-15T16:00:00",
  "campos": null
}
```

Para erros de validação, o campo `campos` lista os campos inválidos:

```json
{
  "status": 400,
  "erro": "Erro de validação",
  "mensagem": "Um ou mais campos são inválidos",
  "path": "/api/veiculos",
  "timestamp": "2026-05-15T16:00:00",
  "campos": {
    "modelo": "Modelo é obrigatório",
    "ano": "Ano inválido"
  }
}
```

---

## Credenciais de Teste

| Perfil | Email | Senha |
|---|---|---|
| ADMIN | admin@specradar.com | Admin@2026 |
| ANALISTA | analista@specradar.com | Analista@2026 |

> Senhas armazenadas com BCrypt custo 12. Nunca em texto plano.

---

## Documentação Swagger

Com a aplicação rodando, acesse:

```
http://localhost:8080/swagger-ui.html
```

A documentação completa da API com exemplos de request e response está disponível diretamente no Swagger UI. Para testar endpoints protegidos, clique em **Authorize** e informe o token JWT obtido no login.

A especificação OpenAPI 3.0 em formato JSON está disponível em:

```
http://localhost:8080/v3/api-docs
```

---

## Validação dos Endpoints

Todos os endpoints foram testados via **Swagger UI** e **Insomnia** nos perfis `dev` (H2) e `prod` (Oracle).

---

### ✅ Testes de Sucesso

### Autenticação

**Login como ADMIN - `POST /api/auth/login`**

Request:
```json
{
  "email": "admin@specradar.com",
  "senha": "Admin@2026"
}
```

Response `200 OK`:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer",
  "expiraEm": "2026-05-15T21:00:00"
}
```

---

**Login como ANALISTA - `POST /api/auth/login`**

Request:
```json
{
  "email": "analista@specradar.com",
  "senha": "Analista@2026"
}
```

Response `200 OK`:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer",
  "expiraEm": "2026-05-15T22:00:00"
}
```

---

### Veículos

| Cenário | Método | Endpoint | Resultado |
|---|---|---|---|
| Listar veículos ativos | GET | `/api/veiculos` | 200 com lista |
| Filtrar por marca | GET | `/api/veiculos?marca=TOYOTA` | 200 só com Toyotas |
| Buscar por ID (registra consulta) | GET | `/api/veiculos/1` | 200 com veículo |
| Listar todos incluindo inativos | GET | `/api/veiculos/todos` | 200 com lista completa |
| Cadastrar veículo | POST | `/api/veiculos` | 201 com veículo criado |
| Atualizar veículo | PUT | `/api/veiculos/{id}` | 200 com dados atualizados |
| Desativar veículo | DELETE | `/api/veiculos/{id}` | 204 sem body |
| Reativar veículo | PATCH | `/api/veiculos/{id}/reativar` | 200 com veículo reativado |

---

**Cadastrar veículo - `POST /api/veiculos`**

Request:
```json
{
  "marca": "JEEP",
  "modelo": "Compass",
  "versao": "Limited",
  "ano": 2025
}
```

Response `201 Created`:
```json
{
  "id": 5,
  "marca": "JEEP",
  "modelo": "Compass",
  "versao": "Limited",
  "ano": 2025,
  "ativo": true,
  "criadoEm": "2026-05-15T16:09:29"
}
```

---

**Buscar veículo por ID - `GET /api/veiculos/4`**

Response `200 OK`:
```json
{
  "id": 4,
  "marca": "FORD",
  "modelo": "Ranger",
  "versao": "Raptor",
  "ano": 2025,
  "ativo": true,
  "criadoEm": "2026-05-15T15:41:56"
}
```

---

**Filtrar por marca - `GET /api/veiculos?marca=TOYOTA`**

Response `200 OK`:
```json
[
  {
    "id": 1,
    "marca": "TOYOTA",
    "modelo": "Hilux",
    "versao": "GR-Sport",
    "ano": 2025,
    "ativo": true,
    "criadoEm": "2026-05-15T13:59:13"
  }
]
```

---

**Desativar veículo - `DELETE /api/veiculos/5`**

Response `204 No Content` - sem body.

---

**Listar veículos ativos - `GET /api/veiculos`**

Response `200 OK`:
```json
[
  {
    "id": 1,
    "marca": "TOYOTA",
    "modelo": "Hilux",
    "versao": "GR-Sport",
    "ano": 2025,
    "ativo": true,
    "criadoEm": "2026-05-15T13:59:13"
  },
  {
    "id": 2,
    "marca": "VOLKSWAGEN",
    "modelo": "Amarok",
    "versao": "V6 Extreme",
    "ano": 2025,
    "ativo": true,
    "criadoEm": "2026-05-15T13:59:13"
  },
  {
    "id": 3,
    "marca": "CHEVROLET",
    "modelo": "S10",
    "versao": "High Country",
    "ano": 2025,
    "ativo": true,
    "criadoEm": "2026-05-15T13:59:13"
  },
  {
    "id": 4,
    "marca": "FORD",
    "modelo": "Ranger",
    "versao": "Raptor",
    "ano": 2025,
    "ativo": true,
    "criadoEm": "2026-05-15T15:41:56"
  }
]
```

---

**Listar todos os veículos incluindo inativos - `GET /api/veiculos/todos`**

Response `200 OK`:
```json
[
  {
    "id": 1,
    "marca": "TOYOTA",
    "modelo": "Hilux",
    "versao": "GR-Sport",
    "ano": 2025,
    "ativo": true,
    "criadoEm": "2026-05-15T13:59:13"
  },
  {
    "id": 4,
    "marca": "FORD",
    "modelo": "Ranger",
    "versao": "Raptor",
    "ano": 2025,
    "ativo": true,
    "criadoEm": "2026-05-15T15:41:56"
  },
  {
    "id": 5,
    "marca": "JEEP",
    "modelo": "Compass",
    "versao": "Limited",
    "ano": 2025,
    "ativo": false,
    "criadoEm": "2026-05-15T16:09:29"
  }
]
```

---

**Reativar veículo - `PATCH /api/veiculos/5/reativar`**

Response `200 OK`:
```json
{
  "id": 5,
  "marca": "JEEP",
  "modelo": "Compass",
  "versao": "Limited",
  "ano": 2025,
  "ativo": true,
  "criadoEm": "2026-05-15T16:09:29"
}
```

---

**Atualizar veículo - `PUT /api/veiculos/5`**

Request:
```json
{
  "marca": "JEEP",
  "modelo": "Compass",
  "versao": "Limited 4x4",
  "ano": 2025
}
```

Response `200 OK`:
```json
{
  "id": 5,
  "marca": "JEEP",
  "modelo": "Compass",
  "versao": "Limited 4x4",
  "ano": 2025,
  "ativo": true,
  "criadoEm": "2026-05-15T16:09:29"
}
```

---

### Especificações

| Cenário | Método | Endpoint | Resultado |
|---|---|---|---|
| Listar specs de um veículo | GET | `/api/veiculos/{id}/especificacoes` | 200 com lista e dados do veículo |
| Buscar spec por ID | GET | `/api/veiculos/{id}/especificacoes/{specId}` | 200 com spec |
| Cadastrar spec | POST | `/api/veiculos/{id}/especificacoes` | 201 com spec criada |
| Atualizar spec | PUT | `/api/veiculos/{id}/especificacoes/{specId}` | 200 com spec atualizada |
| Deletar spec | DELETE | `/api/veiculos/{id}/especificacoes/{specId}` | 204 sem body |

---

**Listar especificações - `GET /api/veiculos/4/especificacoes`**

Exibe todas as outras especificacoes relacionadas ao veículo de ID = 4, vou apenas deixar uma aqui exibindo

Response `200 OK`:
```json
[
  {
    "id": 9,
    "veiculo": {
      "id": 4,
      "marca": "FORD",
      "modelo": "Ranger",
      "versao": "Raptor",
      "ano": 2025
    },
    "atributo": "Motor",
    "valor": "V6 3.0L Nano bi turbo",
    "unidade": null,
    "disponivel": true,
    "criadoEm": "2026-05-15T15:41:56"
  }
   
]
```

---

**Cadastrar especificação - `POST /api/veiculos/5/especificacoes`**

Request:
```json
{
  "atributo": "Motor",
  "valor": "1.3 Turbo Flex",
  "unidade": null,
  "disponivel": true
}
```

Response `201 Created`:
```json
{
  "id": 23,
  "veiculo": {
    "id": 5,
    "marca": "JEEP",
    "modelo": "Compass",
    "versao": "Limited",
    "ano": 2025
  },
  "atributo": "Motor",
  "valor": "1.3 Turbo Flex",
  "unidade": null,
  "disponivel": true,
  "criadoEm": "2026-05-15T16:14:47"
}
```

---

**Buscar especificação por ID - `GET /api/veiculos/4/especificacoes/9`**

Response `200 OK`:
```json
{
  "id": 9,
  "veiculo": {
    "id": 4,
    "marca": "FORD",
    "modelo": "Ranger",
    "versao": "Raptor",
    "ano": 2025
  },
  "atributo": "Motor",
  "valor": "V6 3.0L Nano bi turbo",
  "unidade": null,
  "disponivel": true,
  "criadoEm": "2026-05-15T15:41:56"
}
```

---

**Atualizar especificação - `PUT /api/veiculos/1/especificacoes/2`**

Request:
```json
{
  "atributo": "Potencia",
  "valor": "210",
  "unidade": "cv",
  "disponivel": true
}
```

Response `200 OK`:
```json
{
  "id": 2,
  "veiculo": {
    "id": 1,
    "marca": "TOYOTA",
    "modelo": "Hilux",
    "versao": "GR-Sport",
    "ano": 2025
  },
  "atributo": "Potencia",
  "valor": "210",
  "unidade": "cv",
  "disponivel": true,
  "criadoEm": "2026-05-15T13:59:13"
}
```

---

**Deletar especificação - `DELETE /api/veiculos/5/especificacoes/23`**

Response `204 No Content` - sem body.

---

**Especificação marcada como não disponível - `POST /api/veiculos/1/especificacoes`**

Request:
```json
{
  "atributo": "Turbo Compressor Duplo",
  "valor": "Não disponível nesta versão",
  "unidade": null,
  "disponivel": false
}
```

Response `201 Created`:
```json
{
  "id": 24,
  "veiculo": {
    "id": 1,
    "marca": "TOYOTA",
    "modelo": "Hilux",
    "versao": "GR-Sport",
    "ano": 2025
  },
  "atributo": "Turbo Compressor Duplo",
  "valor": "Não disponível nesta versão",
  "unidade": null,
  "disponivel": false,
  "criadoEm": "2026-05-15T16:20:00"
}
```

---

### Consultas

| Cenário | Método | Endpoint | Resultado |
|---|---|---|---|
| ADMIN lista todas as consultas | GET | `/api/consultas` | 200 com todas |
| ANALISTA lista só as próprias | GET | `/api/consultas` | 200 só com as do analista |

---

**Consultas como ADMIN - `GET /api/consultas`**

Response `200 OK`:
```json
[
  {
    "id": 1,
    "nomeUsuario": "Administrador SpecRadar",
    "emailUsuario": "admin@specradar.com",
    "marcaVeiculo": "TOYOTA",
    "modeloVeiculo": "Hilux",
    "versaoVeiculo": "GR-Sport",
    "realizadaEm": "2026-05-15T16:15:56"
  },
  {
    "id": 2,
    "nomeUsuario": "Analista Ford",
    "emailUsuario": "analista@specradar.com",
    "marcaVeiculo": "FORD",
    "modeloVeiculo": "Ranger",
    "versaoVeiculo": "Raptor",
    "realizadaEm": "2026-05-15T16:18:57"
  }
]
```

---

**Consultas como ANALISTA - `GET /api/consultas`**

É possível verificar apenas as suas consultas, não é possível verificar consultas do ADMIN

Response `200 OK`:
```json
[
  {
    "id": 2,
    "nomeUsuario": "Analista Ford",
    "emailUsuario": "analista@specradar.com",
    "marcaVeiculo": "FORD",
    "modeloVeiculo": "Ranger",
    "versaoVeiculo": "Raptor",
    "realizadaEm": "2026-05-15T16:18:57"
  }
]
```

---

### Usuarios

| Cenário | Método | Endpoint | Resultado |
|---|---|---|---|
| Listar usuários | GET | `/api/usuarios` | 200 sem expor senhas |
| Buscar por ID | GET | `/api/usuarios/{id}` | 200 com usuário |
| Criar usuário | POST | `/api/usuarios` | 201 com usuário criado |
| Atualizar usuário | PUT | `/api/usuarios/{id}` | 200 com dados atualizados |
| Desativar usuário | DELETE | `/api/usuarios/{id}` | 204 sem body |
| Reativar usuário | PATCH | `/api/usuarios/{id}/reativar` | 200 com usuário reativado |

---

**Listar usuários - `GET /api/usuarios`**

Response `200 OK`:
```json
[
  {
    "id": 1,
    "nome": "Administrador SpecRadar",
    "email": "admin@specradar.com",
    "role": "ADMIN",
    "ativo": true,
    "criadoEm": "2026-05-15T13:59:13"
  },
  {
    "id": 2,
    "nome": "Analista Ford",
    "email": "analista@specradar.com",
    "role": "ANALISTA",
    "ativo": true,
    "criadoEm": "2026-05-15T13:59:13"
  }
]
```

---

**Buscar usuário por ID - `GET /api/usuarios/2`**

Response `200 OK`:
```json
{
  "id": 2,
  "nome": "Analista Ford",
  "email": "analista@specradar.com",
  "role": "ANALISTA",
  "ativo": true,
  "criadoEm": "2026-05-15T13:59:13"
}
```

---

**Atualizar usuário - `PUT /api/usuarios/2`**

Request:
```json
{
  "nome": "Analista Ford Atualizado",
  "email": "analista@specradar.com",
  "senha": "Analista@2026",
  "role": "ANALISTA"
}
```

Response `200 OK`:
```json
{
  "id": 2,
  "nome": "Analista Ford Atualizado",
  "email": "analista@specradar.com",
  "role": "ANALISTA",
  "ativo": true,
  "criadoEm": "2026-05-15T13:59:13"
}
```

---

**Criar usuário - `POST /api/usuarios`**

Request:
```json
{
  "nome": "Novo Analista",
  "email": "novo@specradar.com",
  "senha": "Novo@2026",
  "role": "ANALISTA"
}
```

Response `201 Created`:
```json
{
  "id": 3,
  "nome": "Novo Analista",
  "email": "novo@specradar.com",
  "role": "ANALISTA",
  "ativo": true,
  "criadoEm": "2026-05-14T23:55:00"
}
```

---

**Desativar usuário - `DELETE /api/usuarios/3`**

Response `204 No Content` - sem body.

---

**Reativar usuário - `PATCH /api/usuarios/3/reativar`**

Response `200 OK`:
```json
{
  "id": 3,
  "nome": "Novo Analista",
  "email": "novo@specradar.com",
  "role": "ANALISTA",
  "ativo": true,
  "criadoEm": "2026-05-14T23:55:00"
}
```

---

### ❌ Testes de Falha e Segurança

### Autenticação e Autorização

| Cenário | Resultado Esperado | Obtido |
|---|---|---|
| Login com senha errada | 401 | ✅ |
| Login com email inexistente | 401 | ✅ |
| Login com body vazio | 400 com campos inválidos | ✅ |
| Acessar endpoint sem token | 401 com mensagem | ✅ |
| ANALISTA tentando criar veículo | 403 com mensagem | ✅ |
| ANALISTA tentando criar especificação | 403 com mensagem | ✅ |
| ANALISTA tentando listar usuários | 403 com mensagem | ✅ |
| ANALISTA tentando registrar usuário | 403 com mensagem | ✅ |
| Token inválido em qualquer endpoint | 401 com mensagem | ✅ |

---

**ANALISTA tentando criar veículo - `POST /api/veiculos`**

Response `403 Forbidden`:
```json
{
  "status": 403,
  "erro": "Acesso negado",
  "mensagem": "Você não tem permissão para acessar este recurso",
  "path": "/api/veiculos",
  "timestamp": "2026-05-15T00:06:05"
}
```

---

**Sem token - `GET /api/veiculos`**

Response `401 Unauthorized`:
```json
{
  "status": 401,
  "erro": "Não autenticado",
  "mensagem": "É necessário realizar o login para acessar este recurso",
  "path": "/api/veiculos",
  "timestamp": "2026-05-15T00:07:00"
}
```

---

**Login com senha errada - `POST /api/auth/login`**

Request:
```json
{
  "email": "admin@specradar.com",
  "senha": "senhaerrada123"
}
```

Response `401 Unauthorized`:
```json
{
  "status": 401,
  "erro": "Não autenticado",
  "mensagem": "Credenciais inválidas ou token ausente",
  "path": "/api/auth/login",
  "timestamp": "2026-05-14T19:55:26"
}
```

---

### Validação de Entrada

| Cenário | Resultado Esperado | Obtido |
|---|---|---|
| Cadastrar veículo com marca inválida (`MARCA_INVALIDA`) | 400 identificando o campo | ✅ |
| Cadastrar veículo com body vazio | 400 com todos os campos | ✅ |
| Cadastrar veículo com ano inválido (`2300`) | 400 com campo `ano` | ✅ |
| Cadastrar veículo com modelo em branco | 400 com campo `modelo` | ✅ |
| Cadastrar veículo com modelo muito longo (+100 chars) | 400 - previne buffer overflow | ✅ |
| Registrar usuário com role inválida (`GERENTE`) | 400 identificando o campo | ✅ |
| Registrar usuário com email duplicado | 400 com mensagem clara | ✅ |
| Registrar usuário com campos faltando | 400 com lista de campos | ✅ |

---

**Marca inválida - `POST /api/veiculos`**

Request:
```json
{
  "marca": "MARCA_INVALIDA",
  "modelo": "F40",
  "versao": "Base",
  "ano": 2025
}
```

Response `400 Bad Request`:
```json
{
  "status": 400,
  "erro": "Erro de validação",
  "mensagem": "Valor inválido para o campo 'marca': 'MARCA_INVALIDA'",
  "path": "/api/veiculos",
  "timestamp": "2026-05-14T23:56:44",
  "campos": null
}
```

---

**Body vazio - `POST /api/veiculos`**

Request:
```json
{}
```

Response `400 Bad Request`:
```json
{
  "status": 400,
  "erro": "Erro de validação",
  "mensagem": "Um ou mais campos são inválidos",
  "path": "/api/veiculos",
  "timestamp": "2026-05-14T20:34:49",
  "campos": {
    "marca": "Marca é obrigatória",
    "ano": "Ano é obrigatório",
    "modelo": "Modelo é obrigatório",
    "versao": "Versão é obrigatória"
  }
}
```

---

**Email duplicado - `POST /api/usuarios`**

Request:
```json
{
  "nome": "Duplicado",
  "email": "admin@specradar.com",
  "senha": "Admin@2026",
  "role": "ANALISTA"
}
```

Response `400 Bad Request`:
```json
{
  "status": 400,
  "erro": "Requisição inválida",
  "mensagem": "Email já cadastrado: admin@specradar.com",
  "path": "/api/usuarios",
  "timestamp": "2026-05-14T20:13:25",
  "campos": null
}
```

---

### Recursos Não Encontrados e Segurança de Pertencimento

| Cenário | Resultado Esperado | Obtido |
|---|---|---|
| Buscar veículo com ID inexistente | 404 com mensagem | ✅ |
| Buscar spec com ID inexistente | 404 com mensagem | ✅ |
| Acessar spec com veículo errado | 404 - impede acesso cruzado | ✅ |
| Deletar spec de outro veículo | 404 - impede acesso cruzado | ✅ |
| Buscar usuário inexistente | 404 com mensagem | ✅ |
| Atualizar veículo inexistente | 404 com mensagem | ✅ |

---

**Segurança de pertencimento - `GET /api/veiculos/2/especificacoes/1`**

A spec `id=1` pertence ao veículo `id=1`. Acessar via veículo `id=2` retorna:

Response `404 Not Found`:
```json
{
  "status": 404,
  "erro": "Recurso não encontrado",
  "mensagem": "Especificacao não encontrado com id: 1",
  "path": "/api/veiculos/2/especificacoes/1",
  "timestamp": "2026-05-14T20:48:40",
  "campos": null
}
```

---

**Veículo inexistente - `GET /api/veiculos/999`**

Response `404 Not Found`:
```json
{
  "status": 404,
  "erro": "Recurso não encontrado",
  "mensagem": "Veiculo não encontrado com id: 999",
  "path": "/api/veiculos/999",
  "timestamp": "2026-05-14T20:32:39",
  "campos": null
}
```

---

**Usuário inexistente - `GET /api/usuarios/999`**

Response `404 Not Found`:
```json
{
  "status": 404,
  "erro": "Recurso não encontrado",
  "mensagem": "Usuario não encontrado com id: 999",
  "path": "/api/usuarios/999",
  "timestamp": "2026-05-14T20:57:03",
  "campos": null
}
```

---

## Testes

O projeto conta com **101 testes unitários** organizados em suite, cobrindo services, domain, security e exception handling. Os testes usam **JUnit 5** e **Mockito** - nenhum deles toca o banco de dados, podendo ser executados com o servidor parado ou rodando.

### Arquitetura de Testes

```
SuiteDeTestesGeral
├── service/ (42 testes)
│   ├── UsuarioServiceTest (11)
│   ├── VeiculoServiceTest (12)
│   ├── EspecificacaoServiceTest (11)
│   └── ConsultaServiceTest (8)
│
├── domain/ (36 testes)
│   ├── UsuarioTest (13)
│   ├── VeiculoTest (7)
│   ├── EspecificacaoTest (9)
│   └── ConsultaTest (7)
│
├── security/ (13 testes)
│   └── JwtServiceTest (13)
│
└── exception/ (10 testes)
    └── GlobalExceptionHandlerTest (10)

TOTAL: 101 testes automatizados
Nenhum teste interfere no banco de dados
```

### Como rodar

Rodar a suite completa via Maven:
```bash
mvn test
```

Ou rodar individualmente:
```bash
# Pacote específico
mvn test -Dtest="br.com.ford.specradar.service.*Test"
mvn test -Dtest="br.com.ford.specradar.domain.*Test"

# Teste individual
mvn test -Dtest="VeiculoServiceTest"
mvn test -Dtest="JwtServiceTest"
```

Ou diretamente pelo IntelliJ clicando com o botão direito em `SuiteDeTestesGeral` → `Run`.

### O que é testado

- **Services** - regras de negócio, cenários de sucesso e de erro, validação de pertencimento
- **Domain** - construtores, campos, `UserDetails`, relacionamentos entre entidades
- **Security** - geração de token JWT, extração de claims, validação e expiração
- **Exception** - códigos HTTP corretos, mensagens seguras sem stack trace, campos inválidos

### Destaque dos testes

- **Isolamento total** - Mockito mocka todos os repositories, zero acesso ao banco
- **Cenários de segurança** - token inválido, acesso cruzado entre veículos e specs, RBAC
- **Erros semânticos** - 400, 401, 403, 404 e 500 validados individualmente
- **JWT completo** - geração, extração de email e role, expiração de 8 horas

---

## **Equipe**

### **Desenvolvedores - ICERS**

- **Renan Dias Utida - RM 558540**
- **Camila Pedroza da Cunha - RM 558768**
- **Isabelle Dallabeneta Carlesso - RM554592**
- **Nicoli Amy Kassa - RM 559104**
- **Pedro Almeida e Camacho - RM 556831**

---

- **Instituição:** FIAP - Faculdade de Informática e Administração Paulista
- **Disciplina:** Arquitetura Orientada a Serviços (SOA) & Web Services e CiberSecurity
- **Professor:** Salatiel Marinho (SOA) e Vitor Miguel Lasse (Cyber)
- **Challenge:** FORD

**SpecRadar - FIAP 3ESPW**

**[⬆ Voltar ao topo](#specradar-api)**
