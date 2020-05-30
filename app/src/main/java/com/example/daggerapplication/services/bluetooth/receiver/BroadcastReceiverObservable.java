package com.example.daggerapplication.services.bluetooth.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.lang.ref.WeakReference;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposables;
import io.reactivex.schedulers.Schedulers;

public class BroadcastReceiverObservable implements ObservableOnSubscribe<Intent> {

    private static final String UNBLOCK = "UNBLOCK";
    private final WeakReference<Context> contextWeakReference;
    private IntentFilter intentFilter;

    public static Observable<Intent> create(Context context, IntentFilter intentFilter) {
        return Observable.defer(() -> Observable.create(new BroadcastReceiverObservable(context, intentFilter))
                .subscribeOn(Schedulers.io())
        );
    }

    private BroadcastReceiverObservable(Context context, IntentFilter intentFilter) {
        contextWeakReference = new WeakReference<>(context.getApplicationContext());
        this.intentFilter = intentFilter;
    }

    @Override
    public void subscribe(ObservableEmitter<Intent> emitter) {
        final Intent intentUnblockInitalCombinedLatest = new Intent();
        intentUnblockInitalCombinedLatest.setAction(UNBLOCK);
        emitter.onNext(intentUnblockInitalCombinedLatest);
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                emitter.onNext(intent);
            }
        };
        emitter.setDisposable(Disposables.fromRunnable(() -> {
            if (contextWeakReference != null && contextWeakReference.get() != null) {
                contextWeakReference.get().unregisterReceiver(broadcastReceiver);
            }
        }));

        if (contextWeakReference != null && contextWeakReference.get() != null) {
            contextWeakReference.get().registerReceiver(broadcastReceiver, intentFilter);
        }
    }
}