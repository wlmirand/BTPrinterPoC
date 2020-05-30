package com.example.daggerapplication.services.printer.template;

public enum TemplateType {

    DELIVERY_CHARGES_PAYMENT_RECEIPT("delivery_receipt"),
    ATTEMPTED_DELIVERY_NOTIFICATION("non_delivery_receipt");

    private final String templateName;

    TemplateType(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateName() {
        return templateName;
    }
}
