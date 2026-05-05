# Todo List Portlet — Liferay 7.4

Portlet "To-do-list" desenvolvido para a plataforma Liferay 7.4 CE como parte do desafio técnico SEA Tecnologia.

**Demo:** [https://desafio2.alje.app](https://desafio2.alje.app)

---

## Funcionalidades Implementadas

### Autenticação e Usuários
- Cadastro de novos usuários via formulário próprio (`register.jsp`) usando `UserLocalServiceUtil.addUser()`
- Login pelo portlet nativo do Liferay (email + senha)
- Cada usuário acessa **apenas suas próprias tarefas** — isolamento garantido por `WHERE userId=?` em todas as consultas

### Gerenciamento de Tarefas
- **Criar** tarefas com título (obrigatório, máx. 200 chars) e descrição (opcional, máx. 2.000 chars)
- **Listar** tarefas ordenadas: pendentes primeiro, depois concluídas; dentro de cada grupo, mais recentes primeiro
- **Editar** título e descrição de qualquer tarefa própria
- **Excluir** tarefa (com confirmação) — remove também todas as subtarefas automaticamente
- **Toggle concluída/pendente** com feedback visual (checkbox + estilo riscado)

### Funcionalidades Avançadas
- **Contadores** no topo: Total / Pendentes / Concluídas — atualizados em tempo real
- **Upload de imagem** por tarefa (JPEG, PNG ou GIF, máx. 5 MB), armazenada no Document Library do Liferay; thumbnail exibida na lista e na edição
- **Subtarefas**: criar, editar inline, marcar concluída e excluir — cada tarefa pode ter N subtarefas
- **Validações** client-side (AUI validators: required, maxLength, email, minLength) e server-side (sanitização, verificação de owner)
- **Mensagens de feedback** localizadas em português para todas as ações (sucesso e erro)

---

## Pré-requisitos

| Componente | Versão mínima |
|-----------|---------------|
| Docker Engine | 24+ |
| Docker Compose | v2+ |
| Java (JDK) | 21 (fornecido pelo container) |
| Gradle | 8.x (via `gradlew` no projeto) |
| PostgreSQL | 14+ (container externo `alje-postgres-1`) |

> Para desenvolvimento local sem Docker, é necessário JDK 21 instalado e PostgreSQL acessível.

---

## Instalação e Configuração

### 1. Clonar o repositório

```bash
git clone <url-do-repositorio> /var/www/desafio2
cd /var/www/desafio2
```

### 2. Configurar o banco de dados

O banco PostgreSQL `desafio2` deve existir antes de iniciar o Liferay.
Execute o schema inicial (só na primeira vez):

```bash
psql -h <host> -U alje -d desafio2 -f sql/create_tables.sql
```

As tabelas criadas são:
- `Todo_Task` — tarefas (taskId, userId, title, description, completed, imageFileEntryId, createdAt, modifiedAt)
- `Todo_SubTask` — subtarefas (subTaskId, taskId FK, title, completed, createdAt)

### 3. Revisar `liferay/portal-ext.properties`

```properties
# Banco de dados
jdbc.default.url=jdbc:postgresql://<host>:5432/desafio2
jdbc.default.username=alje
jdbc.default.password=alje

# Admin padrão criado na inicialização
default.admin.email.address=test@desafio2.alje.app
default.admin.password=admin123
```

Ajuste o host e credenciais conforme seu ambiente.

### 4. Subir o container Liferay

```bash
docker compose up -d
```

O Liferay demora entre 3 e 5 minutos para inicializar completamente. Acompanhe com:

```bash
docker logs -f desafio2-liferay | grep -E "STARTED|ERROR|startup"
```

Quando aparecer `Server startup in [X] milliseconds`, o portal está pronto.

### 5. Acessar o portal

- URL: `http://localhost:8080` (ou `https://desafio2.alje.app` se com nginx/SSL)
- Admin: `test@desafio2.alje.app` / `admin123`

---

## Build e Deploy

### Compilar o portlet

```bash
./gradlew :modules:todo-list-web:jar
```

O JAR gerado fica em:
```
modules/todo-list-web/build/libs/com.seatecnologia.todo.web.jar
```

### Deploy (hot-deploy)

Copie o JAR para a pasta de deploy do Liferay em execução:

```bash
docker cp modules/todo-list-web/build/libs/com.seatecnologia.todo.web.jar \
    desafio2-liferay:/opt/liferay/deploy/
```

O Liferay detecta o arquivo automaticamente e realiza o deploy em ~15 segundos.
Confirme no log:

```
STARTED com.seatecnologia.todo.web_1.0.0 [1389]
```

### Build e deploy em um comando

```bash
./gradlew :modules:todo-list-web:jar && \
docker cp modules/todo-list-web/build/libs/com.seatecnologia.todo.web.jar \
    desafio2-liferay:/opt/liferay/deploy/
```

---

## Estrutura do Projeto

```
desafio2/
├── Dockerfile                          # Container Liferay (JDK 21 + Tomcat)
├── docker-compose.yml                  # Serviço desafio2-liferay
├── build.gradle                        # Root build (BND Gradle plugin)
├── settings.gradle                     # Inclui módulo todo-list-web
├── gradle.properties                   # liferay.workspace.home.dir
├── gradlew / gradlew.bat               # Gradle Wrapper 8.13
│
├── liferay/                            # Instalação Liferay 7.4 GA132
│   ├── portal-ext.properties           # Configs: DB, server, segurança
│   ├── tomcat/                         # Tomcat 9.x com Liferay
│   └── deploy/                         # Pasta de hot-deploy
│
├── sql/
│   └── create_tables.sql               # Schema: Todo_Task + Todo_SubTask
│
└── modules/
    └── todo-list-web/
        ├── build.gradle                # Dependências OSGi + BND
        ├── bnd.bnd                     # Bundle-SymbolicName, Import-Package
        └── src/main/
            ├── java/com/seatecnologia/todo/
            │   ├── model/
            │   │   ├── Task.java       # POJO da entidade Task
            │   │   └── SubTask.java    # POJO da entidade SubTask
            │   ├── service/
            │   │   └── TaskLocalService.java  # Camada de dados (JDBC)
            │   └── web/portlet/
            │       └── TodoListPortlet.java   # Controller MVCPortlet
            └── resources/
                ├── content/
                │   └── Language.properties   # Mensagens pt_BR
                └── META-INF/resources/
                    ├── init.jsp        # Taglibs + imports compartilhados
                    ├── view.jsp        # Dashboard: lista + form + contadores
                    ├── edit_task.jsp   # Edição + imagem + subtarefas
                    ├── register.jsp    # Cadastro de usuário
                    └── css/
                        └── main.css   # Estilos personalizados
```

---

## Arquitetura e Decisões Técnicas

### Padrão MVC com MVCPortlet

O portlet segue o padrão MVC nativo do Liferay:

| Camada | Arquivo | Responsabilidade |
|--------|---------|------------------|
| Controller | `TodoListPortlet.java` | Recebe ActionRequests, valida, chama serviço, redireciona |
| Model | `Task.java`, `SubTask.java`, `TaskLocalService.java` | Entidades e acesso ao banco |
| View | `view.jsp`, `edit_task.jsp`, `register.jsp` | Renderização HTML via JSP + taglibs Liferay |

### JDBC direto em vez de Service Builder

O desafio recomenda o uso de Service Builder. Optamos por **JDBC direto com POJO** pelos seguintes motivos:

1. **Bundle único**: Service Builder gera múltiplos módulos (`-api`, `-service`), aumentando a complexidade de deploy e dependências OSGi. O JDBC permite manter tudo em um único bundle.
2. **Controle total**: Queries otimizadas sem abstração de ORM; PreparedStatements previnem SQL Injection nativamente.
3. **Compatibilidade OSGi**: O driver PostgreSQL está no classloader do Tomcat (não é um bundle OSGi), exigindo `Import-Package: !org.postgresql.*, *` no `bnd.bnd` para excluí-lo do manifesto OSGi.

O `TaskLocalService.java` implementa as mesmas operações que o Service Builder geraria: `addTask`, `getTask`, `updateTask`, `deleteTask`, `toggleTaskCompleted`, `getTasksByUserId`, e equivalentes para SubTask.

### OSGi Declarative Services (DS)

O portlet é registrado como componente OSGi via `@Component` com DS annotations **v1.4.0** (compatível com Felix SCR 2.1.x do Liferay 7.4). A versão 1.5.x não é suportada pelo container OSGi desta versão do Liferay.

```java
@Component(
    immediate = true,
    property = {
        "com.liferay.portlet.instanceable=false",
        "javax.portlet.name=com_seatecnologia_todo_web_portlet_TodoListPortlet",
        ...
    },
    service = Portlet.class
)
```

### Autenticação

Utilizamos a **API nativa de usuários do Liferay** (`UserLocalServiceUtil.addUser()`), integrando o cadastro com o sistema de login padrão do portal. Isso elimina a necessidade de gerenciar senhas, sessions ou tokens — tudo é tratado pelo Liferay.

### Armazenamento de Imagens

Imagens são salvas no **Document Library do Liferay** via `DLAppLocalServiceUtil.addFileEntry()`. A referência à imagem é armazenada como `imageFileEntryId` na tabela `Todo_Task`. A URL pública é construída como:
```
/documents/{groupId}/{folderId}/{fileName}
```

---

## Segurança

### Proteções implementadas (OWASP Top 10)

| Vulnerabilidade | Proteção implementada |
|----------------|----------------------|
| **A01 — Broken Access Control** | Verificação `task.getUserId() != currentUserId` em **todas** as actions |
| **A03 — Injection (SQL)** | 100% PreparedStatements; zero SQL dinâmico com concatenação |
| **A03 — Injection (XSS)** | `HtmlUtil.escape()` em outputs dinâmicos; `<c:out value="..."/>` em JSPs |
| **A05 — Security Misconfiguration** | `portal-ext.properties` com session timeout, password policy, upload limits |
| **A07 — Auth Failures** | Login via Liferay nativo; todas as actions verificam `isSignedIn()` |
| **A08 — Integrity Failures (upload)** | Verificação de magic bytes (JPEG/PNG/GIF) antes de salvar |
| **CSRF** | `<aui:form>` inclui token `p_auth` automaticamente em todas as submissões |

### Sanitização de inputs

```java
private String sanitize(String input, int maxLength) {
    if (input == null) return "";
    // Remove caracteres de controle (exceto \r\n\t)
    input = input.replaceAll("[\\p{Cntrl}&&[^\\r\\n\\t]]", "").trim();
    return input.length() > maxLength ? input.substring(0, maxLength) : input;
}
```

### Headers de segurança (nginx)

O nginx configurado na infraestrutura inclui:
- `Strict-Transport-Security: max-age=31536000; includeSubDomains`
- `X-Frame-Options: SAMEORIGIN`
- `X-Content-Type-Options: nosniff`
- `Content-Security-Policy: default-src 'self' ...`

---

## Testes

> **Status atual:** Testes unitários e de integração estão pendentes (Fase 8 do ROADMAP).

Para rodar os testes quando implementados:

```bash
./gradlew :modules:todo-list-web:test
```

### Cobertura planejada

- Testes unitários para `TaskLocalService` (add, update, delete, finders)
- Testes de isolamento de dados entre usuários
- Testes de validação de upload (rejeição de não-imagens e arquivos > 5 MB)
- Testes de segurança (tentativa de IDOR — acesso a task de outro usuário)

---

## Variáveis de Ambiente

| Variável | Padrão | Descrição |
|----------|--------|-----------|
| `JAVA_HOME` | `/opt/java/openjdk` | JDK 21 no container |
| `JAVA_OPTS` | `-Xms2048m -Xmx4096m` | Heap JVM |
| `LIFERAY_HOME` | `/opt/liferay` | Raiz da instalação Liferay |

---

## Endpoints e URLs

| URL | Descrição |
|-----|-----------|
| `/web/guest/home` | Página principal com o portlet Todo List |
| `/c/portal/login` | Login nativo do Liferay |
| `/web/guest/home?p_p_id=...&_...mvcPath=/register.jsp` | Tela de cadastro |
| `/api/jsonws/portal/get-build-number` | Health check do portal (retorna `7403`) |

---

## Licença

Projeto desenvolvido exclusivamente para avaliação técnica — SEA Tecnologia.
