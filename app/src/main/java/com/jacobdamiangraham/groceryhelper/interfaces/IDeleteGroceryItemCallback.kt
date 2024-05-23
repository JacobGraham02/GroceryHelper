package com.jacobdamiangraham.groceryhelper.interfaces

interface IDeleteGroceryItemCallback {

    fun onDeleteSuccess(successMessage: String)

    fun onDeleteFailure(failureMessage: String)
}