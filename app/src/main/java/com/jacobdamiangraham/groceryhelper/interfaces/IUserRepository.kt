package com.jacobdamiangraham.groceryhelper.interfaces

import com.jacobdamiangraham.groceryhelper.model.User

interface IUserRepository {
    fun getUser(userId: String, callback: (User?, String) -> Unit)
    fun addUser(currentUserId: String, friendUserId: String, user: User, callback: (Boolean, String) -> Unit)
    fun deleteUser(currentUserId: String, friendUserId: String, callback: (Boolean, String) -> Unit)
    fun sendFriendRequest(toUserId: String, fromUserId: String, callback: (Boolean, String) -> Unit)
    fun removeFriendRequest(toUserId: String, fromUserId: String, callback: (Boolean, String) -> Unit)
}