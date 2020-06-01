package org.universalpostalunion.printerlibrary.printer.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PrintStatus {

    public enum Status {
        ERROR,
        STARTED,
        SUCCESS
    }

    private Status status;
    private String message;

}
