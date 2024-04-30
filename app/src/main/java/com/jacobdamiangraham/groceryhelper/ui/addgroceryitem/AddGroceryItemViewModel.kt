package com.jacobdamiangraham.groceryhelper.ui.addgroceryitem

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AddGroceryItemViewModel: ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is the add grocery item fragment"
    }
    val text: LiveData<String> = _text
}