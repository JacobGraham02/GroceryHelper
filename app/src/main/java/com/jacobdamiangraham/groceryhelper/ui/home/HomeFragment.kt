package com.jacobdamiangraham.groceryhelper.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.jacobdamiangraham.groceryhelper.databinding.FragmentHomeBinding
import com.jacobdamiangraham.groceryhelper.ui.GroceryItemAdapter
import com.jacobdamiangraham.groceryhelper.viewmodel.GroceryViewModel

class HomeFragment : Fragment() {

    private lateinit var viewModel: GroceryViewModel
    private lateinit var adapter: GroceryItemAdapter

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
                ViewModelProvider(this).get(GroceryViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        viewModel = ViewModelProvider(this).get(GroceryViewModel::class.java)
        adapter = GroceryItemAdapter(requireContext())
        binding.recyclerViewGroceryItemsList.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewGroceryItemsList.adapter = adapter

        viewModel.groceryItems.observe(viewLifecycleOwner, { items ->
            adapter.updateGroceryItems(items)
        })

//        val textView: TextView = binding.yourGroceryListTextView
//        homeViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}