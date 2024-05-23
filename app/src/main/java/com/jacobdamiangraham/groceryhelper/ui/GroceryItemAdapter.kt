package com.jacobdamiangraham.groceryhelper.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.jacobdamiangraham.groceryhelper.R
import com.jacobdamiangraham.groceryhelper.factory.PromptBuilderFactory
import com.jacobdamiangraham.groceryhelper.interfaces.IOnGroceryItemInteractionListener
import com.jacobdamiangraham.groceryhelper.model.DialogInformation
import com.jacobdamiangraham.groceryhelper.model.GroceryItem

class GroceryItemAdapter(
    private val context: Context,
    private val interactionListener: IOnGroceryItemInteractionListener?,
    private val onItemClick: (GroceryItem) -> Unit):
    RecyclerView.Adapter<GroceryItemAdapter.GroceryItemViewHolder>() {

        private var arrayListGroceryItems: ArrayList<View> = ArrayList()
        private var groceryItemList: MutableList<GroceryItem> = ArrayList()

    fun updateGroceryItems(newGroceryItems: MutableList<GroceryItem>) {
        groceryItemList = newGroceryItems
        notifyDataSetChanged()
    }

    inner class GroceryItemViewHolder(groceryItemView: View): RecyclerView.ViewHolder(groceryItemView) {
        val groceryItemCategoryHeading: TextView = groceryItemView.findViewById(R.id.itemCategoryHeading)
        val groceryItemName: TextView = groceryItemView.findViewById(R.id.groceryItemNameTextView)
        val groceryItemAmount: TextView = groceryItemView.findViewById(R.id.groceryItemAmountTextView)
        val groceryItemCost: TextView = groceryItemView.findViewById(R.id.itemCostTextView)
        val groceryItemCategory: TextView = groceryItemView.findViewById(R.id.itemCategoryTextView)
        val groceryItemArrowIndicator: AppCompatImageView = groceryItemView.findViewById(R.id.arrowIndicator)
        val groceryItemAdditionalInformation: LinearLayout = groceryItemView.findViewById(R.id.linearLayoutAdditionalInformation)
        val deleteGroceryItemButton: Button = groceryItemView.findViewById(R.id.deleteStoreItemButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroceryItemViewHolder {
        val groceryItemInflater = LayoutInflater.from(parent.context)
        val groceryItemView = groceryItemInflater.inflate(R.layout.groceryitem, parent, false)
        arrayListGroceryItems.add(groceryItemView)

        return GroceryItemViewHolder(groceryItemView)
    }

    override fun getItemCount(): Int {
        return groceryItemList.size
    }

    override fun onBindViewHolder(holder: GroceryItemViewHolder, position: Int) {
        val groceryItem = groceryItemList[position]
        holder.itemView.setOnClickListener {
            onItemClick(groceryItem)
        }
        with(holder) {
            var isExpanded = false
            groceryItemCategoryHeading.text = groceryItem.category
            groceryItemName.text = groceryItem.name
            groceryItemAmount.text = groceryItem.quantity.toString()
            groceryItemCost.text = groceryItem.cost.toString()
            groceryItemCategory.text = groceryItem.category
            groceryItemArrowIndicator.setOnClickListener {
                if (isExpanded) {
                    groceryItemAdditionalInformation.visibility = View.GONE
                    groceryItemArrowIndicator.rotation = 0f
                } else {
                    groceryItemAdditionalInformation.visibility = View.VISIBLE
                    groceryItemArrowIndicator.rotation = 180f
                }
                isExpanded = !isExpanded
            }
            deleteGroceryItemButton.setOnClickListener {
                val dialogInfo = DialogInformation(
                    title = "Confirm delete",
                    message = "Are you sure you want to delete ${groceryItem.name} from your list?"
                )
                val alertDialogGenerator = PromptBuilderFactory.getAlertDialogGenerator(
                    "confirmation")
                alertDialogGenerator.configure(
                    AlertDialog.Builder(context),
                    dialogInfo,
                    positiveButtonAction = {
                        deleteGroceryItem(position)
                        interactionListener!!.onDeleteGroceryItem(groceryItem)
                    },
                ).show()
            }
        }
    }

    private fun deleteGroceryItem(position: Int) {
        groceryItemList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, groceryItemList.size)
    }
}