package com.chaeni__beam.a409_lab_project

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.ContactsContract
import android.provider.ContactsContract.Contacts.Photo
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.DragEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.isInvisible
import androidx.databinding.DataBindingUtil
import androidx.databinding.DataBindingUtil.setContentView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.chaeni__beam.a409_lab_project.databinding.ActivityJoinBinding
import com.chaeni__beam.a409_lab_project.databinding.ActivityMyPageBinding
import com.google.android.play.core.integrity.n
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.net.URI
import java.net.URL


class MyPageActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var storage: StorageReference
    private lateinit var firestore: FirebaseFirestore

    private lateinit var binding: ActivityMyPageBinding

    private lateinit var launcher: ActivityResultLauncher<Intent>

    val permission_request = 99

    var permissions = arrayOf(
        android.Manifest.permission.READ_MEDIA_IMAGES
    )

    private lateinit var user: FirebaseUser

    private lateinit var uid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_my_page)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_my_page)

        auth = Firebase.auth
        user = auth.currentUser!!
        uid = user.uid
        storage = FirebaseStorage.getInstance().reference
        database = FirebaseDatabase.getInstance().getReference().child("Users/$uid/userInfo")
        firestore = Firebase.firestore

        firestore.collection("Users").document(auth.currentUser!!.uid)
            .collection("userInfo").document("userProfile")
            .get()
            .addOnSuccessListener { value ->
                val user = value.toObject<User>()
                binding.profileNameArea.setText(user?.userName)
                binding.dateOfSubcriptionText.text = "가입일 : ${user?.dateOfSubscription}"
            }


        setStatusBarTransparent()

        logoutBtnClick()

        profileNameChangeBtnClick()

        selectGallery()

        loadImage()

        backBtnClick()


    }

    fun backBtnClick(){
        binding.backBtn.setOnClickListener{
            finish()
            //startActivity(Intent(this, NaviActivity::class.java))
        }
    }


    //로그아웃 메서드
    fun logoutBtnClick() {
        binding.logoutBtn.setOnClickListener {
            Firebase.auth.signOut()
            startActivity(Intent(this, JoinActivity::class.java))
        }
    }

    //닉네임 변경 메서드
    fun profileNameChangeBtnClick() {
        binding.profileNameChangeBtn.setOnClickListener {
            binding.profileNameArea.isEnabled = true
            binding.profileNameChangeBtn.visibility = View.GONE
            binding.profileNameSaveBtn.visibility = View.VISIBLE
        }
        binding.profileNameSaveBtn.setOnClickListener {
            if (!binding.profileNameArea.text.isEmpty()) {

                firestore.collection("Users").document(auth.currentUser!!.uid)
                    .collection("userInfo").document("userProfile")
                    .update("userName", binding.profileNameArea.text.toString())

                Toast.makeText(this, "이름이 변경되었습니다.", Toast.LENGTH_SHORT).show()

                binding.profileNameChangeBtn.visibility = View.VISIBLE
                binding.profileNameSaveBtn.visibility = View.GONE
                binding.profileNameArea.isEnabled = false
            } else {
                Toast.makeText(this, "이름을 한 글자 이상 입력하세요.", Toast.LENGTH_SHORT).show()
            }

        }
    }


    fun isPermitted() : Boolean{
        for (perm in permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    //권한 설정, 갤러리 열기, 이미지 가져오기, firebase storage에 저장
    fun selectGallery() {
        if(isPermitted()){
            launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val intent = checkNotNull(result.data)
                    val imageUri = intent.data
//
                    Glide.with(this)
                        .load(imageUri)
                        .into(binding.profilePhotoArea)

                    storage.child("userPhoto/$uid/profileImage.png")
                        .putFile(imageUri!!)
                        .addOnSuccessListener {
                                taskSnapshot -> // 업로드 정보를 담는다
                            taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener {
                                    it->
                                var imageUrl = it.toString()
                                firestore.collection("Users").document(auth.currentUser!!.uid)
                                    .collection("userInfo").document("userProfile")
                                    .update("userImage", imageUrl)
                                    .addOnSuccessListener {
                                        finish()
                                    }
                            }
                        }

                }
            }
            binding.profilePhotoArea.setOnClickListener {
                val intent = Intent().also { intent ->
                    intent.type = "image/"
                    intent.action = Intent.ACTION_PICK
                }
                launcher.launch(intent)
            }

        }else{
            ActivityCompat.requestPermissions(this, permissions, permission_request)
        }

    }


    fun loadImage() {
        firestore.collection("Users").document(auth.currentUser!!.uid)
            .collection("userInfo").document("userProfile")
            .get()
            .addOnSuccessListener { value ->
                val user = value.toObject<User>()
                if(user?.userImage!=null) {
                    Glide.with(this)
                        .load(user?.userImage)
                        .into(binding.profilePhotoArea)
                }else {
                    val drawable = getDrawable(R.drawable.baseline_account_circle_24)
                    binding.profilePhotoArea.setImageDrawable(drawable)
                }
        }
    }

    //풀스크린 메서드
    fun Activity.setStatusBarTransparent() {
        window.apply {
            setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }
        if (Build.VERSION.SDK_INT >= 30) {    // API 30 에 적용
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
    }

}




