package com.jacobdamiangraham.groceryhelper.interfaces

interface IMergeGroceryListOperation {
    fun onSuccess(message: String)

    fun onFailure(message: String)
}