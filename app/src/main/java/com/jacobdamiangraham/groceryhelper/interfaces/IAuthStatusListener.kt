package com.jacobdamiangraham.groceryhelper.interfaces

interface IAuthStatusListener {

    fun onUserAuthenticate()
    fun onUserUnauthenticated()
}