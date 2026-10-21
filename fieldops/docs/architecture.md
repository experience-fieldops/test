# FieldOps — Documentação de Arquitetura

> **Status:** Draft  
> **Versão:** 1.0.0  
> **Última atualização:** Setembro/2026  
> **Revisores:** Time técnico FieldOps

---

## Sumário

1. [Visão Geral](#1-visão-geral)
2. [Diagrama de Componentes (C4 — Nível 2)](#2-diagrama-de-componentes-c4--nível-2)
3. [Diagrama de Componentes (C4 — Nível 3 — Backend)](#3-diagrama-de-componentes-c4--nível-3--backend)
4. [Componentes e Responsabilidades](#4-componentes-e-responsabilidades)
5. [Autenticação JWT — Fluxo Completo](#5-autenticação-jwt--fluxo-completo)
6. [Sincronização Offline — SyncOutbox Pattern](#6-sincronização-offline--syncoutbox-pattern)
7. [Camadas do Backend](#7-camadas-do-backend)
8. [Modelo de Domínio](#8-modelo-de-domínio)
9. [ADRs — Architectural Decision Records](#9-adrs--architectural-decision-records)
10. [Checklist de Conformidade](#10-checklist-de-conformidade)

---

## 1. Visão Geral

O **FieldOps** é uma plataforma digital de inspeções em campo que substitui formulários impressos e planilhas por um fluxo integrado entre três frentes: app mobile para técnicos, painel web para supervisores/admins e uma API REST central.

### Fluxo principal do produto

```
┌──────────────────────────────────────────────────────────────────┐
│  1. Admin configura: usuários, clientes, locais, equipamentos     │
│  2. Supervisor cria e publica modelo de inspeção (template)       │
│  3. Supervisor agenda e atribui inspeção ao Técnico               │
│  4. App mobile sincroniza a inspeção (download)                   │
│  5. Técnico executa em campo (offline-first)                      │
│     └── checklist, fotos, QR Code, localização GPS               │
│  6. App sincroniza dados executados ao recuperar conexão          │
│  7. Supervisor revisa a execução                                  │
│  8. Supervisor aprova ou reprova (com pedido de correção)         │
└──────────────────────────────────────────────────────────────────┘
```

---

## 2. Diagrama de Componentes (C4 — Nível 2)

Visão de sistema: clientes externos e sistemas internos de alto nível.

```
┌──────────────────────────────────────────────────────────────────────────┐
│                          Sistema FieldOps                                │
│                                                                          │
│  ┌──────────────────┐   HTTPS/REST    ┌──────────────────────────────┐  │
│  │   App Mobile     │ ◄─────────────► │                              │  │
│  │  React Native    │                 │        API REST               │  │
│  │  Expo            │   JWT Auth      │     Spring Boot 3.x           │  │
│  │                  │                 │        Java 17                │  │
│  │  [Técnico]       │                 │                              │  │
│  └──────────────────┘                 │  ┌────────────────────────┐  │  │
│                                       │  │  Controller Layer       │  │  │
│  ┌──────────────────┐   HTTPS/REST    │  │  Service Layer          │  │  │
│  │   Admin Web      │ ◄─────────────► │  │  Repository Layer       │  │  │
│  │   Angular        │                 │  └────────────────────────┘  │  │
│  │                  │   JWT Auth      │                              │  │
│  │  [Admin /        │                 └──────────────┬───────────────┘  │
│  │   Supervisor]    │                                │                  │
│  └──────────────────┘                   JPA/Hibernate│                  │
│                                                      │                  │
│                                  ┌───────────────────▼──────────────┐  │
│                                  │         PostgreSQL                │  │
│                                  │      (Flyway migrations)          │  │
│                                  └──────────────────────────────────┘  │
│                                                                          │
│                                  ┌──────────────────────────────────┐  │
│                                  │     Object Storage (S3)           │  │
│                                  │   Evidências / Fotos              │  │
│                                  └──────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────────────┘
```

---

## 3. Diagrama de Componentes (C4 — Nível 3 — Backend)

Detalhamento interno da API REST (monolito modular).

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          API REST — Spring Boot                          │
│                                                                          │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │  Security Layer                                                     │ │
│  │  JwtFilter ──► JwtUtil ──► UserDetailsServiceImpl                  │ │
│  │  @PreAuthorize(RBAC): ADMIN | SUPERVISOR | TECNICO                 │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                                  │                                       │
│  ┌────────────────────────────────▼───────────────────────────────────┐ │
│  │  Controller Layer  (/api/v1/*)                                      │ │
│  │  AuthController  UserController  InspectionOrderController         │ │
│  │  ClientController  LocationController  EquipmentController         │ │
│  │  TemplateController  ExecutionController  ReviewController         │ │
│  └────────────────────────────────┬───────────────────────────────────┘ │
│                                   │ @Valid + Bean Validation             │
│  ┌────────────────────────────────▼───────────────────────────────────┐ │
│  │  Service Layer                                                      │ │
│  │  Interfaces + impl/  (regras de negócio, retorna DTOs)             │ │
│  │  AuthService  UserService  InspectionOrderService                  │ │
│  │  InspectionExecutionService  ReviewService  SyncService            │ │
│  └────────────────────────────────┬───────────────────────────────────┘ │
│                                   │                                      │
│  ┌────────────────────────────────▼───────────────────────────────────┐ │
│  │  Repository Layer  (JpaRepository)                                  │ │
│  │  UserRepository  ClientRepository  LocationRepository              │ │
│  │  EquipmentRepository  TemplateRepository  ExecutionRepository      │ │
│  │  ReviewRepository  SyncLogRepository                               │ │
│  └────────────────────────────────┬───────────────────────────────────┘ │
│                                   │ JPA / Hibernate                      │
│  ┌────────────────────────────────▼───────────────────────────────────┐ │
│  │  Entity Layer  (com.fieldops.entity)                                │ │
│  │  Soft delete · Auditoria · Controle de versão otimista             │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                                                                          │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │  exception/GlobalExceptionHandler  ·  SpringDoc/OpenAPI (Swagger)│   │
│  └──────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 4. Componentes e Responsabilidades

### 4.1 App Mobile (React Native / Expo)

| Aspecto | Detalhe |
|---|---|
| **Usuário-alvo** | Técnico em campo |
| **Plataformas** | Android e iOS |
| **Estado offline** | SQLite local via Expo SQLite |
| **Sincronização** | SyncOutbox pattern — fila local com retry |
| **Scanner** | QR Code para identificação de equipamentos |
| **Localização** | GPS no momento da execução (snapshot, não rastreamento contínuo) |
| **Autenticação** | JWT armazenado de forma segura (SecureStore) |

Responsabilidades:
- Exibir inspeções atribuídas ao técnico autenticado
- Permitir execução do checklist com respostas, fotos e leitura de QR Code
- Persistir tudo localmente quando offline
- Sincronizar ao recuperar conexão via SyncOutbox

### 4.2 Admin Web (Angular)

| Aspecto | Detalhe |
|---|---|
| **Usuários-alvo** | Admin e Supervisor |
| **Framework** | Angular + TypeScript |
| **Comunicação** | HTTP client com interceptors para JWT |
| **Estado** | Serviços com RxJS / signals |

Responsabilidades:
- Gerenciar usuários, clientes, locais e equipamentos (CRUD completo)
- Criar, versionar e publicar templates de inspeção com checklist
- Agendar inspeções e atribuí-las a técnicos
- Revisar execuções: aprovar, reprovar ou solicitar correção
- Visualizar histórico e evidências de inspeções

### 4.3 API REST (Spring Boot 3.x / Java 17)

| Aspecto | Detalhe |
|---|---|
| **Padrão** | Monolito modular (sem microsserviços no MVP) |
| **Autenticação** | JWT stateless — sem sessão no servidor |
| **Autorização** | RBAC com `@PreAuthorize` — roles: `ADMIN`, `SUPERVISOR`, `TECNICO` |
| **Migrations** | Flyway — controle de versão do schema |
| **Documentação** | SpringDoc / OpenAPI (Swagger UI em `/swagger-ui.html`) |
| **Validação** | Bean Validation (`@Valid`) em todos os endpoints |

Responsabilidades:
- Única fonte de verdade da lógica de negócio
- Autenticar e autorizar todos os clientes (mobile e web)
- Expor endpoints REST padronizados (`/api/v1/{recurso}`)
- Processar e persistir execuções sincronizadas do mobile
- Gerenciar uploads de evidências para Object Storage

### 4.4 PostgreSQL

| Aspecto | Detalhe |
|---|---|
| **Versão alvo** | PostgreSQL 15+ |
| **Migrations** | Flyway (versionadas em `resources/db/migration`) |
| **Soft delete** | Campo `deleted_at` — sem remoção física de histórico |
| **Auditoria** | Campos `created_at`, `updated_at` em todas as entidades |
| **Concorrência** | Controle de versão otimista (`@Version`) em entidades críticas |

### 4.5 Object Storage (S3-compatible)

| Aspecto | Detalhe |
|---|---|
| **Uso** | Upload e download de fotos/evidências de inspeção |
| **Acesso** | URLs pré-assinadas geradas pela API — o app acessa diretamente |
| **Razão** | Manter o banco leve e garantir escalabilidade de arquivos binários |

---

## 5. Autenticação JWT — Fluxo Completo

```
┌─────────┐          ┌──────────────────┐          ┌──────────────┐
│  Client │          │   API REST       │          │  PostgreSQL  │
│(Mobile/ │          │  Spring Boot     │          │              │
│  Web)   │          │                  │          │              │
└────┬────┘          └────────┬─────────┘          └──────┬───────┘
     │                        │                           │
     │  POST /api/public/auth/login                       │
     │  { email, password }   │                           │
     │───────────────────────►│                           │
     │                        │  findByEmail()            │
     │                        │──────────────────────────►│
     │                        │  User (hash da senha)     │
     │                        │◄──────────────────────────│
     │                        │                           │
     │                        │  BCrypt.verify()          │
     │                        │  JwtUtil.generateToken()  │
     │                        │  (sub=userId, role=ROLE)  │
     │                        │                           │
     │  200 OK                │                           │
     │  { token, expiresIn }  │                           │
     │◄───────────────────────│                           │
     │                        │                           │
     │  GET /api/v1/inspection-orders                     │
     │  Authorization: Bearer <token>                     │
     │───────────────────────►│                           │
     │                        │  JwtFilter:               │
     │                        │  1. Extrai token          │
     │                        │  2. Valida assinatura     │
     │                        │  3. Verifica expiração    │
     │                        │  4. Seta SecurityContext  │
     │                        │  @PreAuthorize check      │
     │                        │                           │
     │  200 OK { data: [...] }│                           │
     │◄───────────────────────│                           │
```

**Pontos-chave:**
- Token JWT assinado com chave secreta (HS256) configurada via variável de ambiente
- Payload contém: `sub` (userId), `role`, `iat`, `exp`
- Expiração configurável (padrão: 8h para web, 30 dias para mobile com refresh)
- Endpoint de login em `/api/public/auth/login` — único endpoint sem autenticação além de `/api/public/**`
- Nunca armazenar token em localStorage no web — usar httpOnly cookie ou memory

---

## 6. Sincronização Offline — SyncOutbox Pattern

O app mobile opera **offline-first**: toda ação do técnico é persistida localmente antes de qualquer tentativa de envio ao servidor.

### Diagrama do fluxo de sincronização

```
┌─────────────────────────────────────────────────────────────────┐
│                        App Mobile                                │
│                                                                  │
│  Técnico executa checklist                                       │
│         │                                                        │
│         ▼                                                        │
│  ┌─────────────────┐    persiste    ┌──────────────────────┐    │
│  │ ChecklistResponse│──────────────►│  SQLite Local         │    │
│  │ Evidence (foto) │               │  (source of truth     │    │
│  │ GPS snapshot    │               │   no mobile)          │    │
│  └─────────────────┘               └──────────┬───────────┘    │
│                                               │                 │
│                                  enfileira    ▼                 │
│                                  ┌─────────────────────────┐   │
│                                  │   SyncOutbox Table       │   │
│                                  │  { id, payload, status,  │   │
│                                  │    retries, createdAt }  │   │
│                                  └────────────┬────────────┘   │
└───────────────────────────────────────────────┼────────────────┘
                                                │
                    Ao recuperar conexão         │
                    SyncService processa fila    │
                                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                        API REST                                  │
│                                                                  │
│  POST /api/v1/sync/execution                                     │
│  { executionId, responses[], evidences[], gpsLocation }         │
│         │                                                        │
│         ▼                                                        │
│  SyncService valida idempotência (executionId único)            │
│         │                                                        │
│         ▼                                                        │
│  Persiste no PostgreSQL                                          │
│         │                                                        │
│         ▼                                                        │
│  200 OK → SyncOutbox marca item como SYNCED                     │
│  4xx/5xx → SyncOutbox incrementa retries (max 5, backoff exp.) │
└─────────────────────────────────────────────────────────────────┘
```

### Estados do SyncOutbox

| Status | Descrição |
|---|---|
| `PENDING` | Aguardando envio |
| `IN_PROGRESS` | Tentativa em andamento |
| `SYNCED` | Confirmado pelo servidor |
| `FAILED` | Máximo de tentativas atingido — requer ação manual |

### Garantias do padrão
- **Idempotência**: o servidor usa o `executionId` gerado no mobile como chave de idempotência — reenvios não duplicam dados
- **Ordem**: itens são processados em ordem de `createdAt`
- **Resiliência**: backoff exponencial entre tentativas (1s, 2s, 4s, 8s, 16s)
- **Auditoria**: `SyncLog` registra cada tentativa de sincronização no backend

---

## 7. Camadas do Backend

### Estrutura de pacotes

```
com.fieldops
├── config/
│   ├── SecurityConfig.java       # Filtros, permissões públicas/privadas
│   ├── CorsConfig.java           # Origins permitidas
│   └── OpenApiConfig.java        # Configuração do Swagger
│
├── controller/                   # Recebe HTTP, valida com @Valid, delega ao Service
│   ├── AuthController.java
│   ├── UserController.java
│   ├── ClientController.java
│   ├── LocationController.java
│   ├── EquipmentController.java
│   ├── InspectionTemplateController.java
│   ├── InspectionOrderController.java
│   ├── InspectionExecutionController.java
│   ├── ReviewController.java
│   └── SyncController.java
│
├── dto/
│   ├── request/                  # Objetos de entrada validados
│   └── response/                 # Objetos de saída (nunca expor Entity)
│
├── entity/                       # Entidades JPA com auditoria e soft delete
│
├── enums/
│   ├── Role.java                 # ADMIN, SUPERVISOR, TECNICO
│   ├── StatusInspecao.java       # AGENDADA, EM_ANDAMENTO, CONCLUIDA, REPROVADA
│   ├── TipoItem.java             # TEXTO, SIM_NAO, FOTO, NUMERICO, QR_CODE
│   └── StatusSync.java           # PENDING, IN_PROGRESS, SYNCED, FAILED
│
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   └── BusinessException.java
│
├── repository/                   # Interfaces JpaRepository
│
├── service/
│   ├── AuthService.java          # Interface
│   ├── UserService.java
│   └── ...
│   └── impl/                    # Implementações — contêm a lógica de negócio
│
├── security/
│   ├── JwtFilter.java            # Intercepta requisições e valida JWT
│   ├── JwtUtil.java              # Geração e validação de tokens
│   └── UserDetailsServiceImpl.java
│
└── util/                         # Classes utilitárias gerais
```

### Padrão de resposta da API

```json
// Sucesso (2xx)
{
  "data": { "id": 1, "..." },
  "message": "Operação realizada com sucesso",
  "timestamp": "2026-09-08T21:00:00Z"
}

// Erro (4xx / 5xx)
{
  "status": 404,
  "message": "Inspeção não encontrada com id: 42",
  "timestamp": "2026-09-08T21:00:00Z"
}
```

### Padrão de endpoints

| Método | URL | Ação |
|---|---|---|
| `GET` | `/api/v1/{recurso}` | Listar |
| `GET` | `/api/v1/{recurso}/{id}` | Buscar por ID |
| `POST` | `/api/v1/{recurso}` | Criar |
| `PUT` | `/api/v1/{recurso}/{id}` | Atualizar completo |
| `PATCH` | `/api/v1/{recurso}/{id}` | Atualizar parcial |
| `DELETE` | `/api/v1/{recurso}/{id}` | Remover (soft delete) |

---

## 8. Modelo de Domínio

```
┌──────────┐     ┌──────────┐     ┌───────────┐
│   User   │     │  Client  │────►│ Location  │
│  (Role)  │     └──────────┘     └─────┬─────┘
└────┬─────┘                            │
     │                         ┌────────▼──────┐
     │  atribuído              │  Equipment    │
     │  ◄──────────────────────│  (QR Code)    │
     │                         └───────────────┘
     │
     │  cria
     ▼
┌────────────────────┐    contém    ┌───────────────┐
│ InspectionTemplate │─────────────►│ ChecklistItem │
│   (Supervisor)     │              │  (TipoItem)   │
└────────┬───────────┘              └───────────────┘
         │ baseado em
         ▼
┌────────────────────┐    executa   ┌─────────────────────┐
│  InspectionOrder   │─────────────►│ InspectionExecution │
│ (AGENDADA/EM_AND.) │              │   (Técnico)         │
└────────────────────┘              └──────────┬──────────┘
                                               │
                          ┌────────────────────┼───────────────┐
                          │                    │               │
                    ┌─────▼──────┐   ┌────────▼──────┐  ┌────▼─────┐
                    │ Checklist  │   │   Evidence    │  │ SyncLog  │
                    │ Response   │   │ (foto / S3)   │  │          │
                    └────────────┘   └───────────────┘  └──────────┘
                                               │
                                    ┌──────────▼──────────┐
                                    │       Review        │
                                    │  (Aprovado/Reprov.) │
                                    └─────────────────────┘
```

---

## 9. ADRs — Architectural Decision Records

### ADR-001 — Backend: Spring Boot + Java 17

| Campo | Detalhe |
|---|---|
| **Status** | Aceito |
| **Contexto** | Escolha da stack de backend para o MVP |
| **Decisão** | Spring Boot 3.x com Java 17 (LTS) |
| **Justificativa** | Stack madura com suporte LTS até 2029, ecossistema robusto (Spring Security, Data JPA, Validation), familiaridade do time, ampla documentação e comunidade |
| **Alternativas consideradas** | Node.js + Express (menos tipagem, menos estrutura para APIs enterprise), Quarkus (menor adoção no time) |
| **Consequências** | Build mais pesado que alternativas reativas; overhead de startup aceitável para o MVP |

---

### ADR-002 — Mobile: React Native + Expo

| Campo | Detalhe |
|---|---|
| **Status** | Aceito |
| **Contexto** | Aplicativo para técnicos em campo (Android e iOS) |
| **Decisão** | React Native com Expo (managed workflow) |
| **Justificativa** | Desenvolvimento multiplataforma com uma única codebase, ciclo de build rápido via Expo Go, acesso a APIs nativas (câmera, GPS, SecureStore), familiaridade do time com React/TypeScript |
| **Alternativas consideradas** | Flutter (curva de aprendizado maior para o time), Native (custo de manutenção dobrado) |
| **Consequências** | Limitações pontuais de acesso a APIs nativas resolvidas com bare workflow quando necessário |

---

### ADR-003 — Admin Web: Angular

| Campo | Detalhe |
|---|---|
| **Status** | Aceito |
| **Contexto** | Interface web para Admins e Supervisores |
| **Decisão** | Angular + TypeScript |
| **Justificativa** | Framework com opinião forte (estrutura previsível), tipagem forte nativa, familiaridade do time, adequado para dashboards administrativos complexos |
| **Alternativas consideradas** | React (mais flexível, porém menos estruturado para aplicações enterprise), Vue (menor adoção no time) |
| **Consequências** | Bundle inicial maior; custo de setup maior que React, compensado pela estrutura e produtividade a longo prazo |

---

### ADR-004 — Banco de Dados: PostgreSQL

| Campo | Detalhe |
|---|---|
| **Status** | Aceito |
| **Contexto** | Banco de dados relacional para persistência central |
| **Decisão** | PostgreSQL 15+ |
| **Justificativa** | Robusto, open source, sem custo de licença, suporte nativo a JSON (para payloads de sync), integração perfeita com JPA/Hibernate, índices avançados (GiST para geolocalização futura) |
| **Alternativas consideradas** | MySQL (menos recursos avançados), MongoDB (flexibilidade excessiva para um domínio bem definido) |
| **Consequências** | Requer gerenciamento de migrations (resolvido com Flyway) |

---

### ADR-005 — Autenticação: JWT Stateless

| Campo | Detalhe |
|---|---|
| **Status** | Aceito |
| **Contexto** | Autenticação para clientes mobile (offline-first) e web |
| **Decisão** | JWT stateless com HS256 |
| **Justificativa** | O app mobile precisa funcionar offline — sessões server-side são incompatíveis com esse requisito. JWT permite que o app mantenha autenticação sem conexão com o servidor |
| **Alternativas consideradas** | Sessions + Redis (incompatível com offline), OAuth2 (overhead desnecessário para o MVP) |
| **Consequências** | Revogação de token requer blacklist ou TTL curto; tokens de longa duração para mobile gerenciados com refresh token |

---

### ADR-006 — Sincronização Offline: SyncOutbox Pattern

| Campo | Detalhe |
|---|---|
| **Status** | Aceito |
| **Contexto** | Técnicos executam inspeções em locais sem conectividade |
| **Decisão** | SyncOutbox pattern — fila local no SQLite do mobile com idempotência no servidor |
| **Justificativa** | Garante que nenhum dado é perdido em caso de falha de conexão. A idempotência via `executionId` garante que reenvios não causem duplicações. Simpler que event sourcing completo para o MVP |
| **Alternativas consideradas** | Sync apenas quando online (inaceitável para o caso de uso), CRDTs (complexidade excessiva para o MVP) |
| **Consequências** | Necessidade de gerenciar estado de sincronização no mobile e lógica de idempotência no backend |

---

### ADR-007 — Armazenamento de Arquivos: Object Storage (S3)

| Campo | Detalhe |
|---|---|
| **Status** | Aceito |
| **Contexto** | Evidências (fotos) coletadas durante inspeções |
| **Decisão** | Object Storage compatível com S3 (AWS S3 ou MinIO para dev) |
| **Justificativa** | Armazenar binários no banco de dados gera crescimento descontrolado e impacta performance de queries. S3 oferece escalabilidade ilimitada, durabilidade 99.999999999% e custo proporcional ao uso |
| **Alternativas consideradas** | Armazenar no filesystem do servidor (sem escalabilidade horizontal), Base64 no banco (impraticável) |
| **Consequências** | Custo adicional de infraestrutura; URLs pré-assinadas usadas para acesso direto sem passar pela API |

---

### ADR-008 — Migrations: Flyway

| Campo | Detalhe |
|---|---|
| **Status** | Aceito |
| **Contexto** | Evolução do schema do banco de dados ao longo do projeto |
| **Decisão** | Flyway com migrations versionadas em SQL |
| **Justificativa** | Rastreabilidade completa de cada alteração no schema, execução automática no startup, integração nativa com Spring Boot, suporte a rollback manual via migrations reversas |
| **Alternativas consideradas** | Liquibase (mais complexo sem ganho significativo para o MVP), `spring.jpa.hibernate.ddl-auto=update` (inseguro para produção) |
| **Consequências** | Toda alteração de schema exige criação de nova migration — boa prática que garante consistência entre ambientes |

---

## 10. Checklist de Conformidade

A ser verificado a cada implementação antes de considerar uma funcionalidade concluída:

- [ ] A funcionalidade está dentro do escopo do MVP?
- [ ] A estrutura de pacotes foi respeitada?
- [ ] Controller delega para Service sem conter regra de negócio?
- [ ] Service retorna DTO (não Entity)?
- [ ] Entrada validada com `@Valid`?
- [ ] Exceções tratadas pelo `GlobalExceptionHandler`?
- [ ] Endpoint segue o padrão `/api/v1/{recurso}`?
- [ ] Autorização por Role configurada (`@PreAuthorize`)?
- [ ] Entidade tem `createdAt`, `updatedAt`, `deletedAt`?
- [ ] Migration Flyway criada para alterações no banco?
- [ ] Endpoint documentado no Swagger (SpringDoc)?
- [ ] Branch segue o padrão `feature/` ou `fix/`?

---

*Documento mantido pelo time técnico FieldOps. Alterações devem ser revisadas e aprovadas antes do merge.*
