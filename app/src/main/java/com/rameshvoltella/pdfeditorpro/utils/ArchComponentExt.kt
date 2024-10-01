package com.rameshvoltella.pdfeditorpro.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

fun <T> LifecycleOwner.observe(liveData: LiveData<T>, action: (t: T) -> Unit) {
    liveData.observe(this, Observer { it?.let { t -> action(t) } })
}

fun <T> LifecycleOwner.observe(refrence:LifecycleOwner,liveData: LiveData<T>, action: (t: T) -> Unit) {
    liveData.observe(refrence, Observer { it?.let { t -> action(t) } })
}

fun <T> LifecycleOwner.observeEvent(liveData: LiveData<SingleEvent<T>>, action: (t: SingleEvent<T>) -> Unit) {
    liveData.observe(this, Observer { it?.let { t -> action(t) } })
}
fun <T> LifecycleOwner.observeMutableState(liveData: MutableLiveData<T>, action: (t: T) -> Unit) {
    liveData.observe(this, Observer { it?.let { t -> action(t) } })
}

fun <T> LifecycleOwner.observePlayerState(
    liveData: MutableLiveData<T>,
    handler: (state: T) -> Unit
) {
    liveData.observe(this, Observer { state ->
        state?.let { handler(it) }
    })
}