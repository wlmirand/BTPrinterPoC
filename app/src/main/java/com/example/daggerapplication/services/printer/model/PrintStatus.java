package com.example.daggerapplication.services.printer.model;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
public class PrintStatus {

    public enum Status {
        STARTED,
        SUCCESS,
        ERROR
    }

    private Status status;
    private String message;

}
