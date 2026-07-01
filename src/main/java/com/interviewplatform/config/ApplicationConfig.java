package com.interviewplatform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    @Bean
    @ConfigurationProperties(prefix = "openrouter.api")
    public OpenRouterProperties openRouterProperties() {
        return new OpenRouterProperties();
    }

    public static class OpenRouterProperties {
        private String key;
        private String url;
        private String model;

        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }

        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
    }
}
