package com.jacobdamiangraham.groceryhelper.interfaces

import com.jacobdamiangraham.groceryhelper.model.GroceryItem

interface IOnGroceryItemInteractionListener {
    fun onDeleteGroceryItem(item: GroceryItem)
}