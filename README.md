# PoC — OAuth 2.0, OpenID Connect e JWT

Três aplicações Spring Boot pequenas e um teste de JWT em um único arquivo.

| Pasta | Porta | Papel | O que tem de código |
|---|---|---|---|
| `auth-server` | 9000 | Servidor de Autorização / OpenID Provider | `application.yml` + 1 bean (audiência do token) |
| `resource-server` | 8081 | API que valida o JWT | `application.yml` + regras de acesso + 4 endpoints |
| `client-app` | 8080 | Cliente web / Relying Party | `application.yml` + 1 página em JSON |
| `teste-jwt` | — | Criptografia e ataques a JWT | `JwtLab.java` (só JDK) |
| `scripts` | — | Testes da API por `curl` | `testar-api.sh` |

Usuário: `alice` / `alice123`. Clientes: `poc-client` / `poc-secret` e `poc-service` / `service-secret`.

## Requisitos

JDK 21, Maven 3.9+ (com acesso ao Maven Central), `curl` e `bash` (no Windows: Git Bash ou WSL).

## Executar

```bash
mvn -q -DskipTests package                # uma vez: baixa as dependências
mvn -pl auth-server spring-boot:run       # terminal 1 (sempre primeiro)
mvn -pl resource-server spring-boot:run   # terminal 2
mvn -pl client-app spring-boot:run        # terminal 3
```

1. Abra **http://127.0.0.1:8080** em janela anônima (127.0.0.1, não localhost), entre com `alice` e aprove os escopos.
   A página mostra o ID Token, os escopos do access token e as chamadas à API:
   `/api/me` e `/api/relatorios` → 200, `/api/admin` → 403, sem token → 401, ID Token como Bearer → 401.
2. Em outro terminal: `./scripts/testar-api.sh` (token por Client Credentials, 401, 403, assinatura adulterada, revogação).
3. Teste de JWT, sem servidor nem internet: `cd teste-jwt && java JwtLab.java`
   (referência em `teste-jwt/saida-de-referencia.txt`).

## Limitações propositais

Tudo em memória (reiniciar o `auth-server` gera nova chave e invalida os tokens), HTTP só em loopback,
segredos em texto e tokens do tipo *bearer*. O `JwtLab` é didático: em produção use uma biblioteca mantida.
