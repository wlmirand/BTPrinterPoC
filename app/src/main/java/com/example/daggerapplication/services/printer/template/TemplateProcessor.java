package com.example.daggerapplication.services.printer.template;

import android.content.Context;

import com.example.daggerapplication.R;
import com.example.daggerapplication.services.printer.model.PrintableDocument;
import com.example.daggerapplication.services.printer.util.PrintCommand;
import com.example.daggerapplication.services.printer.util.PrinterUtil;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TemplateProcessor {

    private static final String LOGO = "<LOGO>";
    private static final String BAR_CODE = "<BAR_CODE>";
    private HashMap<TemplateType, Template> templatesMap = new HashMap<>();
    private final VelocityContext mainContext;
    private final Context appContext;
    private final PrinterUtil printerUtil;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Inject
    TemplateProcessor(Context appContext, PrinterUtil printerUtil) {
        this.appContext = appContext;
        this.printerUtil = printerUtil;

        Velocity.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM_CLASS, TemplateLogger.class.getName());
        Velocity.setProperty("resource.loader", "android");
        Velocity.setProperty("android.resource.loader.class", TemplateResourceLoader.class.getName());
        Velocity.setProperty("android.content.res.Resources", appContext.getResources());
        Velocity.setProperty("packageName", "com.example.daggerapplication");
        Velocity.init();

        for (TemplateType templateType : TemplateType.values()) {
            templatesMap.put(templateType, Velocity.getTemplate(templateType.getTemplateName()));
        }

        mainContext = new VelocityContext();

        for (PrintCommand command : PrintCommand.values()) {
            mainContext.put(command.toString(), command.toStringValue());
        }
        initializesText();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////

    public List<byte[]> fillsPrintableDocument(PrintableDocument document) {
        final ArrayList<String> tags = new ArrayList<>();
        tags.add(LOGO);
        tags.add(BAR_CODE);

        final VelocityContext context = new VelocityContext(mainContext);
        Template template = templatesMap.get(document.getTemplateType());
        final StringWriter stringWriter = new StringWriter();

        switch (document.getTemplateType()) {
            case DELIVERY_CHARGES_PAYMENT_RECEIPT:
                context.put("currentDateTime", printerUtil.getDateTime());
                context.put("deliveryPrintableDocument", document);
                break;
            case ATTEMPTED_DELIVERY_NOTIFICATION:
                context.put("currentDate", printerUtil.getDate());
                context.put("currentTime", printerUtil.getTime());
                context.put("nonDeliveryPrintableDocument", document);
                break;
        }

        if (template != null) {
            template.merge(context, stringWriter);
        }
        return splitResultToIncludeImages(stringWriter.toString(), tags);
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private List<byte[]> splitResultToIncludeImages(String document, List<String> tags) {
        final ArrayList<byte[]> splitDocumentList = new ArrayList<>();
        final String currentTag = tags.remove(0);
        final String[] docTable = document.split(currentTag);

        if (docTable.length == 1) {
            splitDocumentList.add(document.getBytes());
            return splitDocumentList;
        }

        if (currentTag.equals(LOGO)) {
            splitDocumentList.add(docTable[0].getBytes());
            splitDocumentList.add(printerUtil.getUPULogo());
            splitDocumentList.addAll(splitResultToIncludeImages(docTable[1], tags));
        } else if (currentTag.equals(BAR_CODE)) {
            final String[] splitForValueAndRemainingDoc = docTable[1].split("\\|");
            final String toEncode = splitForValueAndRemainingDoc[0];
            splitDocumentList.add(docTable[0].getBytes());
            splitDocumentList.add(printerUtil.getBarCode(toEncode));
            splitDocumentList.add(splitForValueAndRemainingDoc[1].getBytes());
        }

        return splitDocumentList;
    }

    private void initializesText() {
        mainContext.put("not_delivered_title", appContext.getResources().getString(R.string.print_delivery_notification_card_title));
        mainContext.put("not_delivered_attempt_content", appContext.getResources().getString(R.string.print_delivery_notification_card_content_one));
        mainContext.put("not_delivered_attempt_content_at", appContext.getResources().getString(R.string.print_delivery_notification_card_content_one_at));
        mainContext.put("not_delivered_attempt_content_on", appContext.getResources().getString(R.string.print_delivery_notification_card_content_one_on));
        mainContext.put("not_delivered_shipment", appContext.getResources().getString(R.string.print_delivery_notification_card_shipment));
        mainContext.put("not_delivered_type", appContext.getResources().getString(R.string.print_delivery_notification_card_type));
        mainContext.put("not_delivered_recipient", appContext.getResources().getString(R.string.print_delivery_notification_card_recipient));
        mainContext.put("not_delivered_delivery", appContext.getResources().getString(R.string.print_delivery_notification_card_delivery));
        mainContext.put("not_delivered_charges", appContext.getResources().getString(R.string.print_delivery_notification_card_charges));
        mainContext.put("not_delivered_retention_period", appContext.getResources().getString(R.string.print_delivery_notification_card_retention_period));
        mainContext.put("not_delivered_retention_period_days", appContext.getResources().getString(R.string.print_delivery_notification_card_retention_period_days));
        mainContext.put("not_delivered_remember", appContext.getResources().getString(R.string.print_delivery_notification_card_remember));

        mainContext.put("delivery_title", appContext.getResources().getString(R.string.print_delivery_charges_payment_receipt_title));
        mainContext.put("delivery_date_time", appContext.getResources().getString(R.string.print_delivery_charges_payment_receipt_date_and_time));
        mainContext.put("delivery_total_amount", appContext.getResources().getString(R.string.print_delivery_charges_payment_receipt_total_amount));
        mainContext.put("delivery_payment_method", appContext.getResources().getString(R.string.print_delivery_charges_payment_receipt_payment_method));
        mainContext.put("delivery_receipt_symbol", appContext.getResources().getString(R.string.print_delivery_charges_payment_receipt_symbol));
    }
}
