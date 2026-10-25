package br.edu.poc.resourceserver;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Quem pode chamar cada endpoint. A validação do JWT (assinatura pelo JWKS, iss, exp e aud)
 * vem do application.yml. Cada escopo do token vira uma autoridade SCOPE_<escopo>.
 */
@Configuration(proxyBeanMethods = false)
public class ResourceServerSecurityConfig {

    @Bean
    SecurityFilterChain api(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/publico").permitAll()
                .requestMatchers("/api/relatorios").hasAuthority("SCOPE_api.read")
                .requestMatchers("/api/admin").hasAuthority("SCOPE_admin") // ninguém tem
                .anyRequest().authenticated())
            .oauth2ResourceServer(rs -> rs.jwt(Customizer.withDefaults()));
        return http.build();
    }
}
