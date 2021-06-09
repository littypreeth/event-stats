package com.litty.eventstats.service;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("test")
@Configuration
public class EventStatsSvcTestConfiguration {
    @Bean
    @Primary
    public EventStatsSvc nameService() {
        return Mockito.mock(EventStatsSvc.class);
    }
}
