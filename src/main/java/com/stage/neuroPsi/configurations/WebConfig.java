package com.stage.neuroPsi.configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            // Permettre la réceptions de requêtes provenant de l'interface graphique
            @Override
            public void addCorsMappings(@SuppressWarnings("null") CorsRegistry registry) {
                registry.addMapping("/greeting-javaconfig").allowedOrigins("http://localhost:3000");
            }
        };
    }

}
