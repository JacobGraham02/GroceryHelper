package com.jacobdamiangraham.groceryhelper.ui.addgroceryitem

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jacobdamiangraham.groceryhelper.model.GroceryItem
import com.jacobdamiangraham.groceryhelper.storage.FirebaseStorage
import java.util.UUID

class AddGroceryItemViewModel: ViewModel() {

    private val firebaseStorage: FirebaseStorage = FirebaseStorage()

    fun addGroceryItemToFirebase(name: String, category: String, store: String, quantity: Int, cost: Float) {
        if (validateInput(name, category, store, quantity, cost)) {
            val groceryItemUUID = UUID.randomUUID()
            val newGroceryItem = GroceryItem(name, groceryItemUUID, category, store, quantity, cost)
            firebaseStorage.addGroceryItemToFirebase(newGroceryItem, "groceryitems")
        }
    }

    private fun validateInput(name: String, category: String, store: String, quantity: Int, cost: Float): Boolean {
        return name.isNotBlank() && category.isNotBlank() && store.isNotBlank() && quantity >= 1 && cost > 0.00
    }

    private val _text = MutableLiveData<String>().apply {
        value = "This is the add grocery item fragment"
    }
    val text: LiveData<String> = _text
}