package com.aa.msw.source.swiss.existenz.exception;

import java.io.IOException;

public class IncorrectDataReceivedException extends IOException {
    public IncorrectDataReceivedException(String message) {
        super(message);
    }
}
