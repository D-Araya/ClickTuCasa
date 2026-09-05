package com.clicktucasa.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Single source of truth for Cross-Origin Resource Sharing.
 *
 * <p>The browser enforces the Same-Origin Policy: a page served from
 * {@code http://localhost:5173} may not read a response from
 * {@code http://localhost:8080}, because a different port already makes it
 * a different origin. The request still reaches this service and this
 * service still answers — it is the browser that discards the response
 * unless the server explicitly authorises the origin through these
 * headers. CORS is therefore a grant made by the server, not a permission
 * requested by the client.
 *
 * <p>Configured here as one central {@code WebMvcConfigurer} rather than
 * with {@code @CrossOrigin} on each controller, so that a new controller
 * cannot silently ship without it and so the web layer stays free of
 * infrastructure concerns. The two mechanisms are deliberately
 * <strong>not</strong> combined: duplicate CORS configuration produces
 * behaviour that is painful to debug.
 *
 * <p>{@code OPTIONS} is listed explicitly because the browser sends a
 * preflight request before any non-simple call — without it the
 * {@code GET} works and every {@code POST} fails.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * Comma-separated list of origins allowed to read this API's
     * responses. Overridable per environment through
     * {@code CORS_ALLOWED_ORIGINS}; the default covers the Vite dev server.
     * Origins must be written exactly — no trailing slash, scheme included.
     */
    @Value("${cors.allowed-origins:http://localhost:5173,http://localhost:4173,http://localhost:3000}")
    private String[] allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}
