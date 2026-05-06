# Relatorio de Seguranca — To-do-list Portlet

## OWASP Dependency Check

**Status:** Configurado no `build.gradle`, mas analise nao executada nesta sessao.
**Motivo:** Ambiente offline / sem cache NVD. O plugin requer download da base NVD (National Vulnerability Database) que nao esta disponivel neste ambiente.

**Configuracao aplicada:**
- Plugin: `org.owasp.dependency-check-gradle:8.4.3`
- Supressoes: `dependency-check-suppressions.xml` (falsos-positivos do Liferay)
- Fail build: desabilitado (`failBuildOnCVSS = 11.0`)
- Analisadores desabilitados: NodeAudit, NodePackage, RetireJS (sem Node.js)

**Como rodar em ambiente com internet:**
```bash
./gradlew dependencyCheckAnalyze
# Relatorio gerado em: build/reports/dependency-check/dependency-check-report.html
```

**Como rodar offline (com cache NVD existente):**
```bash
# Copiar cache NVD para ~/.gradle/dependency-check-data/
./gradlew dependencyCheckAnalyze --noupdate
```

---

## Dependencias do Projeto

| Dependencia | Tipo | Risco | Nota |
|-------------|------|-------|------|
| Liferay Portal Kernel | compileOnly | Baixo | Fornecido pelo container, nao empacotado |
| PostgreSQL Driver | runtime | Baixo | Via pool DataAccess do Tomcat (shielded-container) |
| OSGi Annotations | compileOnly | Nenhum | APACHE-2.0, apenas build-time |
| Servlet API | compileOnly | Baixo | Fornecido pelo Tomcat |

---

## Medidas de Seguranca Implementadas

| Controle | Implementacao |
|----------|---------------|
| SQL Injection | PreparedStatement em todas as queries |
| XSS | `HtmlUtil.escape()`, `<c:out>`, sanitizacao de inputs |
| CSRF | `<aui:form>` com token automatico em todas as forms |
| Broken Access Control | Verificacao `task.getUserId() != currentUserId` em todas as actions |
| File Upload | Magic bytes, 5MB limit, mime-type check |
| Headers | HSTS, X-Frame-Options, CSP (via nginx) |

---

**Data:** 2026-05-06
