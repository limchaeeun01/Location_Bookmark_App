package com.chaeni__beam.a409_lab_project

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class Repo {
    fun getData(): LiveData<MutableList<MyPlaceData>> {
        val mutableData = MutableLiveData<MutableList<MyPlaceData>>()
        val user = Firebase.auth.currentUser
        val firestore = Firebase.firestore
        val myRef = firestore.collection("Users").document(user?.uid.toString())
            .collection("locationList")

        myRef.get()
            .addOnSuccessListener { querySnapshot ->
            val listData: MutableList<MyPlaceData> = mutableListOf<MyPlaceData>()
            if (querySnapshot != null) {
                for (dc in querySnapshot.documentChanges) {
                    if (dc.type == DocumentChange.Type.ADDED) {
                        var getData = dc.document.toObject<MyPlaceData>()

                        listData.add(getData)

                        mutableData.value = listData
                    }
                }
            }
        }
        /*
        myRef.addValueEventListener(object : ValueEventListener {
            val listData: MutableList<MyPlaceData> = mutableListOf<MyPlaceData>()
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for (userSnapshot in snapshot.children){
                        val getData = userSnapshot.getValue(MyPlaceData::class.java)
                        listData.add(getData!!)

                        mutableData.value = listData
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

         */
        return mutableData
    }
}