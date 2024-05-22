package com.jacobdamiangraham.groceryhelper.storage

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.security.crypto.MasterKeys
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.jacobdamiangraham.groceryhelper.event.Observable
import com.jacobdamiangraham.groceryhelper.event.UserDeleteAccountEvent
import com.jacobdamiangraham.groceryhelper.event.UserLogoutAccountEvent
import com.jacobdamiangraham.groceryhelper.interfaces.IAddGroceryItemCallback
import com.jacobdamiangraham.groceryhelper.interfaces.IAddGroceryStoreCallback
import com.jacobdamiangraham.groceryhelper.interfaces.IAuthStatusListener
import com.jacobdamiangraham.groceryhelper.interfaces.IDeleteGroceryItemCallback
import com.jacobdamiangraham.groceryhelper.interfaces.IMergeGroceryListOperation
import com.jacobdamiangraham.groceryhelper.interfaces.IUserLoginCallback
import com.jacobdamiangraham.groceryhelper.interfaces.IUserLogoutCallback
import com.jacobdamiangraham.groceryhelper.interfaces.IUserRegistrationCallback
import com.jacobdamiangraham.groceryhelper.model.GroceryItem
import com.jacobdamiangraham.groceryhelper.model.User
import com.jacobdamiangraham.groceryhelper.ui.signin.SignInView

class FirebaseStorage() {

    private var firebaseAuthentication: FirebaseAuth = Firebase.auth
    private lateinit var firebaseGroceryItemCollectionInstance: DocumentReference
    private lateinit var firebaseUserCollectionInstance: CollectionReference
    private var mutableGroceryItemList: MutableLiveData<MutableList<GroceryItem>> = MutableLiveData<MutableList<GroceryItem>>()
    private lateinit var userId: String

    val deleteAccountObserver = Observable<UserDeleteAccountEvent>()

    init {
        getCollectionOfGroceryItems()
        getCollectionOfUsers()
    }

    private fun getCollectionOfGroceryItems() {
        val firebaseCurrentUser = Firebase.auth.currentUser
        if (firebaseCurrentUser != null) {
            userId = firebaseCurrentUser.uid
            // Assuming each user has a document under "users" and their items are under "groceryitems" collection
            firebaseGroceryItemCollectionInstance = FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
        }
    }

    private fun getCollectionOfUsers() {
        firebaseUserCollectionInstance = FirebaseFirestore.getInstance().collection("users")
    }

    fun deleteGroceryItem(groceryItem: GroceryItem, callback: IDeleteGroceryItemCallback) {
        // Start a transaction to safely modify the array within the document
        val groceryItemId = groceryItem.id
        FirebaseFirestore.getInstance().runTransaction { transaction ->
            val snapshot = transaction.get(firebaseGroceryItemCollectionInstance)
            val groceryItems = snapshot.get("groceryItems") as? List<Map<String, Any>> ?: listOf()

            val updatedItems = groceryItems.filterNot { it["id"] == groceryItemId }

            transaction.update(firebaseGroceryItemCollectionInstance, "groceryItems", updatedItems)
            null // Kotlin requires a return for the transaction block, use null for transactions not returning a value
        }.addOnSuccessListener {
            callback.onDeleteSuccess("Item successfully deleted.")
        }.addOnFailureListener { e ->
            Log.e("FirebaseError", "Failed to delete item: ${e.message}")
            callback.onDeleteFailure("Failed to delete item: ${e.message}")
        }
    }

    private fun getAllUsers(): MutableList<User> {
        val currentFirebaseUser = Firebase.auth.currentUser
        if (currentFirebaseUser != null) {
            val userList = mutableListOf<User>()
            firebaseUserCollectionInstance
                .get()
                .addOnSuccessListener { firebaseUsersQuerySnapshot ->
                    for (userDocument in firebaseUsersQuerySnapshot.documents) {
                        userDocument.toObject(User::class.java)?.let {
                            userList.add(it)
                        }
                    }
                }
            return userList
        }
        return mutableListOf()
    }

