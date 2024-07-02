package com.jacobdamiangraham.groceryhelper.interfaces

interface IUserLoginCallback {

    fun onLoginSuccess(successMessage: String)

    fun onLoginFailure(failureMessage: String)

    fun onVerifyEmailFail(failureMessage: String)
}