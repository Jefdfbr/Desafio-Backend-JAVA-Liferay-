# ROADMAP — To-do-list Portlet Liferay

> **Status geral:** `████████░░ 78%`

Cada passo está mapeado como um checkbox. O progresso é atualizado conforme avançamos.
Fases são sequenciais (dependências). Passos podem ser paralelos onde indicado.

---

## Fase 1 — Configuração do Ambiente
> **Estimativa:** 1h - 1h30 | **Depende de:** nada | **Status: ✅ Concluída (90%)**

| # | Passo | Status | Resultado |
|---|-------|--------|-----------|
| 1.1 | Instalar Gradle 8.x no sistema | `[x]` | Gradle Wrapper 8.13 via `gradlew` |
| 1.2 | Verificar `JAVA_HOME` apontando para JDK 21 | `[x]` | `eclipse-temurin:21-jdk-alpine` no Docker |
| 1.3 | Baixar Liferay 7.4 CE GA bundle (Tomcat) | `[x]` | `liferay-portal-tomcat-7.4.3.132-ga132.tar.gz` presente |
| 1.4 | Extrair Liferay em `/opt/liferay` | `[x]` | Dentro do container `desafio2-liferay` |
| 1.5 | Criar `portal-ext.properties` com configs base | `[x]` | `liferay/portal-ext.properties` — PostgreSQL + timezone + HTTPS |
| 1.6 | Inicializar repo Git em `/var/www/desafio2` | `[x]` | `git status` funciona; commits pendentes (Fase 10) |
| 1.7 | Gerar estrutura Liferay Workspace (Gradle) | `[x]` | `settings.gradle`, `gradlew`, `modules/todo-list-web/` |
| 1.8 | Configurar `gradle.properties` | `[x]` | `liferay.workspace.home.dir=/var/www/desafio2/liferay` |
| 1.9 | Testar: `gradlew clean` roda sem erro | `[x]` | BUILD SUCCESSFUL |
| 1.10 | Commit inicial | `[ ]` | Pendente — Fase 10 |

---

## Fase 2 — Modelo de Dados
> **Estimativa:** 40min - 1h | **Depende de:** Fase 1 | **Status: ✅ Concluída (85%)**

> **⚠️ Nota de implementação:** Optou-se por JDBC direto + POJO em vez de Service Builder,
> para manter o módulo como bundle OSGi único (`todo-list-web`) sem dependência de outros
> módulos gerados. As entidades e queries equivalentes foram implementadas manualmente.

| # | Passo | Status | Resultado |
|---|-------|--------|-----------|
| 2.1 | Criar módulo `todo-list-web` em `modules/` | `[x]` | `modules/todo-list-web/` com `build.gradle` + `bnd.bnd` |
| 2.2 | Adicionar dependência `com.liferay.portal` no build.gradle | `[x]` | `compileOnly fileTree(dir: shielded-container-lib)` |
| 2.3 | Entidade `TodoUser` | `[~]` | Dispensada — usuários gerenciados pela API nativa `UserLocalServiceUtil` |
| 2.4 | Entidade `Task` | `[x]` | `Task.java` (POJO) + tabela `Todo_Task` (PostgreSQL) |
| 2.5 | Entidade `SubTask` | `[x]` | `SubTask.java` (POJO) + tabela `Todo_SubTask` (PostgreSQL) |
| 2.6 | Finders: `findByUserId`, `findByTaskId` | `[x]` | Implementados em `TaskLocalService` com PreparedStatement |
| 2.7 | `buildService` (Service Builder) | `[~]` | Substituído por `TaskLocalService.java` (JDBC) + `sql/create_tables.sql` |
| 2.8 | Commit | `[ ]` | Pendente — Fase 10 |

---

## Fase 3 — Arquitetura MVC (Portlet Controller + Views)
> **Estimativa:** 1h30 - 2h | **Depende de:** Fase 2 | **Status: ✅ Concluída (95%)**

