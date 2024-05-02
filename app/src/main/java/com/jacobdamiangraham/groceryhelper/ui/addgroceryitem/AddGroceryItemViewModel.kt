package com.jacobdamiangraham.groceryhelper.ui.addgroceryitem

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jacobdamiangraham.groceryhelper.model.GroceryItem
import com.jacobdamiangraham.groceryhelper.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class AddGroceryItemViewModel: ViewModel() {

    private val _mutableLiveDataGroceryItem = MutableLiveData<GroceryItem>()

    val liveDataGroceryItem: LiveData<GroceryItem> = _mutableLiveDataGroceryItem

    private val firebaseStorage: FirebaseStorage = FirebaseStorage()

    fun addGroceryItemToFirebase(name: String, category: String, store: String, quantity: Int, cost: Float) {
        if (validateInput(name, category, store, quantity, cost)) {
            val groceryItemUUID = UUID.randomUUID()
            val newGroceryItem = GroceryItem(name, groceryItemUUID.toString(), category, store, quantity, cost)
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    firebaseStorage.addGroceryItemToFirebase(newGroceryItem)
                } catch (e: Exception) {
                    // Handle any exceptions, possibly re-throw on main thread or use postValue to update an error message LiveData
                }
            }
        }
    }

    /*
      Android MVVM code commented out as of May 01, 2024. I get DeadObjectExceptions when attempting to observe the MutableLiveData object that contains
    the data for the grocery item. I have to manually set the input boxes inside of the onCreateView instead of dynamically setting them by using
    the observers. This will be corrected once I find a fix.

    fun setGroceryItem(groceryItem: GroceryItem) {
        _mutableLiveDataGroceryItem.value = groceryItem
    }
    */

    private fun validateInput(name: String, category: String, store: String, quantity: Int, cost: Float): Boolean {
        return name.isNotBlank() && category.isNotBlank() && store.isNotBlank() && quantity >= 1 && cost > 0.00
    }
}