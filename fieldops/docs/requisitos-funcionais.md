# FieldOps — Requisitos Funcionais

> **Status:** Draft — aguardando revisão do time
> **Versão:** 1.0.0
> **Última atualização:** Setembro/2026
> **Grupo:** Produto
> **Prioridade:** 🔴 Alta
> **Revisores:** Time FieldOps

---

## Sumário

1. [Objetivo](#1-objetivo)
2. [Perfis de Usuário](#2-perfis-de-usuário)
3. [Requisitos Funcionais](#3-requisitos-funcionais)
   - [3.1 Autenticação e Sessão](#31-autenticação-e-sessão)
   - [3.2 Gestão de Usuários](#32-gestão-de-usuários)
   - [3.3 Gestão de Clientes e Locais](#33-gestão-de-clientes-e-locais)
   - [3.4 Templates de Inspeção](#34-templates-de-inspeção)
   - [3.5 Inspeções (Ordens)](#35-inspeções-ordens)
   - [3.6 Execução em Campo](#36-execução-em-campo)
   - [3.7 Modo Offline e Sincronização](#37-modo-offline-e-sincronização)
   - [3.8 Revisão e Aprovação](#38-revisão-e-aprovação)
   - [3.9 Auditoria](#39-auditoria)
4. [Matriz de Rastreabilidade por Perfil](#4-matriz-de-rastreabilidade-por-perfil)
5. [Critérios de Aceitação](#5-critérios-de-aceitação)

---

## 1. Objetivo

Levantar e documentar todos os requisitos funcionais do **FieldOps**, a plataforma digital de inspeções em campo. Os requisitos funcionais descrevem **o que o sistema deve fazer**, cobrindo os quatro perfis de usuário e o fluxo completo do produto: configuração, criação de templates, agendamento, execução offline-first em campo, sincronização, revisão e aprovação.

Este documento deve ser revisado e aprovado pelo time antes do início da implementação.

---

## 2. Perfis de Usuário

| Perfil | Role (RBAC) | Responsabilidades |
|---|---|---|
| **Admin** | `ADMIN` | Gerencia usuários, clientes, locais, equipamentos e templates |
| **Supervisor** | `SUPERVISOR` | Cria e publica templates, agenda inspeções, revisa e aprova execuções |
| **Técnico** | `TECHNICIAN` | Recebe ordens, executa checklist em campo (online e offline), sincroniza dados |
| **Visualizador do Cliente** | `CLIENT_VIEWER` | Visualiza relatórios e status de inspeções do seu cliente |

---

## 3. Requisitos Funcionais

### 3.1 Autenticação e Sessão

| ID | Requisito | Perfis |
|---|---|---|
| **RF-001** | Autenticar usuário via login com e-mail e senha, retornando token JWT | Todos |
| **RF-002** | Encerrar sessão via logout, invalidando o token ativo | Todos |
| **RF-003** | Bloquear acesso após tentativas de login inválidas consecutivas | Todos |

### 3.2 Gestão de Usuários

| ID | Requisito | Perfis |
|---|---|---|
| **RF-004** | Criar, consultar, atualizar e remover usuários com atribuição de role (`ADMIN`, `SUPERVISOR`, `TECHNICIAN`, `CLIENT_VIEWER`) | Admin |
| **RF-005** | Ativar, inativar e bloquear usuário | Admin |
| **RF-006** | Alterar senha pelo próprio usuário ou por um Admin | Admin, próprio usuário |

### 3.3 Gestão de Clientes e Locais

| ID | Requisito | Perfis |
|---|---|---|
| **RF-007** | Criar, consultar, atualizar e remover clientes (organizações atendidas) | Admin |
| **RF-008** | Criar, consultar, atualizar e remover locais (`InspectionSite`) vinculados a clientes | Admin |
| **RF-009** | Criar, consultar, atualizar e remover equipamentos (ativos físicos com QR Code) vinculados a locais | Admin |

### 3.4 Templates de Inspeção

| ID | Requisito | Perfis |
|---|---|---|
| **RF-010** | Criar template de inspeção com seções e itens | Supervisor |
| **RF-011** | Definir o tipo de resposta por item (`TEXT`, `BOOLEAN`, `NUMBER`, `DATE`, `PHOTO`, `OPTION`) | Supervisor |
| **RF-012** | Publicar versão do template como snapshot imutável | Supervisor |
| **RF-013** | Versionar template sem afetar inspeções em andamento | Supervisor |

### 3.5 Inspeções (Ordens)

| ID | Requisito | Perfis |
|---|---|---|
| **RF-014** | Criar e agendar inspeção vinculada a template publicado, cliente, local e técnico | Supervisor |
| **RF-015** | Listar inspeções filtradas por técnico, cliente, status e período | Supervisor, Técnico, Client Viewer |
| **RF-016** | Iniciar, executar e concluir inspeção | Técnico |
| **RF-017** | Cancelar inspeção com justificativa obrigatória | Supervisor |

### 3.6 Execução em Campo

| ID | Requisito | Perfis |
|---|---|---|
| **RF-018** | Responder itens do checklist conforme o tipo de resposta definido | Técnico |
| **RF-019** | Registrar não conformidades com severidade e descrição | Técnico |
| **RF-020** | Fazer upload de evidências (fotos) vinculadas à inspeção | Técnico |
| **RF-021** | Escanear QR Code para identificar equipamento | Técnico |
| **RF-022** | Capturar localização GPS no início da inspeção | Técnico |

### 3.7 Modo Offline e Sincronização

| ID | Requisito | Perfis |
|---|---|---|
| **RF-023** | Executar inspeção completa sem conexão à internet | Técnico |
| **RF-024** | Sincronizar dados automaticamente ao recuperar conexão | Técnico |
| **RF-025** | Garantir idempotência no reenvio de operações | Técnico (sistema) |

### 3.8 Revisão e Aprovação

| ID | Requisito | Perfis |
|---|---|---|
| **RF-026** | Iniciar revisão de inspeção concluída | Supervisor |
| **RF-027** | Aprovar inspeção com comentário opcional | Supervisor |
| **RF-028** | Reprovar inspeção com motivo obrigatório e solicitação de correção | Supervisor |

### 3.9 Auditoria

| ID | Requisito | Perfis |
|---|---|---|
| **RF-029** | Registrar automaticamente ações críticas (login, criação, alteração, aprovação) | Sistema |
| **RF-030** | Consultar logs de auditoria com filtros | Admin, Supervisor |

---

## 4. Matriz de Rastreabilidade por Perfil

| Requisito | Admin | Supervisor | Técnico | Client Viewer |
|---|:---:|:---:|:---:|:---:|
| RF-001 Login | ✅ | ✅ | ✅ | ✅ |
| RF-002 Logout | ✅ | ✅ | ✅ | ✅ |
| RF-003 Bloqueio por tentativas | ✅ | ✅ | ✅ | ✅ |
| RF-004 CRUD de usuários | ✅ | — | — | — |
| RF-005 Ativar/inativar/bloquear usuário | ✅ | — | — | — |
| RF-006 Alterar senha | ✅ | ✅¹ | ✅¹ | ✅¹ |
| RF-007 CRUD de clientes | ✅ | — | — | — |
| RF-008 CRUD de locais | ✅ | — | — | — |
| RF-009 CRUD de equipamentos | ✅ | — | — | — |
| RF-010 Criar template | — | ✅ | — | — |
| RF-011 Definir tipo de resposta | — | ✅ | — | — |
| RF-012 Publicar template | — | ✅ | — | — |
| RF-013 Versionar template | — | ✅ | — | — |
| RF-014 Criar/agendar inspeção | — | ✅ | — | — |
| RF-015 Listar inspeções | — | ✅ | ✅² | ✅³ |
| RF-016 Executar inspeção | — | — | ✅ | — |
| RF-017 Cancelar inspeção | — | ✅ | — | — |
| RF-018 Responder checklist | — | — | ✅ | — |
| RF-019 Registrar não conformidade | — | — | ✅ | — |
| RF-020 Upload de evidências | — | — | ✅ | — |
| RF-021 Escanear QR Code | — | — | ✅ | — |
| RF-022 Capturar GPS | — | — | ✅ | — |
| RF-023 Executar offline | — | — | ✅ | — |
| RF-024 Sincronizar | — | — | ✅ | — |
| RF-025 Idempotência | — | — | ✅ | — |
| RF-026 Iniciar revisão | — | ✅ | — | — |
| RF-027 Aprovar inspeção | — | ✅ | — | — |
| RF-028 Reprovar inspeção | — | ✅ | — | — |
| RF-029 Registrar auditoria | ✅⁴ | ✅⁴ | ✅⁴ | ✅⁴ |
| RF-030 Consultar auditoria | ✅ | ✅ | — | — |

> ¹ O próprio usuário altera apenas a sua senha; o Admin pode alterar a de qualquer usuário.
> ² O técnico visualiza apenas as inspeções atribuídas a ele.
> ³ O Client Viewer visualiza apenas as inspeções do seu cliente.
> ⁴ O registro de auditoria é automático para ações de qualquer perfil (não é uma ação manual do usuário).

---

## 5. Critérios de Aceitação

- [x] Todos os requisitos listados e numerados (RF-001 a RF-030)
- [x] Cada requisito descrito com clareza e verbo no infinitivo
- [x] Cobertura dos quatro perfis: `ADMIN`, `SUPERVISOR`, `TECHNICIAN`, `CLIENT_VIEWER`
- [x] Documento publicado em `/docs/requisitos-funcionais.md`
- [ ] Revisado e aprovado pelo time antes do início da implementação

---

*Documento mantido pelo time FieldOps. Alterações devem ser revisadas e aprovadas antes do merge.*
