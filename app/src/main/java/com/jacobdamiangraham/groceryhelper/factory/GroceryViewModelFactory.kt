package com.jacobdamiangraham.groceryhelper.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.jacobdamiangraham.groceryhelper.viewmodel.GroceryViewModel

class GroceryViewModelFactory(private val storeName: String): ViewModelProvider.Factory {

    override fun <T: ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(GroceryViewModel::class.java)) {
            return GroceryViewModel(storeName) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}