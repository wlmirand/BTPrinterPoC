package com.example.daggerapplication.services.printer.model;

import java.util.List;

import lombok.Data;

@Data
public class PostalItem {
    private String id;
    private List<Charge> charges;
}