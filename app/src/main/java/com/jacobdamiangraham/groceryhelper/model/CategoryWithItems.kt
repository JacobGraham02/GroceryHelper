package com.jacobdamiangraham.groceryhelper.model

data class CategoryWithItems(
    val categoryName: String,
    val items: MutableList<GroceryItem>
)