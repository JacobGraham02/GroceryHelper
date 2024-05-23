package com.jacobdamiangraham.groceryhelper.interfaces

interface IUserLogoutCallback {
    fun onLogoutSuccess(successMessage: String)

    fun onLogoutFailure(failureMessage: String)
}