    private fun getGroceryItemsFromCollection(storeName: String?) {
        val currentFirebaseUser = Firebase.auth.currentUser
        if (currentFirebaseUser != null) {
            val firebaseUserId = currentFirebaseUser.uid
            firebaseUserCollectionInstance
                .document(firebaseUserId)
                .get()
                .addOnSuccessListener { userDocument ->
                    val groceryItems = userDocument
                        .get("groceryItems") as? List<Map<String, Any>>
                    if (groceryItems != null) {
                        val groceryItemList = groceryItems
                            .filter {
                                it["store"] == storeName
                            }
                            .map {
                                convertJsonToGroceryItemObjects(it)
                            }
                        mutableGroceryItemList.value = ArrayList(groceryItemList)
                    }
                }
        }
    }

    fun deleteUserAccount(context: Context) {
        val currentUser = firebaseAuthentication.currentUser
        if (currentUser != null) {
            val userDocument = firebaseUserCollectionInstance.document(currentUser.uid)
            // Delete the user document from firebase, and then delete the user from firebase authentication
            userDocument.delete().addOnCompleteListener {
                deleteUserTask ->
                    if (deleteUserTask.isSuccessful) {
                        currentUser.delete().addOnCompleteListener {
                            deleteFirebaseUserTask ->
                                if (deleteFirebaseUserTask.isSuccessful) {
                                    clearToken(context)
                                    deleteAccountObserver.notifyObservers(UserDeleteAccountEvent(true,"Your account has been successfully deleted"))
                                } else {
                                    deleteAccountObserver.notifyObservers(UserDeleteAccountEvent(true, "Failed to delete your account"))
                                }
                        }
                    } else {
                        deleteAccountObserver.notifyObservers(UserDeleteAccountEvent(false,"Failed to delete your user data"))
                    }
            }
        }
    }

    fun shareGroceryItemsWithUser(storeName: String, recipientUserId: String, callback: IMergeGroceryListOperation) {
        val currentLoggedInUser = Firebase.auth.currentUser

        if (currentLoggedInUser == null) {
            callback.onFailure("User not logged in.")
            return
        }

        // Query items from the current user's grocery list that match the store name
        val currentUserGroceryItems = firebaseUserCollectionInstance
            .document(currentLoggedInUser.uid)
            .collection("groceryItems")
            .whereEqualTo("store", storeName)

        currentUserGroceryItems.get().addOnSuccessListener { currentUserSnapshot ->
            if (currentUserSnapshot.isEmpty) {
                callback.onFailure("No items found in your grocery list.")
                return@addOnSuccessListener
            }

            // Reference to the recipient's grocery items collection
            val recipientGroceryItemsRef = firebaseUserCollectionInstance
                .document(recipientUserId)
                .collection("groceryItems")

            // Fetch recipient's current grocery items to avoid duplicates
            recipientGroceryItemsRef.get().addOnSuccessListener { recipientSnapshot ->
                val recipientItems = recipientSnapshot.documents.map { it.getString("id") }.toSet()

                val batch = FirebaseFirestore.getInstance().batch()

                currentUserSnapshot.documents.forEach { document ->
                    val itemId = document.getString("id")
                    if (itemId != null && !recipientItems.contains(itemId)) {
                        // Only add if the item is not already in the recipient's list
                        val newDocumentRef = recipientGroceryItemsRef.document() // Optionally, use itemId to overwrite/update same item
                        batch.set(newDocumentRef, document.data!!)
                    }
                }

                // Commit the batch to add all unique items to the recipient's grocery list
                batch.commit().addOnSuccessListener {
                    callback.onSuccess("Unique items from both grocery lists have been merged successfully.")
                }.addOnFailureListener { e ->
                    callback.onFailure("Failed to merge both grocery lists: ${e.message}")
                }
            }.addOnFailureListener { e ->
                callback.onFailure("Failed to retrieve recipient's grocery list items: ${e.message}")
            }
        }.addOnFailureListener { e ->
            callback.onFailure("Failed to retrieve your grocery list items: ${e.message}")
        }
    }

    private fun convertJsonToGroceryItemObjects(groceryItemMap: Map<String, Any>): GroceryItem {
        return GroceryItem(
            name = groceryItemMap["name"] as String,
            id = groceryItemMap["id"] as String,
            category = groceryItemMap["category"] as String,
            store = groceryItemMap["store"] as String,
            quantity = (groceryItemMap["quantity"] as Number).toInt(),
            cost = (groceryItemMap["cost"] as Number).toFloat(),
        )
    }

