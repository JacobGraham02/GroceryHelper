package com.jacobdamiangraham.groceryhelper.ui

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.jacobdamiangraham.groceryhelper.model.GroceryItem

class GroceryItemAdapter(val context: Context, private val groceryItemList: List<GroceryItem>):
    RecyclerView.Adapter<GroceryItemAdapter.GroceryItemViewHolder>() {

        private lateinit var groceryItem: GroceryItem
        private var arrayListGroceryItems: ArrayList<View>

    inner class GroceryItemViewHolder(groceryItemView: View): RecyclerView.ViewHolder(groceryItemView) {

    }
}