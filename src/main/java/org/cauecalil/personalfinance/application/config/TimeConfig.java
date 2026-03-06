package org.cauecalil.personalfinance.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;

@Configuration
public class TimeConfig {
    @Bean
    public ZoneId defaultZoneId() {
        return ZoneId.of("America/Sao_Paulo");
    }
}
