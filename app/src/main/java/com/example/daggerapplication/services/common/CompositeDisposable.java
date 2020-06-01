package com.example.daggerapplication.services.common;

import io.reactivex.disposables.Disposable;

public class CompositeDisposable {

    private static final io.reactivex.disposables.CompositeDisposable compositeDisposable = new io.reactivex.disposables.CompositeDisposable();

    public static void add(Disposable disposable) {
        compositeDisposable.add(disposable);
    }

    public static void clear() {
        compositeDisposable.clear();
    }

    public static void clearDisposable(Disposable disposable) {
        compositeDisposable.remove(disposable);
    }
}
