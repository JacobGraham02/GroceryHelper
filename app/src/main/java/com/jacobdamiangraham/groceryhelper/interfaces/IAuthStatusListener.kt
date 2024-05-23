package com.jacobdamiangraham.groceryhelper.interfaces

interface IAuthStatusListener {
    fun onUserUnauthenticated(onUserUnauthenticatedMessage: String)
}