package com.acquireindia.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins:http://localhost:5173,http://localhost:3000,https://5173-firebase-acquire-f-1753295047182.cluster-44kx2eiocbhe2tyk3zoyo3ryuo.cloudworkstations.dev}")
    private String allowedOrigins;

    @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS,PATCH}")
    private String allowedMethods;

    @Value("${cors.allowed-headers:*}")
    private String allowedHeaders;

    @Value("${cors.exposed-headers:Authorization,Content-Type,X-Requested-With,Accept,Origin,Access-Control-Request-Method,Access-Control-Request-Headers}")
    private String exposedHeaders;

    @Value("${cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Value("${cors.max-age:3600}")
    private long maxAge;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = allowedOrigins.split(",");
        String[] methods = allowedMethods.split(",");
        String[] headers = allowedHeaders.equals("*") ? new String[]{"*"} : allowedHeaders.split(",");
        String[] exposed = exposedHeaders.split(",");

        registry.addMapping("/**")
                .allowedOriginPatterns(origins)
                .allowedMethods(methods)
                .allowedHeaders(headers)
                .exposedHeaders(exposed)
                .allowCredentials(allowCredentials)
                .maxAge(maxAge);
    }
} 