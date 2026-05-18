# SpecRadar API

**Inteligência Competitiva Automotiva - Ford FIAP 2026**

O **SpecRadar** é uma API REST de inteligência competitiva automotiva desenvolvida para a Ford como parte da Sprint 1 da parceria Ford × FIAP 2026. A solução digitaliza e automatiza o processo de coleta, armazenamento e consulta de especificações técnicas de veículos concorrentes, permitindo que analistas da Ford tomem decisões estratégicas com dados precisos e atualizados em tempo real.

---

## Sumário

- [Contexto](#contexto)
- [Funcionalidades](#funcionalidades)
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

### O Problema (Desafio 1)

A Ford enfrenta um desafio operacional crítico no monitoramento competitivo do mercado automotivo brasileiro. Hoje, o processo de coleta de especificações técnicas de veículos concorrentes - como potência, torque, transmissão, preço e equipamentos - é feito **manualmente por analistas**, consultando sites, catálogos e materiais de marketing de cada fabricante.

Esse processo consome aproximadamente **1 hora por versão de veículo**, com alto risco de imprecisão, falta de padronização e dificuldade de comparação entre modelos. Para uma linha completa de concorrentes com dezenas de versões, isso representa dias de trabalho improdutivo e dados desatualizados.

### A Solução

O **SpecRadar** irá resolver esse problema oferecendo uma API centralizada onde toda a inteligência competitiva é armazenada de forma estruturada e acessível. A solução permite:

**Cadastro padronizado de concorrentes** - veículos de marcas como Toyota, Volkswagen, Chevrolet, Jeep e outros são cadastrados com marca, modelo, versão e ano, seguindo um padrão único independente da fonte de dados.

**Especificações técnicas detalhadas** - cada veículo pode ter N especificações cadastradas (Motor, Potência, Torque, Transmissão, Tração, Preço, etc.) com valor, unidade de medida e flag de disponibilidade - útil para marcar specs que o concorrente não divulga oficialmente.

**Histórico de consultas com auditoria** - toda vez que um analista busca um veículo por ID, o sistema registra automaticamente quem consultou, qual veículo e quando. Isso permite rastrear quais concorrentes estão sendo monitorados com mais frequência.

**Controle de acesso por perfil** - analistas acessam apenas leitura e suas próprias consultas. Administradores gerenciam o cadastro completo e têm visibilidade total do sistema.

**Referência própria com a Ranger Raptor** - o sistema inclui a Ford Ranger Raptor 2025 com 14 especificações técnicas completas, permitindo comparação direta com os concorrentes cadastrados.

### Impacto Esperado

Com o SpecRadar, o tempo de consulta de especificações de um veículo concorrente cai de **~1 hora para segundos**. A padronização elimina imprecisões, o histórico de consultas revela padrões de interesse dos analistas, e o controle de acesso garante que apenas pessoas autorizadas alimentam e gerenciam a base de dados competitiva da Ford.

---

## Funcionalidades

### Gestão de Veículos Concorrentes
- Cadastro de veículos com marca, modelo, versão e ano
- Filtro por marca para análise focada em um fabricante específico
- Listagem separada de veículos ativos e inativos (soft delete preserva histórico)
- Reativação de veículos previamente desativados

### Gestão de Especificações Técnicas
- Cadastro ilimitado de especificações por veículo (Motor, Potência, Torque, Preço, etc.)
- Campo `disponivel` para marcar specs que o concorrente não divulga oficialmente
- Campo `unidade` para padronizar valores numéricos (cv, Nm, s, R$, etc.)
- Segurança de pertencimento - specs de um veículo não são acessíveis via outro
- Dados do veículo embutidos na resposta de cada spec para consulta completa sem chamadas extras

### Histórico e Auditoria de Consultas
- Registro automático de toda consulta a veículo por ID com usuário, veículo e timestamp
- Isolamento por perfil - analistas veem apenas suas próprias consultas
- Administradores têm visibilidade completa de todas as consultas do sistema
- Rastreabilidade de quais concorrentes estão sendo monitorados com mais frequência

### Gestão de Usuários
- Cadastro de usuários com perfil ADMIN ou ANALISTA
- Soft delete com opção de reativação
- Anonimização de dados pessoais de usuários desativados - conformidade com LGPD
- Senhas armazenadas com BCrypt custo 12 - nunca em texto plano

### Segurança e Controle de Acesso
- Autenticação via JWT com expiração de 8 horas e assinatura HS256
- RBAC com dois perfis - ADMIN com acesso total, ANALISTA com acesso restrito à leitura
- Rate limiting por IP via Bucket4j para prevenção de abuso
- CORS configurado via variável de ambiente - nunca com origem aberta (`*`)
- Logs de auditoria para todas as ações críticas - criação, atualização, desativação e reativação

### Dados Iniciais
O sistema já vem com dados pré-carregados via Flyway para teste imediato:
- **2 usuários** - um ADMIN e um ANALISTA com credenciais de teste
- **3 concorrentes** - Toyota Hilux GR-Sport, Volkswagen Amarok V6 Extreme e Chevrolet S10 High Country, todos com specs de Motor, Potência, Torque e Preço
- **Ford Ranger Raptor 2025** - veículo de referência com **14 especificações técnicas completas** baseadas no material oficial da Ford

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
                    ┌─────────────────────────────────┐
                    │      Cliente / Swagger UI       │
                    └──────────────┬──────────────────┘
                                   │ HTTP Request
┌──────────────────────────────────▼───────────────────────────────┐
│                Presentation Layer  (Controller)                  │
│                                                                  │
│  AuthController   VeiculoController   EspecificacaoController    │
│  ConsultaController                   UsuarioController          │
│                                                                  │
│  Recebe as requisições HTTP, valida entrada e delega ao service  │
├──────────────────────────────────┬───────────────────────────────┤
│          Security / JWT          │           DTO Layer           │
│  JwtFilter  JwtService           │  Request DTOs (validação)     │
│  UserDetailsServiceImpl          │  Response DTOs (saída segura) │
├──────────────────────────────────┴───────────────────────────────┤
│                  Business Layer (Service)                        │
│                                                                  │
│  UsuarioService    VeiculoService    EspecificacaoService        │
│  ConsultaService                                                 │
│                                                                  │
│  Contém todas as regras de negócio e orquestra as operações      │
├──────────────────────────────────────────────────────────────────┤
│               	 GlobalExceptionHandler                        │
│  Intercepta exceções - nunca expõe stack trace ao cliente        │
├──────────────────────────────────────────────────────────────────┤
│                   Data Layer (Repository)                        │
│                                                                  │
│  UsuarioRepository   VeiculoRepository   EspecificacaoRepository │
│  ConsultaRepository                                              │
│                                                                  │
│  Abstrai o acesso ao banco via Spring Data JPA                   │
├──────────────────────────────────────────────────────────────────┤
│                          Domain Layer                            │
│                                                                  │
│  Usuario    Veiculo    Especificacao    Consulta                 │
│  MarcaVeiculo (Enum)   RoleUsuario (Enum)                        │
│                                                                  │
│  Entidades JPA e enums que representam o modelo de negócio       │
├──────────────────────────────────────────────────────────────────┤
│                   Banco de Dados (via Flyway)                    │
│                                                                  │
│  dev: H2 em memória          prod: Oracle 19c (FIAP)             │
│  												                   │
│  Tabelas criadas:  ford_usuarios  ford_veiculos                  │
│  ford_consultas ford_especificacoes                              │
└──────────────────────────────────────────────────────────────────┘
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

### Padrões REST e JSON

A API segue rigorosamente o padrão **REST** com **JSON** como formato exclusivo de comunicação, tanto para requests quanto para responses, incluindo os erros. Cada endpoint representa um recurso (`/veiculos`, `/especificacoes`, `/consultas`, `/usuarios`) e opera sobre ele via métodos HTTP semânticos. Não há endpoints com verbos no path (`/getVeiculo`, `/createEspecificacao`) - a ação é sempre inferida pelo método HTTP.

### Modularidade e Reutilização de Serviços

Cada service é independente e responsável por um único domínio de negócio, princípio central do SOA. O `ConsultaService` é um exemplo direto de reutilização: ele é injetado no `VeiculoController` para registrar automaticamente a consulta toda vez que um analista busca um veículo por ID, sem que o `VeiculoService` precise conhecer ou depender da lógica de consultas. Essa separação de responsabilidades garante que cada serviço possa evoluir, ser testado e ser substituído de forma independente.

---

## Estrutura de Pacotes

```
Sprint-Ford-API/
│
├── src/main/java/br/com/ford/specradar/
│   │
│   ├── config/                                          # Configurações globais da aplicação
│   │   ├── SecurityConfig.java                          	# Regras de autenticação, RBAC e filtros JWT
│   │   ├── SwaggerConfig.java                           	# Documentação OpenAPI e esquema Bearer
│   │   └── CorsConfig.java                              	# Origens permitidas via variável de ambiente
│   │
│   ├── controller/                                      # Endpoints REST - recebe e responde requisições HTTP
│   │   ├── AuthController.java                          	# POST /api/auth/login - autenticação pública
│   │   ├── VeiculoController.java                       	# CRUD de veículos + registro automático de consulta
│   │   ├── EspecificacaoController.java                 	# CRUD de especificações técnicas por veículo
│   │   ├── ConsultaController.java                      	# Histórico de consultas com isolamento por perfil
│   │   └── UsuarioController.java       					# CRUD de usuários + anonimização - apenas ADMIN
│   │
│   ├── domain/                                          # Entidades JPA e enums do modelo de negócio
│   │   ├── enums/                                       	# Tipos enumerados para normalização de dados
│   │   │   ├── MarcaVeiculo.java                        		# Enum com marcas suportadas (FORD, TOYOTA, etc.)
│   │   │   └── RoleUsuario.java                         		# Enum de perfis de acesso (ADMIN, ANALISTA)
│   │   ├── Usuario.java                                 	# Entidade ford_usuarios - implementa UserDetails
│   │   ├── Veiculo.java                                 	# Entidade ford_veiculos - soft delete com ativo
│   │   ├── Especificacao.java                           	# Entidade ford_especificacoes - ManyToOne Veiculo
│   │   └── Consulta.java                                	# Entidade ford_consultas - auditoria de acessos
│   │
│   ├── dto/                                             # Objetos de transferência - separa domínio da API
│   │   ├── request/                                     	# DTOs de entrada com Bean Validation
│   │   │   ├── LoginRequest.java                        		# email + senha para autenticação
│   │   │   ├── UsuarioRequest.java                      		# nome, email, senha, role para criar/atualizar
│   │   │   ├── VeiculoRequest.java                      		# marca, modelo, versao, ano para criar/atualizar
│   │   │   ├── EspecificacaoRequest.java                		# atributo, valor, unidade, disponivel
│   │   │   └── ConsultaRequest.java                     		# veiculoId para registrar consulta manual
│   │   └── response/                                    	# DTOs de saída - nunca expõem senha ou dados internos
│   │       ├── TokenResponse.java                       		# token JWT + tipo + expiraEm
│   │       ├── UsuarioResponse.java                     		# id, nome, email, role, ativo, criadoEm
│   │       ├── VeiculoResponse.java                     		# id, marca, modelo, versao, ano, ativo, criadoEm
│   │       ├── EspecificacaoResponse.java               		# spec + objeto VeiculoInfo aninhado
│   │       └── ConsultaResponse.java                    		# nomeUsuario, emailUsuario, marca, modelo, realizadaEm
│   │
│   ├── exception/                                       # Tratamento centralizado de erros
│   │   ├── GlobalExceptionHandler.java                  	# @RestControllerAdvice - nunca expõe stack trace
│   │   └── ResourceNotFoundException.java               	# Runtime exception para recursos não encontrados (404)
│   │
│   ├── repository/                                      # Acesso ao banco via Spring Data JPA
│   │   ├── UsuarioRepository.java                       	# findByEmail, existsByEmail
│   │   ├── VeiculoRepository.java                       	# findByAtivoTrue, findByMarcaAndAtivoTrue
│   │   ├── EspecificacaoRepository.java                 	# findByVeiculoId, findByVeiculoIdAndDisponivelTrue
│   │   └── ConsultaRepository.java                      	# findByUsuarioId, findByVeiculoId
│   │
│   ├── security/                                        # Autenticação e autorização JWT
│   │   ├── JwtService.java                              	# Geração, extração de claims e validação de tokens
│   │   ├── JwtFilter.java                               	# OncePerRequestFilter - intercepta e autentica JWT
│   │   └── UserDetailsServiceImpl.java                  	# Carrega usuário do banco pelo email
│   │
│   ├── service/                                         # Regras de negócio - camada intermediária
│   │   ├── UsuarioService.java          					# Criar, atualizar, desativar, reativar, anonimizar usuários
│   │   ├── VeiculoService.java                          	# CRUD + listarAtivos, listarTodos, reativar
│   │   ├── EspecificacaoService.java                    	# CRUD + validação de pertencimento ao veículo
│   │   └── ConsultaService.java                         	# Registrar e listar consultas com isolamento
│   │
│   └── SpecradarApplication.java                        # Entry point - banner adaptativo por perfil
│
├── src/main/resources/
│   ├── application.properties                           # Configurações globais compartilhadas entre perfis
│   ├── application-dev.properties                       # Perfil dev - H2 em memória + H2 Console
│   ├── application-prod.properties                      # Perfil prod - Oracle FIAP via variáveis de ambiente
│   └── db/migration/                                    # Migrations Flyway versionadas e imutáveis
│       ├── V1__create_usuarios.sql                      	# Criação da tabela ford_usuarios
│       ├── V2__create_veiculos.sql                      	# Criação da tabela ford_veiculos
│       ├── V3__create_especificacoes.sql                	# Criação da tabela ford_especificacoes
│       ├── V4__create_consultas.sql                     	# Criação da tabela ford_consultas
│       ├── V5__insert_dados_iniciais.sql                	# 2 usuários + 3 veículos concorrentes com specs
│       └── V6__insert_ranger_raptor.sql                 	# Ford Ranger Raptor 2025 com 14 especificações
│
├── src/test/java/br/com/ford/specradar/               # Testes unitários - zero acesso ao banco
│   ├── SuiteDeTestesGeral.java                          # Suite principal - agrupa todos os pacotes de teste
│   ├── service/                                         # Testes das regras de negócio com Mockito
│   │   ├── UsuarioServiceTest.java                      	# 14 testes - criar, atualizar, desativar, reativar
│   │   ├── VeiculoServiceTest.java                      	# 12 testes - listar, buscar, CRUD, reativar
│   │   ├── EspecificacaoServiceTest.java                	# 11 testes - CRUD + pertencimento ao veículo
│   │   └── ConsultaServiceTest.java                     	# 8 testes - registrar + isolamento por usuário
│   ├── domain/                                          # Testes das entidades JPA
│   │   ├── UsuarioTest.java                             	# 13 testes - UserDetails, authorities, isEnabled
│   │   ├── VeiculoTest.java                             	# 7 testes - builder, marcas, listas
│   │   ├── EspecificacaoTest.java                       	# 9 testes - disponivel, unidade, pertencimento
│   │   └── ConsultaTest.java                            	# 7 testes - relacionamentos, isolamento
│   ├── security/                                        # Testes do JWT
│   │   └── JwtServiceTest.java                          	# 13 testes - gerar, extrair claims, validar, expirar
│   └── exception/                                       # Testes do tratamento de erros
│       └── GlobalExceptionHandlerTest.java              	# 10 testes - 400, 401, 403, 404, 500
│
├── .env.example                                     # Template de variáveis de ambiente
├── .gitignore                                       # .env e application-prod.properties excluídos
├── pom.xml                                          # Dependências Maven + plugins Surefire e Compiler
└── README.md                                        # Documentação completa do projeto
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

O projeto usa **spring-dotenv** - o arquivo `.env` é lido automaticamente pelo Spring na inicialização. Não é necessário configurar variáveis de ambiente manualmente no IntelliJ ou no sistema operacional.

**1. Copie o arquivo de exemplo (`.env.example`) para `.env`:**
```bash
cp .env.example .env
```

**2. Preencha o `.env` com suas credenciais:**

```env
# Projeto SpecRadar - Variáveis de Ambiente
# Copie este arquivo para .env e preencha os valores

# Perfil ativo: dev ou prod
SPRING_PROFILE=dev

# Oracle (necessário em prod)
ORACLE_URL=jdbc:oracle:thin:@oracle.fiap.com.br:1521:orcl
ORACLE_USER=seu_rm_aqui
ORACLE_PASSWORD=sua_senha_aqui

# JWT - gere com: openssl rand -base64 64
JWT_SECRET=
JWT_EXPIRATION=28800000

# Servidor
SERVER_PORT=8080

# CORS - separar múltiplas origens por vírgula
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

Perfil de desenvolvimento com **H2 em memória** - banco zerado a cada restart:

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

# H2 CONSOLE - acessível em http://localhost:8080/h2-console
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

Perfil de produção com **Oracle FIAP** - dados persistidos entre sessões:

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

### pom.xml - Configuração Maven

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

		<!-- DOTENV - lê o .env automaticamente -->
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

A API expõe **21 endpoints REST** organizados em 5 grupos de recursos. Todos os endpoints protegidos exigem autenticação via JWT no header `Authorization: Bearer {token}`. O acesso é controlado por perfil - **ADMIN** tem acesso total e **ANALISTA** tem acesso restrito à leitura de veículos, especificações e suas próprias consultas.

### Uso semântico dos métodos HTTP

| Método | Uso no SpecRadar |
|---|---|
| `GET` | Consulta de recursos sem efeitos colaterais |
| `POST` | Criação de novos recursos - retorna 201 Created |
| `PUT` | Atualização completa de um recurso existente - retorna 200 OK |
| `DELETE` | Desativação lógica via soft delete - retorna 204 No Content |
| `PATCH` | Atualização parcial - reativação e anonimização de usuários e veículos |

---

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
| PATCH | `/api/usuarios/{id}/anonimizar` | Anonimiza dados pessoais de usuário desativado | ADMIN |

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

O SpecRadar implementa segurança em múltiplas camadas, atendendo aos 5 critérios de CyberSecurity exigidos no Challenge Ford FIAP 2026.

---

### 1. Segurança de Entrada e Validação de Dados

**Validação de entradas e sanitização**

Todas as entradas da API são validadas via **Bean Validation** antes de chegarem ao service. SQL Injection é prevenido pelo uso de **Spring Data JPA com queries parametrizadas** - nunca há concatenação de strings SQL. XSS e command injection não são aplicáveis pois a API retorna JSON, não HTML.

```java
// VeiculoRequest.java - validação declarativa nos DTOs de entrada
@NotNull(message = "Marca é obrigatória")
private MarcaVeiculo marca;

@NotBlank(message = "Modelo é obrigatório")
@Size(min = 1, max = 100, message = "Modelo deve ter no máximo 100 caracteres")
private String modelo;

@NotNull(message = "Ano é obrigatório")
@Min(value = 1900, message = "Ano inválido")
@Max(value = 2100, message = "Ano inválido")
private Integer ano;
```

**Normalização e validação de parâmetros de API**

`MarcaVeiculo` e `RoleUsuario` são enums - valores fora do padrão retornam 400 automaticamente. Com `accept-case-insensitive-enums=true`, `"toyota"`, `"Toyota"` e `"TOYOTA"` são todos normalizados para `TOYOTA`.

```java
// MarcaVeiculo.java - enum garante que apenas valores válidos são aceitos
public enum MarcaVeiculo {
    FORD, TOYOTA, VOLKSWAGEN, CHEVROLET, FIAT,
    HYUNDAI, NISSAN, MITSUBISHI, JEEP, RAM, MERCEDES
}
```

```properties
# application.properties - normalização case-insensitive
spring.jackson.mapper.accept-case-insensitive-enums=true
```

**Limitação de tamanho e formato - prevenção de buffer overflow**

Todos os campos String têm `@Size` com limite máximo definido. Testado e validado - modelo com atributo com +100 caracteres retorna 400:

```java
// EspecificacaoRequest.java
@NotBlank(message = "Atributo é obrigatório")
@Size(min = 1, max = 100, message = "Atributo deve ter no máximo 100 caracteres")
private String atributo;

@NotBlank(message = "Valor é obrigatório")
@Size(min = 1, max = 255, message = "Valor deve ter no máximo 255 caracteres")
private String valor;
```

**Tratamento seguro de erros**

O `GlobalExceptionHandler` intercepta todas as exceções e nunca expõe stack trace, nome de classes ou tecnologia ao cliente:

```java
// GlobalExceptionHandler.java - handler genérico
@ExceptionHandler(Exception.class)
public ResponseEntity handleGenerico(
        Exception ex, HttpServletRequest request) {

    // Loga internamente via SLF4J - nunca expõe ao cliente
    log.error("[ERRO INTERNO] {} : {}", ex.getClass().getName(), ex.getMessage());

    return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErroResponse.of(
                    500,
                    "Erro interno do servidor",
                    "Ocorreu um erro inesperado. Tente novamente mais tarde.",
                    request.getRequestURI()
            ));
}
```

Response ao cliente - sem stack trace, sem tecnologia exposta:
```json
{
  "status": 500,
  "erro": "Erro interno do servidor",
  "mensagem": "Ocorreu um erro inesperado. Tente novamente mais tarde.",
  "path": "/api/veiculos",
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

### 2. Autenticação e Autorização

**JWT com expiração, assinatura forte e renovação controlada**

Autenticação via **JWT HS256** com secret gerado por `openssl rand -base64 64` (512 bits - bem acima do mínimo de 256 bits exigido). Expiração padrão de 8 horas configurável via variável de ambiente.

```java
// JwtService.java - geração do token com claims de role e nome
public String gerarToken(Usuario usuario) {
    Map claims = new HashMap<>();
    claims.put("role", usuario.getRole().name());
    claims.put("nome", usuario.getNome());

    return Jwts.builder()
            .claims(claims)
            .subject(usuario.getEmail())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getSecretKey())
            .compact();
}
```

```java
// JwtFilter.java - validação em cada requisição protegida
if (jwtService.isTokenValido(token, userDetails)) {
    UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );
    SecurityContextHolder.getContext().setAuthentication(authToken);
}
```

**Controle de acesso baseado em papéis (RBAC)**

Dois perfis com permissões distintas definidas no `SecurityConfig`:

```java
// SecurityConfig.java - regras RBAC
.requestMatchers(HttpMethod.GET, "/api/veiculos/todos").hasRole("ADMIN")
.requestMatchers(HttpMethod.GET, "/api/veiculos/**").hasAnyRole("ANALISTA", "ADMIN")
.requestMatchers(HttpMethod.POST, "/api/veiculos/**").hasRole("ADMIN")
.requestMatchers("/api/usuarios/**").hasRole("ADMIN")
.requestMatchers("/api/consultas/**").hasAnyRole("ANALISTA", "ADMIN")
```

| Ação | ANALISTA | ADMIN |
|---|---|---|
| Login | ✅ | ✅ |
| Listar/buscar veículos e specs | ✅ | ✅ |
| Ver próprias consultas | ✅ | ✅ |
| Ver todas as consultas | ❌ | ✅ |
| Cadastrar/editar/desativar recursos | ❌ | ✅ |
| Gerenciar usuários | ❌ | ✅ |

---

### 3. Proteção de APIs e Serviços

**HTTPS/TLS**

Configurado via variável de ambiente em produção. Em dev, HTTP é usado localmente - comportamento aceitável para ambiente de desenvolvimento.

**Rate limiting e throttling**

Implementado via **Bucket4j** por IP - previne abuso, scraping excessivo e ataques DoS:

```xml
		<dependency>
			<groupId>com.bucket4j</groupId>
			<artifactId>bucket4j-core</artifactId>
			<version>${bucket4j.version}</version>
		</dependency>
