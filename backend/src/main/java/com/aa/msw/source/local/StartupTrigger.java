package com.aa.msw.source.local;

import com.aa.msw.source.InputDataFetcherService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;

@Component
public class StartupTrigger implements CommandLineRunner {
    private final InputDataFetcherService inputDataFetcherService;

    public StartupTrigger(InputDataFetcherService inputDataFetcherService) {
        this.inputDataFetcherService = inputDataFetcherService;
    }

    @Override
    public void run(String... args) throws IOException, URISyntaxException {
        inputDataFetcherService.fetchDataAndWriteToDb();
    }
}
