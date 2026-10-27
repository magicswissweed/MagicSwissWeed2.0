package com.aa.msw;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.Map;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class MswApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(MswApplication.class);
        // Spring Boot 4 defaults to Jackson 3; the API models (JsonNullable) are built for Jackson 2
        app.setDefaultProperties(Map.of("spring.http.converters.preferred-json-mapper", "jackson2"));
        app.run(args);
    }

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5); // number of parallel threads
        scheduler.setThreadNamePrefix("scheduled-task-");
        scheduler.initialize();
        return scheduler;
    }
}