| # | Passo | Status | Resultado |
|---|-------|--------|-----------|
| 3.1 | Classe `TodoListPortlet` estendendo `MVCPortlet` | `[x]` | `@Component` OSGi DS, `instanceable=false` |
| 3.2 | Verificação de login | `[x]` | `themeDisplay.isSignedIn()` em todas as views e actions |
| 3.3 | Action `addTask` | `[x]` | Valida título, chama `TaskLocalService.addTask()` |
| 3.4 | Action `editTask` | `[x]` | Valida owner + título, atualiza task |
| 3.5 | Action `deleteTask` | `[x]` | Valida owner, deleta task + subtasks |
| 3.6 | Action `toggleComplete` | `[x]` | Valida owner, inverte `completed` |
| 3.7 | Action `addSubTask` | `[x]` | Valida task pai pertence ao user |
| 3.8 | Action `editSubTask` | `[x]` | Validação encadeada: subtask → task → user |
| 3.9 | Action `deleteSubTask` | `[x]` | Validação encadeada, deleta |
| 3.10 | Action `uploadImage` | `[x]` | Magic bytes, 5 MB limit, salva no Document Library |
| 3.11 | `view.jsp` (dashboard principal) | `[x]` | Contadores + lista + form nova task + subtarefas inline |
| 3.12 | `edit_task.jsp` | `[x]` | Edição de task, upload de imagem, gestão de subtarefas |
| 3.13 | `register.jsp` | `[x]` | Cadastro com AUI validators (email, minLength, required) |
| 3.14 | Portlet registrado no Liferay | `[x]` | Bundle STARTED, portlet visível na home (`/web/guest/home`) |
| 3.15 | Commit | `[ ]` | Pendente — Fase 10 |

---

## Fase 4 — Autenticação de Usuários
> **Estimativa:** 40min | **Depende de:** Fase 3 | **Status: ✅ Concluída (90%)**

| # | Passo | Status | Resultado |
|---|-------|--------|-----------|
| 4.1 | Action `registerUser` | `[x]` | `UserLocalServiceUtil.addUser()`, valida email único |
| 4.2 | Sign-In portlet nativo do Liferay | `[x]` | Padrão Liferay — login via `/c/portal/login` |
| 4.3 | Verificação `isSignedIn()` em todas as actions | `[x]` | Todas as actions retornam erro se não autenticado |
| 4.4 | Filtro `userId` em todas as queries | `[x]` | `WHERE userId=?` em todas as consultas de Task |
| 4.5 | Isolamento de dados entre usuários | `[x]` | Testado com 2 usuários independentes |
| 4.6 | Commit | `[ ]` | Pendente — Fase 10 |

---

## Fase 5 — CRUD Completo de Tarefas
> **Estimativa:** 1h - 1h30 | **Depende de:** Fase 4 | **Status: ✅ Concluída (95%)**

| # | Passo | Status | Resultado |
|---|-------|--------|-----------|
| 5.1 | Formulário nova task em `view.jsp` | `[x]` | Campos título + descrição, botão "Adicionar Tarefa" |
| 5.2 | Listagem com ordenação | `[x]` | Pendentes primeiro (`ORDER BY completed ASC, createdAt DESC`) |
| 5.3 | Toggle "Concluir/Reabrir" | `[x]` | Checkbox visual + action `toggleComplete` |
| 5.4 | Link "Editar" → `edit_task.jsp` | `[x]` | Formulário preenchido com dados da task |
| 5.5 | Botão "Excluir" com confirmação | `[x]` | `confirm()` JavaScript + action `deleteTask` |
| 5.6 | Feedback `SessionMessages` / `SessionErrors` | `[x]` | Mensagens `<liferay-ui:success>` e `<liferay-ui:error>` |
| 5.7 | CSS personalizado | `[x]` | `main.css` com design cards, variáveis CSS, responsivo |
| 5.8 | Commit | `[ ]` | Pendente — Fase 10 |

---

## Fase 6 — Funcionalidades Avançadas
> **Estimativa:** 2h - 3h | **Depende de:** Fase 5 | **Status: ✅ Concluída (95%)**

| # | Passo | Status | Resultado |
|---|-------|--------|-----------|
| 6.1 | Contadores no topo de `view.jsp` | `[x]` | Cards: Total / Pendentes / Concluídas com números coloridos |
| 6.2 | Lógica de contagem | `[x]` | Calculado inline na view a partir da lista já carregada |
| 6.3 | Upload de imagem em `edit_task.jsp` | `[x]` | `<aui:input type="file">`, enctype multipart |
| 6.4 | Action `uploadImage` | `[x]` | Document Library, tamanho máx 5 MB, validação de tipo |
| 6.5 | Thumbnail na lista e edição | `[x]` | `<img>` com URL `/documents/{groupId}/{folderId}/{title}` |
| 6.6 | Seção de subtarefas em cada task | `[x]` | Lista com checkbox + botão excluir |
| 6.7 | Formulário inline nova subtarefa | `[x]` | Campo texto + botão "+" em cada task card |
| 6.8 | Validações client-side (AUI) | `[x]` | `required`, `maxLength`, `email`, `minLength` |
| 6.9 | Validações server-side | `[x]` | Null/empty check, length, owner check em todas as actions |
| 6.10 | Commit | `[ ]` | Pendente — Fase 10 |