    fun registerUserInFirebase(email: String, password: String, callback: IUserRegistrationCallback) {
        firebaseAuthentication.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { completedCreateUserTask ->
                if (completedCreateUserTask.isSuccessful) {
                    val firebaseUser = completedCreateUserTask.result?.user
                    if (firebaseUser != null) {
                        Log.w("ApplicationLogs", "Firebase user when registering user is not null")
                        val firebaseUserUid = firebaseUser.uid

                        val user = hashMapOf(
                            "email" to email,
                            "uid" to firebaseUser.uid,
                        )
                        FirebaseFirestore.getInstance().collection("users")
                            .document(firebaseUserUid)
                            .set(user)
                            .addOnSuccessListener {
                                callback.onRegistrationSuccess("Registration successful")
                            }
                            .addOnFailureListener { exception ->
                                callback.onRegistrationFailure("Registration failed. Please try again")
                            }
                    } else {
                        callback.onRegistrationFailure("User registration failed. Please try again")
                    }
                } else {
                    if (completedCreateUserTask.exception is FirebaseAuthUserCollisionException) {
                        callback.onRegistrationFailure("This email is already in use. Please use a different email")
                    } else {
                        callback.onRegistrationFailure("Registration failure. Please contact app developer")
                    }
                }
            }
    }

    fun addGroceryStoreToUser(storeName: String, callback: IAddGroceryStoreCallback) {
        val currentUser = firebaseAuthentication.currentUser
        if (currentUser != null) {
            val userDocumentReference = firebaseUserCollectionInstance
                .document(currentUser.uid)
            userDocumentReference.update("groceryStores", FieldValue.arrayUnion(storeName))
                .addOnSuccessListener {
                    callback.onAddStoreSuccess("You successfully added a new store")
                }
                .addOnFailureListener {
                    callback.onAddStoreFailure("Failed to add a new store")
                }
        }
    }

    fun deleteAllGroceryItemsByStore(storeName: String, callback: IDeleteGroceryItemCallback) {
        val currentUser = firebaseAuthentication.currentUser
        if (currentUser != null) {
            val userDocumentReference = firebaseUserCollectionInstance.document(currentUser.uid)

            userDocumentReference.get().addOnSuccessListener { documentSnapshot ->
                val groceryItems = documentSnapshot.get("groceryItems") as? List<Map<String, Any>> ?: listOf()

                val itemsToKeep = groceryItems.filterNot { it["store"] == storeName }

                userDocumentReference.update("groceryItems", itemsToKeep)
                    .addOnSuccessListener {
                        callback.onDeleteSuccess("All items from store '${storeName}' have been deleted.")
                    }
                    .addOnFailureListener { e ->
                        callback.onDeleteFailure("Failed to delete items from '${storeName}")
                    }
            }.addOnFailureListener { e ->
                callback.onDeleteFailure("Failed to retrieve grocery items to delete from '${storeName}")
            }
        }
    }

    fun deleteGroceryStoreFromUser(storeName: String, callback: IAddGroceryStoreCallback) {
        val currentUser = firebaseAuthentication.currentUser
        if (currentUser != null) {
            val userDocumentReference = firebaseUserCollectionInstance.document(currentUser.uid)

            userDocumentReference.update("groceryStores", FieldValue.arrayRemove(storeName))
                .addOnSuccessListener {
                    callback.onAddStoreSuccess("Store successfully removed")
                }
                .addOnFailureListener { e ->
                    callback.onAddStoreFailure("Failed to remove store")
                }
        }
    }

    fun getGroceryStoreNames(callback: (List<String>) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userGroceryStoresReference = firebaseUserCollectionInstance
                .document(currentUser.uid)

            userGroceryStoresReference.addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    callback(listOf())
                    return@addSnapshotListener
                }

                val groceryStores = snapshot
                    ?.get("groceryStores")
                    as? List<String> ?: emptyList()

