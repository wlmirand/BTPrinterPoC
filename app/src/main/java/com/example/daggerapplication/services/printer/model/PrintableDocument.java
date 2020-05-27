package com.example.daggerapplication.services.printer.model;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PrintableDocument {
    private String title;
    private List<String> lines;

}
