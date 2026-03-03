package com.aa.msw.helper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class TestResourceLoader {

    private TestResourceLoader() {
    }

    public static String load(String path) {
        try (InputStream is = TestResourceLoader.class.getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalArgumentException("Test resource not found: " + path);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
