package br.net.convertix.dinix.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        SecurityScheme bearer = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("autorizacao")
                .description("Informe: Bearer {token}");
        return new OpenAPI()
                .info(new Info()
                        .title("Dinix API")
                        .description("API de controle financeiro pessoal. Rotas em /api/v1, campos em snake_case português. Header de autenticação: autorizacao.")
                        .version("1.0.0"))
                .addServersItem(new Server().url("/").description("Servidor local"))
                .components(new Components().addSecuritySchemes("autorizacao", bearer))
                .addSecurityItem(new SecurityRequirement().addList("autorizacao"));
    }
}
