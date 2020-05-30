package com.example.daggerapplication.services.printer.model;

import com.example.daggerapplication.services.printer.template.TemplateType;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NotDeliveredPrintableDocument implements PrintableDocument {

    private String id;
    private String productTypeName;
    private String recipientName;
    private String recipientAddress;
    private String recipientPostCode;
    private String recipientCity;
    private String sumOfCharges;
    private int retentionPeriod;
    private String pickUpPointName;
    private List<String> pickupPointOpeningHours;

    @Override
    public TemplateType getTemplateType() {
        return TemplateType.ATTEMPTED_DELIVERY_NOTIFICATION;
    }
}
