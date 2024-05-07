package com.jacobdamiangraham.groceryhelper.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jacobdamiangraham.groceryhelper.model.GroceryItem
import com.jacobdamiangraham.groceryhelper.repository.GroceryItemsRepository
import com.jacobdamiangraham.groceryhelper.storage.FirebaseStorage

class GroceryViewModel(storeName: String? = "food basics"): ViewModel() {
    private val firebaseStorage: FirebaseStorage = FirebaseStorage("users")
    private val repository = GroceryItemsRepository(firebaseStorage)

    val groceryItems: MutableLiveData<MutableList<GroceryItem>> = repository.getMutableListOfGroceryItems(storeName)

    fun sortByNameAToZ() {
        val sortedList = groceryItems.value?.sortedBy { it.name }
        groceryItems.value = sortedList?.toMutableList()
    }

    fun sortByCategoryAToZ() {
        val sortedList = groceryItems.value?.sortedBy { it.category }
        groceryItems.value = sortedList?.toMutableList()
    }

    fun sortByCostHighToLow() {
        val sortedList = groceryItems.value?.sortedByDescending { it.cost }
        groceryItems.value = sortedList?.toMutableList()
    }

    fun sortByCostLowToHigh() {
        val sortedList = groceryItems.value?.sortedBy { it.cost }
        groceryItems.value = sortedList?.toMutableList()
    }
}