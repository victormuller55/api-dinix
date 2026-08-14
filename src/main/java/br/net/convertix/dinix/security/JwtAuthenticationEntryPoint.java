package br.net.convertix.dinix.security;

import br.net.convertix.dinix.exception.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        ApiErrorResponse body = new ApiErrorResponse(
                401, "nao_autorizado", "Autenticação necessária", request.getRequestURI(), null);
        String json = """
                {"data_hora":"%s","status":401,"erro":"nao_autorizado","mensagem":"Autenticação necessária","caminho":"%s","erros_campos":null}
                """.formatted(
                body.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                request.getRequestURI() == null ? "" : request.getRequestURI().replace("\"", ""));
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(json);
    }
}