                callback(groceryStores)
            }
        }
    }

    fun logInUserWithFirebase(email: String, password: String, context: Context, callback: IUserLoginCallback) {
        firebaseAuthentication.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { completedLogInUserTask ->
                if (completedLogInUserTask.isSuccessful) {
                    val firebaseUser = firebaseAuthentication.currentUser
                    firebaseUser?.getIdToken(true)?.addOnCompleteListener {
                        task ->
                            if (task.isSuccessful) {
                                val idToken = task.result?.token
                                if (idToken != null) {
                                    saveToken(idToken, context)
                                }
                                callback.onLoginSuccess("You logged in successfully")
                            } else {
                                callback.onLoginFailure("Unable to log you in")
                            }
                    }
                } else {
                    callback.onLoginFailure("Unable to log you in")
                }
            }
    }

    private fun saveToken(token: String, context: Context) {
        try {
            val masterKeyAlias = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            val sharedPreferences = EncryptedSharedPreferences.create(
                context,
                "grocery_helper_shared_preferences",
                masterKeyAlias,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            with(sharedPreferences.edit()) {
                putString("grocery_helper_user_token", token)
                apply()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addGroceryItemToFirebase(groceryItem: GroceryItem, callback: IAddGroceryItemCallback) {
        Log.w("addGroceryItemToFirebase", "Function was called")
        val currentFirebaseUser = Firebase.auth.currentUser
        val currentFirebaseUserUid = currentFirebaseUser?.uid

        if (currentFirebaseUserUid != null) {
            val userDocumentReference = FirebaseFirestore
                .getInstance()
                .collection("users")
                .document(currentFirebaseUserUid)

            firebaseUserCollectionInstance
                .document(currentFirebaseUserUid)
                .get()
                .addOnSuccessListener { userDocument ->
                    val groceryItems = userDocument.get("groceryItems") as? List<Map<String, Any>>
                    if (groceryItems == null) {
                        userDocumentReference.update("groceryItems", listOf(groceryItem))
                            .addOnSuccessListener {
                                callback.onAddSuccess("Grocery item added successfully")
                            }
                            .addOnFailureListener { e ->
                                callback.onAddFailure("Failed to initialize grocery items: ${e.message}")
                            }
                    }
                    if (groceryItems != null) {
                        val itemExistsByName = groceryItems.any { it["name"] == groceryItem.name }
                        val itemExistsById = groceryItems.any {
                            Log.d("FirebaseDebug", "Existing item ID: ${it["id"]}, New item ID: ${groceryItem.id}")
                            it["id"] == groceryItem.id }

                        if (itemExistsById && itemExistsByName) {
                            // First, delete the existing grocery item
                            deleteGroceryItem(groceryItem, object : IDeleteGroceryItemCallback {
                                override fun onDeleteSuccess(successMessage: String) {
                                    // After successful deletion, add the new grocery item
                                    userDocumentReference.update(
                                        "groceryItems",
                                        FieldValue.arrayUnion(groceryItem)
                                    )
                                        .addOnSuccessListener {
                                            callback.onAddSuccess("Grocery item added successfully")
                                        }
                                        .addOnFailureListener { e ->
                                            callback.onAddFailure("Failed to add grocery item: ${e.message}")
                                        }
                                }

                                override fun onDeleteFailure(failureMessage: String) {
                                    // If deletion fails, do not attempt to add the new item
                                    Log.w("ApplicationErrors", failureMessage)
                                    callback.onAddFailure("Failed to delete existing item: $failureMessage")
                                }
                            })
                        } else if (itemExistsByName){
                            callback.onAddFailure("This item already exists")
                        } else {
                            userDocumentReference.update(
                                "groceryItems",
                                FieldValue.arrayUnion(groceryItem)
                            )
                            callback.onAddSuccess("Successfully added new item")
                        }
                    }
                }
        }
    }

    fun logoutWithFirebase(context: Context, callback: IUserLogoutCallback) {
        try {
            clearToken(context)
            firebaseAuthentication.signOut()
            callback.onLogoutSuccess("You have successfully logged out")
        } catch (e: Exception) {
            callback.onLogoutFailure("Failed to log out. Try again")
        }
    }

    private fun clearToken(context: Context) {
        try {
            val masterKeyAlias = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            val sharedPreferences = EncryptedSharedPreferences.create(
                context,
                "grocery_helper_shared_preferences",
                masterKeyAlias,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            with(sharedPreferences.edit()) {
                remove("grocery_helper_user_token")
                apply()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun registerGlobalAuthenticationCheck(context: Context, callback: IAuthStatusListener) {
        FirebaseAuth.getInstance().addAuthStateListener { firebaseAuthentication ->
            if (firebaseAuthentication.currentUser == null) {
                val signInIntent = Intent(context, SignInView::class.java)
                signInIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                callback.onUserUnauthenticated("You have been signed out")
            }
        }
    }

    fun getMutableLiveDataListOfGroceryItem(storeName: String?): MutableLiveData<MutableList<GroceryItem>> {
        getGroceryItemsFromCollection(storeName)
        return mutableGroceryItemList
    }
}