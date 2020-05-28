package com.example.daggerapplication.services.printer;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.example.daggerapplication.services.bluetooth.BluetoothService;
import com.example.daggerapplication.services.bluetooth.model.DeviceType;
import com.example.daggerapplication.services.printer.model.PrintStatus;
import com.example.daggerapplication.services.printer.model.PrintableDocument;
import com.example.daggerapplication.services.printer.util.PrinterCommands;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class PrinterService {

    private static final String LOG_TAG = "PrinterService";
    private final BluetoothService bluetoothService;

    private class PrinterStatusObserver implements ObservableOnSubscribe<PrintStatus> {

        private final PrintableDocument document;

        PrinterStatusObserver(PrintableDocument document) {
            this.document = document;
        }

        @Override
        public void subscribe(ObservableEmitter<PrintStatus> emitter) {
            bluetoothService.getConnectionSocketFor(DeviceType.PRINTER)
                    .map(bluetoothSocket -> bluetoothSocket.getOutputStream())
                    .subscribe(
                            outputStream -> {
                                emitter.onNext(PrintStatus.builder().status(PrintStatus.Status.STARTED).build());
                                byte[] printout = {0x1B, 0x21, 1};
                                outputStream.write(printout);
                                outputStream.write(PrinterCommands.ESC_ALIGN_LEFT);
                                outputStream.write("TEST TEST TEST SIMPLE\n".getBytes());
                                outputStream.write("TEST TEST TEST SIMPLE\n".getBytes());
                                outputStream.write("TEST TEST TEST SIMPLE\n".getBytes());
                                outputStream.write("TEST TEST TEST SIMPLE\n".getBytes());
                                outputStream.write("TEST TEST TEST SIMPLE\n".getBytes());
                                outputStream.write("TEST TEST TEST SIMPLE\n".getBytes());
                                outputStream.write("TEST TEST TEST SIMPLE\n".getBytes());
                                outputStream.write("TEST TEST TEST SIMPLE\n".getBytes());
                                outputStream.write("TEST TEST TEST SIMPLE\n".getBytes());
                                outputStream.write("TEST TEST TEST SIMPLE\n".getBytes());
                                outputStream.write("TEST TEST TEST SIMPLE\n".getBytes());
                                outputStream.write("TEST TEST TEST SIMPLE\n".getBytes());
                                outputStream.write("TEST TEST TEST SIMPLE\n".getBytes());
                                outputStream.write("TEST TEST TEST SIMPLE\n".getBytes());
                                outputStream.write("TEST TEST TEST SIMPLE\n".getBytes());
                                outputStream.write("TEST TEST TEST SIMPLE\n".getBytes());
                                outputStream.write("TEST TEST TEST SIMPLE\n".getBytes());
                                outputStream.write("TEST TEST TEST SIMPLE\n".getBytes());
                                outputStream.write("TEST TEST TEST SIMPLE\n".getBytes());
                                outputStream.write("TEST TEST TEST SIMPLE\n".getBytes());
                                outputStream.write("TEST TEST TEST SIMPLE\n".getBytes());
                                outputStream.write("TEST TEST TEST SIMPLE\n".getBytes());
                                outputStream.write("TEST TEST TEST SIMPLE\n".getBytes());
                                outputStream.write("TEST TEST TEST SIMPLE\n".getBytes());
                                outputStream.write("TEST TEST TEST SIMPLE\n".getBytes());
                                outputStream.write("TEST TEST TEST SIMPLE\n".getBytes());
                                outputStream.write("TEST TEST TEST SIMPLE\n".getBytes());
                                outputStream.write("TEST TEST TEST SIMPLE\n".getBytes());
                                outputStream.write("TEST TEST TEST SIMPLE\n".getBytes());
                                outputStream.write("TEST TEST TEST SIMPLE\n".getBytes());

                                outputStream.flush();
                                emitter.onNext(PrintStatus.builder().status(PrintStatus.Status.SUCCESS).build());
                            },
                            throwable -> {
                                Log.e(LOG_TAG, "Error encountered while printing", throwable);
                                emitter.onNext(PrintStatus.builder().status(PrintStatus.Status.ERROR).message(throwable.getMessage()).build());
                            }).dispose();
        }
    }

    @Inject
    PrinterService(BluetoothService bluetoothService) {
        this.bluetoothService = bluetoothService;
    }

    public Observable<PrintStatus> print(final PrintableDocument document) {
//        return Observable.defer(() -> Observable.create(new PrinterStatusObserver(document))
//                .observeOn(AndroidSchedulers.mainThread()));

        return Observable.create(new PrinterStatusObserver(document))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io());
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////

}