```

**CORS configurado corretamente**

Origens permitidas via variável de ambiente - nunca `*`. Métodos e headers explicitamente listados:

```java
// CorsConfig.java
List origins = Arrays.asList(allowedOriginsRaw.split(","));
config.setAllowedOrigins(origins);
config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
config.setAllowCredentials(true);
config.setMaxAge(3600L);
```

```env
# .env - origens controladas por variável de ambiente
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:8081
```

**Assinatura e verificação de integridade de payloads**

JWT com assinatura HS256 garante que o token não foi manipulado em trânsito. Token com assinatura incorreta é rejeitado pelo `JwtService`:

```java
// JwtService.java - verificação de assinatura
private Claims extrairTodosClaims(String token) {
    return Jwts.parser()
            .verifyWith(getSecretKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
}
```

---

### 4. Segurança de Dados e Privacidade

**Criptografia de dados sensíveis em repouso**

Senhas armazenadas com **BCrypt custo 12** - nunca em texto plano. Todo novo usuário criado via API tem a senha encodada antes de persistir:

```java
// UsuarioService.java - senha sempre encodada antes de salvar
Usuario usuario = Usuario.builder()
        .nome(dto.getNome())
        .email(dto.getEmail())
        .senha(passwordEncoder.encode(dto.getSenha()))
        .role(dto.getRole())
        .ativo(true)
        .build();
```

Evidência no Oracle - coluna `senha` sempre com hash BCrypt:
```
$2a$12$qe56g7GA45wxn2vRaAsFwu0BBfMCWiBifjIzWJgBs4OOdbATQlExC
```

**Política de retenção e descarte seguro**

Implementado via **soft delete** - registros nunca são deletados fisicamente, preservando integridade referencial. Para usuários que precisam ter dados removidos, o endpoint de anonimização sobrescreve os dados pessoais:

```java
// UsuarioService.java - anonimização de dados pessoais
public void anonimizar(Long id) {
    Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));

    if (usuario.getAtivo()) {
        throw new IllegalArgumentException(
                "Apenas usuários desativados podem ser anonimizados"
        );
    }

    String emailOriginal = usuario.getEmail();
    usuario.setNome("Usuário Removido");
    usuario.setEmail("anonimizado_" + id + "@specradar.com");
    usuario.setSenha(passwordEncoder.encode(UUID.randomUUID().toString()));
    usuarioRepository.save(usuario);
    log.info("[AUDITORIA] Usuário anonimizado - id: {} email original: {}",
            id, emailOriginal);
}
```

**Anonimização/pseudonimização de dados pessoais**

Endpoint `PATCH /api/usuarios/{id}/anonimizar` - disponível apenas para ADMIN. Após a anonimização o registro preserva o ID para manter FKs mas perde todos os dados pessoais identificáveis.

**Proteção contra exposição acidental de dados**

- `UsuarioResponse` nunca inclui o campo `senha`
- `GlobalExceptionHandler` nunca expõe estrutura interna
- `.env` no `.gitignore` - credenciais nunca vão para o repositório
- `spring.jpa.open-in-view=false` previne lazy loading inesperado
- Swagger documentado - nenhum endpoint não documentado

---

### 5. Monitoramento, Logs e Auditoria

**Logs estruturados e seguros via SLF4J**

Substituição completa de `System.err.println` por **SLF4J** em todo o projeto. Logs não contêm senhas, tokens ou dados sensíveis:

```java
// GlobalExceptionHandler.java
private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

