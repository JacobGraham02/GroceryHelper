package com.jacobdamiangraham.groceryhelper.ui.addgroceryitem

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jacobdamiangraham.groceryhelper.model.GroceryItem

class AddGroceryItemViewModel: ViewModel() {

    private val _mutableLiveDataGroceryItem = MutableLiveData<GroceryItem>()

    val liveDataGroceryItem: LiveData<GroceryItem> = _mutableLiveDataGroceryItem

    /*
      Android MVVM code commented out as of May 01, 2024. I get DeadObjectExceptions when attempting to observe the MutableLiveData object that contains
    the data for the grocery item. I have to manually set the input boxes inside of the onCreateView instead of dynamically setting them by using
    the observers. This will be corrected once I find a fix.

    fun setGroceryItem(groceryItem: GroceryItem) {
        _mutableLiveDataGroceryItem.value = groceryItem
    }
    */
}