package br.com.estudos.apiinteligente.infra.audit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RequestAuditFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestAuditFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long inicio = System.nanoTime();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long duracaoMs = (System.nanoTime() - inicio) / 1_000_000;
            // Não registra corpo, áudio, prompts ou dados financeiros nos logs.
            log.info("HTTP {} {} -> {} ({} ms)", request.getMethod(),
                    request.getRequestURI(), response.getStatus(), duracaoMs);
        }
    }
}
