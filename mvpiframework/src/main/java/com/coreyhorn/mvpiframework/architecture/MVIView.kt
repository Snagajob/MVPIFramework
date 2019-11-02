package com.coreyhorn.mvpiframework.architecture

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.coreyhorn.mvpiframework.MVIEvent
import com.coreyhorn.mvpiframework.MVIResult
import com.coreyhorn.mvpiframework.MVIState
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.ReplaySubject

interface MVIView<E: MVIEvent, R: MVIResult, S: MVIState> {

    var events: ReplaySubject<E>
    var presenter: MVIViewModel<E, R, S>?
    var eventDisposables: CompositeDisposable

    var rootView: View?

    var attached: Boolean

    var lifecycleOwner: LifecycleOwner

    fun presenterProvider(): MVIViewModel<E, R, S>

    fun renderState(view: View, state: S)

    /**
     * Passes an instance of the view in to bind your events to. If you are using Rx,
     * make sure to dispose with eventDisposables so they are cleaned up automatically.
     */
    fun setupViewBindings(view: View)

    /**
     * Used to provide an initial state to seed the ViewModel if it requires one.
     * This can be used to provide date returned from your savedInstanceState.
     */
    fun initialState(): S

    /**
     * Call when the view is inflated and initialState() is returning the proper value
     */
    fun viewReady(view: View, lifecycleOwner: LifecycleOwner, presenter: MVIViewModel<E, R, S>) {
        this.presenter = presenter
        this.lifecycleOwner = lifecycleOwner
        this.rootView = view

        attachToViewModel()
    }

    fun detachView() {
        presenter?.states()?.removeObservers(lifecycleOwner)
        presenter?.detachView()
        eventDisposables.clear()
        events = ReplaySubject.create()
        attached = false
    }

    fun attachToViewModel() {
        if (!attached) {
            attached = true
            eventDisposables.clear()

            rootView?.let { it.post { setupViewBindings(it) } }
            presenter?.let {
                it.states().removeObservers(lifecycleOwner)
                it.attachEvents(events, initialState())
                it.states()
                        .observe(lifecycleOwner, object: Observer<S> {
                            override fun onChanged(state: S) {
                                rootView?.let { view ->
                                    view.post {
                                        if (attached) {
                                            renderState(view, state)
                                        }
                                    }
                                }
                            }
                        })
            }
        }
    }
}