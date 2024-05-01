package com.jacobdamiangraham.groceryhelper.storage

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.ktx.Firebase
import com.jacobdamiangraham.groceryhelper.interfaces.IAuthStatusListener
import com.jacobdamiangraham.groceryhelper.model.GroceryItem

class FirebaseStorage(collectionName: String? = "groceryitems") {

    private var firebaseAuthentication: FirebaseAuth = Firebase.auth
    private lateinit var firebaseCollectionInstance: CollectionReference
    private var mutableGroceryItemList: MutableLiveData<List<GroceryItem>> = MutableLiveData<List<GroceryItem>>()
    private lateinit var groceryItemList: ArrayList<GroceryItem>
    private lateinit var userId: String
    private var authStatusListener: IAuthStatusListener? = null

    init {
        if (collectionName != null) {
            getCollectionOfGroceryItems(collectionName)
        }
    }

    fun setAuthStatusListener(listener: IAuthStatusListener) {
        this.authStatusListener = listener
    }

    private fun authenticateUserWithFirebase() {
        if (Firebase.auth.currentUser != null && authStatusListener != null) {
            authStatusListener!!.onUserAuthenticate()
        } else {
            authStatusListener?.onUserUnauthenticated()
        }
    }

    private fun getCollectionOfGroceryItems(collectionName: String) {
        val firebaseCurrentUser = Firebase.auth.currentUser
        firebaseCollectionInstance = FirebaseFirestore.getInstance().collection(collectionName)
        userId = "1"
        if (firebaseCurrentUser != null) {
            userId = firebaseCurrentUser.uid
        }
    }

    private fun getGroceryItemsFromCollection() {
        firebaseCollectionInstance.whereEqualTo("userId", userId).orderBy("itemName")
            .addSnapshotListener { groceryItemDocuments, exception ->
                groceryItemDocuments.let {
                    groceryItemList = ArrayList()
                    if (groceryItemDocuments != null) {
                        for (groceryItemDocument in groceryItemDocuments) {
                            val groceryItem = convertJsonToGroceryItemObjects(groceryItemDocument)
                            groceryItemList.add(groceryItem)
                        }
                    }
                    mutableGroceryItemList.value = groceryItemList
                }
            }
    }

    fun addGroceryItemToFirebase(groceryItem: GroceryItem) {
        val firebaseCurrentUser = Firebase.auth.currentUser
        Log.w("Firebase add grocery item", "Add grocery item")
        firebaseCollectionInstance
            .document(groceryItem.id.toString())
            .set(groceryItem)
            .addOnSuccessListener {
                Log.w("Success writing Firebase object", "GroceryItem was successfully written to the database")
            }
            .addOnFailureListener {
                Log.w("Error writing Firebase object", "GroceryItem could not be written to the database")
            }
    }

    private fun convertJsonToGroceryItemObjects(groceryItemDocument: QueryDocumentSnapshot): GroceryItem {
        return groceryItemDocument.toObject(GroceryItem::class.java)
    }

    fun getMutableLiveDataListOfGroceryItem(collectionName: String): MutableLiveData<List<GroceryItem>> {
        getGroceryItemsFromCollection()
        return mutableGroceryItemList
    }
}