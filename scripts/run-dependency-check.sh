#!/bin/bash
# OWASP Dependency Check — execucao manual via CLI (jar standalone)
# Este script baixa o OWASP Dependency Check CLI e executa a analise
# Usa o cache local do NVD para evitar downloads repetidos

DC_VERSION="8.4.3"
DC_ZIP="dependency-check-${DC_VERSION}-release.zip"
DC_DIR="/opt/dependency-check"

echo "=== OWASP Dependency Check Manual ==="
echo "Versao: ${DC_VERSION}"
echo ""

# Verificar se ja esta instalado
if [ ! -d "${DC_DIR}/dependency-check/bin" ]; then
    echo "Baixando OWASP Dependency Check CLI..."
    mkdir -p /tmp/owasp-dc
    cd /tmp/owasp-dc
    curl -sL "https://github.com/jeremylong/DependencyCheck/releases/download/v${DC_VERSION}/${DC_ZIP}" -o dc.zip
    unzip -q dc.zip || { echo "Falha ao extrair. Verifique conectividade com GitHub."; exit 1; }
    mkdir -p "${DC_DIR}"
    mv dependency-check "${DC_DIR}/"
    rm -rf /tmp/owasp-dc
    echo "Instalado em ${DC_DIR}"
fi

# Executar a analise
PROJECT_DIR="/var/www/desafio2"
OUTPUT_DIR="${PROJECT_DIR}/build/reports/dependency-check"
mkdir -p "${OUTPUT_DIR}"

# Suprimir analisadores que exigem internet ou Node.js
"${DC_DIR}/dependency-check/bin/dependency-check.sh" \
    --project "desafio2-todo-list" \
    --scan "${PROJECT_DIR}/modules/todo-list-web/build/libs/*.jar" \
    --suppression "${PROJECT_DIR}/dependency-check-suppressions.xml" \
    --format HTML \
    --format JSON \
    --out "${OUTPUT_DIR}" \
    --enableExperimental \
    --noupdate \
    2>&1

echo ""
echo "=== Analise Concluida ==="
echo "Relatorio HTML: ${OUTPUT_DIR}/dependency-check-report.html"
echo "Relatorio JSON: ${OUTPUT_DIR}/dependency-check-report.json"
