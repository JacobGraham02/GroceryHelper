package com.jacobdamiangraham.groceryhelper.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.jacobdamiangraham.groceryhelper.R
import com.jacobdamiangraham.groceryhelper.databinding.FragmentHomeBinding
import com.jacobdamiangraham.groceryhelper.factory.GroceryViewModelFactory
import com.jacobdamiangraham.groceryhelper.ui.GroceryItemAdapter
import com.jacobdamiangraham.groceryhelper.viewmodel.GroceryViewModel

class HomeFragment : Fragment() {

    private lateinit var viewModel: GroceryViewModel
    private lateinit var adapter: GroceryItemAdapter
    private lateinit var viewModelFactory: GroceryViewModelFactory

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val storeName = arguments?.getString("storeName")

        if (storeName != null && (storeName == "food basics" || storeName == "zehrs")) {
            viewModelFactory = GroceryViewModelFactory(storeName)
            binding.yourGroceryListTextView.text = getString(R.string.grocery_list_title, storeName)
        } else {
            viewModelFactory = GroceryViewModelFactory("food basics")
            binding.yourGroceryListTextView.text = getString(R.string.grocery_list_title, "food basics")
        }

        viewModel = ViewModelProvider(this, viewModelFactory).get(GroceryViewModel::class.java)

        adapter = GroceryItemAdapter(requireContext()) { selectedGroceryItem ->
            val groceryItemId = selectedGroceryItem.id
            val groceryItemName = selectedGroceryItem.name
            val groceryItemCategory = selectedGroceryItem.category
            val groceryItemStore = selectedGroceryItem.store ?: " "
            val groceryItemQuantity = selectedGroceryItem.quantity ?: 0
            val groceryItemCost = selectedGroceryItem.cost ?: 0.00f

            val action = HomeFragmentDirections.actionHomeFragmentToAddGroceryItemFragment(
                groceryItemId,
                groceryItemName,
                groceryItemCategory,
                groceryItemStore,
                groceryItemQuantity,
                groceryItemCost
            )

            try {
                if (isAdded && findNavController().currentDestination?.id == R.id.nav_home) {
                    findNavController().navigate(action)
                }
            } catch (e: Exception) {
                Log.e("NavigationError", "Failed to navigate", e)
            }
        }

        binding.recyclerViewGroceryItemsList.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewGroceryItemsList.adapter = adapter

        viewModel.groceryItems.observe(viewLifecycleOwner, { items ->
            adapter.updateGroceryItems(items)
        })

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.groceryItems.removeObservers(viewLifecycleOwner)
        _binding = null
    }
}