---

## Fase 7 — Segurança (OWASP Top 10 + CVEs + Injection)
> **Estimativa:** 2h - 3h | **Depende de:** Fase 6 | **Status: 🔶 Parcial (70%)**

| # | Passo | Status | Resultado |
|---|-------|--------|-----------|
| 7.1 | Verificação de owner em TODAS as actions | `[x]` | `task.getUserId() != td.getUserId()` em todas as actions |
| 7.2 | `HtmlUtil.escape()` nos outputs JSP | `[x]` | Aplicado em caminhos de URL de imagem e inputs inline |
| 7.3 | `<c:out value="..."/>` nos outputs de texto | `[x]` | Usado para títulos e descrições em `view.jsp` e `edit_task.jsp` |
| 7.4 | Forms com `<aui:form>` (CSRF) | `[x]` | Todas as forms principais usam `<aui:form>` com token automático |
| 7.5 | Validar magic bytes no upload | `[x]` | Verifica header JPEG (FF D8 FF), PNG (89 50 4E 47), GIF (47 49 46 38) |
| 7.6 | `portal-ext.properties` hardening | `[x]` | `session.timeout=120`, `dl.file.max.size=5242880`, password policy |
| 7.7 | Logging de auditoria | `[ ]` | Pendente — logs básicos de erro existem via `_log.error()` |
| 7.8 | Sanitizar inputs | `[x]` | `sanitize()` — strip controle chars + trim + limit length |
| 7.9 | Ausência de `Runtime.exec()` / SQL nativo sem parametrização | `[x]` | Zero ocorrências; apenas PreparedStatement |
| 7.10 | OWASP Dependency Check no `build.gradle` | `[ ]` | Pendente |
| 7.11 | Rodar `dependencyCheckAnalyze` | `[ ]` | Pendente (depende de 7.10) |
| 7.12 | Documentar headers HTTP de segurança | `[x]` | Documentado no README (nginx config: HSTS, X-Frame, CSP) |
| 7.13 | Commit | `[ ]` | Pendente — Fase 10 |

---

## Fase 8 — Testes
> **Estimativa:** 1h30 - 2h | **Depende de:** Fase 7 | **Status: ❌ Pendente (0%)**

| # | Passo | Status | Resultado Esperado |
|---|-------|--------|---------------------|
| 8.1 | Teste unitário para `TaskLocalService` | `[ ]` | Testa add, update, delete, finders |
| 8.2 | Teste unitário para subtarefas | `[ ]` | CRUD de SubTask |
| 8.3 | Teste de validação de formulário | `[ ]` | Campos vazios, muito longos, owner inválido |
| 8.4 | Teste de isolamento de dados | `[ ]` | Usuário A não vê/edita tarefas do usuário B |
| 8.5 | Teste de upload de imagem | `[ ]` | Rejeita não-imagens e arquivos > 5 MB |
| 8.6 | Teste de segurança (IDOR, CSRF) | `[ ]` | Actions sem token ou de outro usuário são rejeitadas |
| 8.7 | Teste em Chrome e Firefox | `[ ]` | Layout e funcionalidades consistentes |
| 8.8 | `gradlew test` — todos passando | `[ ]` | BUILD SUCCESSFUL, zero falhas |
| 8.9 | Commit | `[ ]` | Pendente — Fase 10 |

---

## Fase 9 — Documentação (README.md)
> **Estimativa:** 40min | **Depende de:** Fase 7 | **Status: ✅ Concluída (100%)**

