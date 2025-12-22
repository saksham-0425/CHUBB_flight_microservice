package com.quiz.api_gateway.security;

import io.jsonwebtoken.Claims;
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

import java.util.List;

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

            // Allow CORS preflight
            if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
                return chain.filter(exchange);
            }

            // Public routes
            if (isPublicRoute(exchange)) {
                return chain.filter(exchange);
            }

            String token = getAuthHeader(exchange);

            if (token == null) {
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            try {
            	Claims claims = Jwts.parser()
            		    .setSigningKey(secret.getBytes())
            		    .parseClaimsJws(token)
            		    .getBody();

            		String email = claims.getSubject();
            		List<String> roles = claims.get("roles", List.class);

            		// forward headers
            		ServerWebExchange mutatedExchange = exchange.mutate()
            		    .request(
            		        exchange.getRequest().mutate()
            		            .header("X-User-Email", email)
            		            .header("X-User-Roles", String.join(",", roles))
            		            .build()
            		    )
            		    .build();

            		return chain.filter(mutatedExchange);

            } catch (Exception e) {
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }
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
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }

    private boolean isPublicRoute(ServerWebExchange exchange) {
        String path = exchange.getRequest().getURI().getPath();

        return path.equals("/auth/login")
            || path.equals("/auth/register")
            || path.startsWith("/flights/search");
    }

}
