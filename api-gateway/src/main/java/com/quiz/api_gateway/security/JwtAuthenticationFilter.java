package com.quiz.api_gateway.security;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
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

       
            if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
                return chain.filter(exchange);
            }

          
            if (isPublicRoute(exchange)) {
                return chain.filter(exchange);
            }

            String token = getAuthHeader(exchange);

            if (token == null) {
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            try {
                Jwts.parser()
                    .setSigningKey(secret.getBytes())
                    .parseClaimsJws(token);
            } catch (Exception e) {
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            return chain.filter(exchange);
        };
    }


    private String getAuthHeader(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status) {

       
        exchange.getResponse().getHeaders().add(
                "Access-Control-Allow-Origin", "http://localhost:4200");
        exchange.getResponse().getHeaders().add(
                "Access-Control-Allow-Headers", "Authorization, Content-Type");
        exchange.getResponse().getHeaders().add(
                "Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");

        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }
    
    private boolean isPublicRoute(ServerWebExchange exchange) {
        String path = exchange.getRequest().getURI().getPath();

        return path.startsWith("/auth/")
            || path.startsWith("/flights/search");
    }

}
