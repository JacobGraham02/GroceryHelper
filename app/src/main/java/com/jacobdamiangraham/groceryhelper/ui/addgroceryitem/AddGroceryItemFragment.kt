package com.jacobdamiangraham.groceryhelper.ui.addgroceryitem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.jacobdamiangraham.groceryhelper.R
import com.jacobdamiangraham.groceryhelper.databinding.FragmentAddGroceryItemBinding

class AddGroceryItemFragment: Fragment() {

    private var _binding: FragmentAddGroceryItemBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AddGroceryItemViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[AddGroceryItemViewModel::class.java]
        _binding = FragmentAddGroceryItemBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val groceryItemArgs: AddGroceryItemFragmentArgs by navArgs()

        val groceryItemValueConditions = mapOf(
            0 to { item: Any? -> item != null && item.toString() != "undefined" }, // Name
            1 to { item: Any? -> item != null && item != 1 },                      // Quantity
            2 to { item: Any? -> item != null && item != 0.00 },                   // Cost
            3 to { item: Any? -> item != null && item.toString() != "undefined" }  // Store
        )

        val arrayListBindingElements = arrayListOf(
            binding.addItemName,
            binding.addItemQuantity,
            binding.addItemCost,
            binding.addStoreName)

        val arrayListGroceryItemArgs = arrayListOf(
            groceryItemArgs.groceryItemName,
            groceryItemArgs.groceryItemQuantity,
            groceryItemArgs.groceryItemCost,
            groceryItemArgs.groceryItemStore,
        )

        val arrayListGroceryItemCategory = arrayListOf(
            "Baking",
            "Canned goods",
            "Cereal",
            "Condiments",
            "Dairy",
            "Deli",
            "Meat",
            "Pasta",
            "Rice",
            "Seafood",
            "Spice",
            "Sweet",
            "Vegetable"
        )

        for (index in arrayListBindingElements.indices) {
            val editText: EditText = arrayListBindingElements[index]
            val itemValue = arrayListGroceryItemArgs[index]

            groceryItemValueConditions[index]?.let { condition ->
                if (condition(itemValue)) {
                    editText.setText(itemValue.toString())
                }
            }
        }

        val groceryItemCategory = groceryItemArgs.groceryItemCategory

        if (arrayListGroceryItemCategory.contains(groceryItemCategory)) {
            val categoryAdapter =
                binding.addGroceryItemCategorySpinner.adapter as ArrayAdapter<String>
            val categoryPosition = categoryAdapter.getPosition(groceryItemCategory)
            binding.addGroceryItemCategorySpinner.setSelection(categoryPosition)
        }

        setupCategorySpinner()
        setupAddItemButton()

        return root
    }

    /*
    Android MVVM code commented out as of May 01, 2024. I get DeadObjectExceptions when attempting to observe the MutableLiveData object that contains
    the data for the grocery item. I have to manually set the input boxes inside of the onCreateView instead of dynamically setting them by using
    the observers. This will be corrected once I find a fix.

        viewModel.setGroceryItem(
            GroceryItem(
                groceryItemName,
                groceryItemId,
                groceryItemCategory,
                groceryItemStore,
                groceryItemQuantity,
                groceryItemCost
            )
        )
    private fun setupObservers() {
        viewModel.liveDataGroceryItem.observe(viewLifecycleOwner) { item ->
            val categoriesAdapter =
                binding.addGroceryItemCategorySpinner.adapter as ArrayAdapter<String>
            val categoryPosition = categoriesAdapter.getPosition(item.category)
            binding.addItemName.setText(item.name)
            binding.addItemQuantity.setText(item.quantity!!)
            binding.addItemCost.setText(String.format("%.2f", item.cost))
            binding.addStoreName.setText(item.store)
            binding.addGroceryItemCategorySpinner.setSelection(categoryPosition)
        }
    }
    */

    private fun setupAddItemButton() {
        binding.addItemButton.setOnClickListener {
            val groceryItemName = binding.addItemName.text.toString()
            val groceryItemQuantity = binding.addItemQuantity.text.toString().toInt()
            val groceryItemCost = binding.addItemCost.text.toString().toFloat()
            val groceryItemStore = binding.addStoreName.text.toString()
            val groceryItemCategory = binding.addGroceryItemCategorySpinner.selectedItem.toString()

            viewModel.addGroceryItemToFirebase(
                groceryItemName,
                groceryItemCategory,
                groceryItemStore,
                groceryItemQuantity,
                groceryItemCost
            )
        }
    }

    private fun setupCategorySpinner() {
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.categories,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.addGroceryItemCategorySpinner.adapter = adapter
        }
    }

    override fun onDestroyView() {
        viewModel.liveDataGroceryItem.removeObservers(viewLifecycleOwner)
        super.onDestroyView()
        _binding = null
    }
}
