package com.jacobdamiangraham.groceryhelper.ui.addgroceryitem

import android.app.VoiceInteractor.Prompt
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.jacobdamiangraham.groceryhelper.R
import com.jacobdamiangraham.groceryhelper.databinding.FragmentAddGroceryItemBinding
import com.jacobdamiangraham.groceryhelper.enums.AddGroceryItemInputType
import com.jacobdamiangraham.groceryhelper.factory.PromptBuilderFactory
import com.jacobdamiangraham.groceryhelper.interfaces.IAddGroceryItemCallback
import com.jacobdamiangraham.groceryhelper.model.DialogInformation
import com.jacobdamiangraham.groceryhelper.model.GroceryItem
import com.jacobdamiangraham.groceryhelper.storage.FirebaseStorage
import com.jacobdamiangraham.groceryhelper.utils.ValidationUtil
import java.util.UUID

class AddGroceryItemFragment: Fragment() {

    private var _binding: FragmentAddGroceryItemBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AddGroceryItemViewModel

    private val firebaseStorage: FirebaseStorage = FirebaseStorage("users")

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
        )

        val arrayListBindingElements = arrayListOf(
            binding.addItemName,
            binding.addItemQuantity,
            binding.addItemCost)

        val arrayListGroceryItemArgs = arrayListOf(
            groceryItemArgs.groceryItemName,
            groceryItemArgs.groceryItemQuantity,
            groceryItemArgs.groceryItemCost)

        val groceryItemCategory = groceryItemArgs.groceryItemCategory
        val groceryItemStore = groceryItemArgs.groceryItemStore

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

        val arrayListGroceryStoreNameCategory = arrayListOf(
            "food basics",
            "zehrs"
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

        if (groceryItemArgs.groceryItemName != "undefined") {
            binding.addItemButton.text = getString(R.string.grocery_list_modify_item, groceryItemArgs.groceryItemName)
            binding.addItemPageLabel.text = getString(R.string.grocery_list_title_modify, groceryItemArgs.groceryItemName)
        }

        setupCategorySpinner()
        setupStoreNameSpinner()
        setupAddItemButton()

        if (arrayListGroceryItemCategory.contains(groceryItemCategory)) {
            val categoryAdapter =
                binding.addGroceryItemCategorySpinner.adapter as ArrayAdapter<String>
            val categoryPosition = categoryAdapter.getPosition(groceryItemCategory)
            binding.addGroceryItemCategorySpinner.setSelection(categoryPosition)
        }
        if (arrayListGroceryStoreNameCategory.contains(groceryItemStore)) {
            val storeAdapter =
                binding.addGroceryStoreNameSpinner.adapter as ArrayAdapter<String>
            val storePosition = storeAdapter.getPosition(groceryItemStore)
            binding.addGroceryStoreNameSpinner.setSelection(storePosition)
        }

        binding.addItemName.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                validate(AddGroceryItemInputType.NAME, s.toString())
            }
        })

        binding.addItemCost.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                validate(AddGroceryItemInputType.COST, s.toString())
            }
        })

        binding.addItemQuantity.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                validate(AddGroceryItemInputType.QUANTITY, s.toString())
            }
        })

        return root
    }

    private fun validate(addGroceryItemInputType: AddGroceryItemInputType, value: String) {
        when (addGroceryItemInputType) {
            AddGroceryItemInputType.NAME -> {
                if (ValidationUtil.isValidGroceryItemString(value)) {
                    binding.addItemName.setBackgroundResource(R.drawable.edit_text_valid)
                    binding.addItemNameLabel.text = getString(R.string.valid_name)
                    binding.addItemNameLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
                    binding.addItemName.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
                } else {
                    binding.addItemName.setBackgroundResource(R.drawable.edit_text_invalid)
                    binding.addItemNameLabel.text = getString(R.string.invalid_name)
                    binding.addItemNameLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                    binding.addItemName.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                }
            }
            AddGroceryItemInputType.QUANTITY -> {
                if (ValidationUtil.isValidGroceryItemString(value)) {
                    binding.addItemQuantity.setBackgroundResource(R.drawable.edit_text_valid)
                    binding.addItemQuantityLabel.text = getString(R.string.valid_quantity)
                    binding.addItemQuantityLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
                    binding.addItemQuantity.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
                } else {
                    binding.addItemQuantity.setBackgroundResource(R.drawable.edit_text_invalid)
                    binding.addItemQuantityLabel.text = getString(R.string.invalid_quantity)
                    binding.addItemQuantityLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                    binding.addItemQuantity.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                }
            }
            AddGroceryItemInputType.COST -> {
                if (ValidationUtil.isValidGroceryItemString(value)) {
                    binding.addItemCost.setBackgroundResource(R.drawable.edit_text_valid)
                    binding.addItemCostLabel.text = getString(R.string.valid_cost)
                    binding.addItemCostLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
                    binding.addItemCost.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
                } else {
                    binding.addItemCost.setBackgroundResource(R.drawable.edit_text_invalid)
                    binding.addItemCostLabel.text = getString(R.string.invalid_cost)
                    binding.addItemCostLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                    binding.addItemCost.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                }
            }
        }
    }

    private fun setupAddItemButton() {
        binding.addItemButton.setOnClickListener {
            val groceryItemName = binding.addItemName.text?.toString() ?: ""
            val groceryItemQuantity = binding.addItemQuantity.text?.toString()?.toIntOrNull() ?: 0
            val groceryItemCost = binding.addItemCost.text?.toString()?.toFloatOrNull() ?: 0.0f
            val groceryItemStore = binding.addGroceryStoreNameSpinner.selectedItem?.toString() ?: ""
            val groceryItemCategory = binding.addGroceryItemCategorySpinner.selectedItem?.toString() ?: ""

            if (!(ValidationUtil.validateGroceryItemInputs(
                    groceryItemName,
                    groceryItemQuantity,
                    groceryItemCategory,
                    groceryItemStore,
                    groceryItemCost))) {

                Toast.makeText(
                    context,
                    "Please enter valid data",
                    Toast.LENGTH_LONG)
                    .show()
                return@setOnClickListener
            }

            val groceryItemUUID = UUID.randomUUID()
            val newGroceryItem = GroceryItem(
                groceryItemName,
                groceryItemUUID.toString(),
                groceryItemCategory,
                groceryItemStore,
                groceryItemQuantity,
                groceryItemCost)

            val dialogInfo = DialogInformation(
                title = "Confirm add grocery item",
                message = "Are you sure you want to add this grocery item?"
            )
            val alertDialogGenerator = PromptBuilderFactory.getAlertDialogGenerator(
                "confirmation"
            )
            alertDialogGenerator.configure(
                requireContext(),
                AlertDialog.Builder(requireContext()),
                dialogInfo,
                positiveButtonAction = {
                    addGroceryItemToFirebase(newGroceryItem)
                }
            )
        }
    }

    private fun addGroceryItemToFirebase(groceryItem: GroceryItem) {
        try {
            firebaseStorage.addGroceryItemToFirebase(
                groceryItem,
                object : IAddGroceryItemCallback {
                    override fun onAddSuccess(successMessage: String) {
                        Toast.makeText(
                            context,
                            successMessage,
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }

                    override fun onAddFailure(failureMessage: String) {
                        Toast.makeText(
                            context,
                            failureMessage,
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }
                })
        } catch (e: Exception) {
            throw Error("There was an error when attempting to insert grocery item into firebase: ${e}")
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

    private fun setupStoreNameSpinner() {
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.stores,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.addGroceryStoreNameSpinner.adapter = adapter
        }
    }

    override fun onDestroyView() {
        viewModel.liveDataGroceryItem.removeObservers(viewLifecycleOwner)
        super.onDestroyView()
        _binding = null
    }
}
