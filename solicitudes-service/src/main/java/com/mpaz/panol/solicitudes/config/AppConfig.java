package com.mpaz.panol.solicitudes.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    // El reloj se inyecta para poder fijar la hora en las pruebas unitarias
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
