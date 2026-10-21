
# FieldOps — Plataforma de Inspeção em Campo

> Projeto acadêmico integrado (Mobile, Backend e Web Administrativo) desenvolvido para a disciplina de Análise e Desenvolvimento de Sistemas.

Repositório: [`experience-fieldops/fieldops`](https://github.com/experience-fieldops/fieldops) · Board do projeto: [GitHub Projects](https://github.com/orgs/experience-fieldops/projects/2)

## Índice

- [Sobre o projeto](#sobre-o-projeto)
- [Componentes do repositório](#componentes-do-repositório)
- [Fluxo principal do produto](#fluxo-principal-do-produto)
- [Modelo de domínio](#modelo-de-domínio)
- [Como executar o projeto](#como-executar-o-projeto)
- [Equipe e responsabilidades](#equipe-e-responsabilidades)
- [Documentação técnica](#documentação-técnica)
- [Dados de demonstração](#dados-de-demonstração)
- [Escopo do MVP](#escopo-do-mvp)
- [Limitações conhecidas](#limitações-conhecidas)
- [Como contribuir](#como-contribuir)
- [Licença](#licença)

## Sobre o projeto

O **FieldOps** é uma plataforma digital para planejamento, execução, acompanhamento e revisão de inspeções técnicas realizadas em campo. Ele substitui processos baseados em formulários impressos, planilhas e mensagens informais por um fluxo digital integrado:

- **Técnicos** usam um aplicativo mobile para consultar inspeções atribuídas, identificar equipamentos por QR Code, responder checklists, registrar evidências e concluir atividades — inclusive sem conexão com a internet.
- **Administradores e supervisores** usam uma interface web para cadastrar dados, criar modelos de inspeção, agendar atividades, atribuir técnicos, acompanhar a execução e aprovar ou reprovar os resultados.
- Uma **API REST** centraliza autenticação, regras de negócio, persistência, auditoria e a integração entre o app mobile e a interface administrativa.

## Componentes do repositório

```
fieldops/
├── mobile/    # Aplicativo Expo + React Native + TypeScript
├── admin/     # Interface administrativa Angular + TypeScript
├── api/       # API REST Java 17 + Spring Boot 3.3 + PostgreSQL
└── docs/      # Documentação, diagramas e contrato OpenAPI
```

| Componente | Tecnologia | Pasta | README específico |
| --- | --- | --- | --- |
| Aplicativo mobile | Expo + React Native + TypeScript | [`/mobile`](./mobile) | [`/mobile/README.md`](./mobile/README.md) |
| Interface administrativa web | Angular + TypeScript | [`/admin`](./admin) | [`/admin/README.md`](./admin/README.md) |
| API REST | Java 17 + Spring Boot 3.3 + PostgreSQL | [`/api`](./api) | [`/api/README.md`](./api/README.md) |

## Fluxo principal do produto

```
Configuração administrativa (usuários, clientes, locais, equipamentos)
        ↓
Criação e publicação do modelo de inspeção
        ↓
Agendamento e atribuição ao técnico
        ↓
Disponibilização ao técnico (sincronização)
        ↓
Execução em campo (checklist, fotos, QR Code, localização — inclusive offline)
        ↓
Sincronização das alterações
        ↓
Revisão do supervisor
        ↓
Aprovação ou reprovação (com solicitação de correção)
```

## Modelo de domínio

Entidades principais da plataforma:

| Entidade | Descrição |
| --- | --- |
| `User` | Usuário do sistema, com perfil (ADMIN, SUPERVISOR, TECNICO) |
| `Client` | Cliente dono dos locais de inspeção |
| `Location` | Local físico vinculado a um cliente |
| `Equipment` | Equipamento com QR Code único, vinculado a um local |
| `InspectionTemplate` | Modelo de inspeção criado pelo supervisor |
| `ChecklistItem` | Item de um modelo de inspeção |
| `InspectionOrder` | Ordem de inspeção agendada e atribuída a um técnico |
| `InspectionExecution` | Execução real da inspeção pelo técnico |
| `ChecklistResponse` | Resposta de cada item durante a execução |
| `Evidence` | Foto/arquivo anexado durante a execução |
| `Review` | Revisão do supervisor sobre a execução |
| `SyncLog` | Registro de sincronização offline → online |

Diagrama de classes e diagrama ER detalhados: `[link a definir em /docs]`.

## Como executar o projeto

### Pré-requisitos

- Node.js e Expo CLI (para o mobile)
- Node.js e Angular CLI (para o admin)
- Java 17 e Maven (para a API)
- PostgreSQL (produção/dev) — a API usa H2 em memória para testes

### Passos gerais

```bash
# 1. Clonar o repositório
git clone https://github.com/experience-fieldops/fieldops.git
cd fieldops

# 2. Subir o banco PostgreSQL e executar as migrações Flyway da API
cd api && [comando de migração — ver api/README.md]

# 3. Executar a API
[comando de execução — ver api/README.md]

# 4. Executar a interface administrativa
cd ../admin && npm install && npm start

# 5. Executar o aplicativo mobile
cd ../mobile && npm install && npx expo start
```

Instruções detalhadas de cada componente estão nos respectivos READMEs.

## Equipe e responsabilidades

| Frente | Responsáveis |
| --- | --- |
| Mobile — Fundação e navegação | `[nome]` |
| Mobile — Checklist e formulários | `[nome]` |
| Mobile — Recursos nativos, offline e sincronização | `[nome]` |
| Backend — Identidade, acesso e cadastros | `[nome]` |
| Backend — Modelos, planejamento, sincronização e revisão | `[nome]` |
| Admin Web — Fundação, autenticação e cadastros | `[nome]` |
| Admin Web — Modelos, planejamento e revisão | `[nome]` |
| Product Owner / QA / Documentação | `[nome]` |

## Documentação técnica

- Contrato da API (OpenAPI/Swagger): disponível em `/api/swagger-ui.html` após subir a aplicação; exportado em `[/docs/openapi.json — a definir]`
- Modelo de dados / diagrama de classes: `[link a definir em /docs]`
- Backlog do produto: [GitHub Projects — Board FieldOps](https://github.com/orgs/experience-fieldops/projects/2)
- Padrões de código, arquitetura do backend e regras do domínio: `.kiro/direção/contexto-do-projeto.md`

## Dados de demonstração

| Perfil | E-mail | Senha |
| --- | --- | --- |
| Administrador | `[a definir]` | `[a definir]` |
| Supervisor | `[a definir]` | `[a definir]` |
| Técnico | `[a definir]` | `[a definir]` |

## Escopo do MVP

- [x] Cadastro básico: usuários, clientes, locais, equipamentos
- [x] Criação de modelos de inspeção com checklist pelo supervisor
- [x] Agendamento e atribuição de inspeção a um técnico
- [x] Recebimento da inspeção no app mobile
- [x] Execução do checklist com fotos, QR Code e localização GPS
- [x] Modo offline com sincronização posterior
- [x] Revisão, aprovação ou reprovação pelo supervisor

**Fora do escopo do MVP:** aplicativo exclusivo para clientes finais, portal público, multiempresa/multi-inquilino, faturamento e cobrança, roteirização automática, transmissão contínua de localização, chamadas de vídeo, assinatura eletrônica, integração com ERPs, IA para diagnóstico, integração com IoT.

## Limitações conhecidas

_Atualizar esta seção ao longo do desenvolvimento com funcionalidades incompletas ou limitações técnicas identificadas._

## Como contribuir

1. Criar uma branch a partir de `main`/`develop` seguindo o padrão `feature/nome-da-funcionalidade` ou `fix/descricao-do-bug`.
2. Nomear a issue seguindo o prefixo da frente: `[Backend] Título`, `[Mobile] Título`, `[Admin] Título`, `[Docs] Título`, `[Testes] Título` ou `[Segurança] Título`.
3. Abrir um Pull Request descrevendo a história de usuário e os critérios de aceitação atendidos.
4. Garantir que o código passa no lint e na verificação de tipos antes de solicitar revisão.
5. Aguardar revisão de ao menos um integrante antes do merge.

## 👥 Integrantes

| Nome |
|------|
|   Bianca Nunes   |
|   Eloisa Lopes   |
|   Julia Harumi   |
|   Lauren Reis    |
|   Paola Costa    |
|   Paola Oliveira |
|   Sara Roza      |
|   Vitor Fogaça   |


## Licença

Projeto acadêmico — uso restrito à disciplina e à instituição de ensino.

