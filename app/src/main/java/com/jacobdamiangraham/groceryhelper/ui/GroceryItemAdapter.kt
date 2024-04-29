package com.jacobdamiangraham.groceryhelper.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jacobdamiangraham.groceryhelper.R
import com.jacobdamiangraham.groceryhelper.model.GroceryItem

class GroceryItemAdapter(val context: Context, private val groceryItemList: List<GroceryItem>):
    RecyclerView.Adapter<GroceryItemAdapter.GroceryItemViewHolder>() {

        private lateinit var groceryItem: GroceryItem
        private var arrayListGroceryItems: ArrayList<View>

        init {
            arrayListGroceryItems = ArrayList()
        }

    inner class GroceryItemViewHolder(groceryItemView: View): RecyclerView.ViewHolder(groceryItemView) {
        val groceryItemName: TextView = groceryItemView.findViewById(R.id.groceryItemNameTextView)
        val groceryItemAmount: TextView = groceryItemView.findViewById(R.id.groceryItemAmountTextView)
        val groceryItemCost: TextView = groceryItemView.findViewById(R.id.itemCostTextView)
        val groceryItemStoreName: TextView = groceryItemView.findViewById(R.id.storeNameTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroceryItemViewHolder {
        val groceryItemInflater = LayoutInflater.from(parent.context)
        val groceryItemView = groceryItemInflater.inflate(R.layout.groceryitem, parent, false)
        arrayListGroceryItems.add(groceryItemView)

        return GroceryItemViewHolder(groceryItemView)
    }

    override fun getItemCount(): Int {
        return arrayListGroceryItems.size
    }

    override fun onBindViewHolder(holder: GroceryItemViewHolder, position: Int) {
        TODO("Not yet implemented")
    }
}