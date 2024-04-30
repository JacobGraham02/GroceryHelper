package com.jacobdamiangraham.groceryhelper.model

import java.util.UUID

data class GroceryItem(var name: String, var id: UUID,
                       var category: String, var store: String?,
                       var quantity: Int? = 1, var cost: Float?) {

    init {
            require(name.isNotBlank()) {
                "The item must have a name"
            }
            require(category.isNotBlank()) {
                "The item must have an assigned category (e.g., vegetable)"
            }
            require(store?.isNotBlank() ?: true) {
                "The store the item is located in must not be blank"
            }
            require(quantity!! >= 1) {
                "The quantity of item must be larger than 0"
            }
            require(cost!! > 0.00) {
                "The cost of item must be greater than $0.00"
            }
        }

    fun updateQuantity(newQuantity: Int) {
        require(newQuantity >= 1) {
            "The update quantity for this grocery item must be larger than 0"
        }
        quantity = newQuantity
    }

    fun updateCost(newCost: Float) {
        require(newCost > 0.00) {
            "The new cost for this grocery item must be greater than 0.00"
        }
        cost = newCost
    }

    fun updateName(newName: String) {
        require(newName.isNotBlank()) {
            "The new name for this grocery item must not be blank"
        }
        name = newName
    }

    fun updateCategory(newCategory: String) {
        require(newCategory.isNotBlank()) {
            "The new category for this grocery item must not be blank"
        }
        category = newCategory
    }

    fun updateStore(newStore: String) {
        require(newStore.isNotBlank()) {
            "The new store for this grocery item must not be blank"
        }
        store = newStore;
    }

    fun getGroceryItem():GroceryItem {
        return this
    }

    override fun toString(): String {
        return "Grocery item: name=${name}\n category=${category}\n store=${store}\n quantity=${quantity}\n cost=${cost}"
    }
}
