package br.edu.poc.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Página única do cliente. O login OIDC (state, nonce, PKCE e validação do ID Token)
 * é feito pela autoconfiguração do Spring Boot: qualquer acesso sem sessão vai para o login.
 */
@RestController
public class HomeController {

    private final HttpClient http = HttpClient.newHttpClient();

    @GetMapping("/")
    Map<String, Object> inicio(@AuthenticationPrincipal OidcUser usuario,
                               @RegisteredOAuth2AuthorizedClient("poc-client") OAuth2AuthorizedClient cliente)
            throws Exception {
        String accessToken = cliente.getAccessToken().getTokenValue();
        String idToken = usuario.getIdToken().getTokenValue();

        Map<String, Object> chamadas = new LinkedHashMap<>();
        chamadas.put("GET /api/me (access token)", chamar("/api/me", accessToken));
        chamadas.put("GET /api/relatorios (access token)", chamar("/api/relatorios", accessToken));
        chamadas.put("GET /api/admin (access token)", chamar("/api/admin", accessToken));
        chamadas.put("GET /api/me (sem token)", chamar("/api/me", null));
        chamadas.put("GET /api/me (ID Token, uso indevido)", chamar("/api/me", idToken));

        Map<String, Object> pagina = new LinkedHashMap<>();
        pagina.put("1_id_token", usuario.getIdToken().getClaims());       // para o cliente: quem é o usuário
        pagina.put("2_access_token_escopos", cliente.getAccessToken().getScopes()); // para a API
        pagina.put("3_chamadas_a_api", chamadas);
        return pagina;
    }

    private String chamar(String caminho, String token) throws Exception {
        HttpRequest.Builder req = HttpRequest.newBuilder(URI.create("http://localhost:8081" + caminho));
        if (token != null) {
            req.header("Authorization", "Bearer " + token); // RFC 6750
        }
        HttpResponse<String> resposta = http.send(req.build(), HttpResponse.BodyHandlers.ofString());
        return resposta.statusCode() + " " + resposta.body();
    }
}
