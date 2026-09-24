package com.example.webapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;
 
import java.util.ArrayList;
import java.util.List;

//外部API呼び出し用のRestTemplateをBean登録する。
@Configuration
public class RestTemplateConfig {
 
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
 
        for (HttpMessageConverter<?> converter : restTemplate.getMessageConverters()) {
            if (converter instanceof JacksonJsonHttpMessageConverter jacksonConverter) {
                List<MediaType> supportedMediaTypes = new ArrayList<>(jacksonConverter.getSupportedMediaTypes());
                supportedMediaTypes.add(MediaType.valueOf("text/javascript"));
                jacksonConverter.setSupportedMediaTypes(supportedMediaTypes);
            }
        }
 
        return restTemplate;
    }
}