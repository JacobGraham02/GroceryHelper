package com.jacobdamiangraham.groceryhelper.interfaces

interface IAddGroceryStoreCallback {

    fun onAddStoreSuccess(successMessage: String)

    fun onAddStoreFailure(failureMessage: String)
}