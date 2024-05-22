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
import com.jacobdamiangraham.groceryhelper.interfaces.IOnGroceryItemInteractionListener
import com.jacobdamiangraham.groceryhelper.model.GroceryItem
import com.jacobdamiangraham.groceryhelper.ui.GroceryItemAdapter
import com.jacobdamiangraham.groceryhelper.viewmodel.GroceryViewModel

class HomeFragment : Fragment(), IOnGroceryItemInteractionListener {

    private lateinit var viewModel: GroceryViewModel
    private lateinit var adapter: GroceryItemAdapter

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val storeName = arguments?.getString("storeName")!!
        setupViewModel(storeName)
        setupRecyclerView(storeName)

        binding.yourGroceryListTextView.text = getString(R.string.grocery_list_title, storeName)

        return root
    }

    private fun setupViewModel(storeName: String) {
        val factory = GroceryViewModelFactory(storeName)
        viewModel = ViewModelProvider(this, factory)[GroceryViewModel::class.java]

        viewModel.groceryItems.observe(viewLifecycleOwner) { items ->
            val filteredItems = items.filter { it.store == storeName }
            adapter.updateGroceryItems(filteredItems as MutableList<GroceryItem>)
        }
    }

    private fun setupRecyclerView(storeName: String) {
        adapter = GroceryItemAdapter(requireContext(), this) { groceryItem ->
            navigateToEditGroceryItem(groceryItem, storeName)
        }
        binding.recyclerViewGroceryItemsList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@HomeFragment.adapter
        }
    }

    private fun navigateToEditGroceryItem(groceryItem: GroceryItem, storeName: String) {
        val action = HomeFragmentDirections.actionHomeFragmentToAddGroceryItemFragment(
            groceryItem.id,
            groceryItem.name,
            groceryItem.category,
            storeName,
            groceryItem.quantity ?: 0,
            groceryItem.cost ?: 0.00f
        )
        try {
            if (isAdded && findNavController().currentDestination?.id == R.id.nav_home) {
                findNavController().navigate(action)
            }
        } catch (e: Exception) {
            Log.e("NavigationError", "Failed to navigate", e)
        }
    }

    override fun onDeleteGroceryItem(item: GroceryItem) {
        viewModel.deleteGroceryItem(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.groceryItems.removeObservers(viewLifecycleOwner)
        _binding = null
    }
}