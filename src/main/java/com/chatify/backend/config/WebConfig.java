package com.chatify.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // General uploads handler
        registry.addResourceHandler("/api/uploads/**")
                .addResourceLocations("file:uploads/")
                .setCachePeriod(3600);
                
        // Specific avatars handler
        registry.addResourceHandler("/api/uploads/avatars/**")
                .addResourceLocations("file:uploads/avatars/")
                .setCachePeriod(3600);
    }
} 