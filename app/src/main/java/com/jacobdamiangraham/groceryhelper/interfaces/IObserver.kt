package com.jacobdamiangraham.groceryhelper.interfaces

interface IObserver<T> {
    fun onUpdate(event: T)
}