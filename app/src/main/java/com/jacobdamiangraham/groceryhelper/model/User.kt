package com.jacobdamiangraham.groceryhelper.model

data class User(
    var email: String,
    var uid: String,
    var friends: List<String> = listOf(),
    var groceryItems: List<GroceryItem> = listOf(),
    var groceryStores: List<GroceryItem> = listOf())