log.error("[ERRO INTERNO] {} : {}", ex.getClass().getName(), ex.getMessage());
```

**Monitoramento de eventos suspeitos**

Tentativas não autenticadas e acessos negados são logados com IP para rastreamento:

```java
// SecurityConfig.java - log de tentativas suspeitas com IP
@Bean
public AuthenticationEntryPoint authenticationEntryPoint() {
    return (request, response, authException) -> {
        log.warn("[SEGURANÇA] Tentativa não autenticada em {} - IP: {}",
                request.getRequestURI(), request.getRemoteAddr());
        // ...
    };
}

@Bean
public AccessDeniedHandler accessDeniedHandler() {
    return (request, response, accessDeniedException) -> {
        log.warn("[SEGURANÇA] Acesso negado em {} - IP: {}",
                request.getRequestURI(), request.getRemoteAddr());
        // ...
    };
}
```

```java
// JwtFilter.java - log de token inválido com path e IP
log.warn("[SEGURANÇA] Token JWT inválido na requisição {} - IP: {} - motivo: {}",
        request.getRequestURI(), request.getRemoteAddr(), e.getMessage());
```

**Trilha de auditoria para ações críticas**

Registro físico no banco via tabela `ford_consultas` - toda consulta a veículo por ID é auditada automaticamente com usuário, veículo e timestamp. Logs de auditoria em todas as ações críticas de usuários, veículos e especificações:

```java
// UsuarioService.java - logs de auditoria
log.info("[AUDITORIA] Usuário criado - id: {} email: {} role: {}",
        response.getId(), response.getEmail(), response.getRole());