| # | Passo | Status | Resultado |
|---|-------|--------|-----------|
| 9.1 | Seção "Pré-requisitos" | `[x]` | JDK 21, Docker, Gradle 8.x, PostgreSQL |
| 9.2 | Seção "Instalação e Configuração" | `[x]` | Passo a passo completo |
| 9.3 | Seção "Build e Deploy" | `[x]` | Comandos Gradle + hot-deploy |
| 9.4 | Seção "Funcionalidades Implementadas" | `[x]` | Checklist completo |
| 9.5 | Seção "Decisões Técnicas" | `[x]` | JDBC vs Service Builder, OSGi DS, etc. |
| 9.6 | Seção "Segurança" | `[x]` | OWASP Top 10, proteções implementadas |
| 9.7 | Seção "Estrutura do Projeto" | `[x]` | Árvore de diretórios anotada |
| 9.8 | Seção "Testes" | `[x]` | Como rodar, status atual |
| 9.9 | Commit | `[ ]` | Pendente — Fase 10 |

---

## Fase 10 — Controle de Versão e Submissão
> **Estimativa:** 30min | **Depende de:** Fase 9 | **Status: ❌ Pendente (0%)**

| # | Passo | Status | Resultado Esperado |
|---|-------|--------|---------------------|
| 10.1 | Revisar histórico de commits | `[ ]` | Commits descritivos com conventional commits |
| 10.2 | Criar `.gitignore` | `[ ]` | `build/`, `.gradle/`, `*.jar` (exceto libs), `*.tar.gz` fora do repo |
| 10.3 | Branch `release` com código final | `[ ]` | Branch de entrega |
| 10.4 | Verificar ausência de credenciais | `[ ]` | Zero passwords/tokens nos sources (só em `portal-ext.properties`) |
| 10.5 | Commit: limpeza final | `[ ]` | — |
| 10.6 | Criar repositório privado no GitHub | `[ ]` | Repo criado |
| 10.7 | Adicionar `seatecnologia@seatecnologia.com.br` | `[ ]` | Colaborador convidado |
| 10.8 | Push final e envio do link por email | `[ ]` | Desafio submetido |

---

## Progresso Geral

```
Fase 1  [█████████░]  90%  (Setup)
Fase 2  [████████░░]  85%  (Modelo de Dados — JDBC em vez de Service Builder)
Fase 3  [█████████░]  95%  (MVC — todas as actions e views implementadas)
Fase 4  [█████████░]  90%  (Autenticação nativa Liferay)
Fase 5  [█████████░]  95%  (CRUD completo com feedback)
Fase 6  [█████████░]  95%  (Contadores, imagens, subtarefas, validações)
Fase 7  [███████░░░]  70%  (Segurança — faltam OWASP DepCheck + audit log)
Fase 8  [░░░░░░░░░░]   0%  (Testes — pendente)
Fase 9  [██████████] 100%  (Documentação — README gerado)
Fase 10 [░░░░░░░░░░]   0%  (Git + Submissão — pendente)
=========================================
TOTAL   [████████░░]  78%
```

---

## Análise de Conformidade com o Enunciado

| Requisito | Status | Observação |
|-----------|--------|------------|
| Cadastro de usuários | ✅ | `register.jsp` + `UserLocalServiceUtil.addUser()` |
| Login individual | ✅ | Login nativo Liferay (`/c/portal/login`) |
| Isolamento por usuário | ✅ | `WHERE userId=?` em todas as queries |
| Adicionar tarefas | ✅ | Action `addTask` com validação |
| Listar tarefas | ✅ | `view.jsp` — lista com ordenação |
| Editar tarefas | ✅ | `edit_task.jsp` + action `editTask` |
| Excluir tarefas | ✅ | Action `deleteTask` com confirmação |
| Marcar concluída/pendente | ✅ | Action `toggleComplete` |
| Contadores por usuário | ✅ | Cards: Total / Pendentes / Concluídas |
| Imagens nas tarefas | ✅ | Upload via Document Library, thumbnail na lista |
| Subtarefas | ✅ | CRUD completo de SubTask |
| Validação de formulários | ✅ | AUI validators (client) + sanitize/checks (server) |
| Arquitetura MVC | ✅ | `MVCPortlet` + JSP views + `TaskLocalService` |
| Service Builder | ⚠️ | Substituído por JDBC direto (decisão técnica documentada) |
| Segurança OWASP | 🔶 | XSS, CSRF, injeção SQL, owner check, magic bytes — falta DepCheck |
| Testes | ❌ | Não implementados |
| Documentação README | ✅ | Gerado nesta fase |
| Controle de versão (Git) | 🔶 | Repo inicializado, commits pendentes |

---

> **Próximo passo:** Fase 8 — Testes unitários e de integração
> **Última atualização:** 2026-05-05
