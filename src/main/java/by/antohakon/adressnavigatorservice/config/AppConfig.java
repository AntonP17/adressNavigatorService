package by.antohakon.adressnavigatorservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;

@Configuration
public class AppConfig {

    @Bean
    public HttpClient httpClient() {

        return HttpClient.newHttpClient();
    }

    @Bean
    public ObjectMapper objectMapper() {

        return new ObjectMapper();
    }

}
