package br.net.convertix.dinix.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.PropertyNamingStrategies;

@Configuration
public class JacksonConfig {

    @Bean
    JsonMapperBuilderCustomizer nomesEmPortugues() {
        return builder -> builder.propertyNamingStrategy(new PortugueseSnakeCaseStrategy());
    }

    static final class PortugueseSnakeCaseStrategy extends PropertyNamingStrategies.NamingBase {
        @Override
        public String translate(String propertyName) {
            return NomesCamposApi.json(propertyName);
        }
    }
}
