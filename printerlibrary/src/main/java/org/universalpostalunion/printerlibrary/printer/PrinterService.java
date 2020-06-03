package org.universalpostalunion.printerlibrary.printer;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import org.universalpostalunion.printerlibrary.bluetooth.BluetoothService;
import org.universalpostalunion.printerlibrary.bluetooth.model.DeviceInformation;
import org.universalpostalunion.printerlibrary.bluetooth.model.DeviceType;
import org.universalpostalunion.printerlibrary.common.CompositeDisposable;
import org.universalpostalunion.printerlibrary.printer.builder.PrintableBuilder;
import org.universalpostalunion.printerlibrary.printer.builder.PrintableBuilderImpl;
import org.universalpostalunion.printerlibrary.printer.escpos.ESCPOSHelper;
import org.universalpostalunion.printerlibrary.printer.exception.PrintException;
import org.universalpostalunion.printerlibrary.printer.model.PrintStatus;
import org.universalpostalunion.printerlibrary.printer.model.PrintableDocument;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class PrinterService {

    private static final String LOG_TAG = PrinterService.class.getSimpleName();

    private final BluetoothService bluetoothService;
    private final Context appContext;
    private final ESCPOSHelper escposHelper;
    private Disposable printStatusDisposable;
    private Disposable availablePrintersDisposable;

    private class PrinterObserver implements ObservableOnSubscribe<PrintStatus> {
        private final PrintableDocument document;
        private final DeviceInformation printer;

        PrinterObserver(PrintableDocument document, DeviceInformation printer) {
            this.document = document;
            this.printer = printer;
        }

        @Override
        public void subscribe(ObservableEmitter<PrintStatus> emitter) {
            bluetoothService.getConnectionSocketFor(printer)
                    .map(bluetoothSocket -> bluetoothSocket.getOutputStream())
                    .subscribe(
                            outputStream -> {
                                Log.i(LOG_TAG, "printing document ....");
                                emitter.onNext(PrintStatus.builder().status(PrintStatus.Status.STARTED).build());
                                outputStream.write(document.getPrintable().toByteArray());
                                document.getPrintable().close();
                                outputStream.flush();
                                emitter.onNext(PrintStatus.builder().status(PrintStatus.Status.SUCCESS).build());
                                Log.i(LOG_TAG, "Document Printed");
                            },
                            throwable -> {
                                Log.e(LOG_TAG, "Error encountered while printing", throwable);
                                emitter.onNext(PrintStatus.builder().status(PrintStatus.Status.ERROR).message(throwable.getMessage()).build());
                            }).dispose();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////

    @Inject
    PrinterService(Context appContext, BluetoothService bluetoothService, ESCPOSHelper helper) {
        this.appContext = appContext;
        this.bluetoothService = bluetoothService;
        escposHelper = helper;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * @param printableDocument The document to print. (construct by PrintableBuilder)
     * @param status            the print status.
     */
    public void print(final PrintableDocument printableDocument, DeviceInformation printer, MutableLiveData<PrintStatus> status) {
        if (this.printStatusDisposable != null) {
            CompositeDisposable.clearDisposable(printStatusDisposable);
        }
        printStatusDisposable = Observable.create(new PrinterObserver(printableDocument, printer))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(printStatus -> status.postValue(printStatus)
                        , throwable -> {
                            final PrintStatus printStatus = PrintStatus.builder()
                                    .status(PrintStatus.Status.ERROR)
                                    .message(throwable.getMessage())
                                    .build();
                            status.postValue(printStatus);
                        });
        CompositeDisposable.add(printStatusDisposable);
    }

    /**
     * @return A builder that helps to construct a PrintableDocument
     * @throws PrintException on Builder initialization.
     */
    public PrintableBuilder getPrintableBuilder() throws PrintException {
        try {
            return new PrintableBuilderImpl(appContext, escposHelper);
        } catch (Exception e) {
            throw new PrintException(e);
        }
    }


    public void getAvailablePrinters(MutableLiveData<List<DeviceInformation>> deviceListToSet) {
        if (availablePrintersDisposable != null) {
            CompositeDisposable.clearDisposable(availablePrintersDisposable);
        }
        availablePrintersDisposable = bluetoothService.getDevicesInformation()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .map(devicesInformation -> {
                    final ArrayList<DeviceInformation> filteredByDeviceType = new ArrayList<>();
                    for (DeviceInformation device : devicesInformation) {
                        if (device.getDeviceType() == DeviceType.PRINTER) {
                            filteredByDeviceType.add(device);
                        }
                    }
                    return filteredByDeviceType;
                }).subscribe(
                        deviceInformations -> deviceListToSet.postValue(deviceInformations),
                        throwable -> Log.e(LOG_TAG, "Error retrieving printers")
                );

        CompositeDisposable.add(availablePrintersDisposable);
    }

    /**
     * Called by Dependency Injection Manager when the application stops
     */
    public void clean() {
        if (printStatusDisposable != null) {
            CompositeDisposable.clearDisposable(printStatusDisposable);
        }

        if (availablePrintersDisposable != null) {
            CompositeDisposable.clearDisposable(availablePrintersDisposable);
        }
        Log.i(LOG_TAG, "Bean Cleaned");
    }
}
