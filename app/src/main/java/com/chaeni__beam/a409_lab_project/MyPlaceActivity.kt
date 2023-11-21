package com.chaeni__beam.a409_lab_project

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.ListAdapter
import androidx.core.view.WindowCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chaeni__beam.a409_lab_project.databinding.ActivityMyPageBinding
import com.chaeni__beam.a409_lab_project.databinding.ActivityMyPlaceBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class MyPlaceActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var storage: StorageReference
    private lateinit var user: FirebaseUser
    private lateinit var uid: String

    private lateinit var binding: ActivityMyPlaceBinding

    private lateinit var adapter: RvAdapter
    private val viewModel by lazy { ViewModelProvider(this).get(ListViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_my_place)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_my_place)

        auth = Firebase.auth
        user = auth.currentUser!!
        uid = user.uid
        storage = FirebaseStorage.getInstance().reference
        database = FirebaseDatabase.getInstance().getReference().child("Users/$uid/userInfo")

        adapter = RvAdapter(this)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
        observerData()

        setStatusBarTransparent()

        backBtnClick()
    }

    fun backBtnClick(){
        binding.backBtn.setOnClickListener{
            finish()
            //startActivity(Intent(this, NaviActivity::class.java))
        }
    }


    fun observerData(){
        viewModel.fetchData().observe(this, Observer {
            adapter.setListData(it)
            adapter.notifyDataSetChanged()
        })
    }

    fun Activity.setStatusBarTransparent() {
        window.apply {
            setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }
        if(Build.VERSION.SDK_INT >= 30) {	// API 30 에 적용
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
    }
}