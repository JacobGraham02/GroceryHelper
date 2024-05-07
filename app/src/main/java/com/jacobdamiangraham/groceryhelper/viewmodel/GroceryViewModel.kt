package com.jacobdamiangraham.groceryhelper.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jacobdamiangraham.groceryhelper.model.GroceryItem
import com.jacobdamiangraham.groceryhelper.repository.GroceryItemsRepository
import com.jacobdamiangraham.groceryhelper.storage.FirebaseStorage

class GroceryViewModel(storeName: String? = "food basics"): ViewModel() {
    private val firebaseStorage: FirebaseStorage = FirebaseStorage("users")
    private val repository = GroceryItemsRepository(firebaseStorage)

    val groceryItems: MutableLiveData<MutableList<GroceryItem>> = repository.getMutableListOfGroceryItems(storeName)
}