package com.jacobdamiangraham.groceryhelper.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.jacobdamiangraham.groceryhelper.model.GroceryItem
import com.jacobdamiangraham.groceryhelper.repository.GroceryItemsRepository
import com.jacobdamiangraham.groceryhelper.storage.FirebaseStorage

class GroceryViewModel: ViewModel() {
    private val firebaseStorage: FirebaseStorage = FirebaseStorage()
    private val repository = GroceryItemsRepository(firebaseStorage)

    val groceryItems: LiveData<List<GroceryItem>> = repository.getMutableListOfGroceryItems()
}