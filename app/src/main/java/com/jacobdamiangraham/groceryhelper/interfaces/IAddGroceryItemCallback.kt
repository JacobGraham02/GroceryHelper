package com.jacobdamiangraham.groceryhelper.interfaces

interface IAddGroceryItemCallback {

    fun onAddSuccess(successMessage: String)
    fun onAddFailure(failureMessage: String)
}