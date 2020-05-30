package com.example.daggerapplication.services.printer.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Charge {
    private String code;
    private String amount;
}
