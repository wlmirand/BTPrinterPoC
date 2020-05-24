package com.example.daggerapplication.services.bluetooth.broadcast;

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

public class RxBroadcastReceiver implements ObservableOnSubscribe<Intent> {

    public static final String NONE_ACTION = "NONE_ACTION";

    protected final WeakReference<Context> contextWeakReference;
    private final String actionForComplete;
    private IntentFilter intentFilter;

    public static Observable<Intent> create(Context context, IntentFilter intentFilter, String ...actionForComplete) {
        return Observable.defer(() -> Observable.create(new RxBroadcastReceiver(context, intentFilter, actionForComplete))
                .subscribeOn(Schedulers.io())
        );
    }

    private RxBroadcastReceiver(Context context, IntentFilter intentFilter, String ...actionForComplete) {
        contextWeakReference = new WeakReference<Context>(context.getApplicationContext());
        this.intentFilter = intentFilter;
        this.actionForComplete = actionForComplete!=null && actionForComplete.length>0 ? actionForComplete[0] : NONE_ACTION;
    }

    @Override
    public void subscribe(ObservableEmitter<Intent> emitter) throws Exception {
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (actionForComplete.equals(intent.getAction())) {
                    emitter.onComplete();
                } else {
                    emitter.onNext(intent);
                }
            }
        };

        emitter.setDisposable(Disposables.fromRunnable(() -> { // thank you Jake W.
            if (contextWeakReference != null && contextWeakReference.get() != null) {
                contextWeakReference.get().unregisterReceiver(broadcastReceiver);
            }
        }));

        if (contextWeakReference != null && contextWeakReference.get() != null) {
            contextWeakReference.get().registerReceiver(broadcastReceiver, intentFilter);
        }
    }
}