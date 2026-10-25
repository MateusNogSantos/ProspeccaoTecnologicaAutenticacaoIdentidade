package br.edu.poc.resourceserver;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiController {

    @GetMapping("/publico")
    Map<String, Object> publico() {
        return Map.of("mensagem", "Endpoint público: não exige token.");
    }

    /** Qualquer access token válido: devolve as claims que a API recebeu. */
    @GetMapping("/me")
    Map<String, Object> me(@AuthenticationPrincipal Jwt jwt) {
        return Map.of("sub", jwt.getSubject(), "aud", jwt.getAudience(), "scope", jwt.getClaimAsString("scope"));
    }

    /** Exige o escopo api.read. */
    @GetMapping("/relatorios")
    List<String> relatorios() {
        return List.of("Vendas 3T/2026", "Acessos por região");
    }

    /** Exige o escopo admin, que nenhum cliente da PoC recebe: resposta esperada 403. */
    @GetMapping("/admin")
    Map<String, Object> admin() {
        return Map.of("mensagem", "Área administrativa.");
    }
}