log.info("[AUDITORIA] Usuário desativado - id: {} email: {}",
        id, usuario.getEmail());

log.info("[AUDITORIA] Usuário anonimizado - id: {} email original: {}",
        id, emailOriginal);
```

```java
// VeiculoService.java - logs de auditoria
log.info("[AUDITORIA] Veículo cadastrado - id: {} marca: {} modelo: {} versao: {}",
        response.getId(), response.getMarca(),
        response.getModelo(), response.getVersao());
```

```java
// EspecificacaoService.java - logs de auditoria
log.info("[AUDITORIA] Especificação cadastrada - id: {} veiculoId: {} atributo: {}",
        response.getId(), veiculoId, response.getAtributo());

log.info("[AUDITORIA] Especificação deletada - id: {} veiculoId: {} atributo: {}",
        specId, veiculoId, especificacao.getAtributo());
```

---

### Resumo de Implementação

| Critério | Implementação | Status |
|---|---|---|
| Validação de entrada | Bean Validation (`@NotBlank`, `@Size`, `@Email`, `@Min`, `@Max`) | ✅ |
| Sanitização SQL Injection | Spring Data JPA com queries parametrizadas | ✅ |
| Normalização de parâmetros | Enums `MarcaVeiculo` e `RoleUsuario` com case-insensitive | ✅ |
| Limitação de tamanho | `@Size(max=100/150/255)` em todos os campos String | ✅ |
| Erros seguros | `GlobalExceptionHandler` sem stack trace ou tecnologia | ✅ |
| Autenticação JWT | HS256, 512 bits, expiração 8h configurável | ✅ |
| RBAC | `ADMIN` e `ANALISTA` com permissões distintas no `SecurityConfig` | ✅ |
| Rate limiting | Bucket4j por IP | ✅ |
| CORS | Origens via variável de ambiente - nunca `*` | ✅ |
| Integridade de payload | JWT com assinatura HS256 verificada em cada requisição | ✅ |
| Senhas em repouso | BCrypt custo 12 - nunca texto plano | ✅ |
| Retenção e descarte | Soft delete + endpoint de anonimização LGPD | ✅ |
| Anonimização | `PATCH /api/usuarios/{id}/anonimizar` sobrescreve dados pessoais | ✅ |
| Proteção de exposição | `UsuarioResponse` sem senha, `.env` no `.gitignore` | ✅ |
| Logs estruturados | SLF4J em todo o projeto - sem dados sensíveis | ✅ |
| Monitoramento | Logs de IP em tentativas suspeitas e tokens inválidos | ✅ |
| Trilha de auditoria | `ford_consultas` no banco + logs `[AUDITORIA]` em services | ✅ |

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
| Anonimizar usuário desativado | PATCH | `/api/usuarios/{id}/anonimizar` | 204 sem body |

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

**Anonimizar usuário desativado - `PATCH /api/usuarios/3/anonimizar`**

> Sobrescreve os dados pessoais do usuário desativado com dados fictícios - atende à política de privacidade e retenção de dados (LGPD).

Response `204 No Content` - sem body.

Após a anonimização, o usuário no banco passa a ter:
```json
{
  "id": 3,
  "nome": "Usuário Removido",
  "email": "anonimizado_3@specradar.com",
  "role": "ANALISTA",
  "ativo": false,
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

O projeto conta com **104 testes unitários** organizados em suite, cobrindo services, domain, security e exception handling. Os testes usam **JUnit 5** e **Mockito** - nenhum deles toca o banco de dados, podendo ser executados com o servidor parado ou rodando.

### Arquitetura de Testes

```
SuiteDeTestesGeral
├── service/ (45 testes)
│   ├── UsuarioServiceTest (14)
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

TOTAL: 104 testes automatizados
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
