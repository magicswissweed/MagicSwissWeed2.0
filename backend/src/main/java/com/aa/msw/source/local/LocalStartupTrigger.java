package com.aa.msw.source.local;

import com.aa.msw.source.InputDataFetcherService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;

@Component
@Profile({"dev", "local"})
public class LocalStartupTrigger implements CommandLineRunner {
    private final InputDataFetcherService inputDataFetcherService;

    public LocalStartupTrigger(InputDataFetcherService inputDataFetcherService) {
        this.inputDataFetcherService = inputDataFetcherService;
    }

    @Override
    public void run(String... args) throws IOException, URISyntaxException {
        inputDataFetcherService.fetchDataAndWriteToDb();
    }
}
