package com.jacobdamiangraham.groceryhelper.storage

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.jacobdamiangraham.groceryhelper.interfaces.IAddGroceryItemCallback
import com.jacobdamiangraham.groceryhelper.interfaces.IAddGroceryStoreCallback
import com.jacobdamiangraham.groceryhelper.interfaces.IAuthStatusListener
import com.jacobdamiangraham.groceryhelper.interfaces.IDeleteGroceryItemCallback
import com.jacobdamiangraham.groceryhelper.interfaces.IMergeGroceryListOperation
import com.jacobdamiangraham.groceryhelper.interfaces.IUserLoginCallback
import com.jacobdamiangraham.groceryhelper.interfaces.IUserLogoutCallback
import com.jacobdamiangraham.groceryhelper.interfaces.IUserRegistrationCallback
import com.jacobdamiangraham.groceryhelper.model.GroceryItem
import com.jacobdamiangraham.groceryhelper.ui.signin.SignInView

class FirebaseStorage() {

    private var firebaseAuthentication: FirebaseAuth = Firebase.auth
    private lateinit var firebaseGroceryItemCollectionInstance: DocumentReference
    private lateinit var firebaseUserCollectionInstance: CollectionReference
    private var mutableGroceryItemList: MutableLiveData<MutableList<GroceryItem>> = MutableLiveData<MutableList<GroceryItem>>()
    private lateinit var userId: String

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

    fun deleteUserAccount(callback: IUserLogoutCallback) {
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
                                    callback.onLogoutSuccess("Your account has been successfully deleted")
                                } else {
                                    callback.onLogoutFailure("Failed to delete your account")
                                }
                        }
                    } else {
                        callback.onLogoutFailure("Failed to delete your user data")
                    }
            }
        } else {
            callback.onLogoutFailure("You are not currently logged in")
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
                                callback.onRegistrationFailure("Registration failure: ${exception.message}")
                            }
                    } else {
                        callback.onRegistrationFailure("User registration failed. Please try again")
                    }
                } else {
                    throw IllegalArgumentException(completedCreateUserTask.exception)
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

    fun getGroceryStoreNames(callback: (List<String>) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userStoresReference = firebaseUserCollectionInstance
                .document(currentUser.uid)
            userStoresReference
                .get()
                .addOnSuccessListener {
                    documentSnapshot ->
                        if (documentSnapshot.exists()) {
                            val stores = documentSnapshot.get("groceryStores") as? List<String> ?: listOf()
                            callback(stores)
                        } else {
                            callback(listOf())
                        }
                }
                .addOnFailureListener {
                    callback(listOf())
                }
        }
    }

    fun logInUserWithFirebase(email: String, password: String, callback: IUserLoginCallback) {
        firebaseAuthentication.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { completedLogInUserTask ->
                if (completedLogInUserTask.isSuccessful) {
                    val firebaseUser = firebaseAuthentication.currentUser
                    callback.onLoginSuccess("You logged in successfully")
                } else {
                    callback.onLoginFailure("Unable to log you in")
                }
            }
    }

    fun addGroceryItemToFirebase(groceryItem: GroceryItem, callback: IAddGroceryItemCallback) {
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

    fun logoutWithFirebase(callback: IUserLogoutCallback) {
        try {
            firebaseAuthentication.signOut()
            callback.onLogoutSuccess("You have successfully logged out")
        } catch (e: Exception) {
            callback.onLogoutFailure("Failed to log out. Try again")
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