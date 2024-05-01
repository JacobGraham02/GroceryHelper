package com.jacobdamiangraham.groceryhelper.ui.addgroceryitem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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
        val addGroceryItemViewModel = ViewModelProvider(this)
            .get(AddGroceryItemViewModel::class.java)

        _binding = FragmentAddGroceryItemBinding.inflate(inflater, container, false)
        val root: View = binding.root
        viewModel = ViewModelProvider(this).get(AddGroceryItemViewModel::class.java)

        val textView: TextView = binding.addItemPageLabel
        addGroceryItemViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        setupCategorySpinner()
        setupAddItemButton()

        return root
    }

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
        ).also {
            adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.addGroceryItemCategorySpinner.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}