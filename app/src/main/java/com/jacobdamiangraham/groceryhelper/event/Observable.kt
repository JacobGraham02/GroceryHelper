package com.jacobdamiangraham.groceryhelper.event

import androidx.lifecycle.Observer

class Observable<T> {

    private val observers = mutableListOf<Observer<T>>()

    fun addObserver(observer: Observer<T>) {
        observers.add(observer)
    }

    fun removeObserver(observer: Observer<T>) {
        observers.remove(observer)
    }

    fun notifyObservers(event: T) {
        for (observer in observers) {
            observer.onChanged(event)
        }
    }
}