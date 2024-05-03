package com.jacobdamiangraham.groceryhelper.interfaces

interface IUserRegistrationCallback {
    fun onRegistrationSuccess(successMessage: String)
    fun onRegistrationFailure(errorMessage: String)
}