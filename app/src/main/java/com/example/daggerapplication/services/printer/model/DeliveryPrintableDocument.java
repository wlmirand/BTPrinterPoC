package com.example.daggerapplication.services.printer.model;

import com.example.daggerapplication.services.printer.template.TemplateType;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DeliveryPrintableDocument implements PrintableDocument  {

    private List<PostalItem> postalItems;
    private String totalAmount;
    private String paymentMethod;

    @Override
    public TemplateType getTemplateType() {
        return TemplateType.DELIVERY_CHARGES_PAYMENT_RECEIPT;
    }

}
