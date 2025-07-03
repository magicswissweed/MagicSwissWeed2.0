package com.aa.msw.source.local;

import com.aa.msw.api.graph.last40days.Last40daysApiServiceImpl;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class LocalStartupTrigger implements CommandLineRunner {
    private final Last40daysApiServiceImpl last40daysApiService;

    public LocalStartupTrigger(Last40daysApiServiceImpl last40daysApiService) {
        this.last40daysApiService = last40daysApiService;
    }

    @Override
    public void run(String... args) {
        this.last40daysApiService.fetchLast40DaysAndSaveToDb();
    }
}
