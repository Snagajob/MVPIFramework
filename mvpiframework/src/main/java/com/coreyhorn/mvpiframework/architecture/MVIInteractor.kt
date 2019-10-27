package com.coreyhorn.mvpiframework.architecture

import android.util.Log
import com.coreyhorn.mvpiframework.basemodels.Event
import com.coreyhorn.mvpiframework.basemodels.Result
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.ReplaySubject

abstract class MVIInteractor<E: Event, R: Result>(events: Observable<E>): Interactor<E, R> {

    private val results: ReplaySubject<R> = ReplaySubject.create()
    val disposables = CompositeDisposable()

    init {
        events.map { eventToResult(it) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(results)
    }

    fun results(): Observable<R> = results

    open fun destroy() {
        Log.d("stuff", "interactor destroyed")
        disposables.clear()
    }

    protected fun pushResult(result: R) {
        results.onNext(result)
    }
}

private interface Interactor<E: Event, R: Result> {
    fun eventToResult(event: E): R
}