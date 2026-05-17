# SpecRadar API

**Inteligência Competitiva Automotiva - Ford FIAP 2026**

API REST para gerenciamento de veículos concorrentes e suas especificações técnicas, desenvolvida como parte da Sprint 1 da parceria Ford × FIAP. Permite que analistas da Ford consultem e comparem especificações de veículos concorrentes de forma padronizada, com autenticação JWT e controle de acesso por perfil.

---

## Sumário

- [Contexto](#contexto)
- [Stack Tecnológica](#stack-tecnológica)
- [Arquitetura](#arquitetura)
- [Estrutura de Pacotes](#estrutura-de-pacotes)
- [Como Rodar](#como-rodar)
- [Variáveis de Ambiente](#variáveis-de-ambiente)
- [Banco de Dados](#banco-de-dados)
- [Endpoints](#endpoints)
- [Autenticação](#autenticação)
- [Perfis e Permissões](#perfis-e-permissões)
- [Segurança](#segurança)
- [Credenciais de Teste](#credenciais-de-teste)
- [Documentação Swagger](#documentação-swagger)

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
┌────────────────────────────────────────────┐
│            Presentation Layer              │
│   AuthController  VeiculoController        │
│   EspecificacaoController                  │
│   ConsultaController UsuarioController     │
├────────────────────────────────────────────┤
│             Business Layer                 │
│   UsuarioService   VeiculoService          │
│   EspecificacaoService ConsultaService     │
├────────────────────────────────────────────┤
│               Data Layer                   │
│   UsuarioRepository VeiculoRepository      │
│   EspecificacaoRepository                  │
│   ConsultaRepository                       │
├────────────────────────────────────────────┤
│               Domain Layer                 │
│   Usuario  Veiculo  Especificacao          │
│   Consulta  MarcaVeiculo(E) RoleUsuario(E) │
└────────────────────────────────────────────┘
```

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

4. O projeto usa **spring-dotenv** — o arquivo `.env` é lido automaticamente pelo Spring na inicialização. Não é necessário configurar variáveis de ambiente manualmente no IntelliJ ou no sistema operacional.

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

## Variáveis de Ambiente

Copie `.env.example` para `.env` e preencha:

```env
# Perfil ativo: dev ou prod
SPRING_PROFILE=dev

# Oracle FIAP (necessário só em prod)
ORACLE_URL=jdbc:oracle:thin:@oracle.fiap.com.br:1521:orcl
ORACLE_USER=seu_rm_aqui
ORACLE_PASSWORD=sua_senha_aqui

# JWT — gere com: openssl rand -base64 64
JWT_SECRET=
JWT_EXPIRATION=28800000

# Servidor
SERVER_PORT=8080

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:8081,exp://localhost:8081
```

> O arquivo `.env` está no `.gitignore` e nunca deve ser commitado.

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
| Senhas | BCrypt com custo 12 — nunca armazenadas em texto plano |
| Validação de entrada | Bean Validation (`@NotBlank`, `@Size`, `@Email`, `@Min`, `@Max`) |
| Normalização de parâmetros | Enums para `marca` e `role` — valores inválidos retornam 400 |
| Proteção contra payloads grandes | `@Size` com limite máximo em todos os campos String |
| Erros seguros | `GlobalExceptionHandler` — nunca expõe stack trace ou tecnologia |
| CORS | Origens configuradas via variável de ambiente — nunca `*` |
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

## Testes

O projeto conta com **101 testes unitários** organizados em suite, cobrindo services, domain, security e exception handling. Os testes usam **JUnit 5** e **Mockito** — nenhum deles toca o banco de dados.

### Como rodar

Rodar a suite completa via Maven:
```bash
mvn test
```

Ou diretamente pelo IntelliJ clicando com o botão direito em `SuiteDeTestesGeral` → `Run`.

### Cobertura por pacote

| Pacote | Classes de Teste | Testes |
|---|---|---|
| `service` | UsuarioServiceTest, VeiculoServiceTest, EspecificacaoServiceTest, ConsultaServiceTest | 38 |
| `domain` | UsuarioTest, VeiculoTest, EspecificacaoTest, ConsultaTest | 33 |
| `exception` | GlobalExceptionHandlerTest | 11 |
| `security` | JwtServiceTest | 11 |
| **Total** | **10 classes** | **101 testes** |

### O que é testado

- Regras de negócio dos services — cenários de sucesso e de erro
- Entidades de domínio — construtores, campos, `UserDetails` e relacionamentos
- Tratamento de exceções — códigos HTTP, mensagens seguras e campos inválidos
- JWT — geração, extração de claims, validação e expiração de token

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

**SpecRadar — FIAP 3ESPW**

**[⬆ Voltar ao topo](#specradar-api)**
