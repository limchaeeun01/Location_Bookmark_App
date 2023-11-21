package com.chaeni__beam.a409_lab_project

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import com.chaeni__beam.a409_lab_project.databinding.ActivityLocationDialogBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class LocationDialog : AppCompatActivity() {

    val binding by lazy { ActivityLocationDialogBinding.inflate(layoutInflater) }

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: StorageReference
    private lateinit var user: FirebaseUser
    private lateinit var uid: String

    lateinit var date : MyPlaceData

    private lateinit var multiImageAdapter : MultiImageAdapter
    var imageList = ArrayList<Uri>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        auth = Firebase.auth
        firestore = Firebase.firestore
        storage = FirebaseStorage.getInstance().reference
        user = auth.currentUser!!
        uid = user.uid

        date = intent.getParcelableExtra("date")!!

        multiImageAdapter = MultiImageAdapter(imageList, this)
        binding.recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerView.adapter = multiImageAdapter

        setInfo()

        deleteInfo()

    }

    fun deleteInfo(){
        binding.deleteBtn.setOnClickListener{
            firestore.collection("Users").document(uid)
                .collection("locationList").document(date.dateCreated)
                .delete().addOnCompleteListener{task->
                    Toast.makeText(this, "장소 삭제가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, NaviActivity::class.java))
                }

            storage.child("userPhoto/$uid/contentImage/${date.dateCreated}").delete()
        }
    }

    fun setInfo(){
        binding.locationName.text = date.locationName
        binding.category.text = date.category
        binding.address.text = date.address
        binding.dateCreated.text = date.dateCreated
        binding.memo.text = date.memo
        binding.infoArea.setBackgroundColor(when(date.markerColorTag){
            "Red" -> ContextCompat.getColor(applicationContext, R.color.red_background)
            "Orange" -> ContextCompat.getColor(applicationContext, R.color.orange_background)
            "Yellow" -> ContextCompat.getColor(applicationContext, R.color.yellow_background)
            "Green" -> ContextCompat.getColor(applicationContext, R.color.green_background)
            "Blue" -> ContextCompat.getColor(applicationContext, R.color.blue_background)
            "Violet" -> ContextCompat.getColor(applicationContext, R.color.violet_background)
            else -> Log.d("tttt", "else")
        })
        if(date.image1 != "null"){
            imageList.add(date.image1.toUri())
        }
        if(date.image2 != "null"){
            imageList.add(date.image2.toUri())
        }
        if(date.image3 != "null"){
            imageList.add(date.image3.toUri())
        }
        if(date.image4 != "null"){
            imageList.add(date.image4.toUri())
        }
        if(date.image5 != "null"){
            imageList.add(date.image5.toUri())
        }
        multiImageAdapter.notifyDataSetChanged()
    }
}