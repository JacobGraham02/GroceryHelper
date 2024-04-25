package com.jacobdamiangraham.groceryhelper.storage

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.ktx.Firebase
import com.jacobdamiangraham.groceryhelper.interfaces.IAuthStatusListener
import com.jacobdamiangraham.groceryhelper.model.GroceryItem

class FirebaseStorage {

    private lateinit var firebaseAuthentication: FirebaseAuth
    private lateinit var firebaseCollectionInstance: CollectionReference
    private lateinit var mutableGroceryItemList: MutableLiveData<List<GroceryItem>>
    private lateinit var groceryItemList: ArrayList<GroceryItem>
    private lateinit var userId: String
    private var authStatusListener: IAuthStatusListener? = null

    init {
        firebaseAuthentication = Firebase.auth
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

    private fun getCollectionOfGroceryItems() {
        val firebaseCurrentUser = Firebase.auth.currentUser
        if (firebaseCurrentUser != null) {
            userId = firebaseCurrentUser.uid
            firebaseCollectionInstance = FirebaseFirestore.getInstance().collection("groceryitems")
        }
    }

    private fun getGroceryItemsFromCollection(collectionName: String) {
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

    private fun convertJsonToGroceryItemObjects(groceryItemDocument: QueryDocumentSnapshot): GroceryItem {
        return groceryItemDocument.toObject(GroceryItem::class.java)
    }

    fun getMutableLiveDataListOfGroceryItem(): MutableLiveData<List<GroceryItem>> {
        return mutableGroceryItemList
    }
}