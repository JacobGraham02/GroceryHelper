package com.jacobdamiangraham.groceryhelper.storage

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.jacobdamiangraham.groceryhelper.interfaces.IAddGroceryItemCallback
import com.jacobdamiangraham.groceryhelper.interfaces.IAuthStatusListener
import com.jacobdamiangraham.groceryhelper.interfaces.IUserLoginCallback
import com.jacobdamiangraham.groceryhelper.interfaces.IUserLogoutCallback
import com.jacobdamiangraham.groceryhelper.interfaces.IUserRegistrationCallback
import com.jacobdamiangraham.groceryhelper.model.GroceryItem
import com.jacobdamiangraham.groceryhelper.ui.signin.SignInView

class FirebaseStorage(collectionName: String? = "groceryitems") {

    private var firebaseAuthentication: FirebaseAuth = Firebase.auth
    private lateinit var firebaseGroceryItemCollectionInstance: CollectionReference
    private lateinit var firebaseUserCollectionInstance: CollectionReference
    private var mutableGroceryItemList: MutableLiveData<List<GroceryItem>> = MutableLiveData<List<GroceryItem>>()
    private lateinit var userId: String

    init {
        if (collectionName != null) {
            getCollectionOfItems(collectionName)
        }
    }

    private fun getCollectionOfItems(collectionName: String) {
        when (collectionName) {
            "groceryitems" -> {
                getCollectionOfGroceryItems("groceryitems")
            }
            "users" -> {
                getCollectionOfUsers("users")
            }
        }
    }

    private fun getCollectionOfGroceryItems(collectionName: String) {
        val firebaseCurrentUser = Firebase.auth.currentUser
        firebaseGroceryItemCollectionInstance = FirebaseFirestore.getInstance().collection(collectionName)
        if (firebaseCurrentUser != null) {
            userId = firebaseCurrentUser.uid
        }
    }

    private fun getCollectionOfUsers(collectionName: String) {
        firebaseUserCollectionInstance = FirebaseFirestore.getInstance().collection(collectionName)
    }

    private fun getGroceryItemsFromCollection(storeName: String?) {
        val currentFirebaseUser = Firebase.auth.currentUser
        if (currentFirebaseUser != null) {
            val firebaseUserId = currentFirebaseUser.uid
            firebaseUserCollectionInstance
                .document(firebaseUserId)
                .get()
                .addOnSuccessListener { userDocument ->
                    val groceryItems = userDocument.get("groceryItems") as? List<Map<String, Any>>
                    if (groceryItems != null) {
                        val groceryItemList = ArrayList<GroceryItem>()
                        for (groceryItemMap in groceryItems) {
                            val groceryItem = convertJsonToGroceryItemObjects(groceryItemMap)
                            groceryItemList.add(groceryItem)
                        }
                        mutableGroceryItemList.value = groceryItemList
                    }
                }
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
            val userDocumentReference =
                FirebaseFirestore
                    .getInstance()
                    .collection("users")
                    .document(currentFirebaseUserUid)
            userDocumentReference.update("groceryItems", FieldValue.arrayUnion(groceryItem))
                .addOnSuccessListener {
                    callback.onAddSuccess("Grocery item added successfully")
                }
                .addOnFailureListener {
                    callback.onAddFailure("Failed to add grocery item")
                }
        } else {
            callback.onAddFailure("You are not logged in")
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

    fun getMutableLiveDataListOfGroceryItem(storeName: String?): MutableLiveData<List<GroceryItem>> {
        getGroceryItemsFromCollection(storeName)
        return mutableGroceryItemList
    }
}