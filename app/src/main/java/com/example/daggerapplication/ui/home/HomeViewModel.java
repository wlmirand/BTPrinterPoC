package com.example.daggerapplication.ui.home;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.daggerapplication.R;

import org.universalpostalunion.printerlibrary.bluetooth.BluetoothService;
import org.universalpostalunion.printerlibrary.bluetooth.model.DeviceInformation;
import org.universalpostalunion.printerlibrary.printer.PrinterService;
import org.universalpostalunion.printerlibrary.printer.builder.PrintableBuilder;
import org.universalpostalunion.printerlibrary.printer.exception.PrintException;
import org.universalpostalunion.printerlibrary.printer.model.PrintStatus;
import org.universalpostalunion.printerlibrary.printer.model.PrintableDocument;
import org.universalpostalunion.printerlibrary.printer.util.PrintCommand;

import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

public class HomeViewModel extends ViewModel {

    private final PrinterService printerService;
    private final Context appContext;
    private BluetoothService btService;
    private MutableLiveData<PrintStatus> printStatus;
    private DeviceInformation selectedDevice;
    private MutableLiveData<List<DeviceInformation>> printerList;

    @Inject
    HomeViewModel(Context appContext, BluetoothService btService, PrinterService printerService) {
        this.appContext = appContext;
        this.btService = btService;
        this.printerService = printerService;
    }

    boolean isBluetoothAvailable() {
        return btService.isAvailable();
    }

    boolean isBlueToothActivated() {
        return btService.isActivated();
    }

    public PrintableBuilder getPrintableBuilder() throws PrintException {
        return printerService.getPrintableBuilder();
    }

    void print() {
        printerService.print(constructDocument(), selectedDevice, getPrintStatus());
    }

    void getAvailablePrinters() {
        printerService.getAvailablePrinters(getPrintersList());
    }

    MutableLiveData<List<DeviceInformation>> getPrintersList() {
        if (printerList == null) {
            printerList = new MutableLiveData<>();
        }
        return printerList;
    }

    MutableLiveData<PrintStatus> getPrintStatus() {
        if (this.printStatus == null) {
            printStatus = new MutableLiveData<>();
        }
        return printStatus;
    }

    void selectPrinter(DeviceInformation deviceInformation) {
        this.selectedDevice = deviceInformation;
    }

    /////////////////////////////////////////////////////////////////////////////////
    // FOR TESTS
    private PrintableDocument constructDocument() {
        try {
            final PrintableBuilder builder = printerService.getPrintableBuilder();
            builder.printPhoto(R.drawable.upu_logo, PrintCommand.ESC_ALIGN_LEFT);
            builder.printLine(appContext.getResources().getString(R.string.print_delivery_notification_card_title), PrintCommand.TITLE_STYLE, PrintCommand.ESC_ALIGN_CENTER);
            builder.printNewLine();
            builder.printNewLine();
            final String[] dateAndTime = getDateTime();
            String cardContent = appContext.getResources().getString(R.string.print_delivery_notification_card_content_one);
            cardContent = String.format(cardContent, dateAndTime[0], dateAndTime[1]);

            builder.printLine(
                    cardContent,
                    PrintCommand.ESC_ALIGN_LEFT);

            builder.printBarCodeFromText("item1", PrintCommand.ESC_ALIGN_CENTER);
            builder.printLine("item1", PrintCommand.ESC_ALIGN_CENTER);

            builder.printStyle(PrintCommand.ESC_TAB, PrintCommand.ESC_ALIGN_LEFT);
            builder.printLine(appContext.getResources().getString(R.string.print_delivery_notification_card_shipment));

            builder.print(appContext.getResources().getString(R.string.print_delivery_notification_card_type));
            builder.printStyle(PrintCommand.ESC_TAB);
            builder.print("BigMac");
            builder.printNewLine();
            builder.printNewLine();

            builder.print(appContext.getResources().getString(R.string.print_delivery_notification_card_recipient));
            builder.printStyle(PrintCommand.ESC_TAB, PrintCommand.ESC_BOLD);
            builder.print("William");
            builder.printStyle(PrintCommand.ESC_NO_BOLD);
            builder.printNewLine();

            //Recipient Address
            builder.printStyle(PrintCommand.ESC_TAB);
            builder.printLine("this is my address");

            //Recipient Postcode
            builder.printStyle(PrintCommand.ESC_TAB);
            builder.printLine("PC 5454");

            builder.printStyle(PrintCommand.ESC_TAB);
            builder.printLine("Marseille");

            String retentionPeriod = appContext.getResources().getString(R.string.print_delivery_notification_card_retention_period);
            retentionPeriod = String.format(retentionPeriod, 10);
            builder.printLine(retentionPeriod, PrintCommand.ESC_ALIGN_LEFT);

            builder.printLine("Picker Point");

            final String[] testPickedPeriod = new String[]{"Mon - 07:00 / 20:00", "Tus - 08:00 / 22:00"};
            for (String s : testPickedPeriod) {
                builder.printLine(s);
            }

            builder.printLine(
                    appContext.getResources().getString(R.string.print_delivery_notification_card_remember),
                    PrintCommand.ESC_ALIGN_LEFT);

            return builder.build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private String[] getDateTime() {
        final Calendar c = Calendar.getInstance();
        String[] dateTime = new String[2];
        dateTime[0] = c.get(Calendar.DAY_OF_MONTH) + "/" + c.get(Calendar.MONTH) + "/" + c.get(Calendar.YEAR);
        dateTime[1] = c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE);
        return dateTime;
    }


}
