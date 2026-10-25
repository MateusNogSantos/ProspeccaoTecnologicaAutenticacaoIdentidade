#!/usr/bin/env bash
# Testes da API sem navegador (cliente de serviço poc-service, Client Credentials).
# Pré-requisito: auth-server (9000) e resource-server (8081) no ar.
# Uso: ./scripts/testar-api.sh
set -uo pipefail
AS=http://localhost:9000
API=http://localhost:8081
passo() { printf '\n\033[1;34m== %s\033[0m\n' "$1"; }
status() { curl -s -o /dev/null -w '%{http_code}' "$@"; }

passo "1. Descoberta OIDC: o emissor publica seus endpoints e o jwks_uri"
curl -s "$AS/.well-known/openid-configuration"; echo

passo "2. Pedir um access token (Client Credentials)"
RESPOSTA=$(curl -s -u poc-service:service-secret -d grant_type=client_credentials -d scope=api.read "$AS/oauth2/token")
echo "$RESPOSTA"
TOKEN=$(echo "$RESPOSTA" | sed -E 's/.*"access_token":"([^"]+)".*/\1/')

passo "3. Ler o payload do token (Base64url não é cifragem)"
P=$(echo -n "$TOKEN" | cut -d. -f2 | tr '_-' '/+'); while [ $(( ${#P} % 4 )) -ne 0 ]; do P="$P="; done
echo "$P" | base64 -d; echo

passo "4. API com token e escopo api.read -> esperado 200"
echo "HTTP $(status -H "Authorization: Bearer $TOKEN" "$API/api/relatorios")"

passo "5. API sem token -> esperado 401"
echo "HTTP $(status "$API/api/relatorios")"

passo "6. Escopo insuficiente em /api/admin -> esperado 403"
echo "HTTP $(status -H "Authorization: Bearer $TOKEN" "$API/api/admin")"

passo "7. Assinatura adulterada (um caractere do meio trocado) -> esperado 401"
SIG=${TOKEN##*.}; INICIO=${TOKEN%.*}
C=${SIG:10:1}; if [ "$C" = "A" ]; then T=B; else T=A; fi
echo "HTTP $(status -H "Authorization: Bearer $INICIO.${SIG:0:10}$T${SIG:11}" "$API/api/relatorios")"

passo "8. Revogar o token e perguntar ao emissor (introspecção) -> esperado active=false"
curl -s -o /dev/null -u poc-service:service-secret -d "token=$TOKEN" "$AS/oauth2/revoke"
curl -s -u poc-service:service-secret -d "token=$TOKEN" "$AS/oauth2/introspect"; echo

passo "9. A API ainda aceita o token revogado? -> esperado 200 (valida o JWT localmente até exp)"
echo "HTTP $(status -H "Authorization: Bearer $TOKEN" "$API/api/relatorios")"
