package com.jacobdamiangraham.groceryhelper.ui.mergelist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jacobdamiangraham.groceryhelper.databinding.FragmentMergeListBinding
import com.jacobdamiangraham.groceryhelper.storage.FirebaseStorage

class MergeListFragment : Fragment() {

    private var _binding: FragmentMergeListBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMergeListBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}