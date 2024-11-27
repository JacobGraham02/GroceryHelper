package com.jacobdamiangraham.groceryhelper.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jacobdamiangraham.groceryhelper.model.User
import com.jacobdamiangraham.groceryhelper.storage.FirebaseStorage
import kotlinx.coroutines.launch

class AddFriendViewModel(private val firebaseStorage: FirebaseStorage): ViewModel() {
    private val friendRequestStatusMutableLiveData = MutableLiveData<Pair<Boolean, String>>()
    val friendRequestStatus: LiveData<Pair<Boolean, String>> get() = friendRequestStatusMutableLiveData

    private val removeFriendRequestStatusMutableLiveData = MutableLiveData<Pair<Boolean, String>>()
    val removeFriendRequestStatus: LiveData<Pair<Boolean, String>> get() = removeFriendRequestStatusMutableLiveData

    private val userMutableLiveData = MutableLiveData<User?>()
    val user: LiveData<User?> get() = userMutableLiveData

    private val userStatusMutableLiveData = MutableLiveData<Pair<Boolean, String>>()
    val userStatus: LiveData<Pair<Boolean, String>> get() = userStatusMutableLiveData


    fun sendFriendRequest(toUserId: String, fromUserId: String) {
        viewModelScope.launch {
            firebaseStorage.sendFriendRequest(toUserId, fromUserId) { success, message ->
                friendRequestStatusMutableLiveData.postValue(Pair(success, message))
            }
        }
    }

    fun removeFriendRequest(toUserId: String, fromUserId: String) {
        viewModelScope.launch {
            firebaseStorage.removeFriendRequest(toUserId, fromUserId) { success, message ->
                removeFriendRequestStatusMutableLiveData.postValue(Pair(success, message))
            }
        }
    }

    fun getUser(userId: String) {
        viewModelScope.launch {
            firebaseStorage.getUser(userId) { retrievedUser, message ->
                userMutableLiveData.postValue(retrievedUser)
                val userBeenRetrieved: Boolean = retrievedUser != null
                userStatusMutableLiveData.postValue(Pair(userBeenRetrieved, message))
            }
        }
    }
}