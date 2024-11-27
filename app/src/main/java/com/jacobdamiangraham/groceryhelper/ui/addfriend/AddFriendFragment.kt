package com.jacobdamiangraham.groceryhelper.ui.addfriend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.firebase.auth.FirebaseAuth
import com.jacobdamiangraham.groceryhelper.databinding.FragmentAddFriendBinding

class AddFriendFragment: Fragment() {
    private var _binding: FragmentAddFriendBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AddFriendViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddFriendBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun setupObservers() {
        viewModel.friendRequestStatus.observe(viewLifecycleOwner, Observer { (success, message) ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        })

        viewModel.userStatus.observe(viewLifecycleOwner, Observer { (success, message) ->
            if (success) {
                val user = viewModel.user.value
                user?.let {
                    sendFriendRequest(it.uid)
                }
            } else {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun setupListeners() {
        binding.sendFriendRequestButton.setOnClickListener {
            val email = binding.emailFieldEditText.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(requireContext(), "You must enter a valid email address", Toast.LENGTH_LONG).show()
            } else {
                getUserByEmail(email)
            }
        }
    }

    private fun getUserByEmail(email: String) {
        val userId = email
        viewModel.getUser(userId)
    }

    private fun sendFriendRequest(friendUserId: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser
        if (currentUserId != null) {
            viewModel.sendFriendRequest(friendUserId, currentUserId.uid)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}