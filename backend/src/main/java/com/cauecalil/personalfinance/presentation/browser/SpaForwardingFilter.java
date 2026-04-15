package com.cauecalil.personalfinance.presentation.browser;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
@Order(Ordered.LOWEST_PRECEDENCE - 10)
public class SpaForwardingFilter extends OncePerRequestFilter {
    private static final Set<String> EXCLUDED_PREFIXES = Set.of(
            "/api",
            "/v3/api-docs",
            "/swagger-ui",
            "/actuator"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String method = request.getMethod();
        if (!"GET".equals(method) && !"HEAD".equals(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        if (isExcluded(path) || hasFileExtension(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        request.getRequestDispatcher("/index.html").forward(request, response);
    }

    private boolean isExcluded(String path) {
        return EXCLUDED_PREFIXES.stream().anyMatch(prefix -> path.equals(prefix) || path.startsWith(prefix + "/"));
    }

    private boolean hasFileExtension(String path) {
        int lastSlash = path.lastIndexOf('/');
        int lastDot = path.lastIndexOf('.');
        return lastDot > lastSlash;
    }
}
