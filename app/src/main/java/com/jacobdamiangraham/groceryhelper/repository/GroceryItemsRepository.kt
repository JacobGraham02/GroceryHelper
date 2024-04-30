package com.jacobdamiangraham.groceryhelper.repository

import androidx.lifecycle.MutableLiveData
import com.jacobdamiangraham.groceryhelper.model.GroceryItem
import com.jacobdamiangraham.groceryhelper.storage.FirebaseStorage

class GroceryItemsRepository(private val firebaseStorageInstance: FirebaseStorage) {

    fun getMutableListOfGroceryItems(): MutableLiveData<List<GroceryItem>> {
        val firebaseCollectionItems = firebaseStorageInstance.getMutableLiveDataListOfGroceryItem("groceryitems")
        return firebaseCollectionItems
    }
}