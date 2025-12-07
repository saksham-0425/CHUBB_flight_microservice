package com.quiz.api_gateway.security;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.*;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter 
    extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    @Value("${jwt.secret}")
    private String secret;

    public static class Config {}

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {

        return (exchange, chain) -> {

            if (isAuthRoute(exchange))
                return chain.filter(exchange);

            String token = getAuthHeader(exchange);

            if (token == null)
                return onError(exchange, "Missing Authorization Header", HttpStatus.UNAUTHORIZED);

            try {
                Jwts.parser()
                    .setSigningKey(secret.getBytes())
                    .parseClaimsJws(token);
            } catch(Exception e) {
                return onError(exchange, "Invalid Token", HttpStatus.UNAUTHORIZED);
            }

            return chain.filter(exchange);
        };
    }

    private boolean isAuthRoute(ServerWebExchange exchange) {
        return exchange.getRequest().getURI().getPath().contains("/auth/");
    }

    private String getAuthHeader(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest()
                .getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer "))
            return authHeader.substring(7);

        return null;
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }
}
