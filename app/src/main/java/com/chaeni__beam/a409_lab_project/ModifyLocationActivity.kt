package com.chaeni__beam.a409_lab_project

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.chaeni__beam.a409_lab_project.databinding.ActivityModifyLocationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ModifyLocationActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var binding : ActivityModifyLocationBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: StorageReference
    private lateinit var firestore: FirebaseFirestore

    private lateinit var user: FirebaseUser

    private lateinit var uid: String

    private lateinit var multiImageAdapter : MultiImageAdapter

    private lateinit var tag : String

    lateinit var getData : MyPlaceData
    var markerColor : String = "null"
    var target : String  = "null"

    lateinit var markerList : MutableList<ImageView>
    var imageList = ArrayList<Uri>()

    var categoryPos : Int = 0
    var imageNum : Int = 0

    val permission_request = 99

    var permissions = arrayOf(
        android.Manifest.permission.READ_MEDIA_IMAGES
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModifyLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        firestore = Firebase.firestore
        user = auth.currentUser!!
        uid = user.uid

        multiImageAdapter = MultiImageAdapter(imageList, this)
        binding.recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerView.adapter = multiImageAdapter

        tag = intent.getStringExtra("tag").toString()

        markerList = mutableListOf(binding.markerColorRed, binding.markerColorOrange,
        binding.markerColorYellow, binding.markerColorGreen, binding.markerColorBlue,
        binding.markerColorViolet)

        binding.guidanceText.visibility = View.GONE



        setLocationInfo()

        markerColor()

        setStatusBarTransparent()

        categorySpinner()

        backBtnClick()

        addImageBtnClick()

        saveLocation()




    }

    fun backBtnClick(){
        binding.backBtn.setOnClickListener{
            finish()
            //startActivity(Intent(this, NaviActivity::class.java))
        }
    }

    var latitude : String = "null"
    var longitude : String = "null"
    var date : String = "null"

    fun setLocationInfo(){
        firestore.collection("Users").document(uid)
            .collection("locationList").document(tag)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot != null) {
                    getData = querySnapshot.toObject<MyPlaceData>()!!

                    binding.addressArea.text = getData.address
                    binding.nameArea.setText(getData.locationName)
                    categoryPos = getData.categoryPos
                    binding.categorySpinner.setSelection(categoryPos)
                    binding.memotArea.setText(getData.memo)
                    latitude = getData.latitude
                    longitude = getData.longitude
                    date = getData.dateCreated

                    for (i in 0..markerList.size - 1) {
                        if (markerList[i].tag.toString() == getData!!.markerColorTag) {
                            val layoutParams = markerList[i].layoutParams
                            layoutParams.width = 60
                            layoutParams.height = 60
                            markerList[i].layoutParams = layoutParams
                        } else {
                            val layoutParams = markerList[i].layoutParams
                            layoutParams.width = 90
                            layoutParams.height = 90
                            markerList[i].layoutParams = layoutParams
                        }
                    }

                    when (getData.imageNum) {
                        1 -> {imageList.add(Uri.parse(
                            ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                                    + resources.getResourcePackageName(R.drawable.add_image1) + '/'
                                    + resources.getResourceTypeName(R.drawable.add_image1)
                                    + '/' + resources.getResourceEntryName(R.drawable.add_image1)))
                        }

                        2 -> imageList.add(Uri.parse(
                            ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                                    + resources.getResourcePackageName(R.drawable.add_image2) + '/'
                                    + resources.getResourceTypeName(R.drawable.add_image2)
                                    + '/' + resources.getResourceEntryName(R.drawable.add_image2))
                        )

                        3 -> imageList.add(Uri.parse(
                            ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                                    + resources.getResourcePackageName(R.drawable.add_image3) + '/'
                                    + resources.getResourceTypeName(R.drawable.add_image3)
                                    + '/' + resources.getResourceEntryName(R.drawable.add_image3))
                        )

                        4 -> imageList.add(Uri.parse(
                            ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                                    + resources.getResourcePackageName(R.drawable.add_image4) + '/'
                                    + resources.getResourceTypeName(R.drawable.add_image4)
                                    + '/' + resources.getResourceEntryName(R.drawable.add_image4))
                        )

                        5 -> imageList.add(Uri.parse(
                            ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                                    + resources.getResourcePackageName(R.drawable.add_image5) + '/'
                                    + resources.getResourceTypeName(R.drawable.add_image5)
                                    + '/' + resources.getResourceEntryName(R.drawable.add_image5))
                        )

                    }

                    when (getData.imageNum) {
                        1 -> {
                            imageList.add(getData.image1.toUri())
                            binding.addImageBtn.visibility = View.GONE
                            binding.guidanceText.visibility = View.VISIBLE
                        }

                        2 -> {
                            imageList.add(getData.image1.toUri())
                            imageList.add(getData.image2.toUri())
                        }

                        3 -> {
                            imageList.add(getData.image1.toUri())
                            imageList.add(getData.image2.toUri())
                            imageList.add(getData.image3.toUri())
                        }

                        4 -> {
                            imageList.add(getData.image1.toUri())
                            imageList.add(getData.image2.toUri())
                            imageList.add(getData.image3.toUri())
                            imageList.add(getData.image4.toUri())
                        }

                        5 -> {
                            imageList.add(getData.image1.toUri())
                            imageList.add(getData.image2.toUri())
                            imageList.add(getData.image3.toUri())
                            imageList.add(getData.image4.toUri())
                            imageList.add(getData.image5.toUri())
                        }

                    }

                    multiImageAdapter.notifyDataSetChanged()

                }


            }
    }



    fun addImageBtnClick() {
        binding.addImageBtn.setOnClickListener {
            selectGallery()
        }

        multiImageAdapter!!.itemClick = object : MultiImageAdapter.ItemClick{
            override fun onClick(view: View, position: Int) {
                if(position == 0){
                    selectGallery()
                }
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

    //권한 설정, 갤러리 열기, 이미지 가져오기
    fun selectGallery() {
        if(isPermitted()){
            var intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)

            activityResult.launch(intent)
        }else {
            ActivityCompat.requestPermissions(this, permissions, permission_request)
        }
    }

    private val activityResult : ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){

        imageList.clear()

        if(it.resultCode == RESULT_OK) {
            if (it.data!!.clipData != null) { //다중 선택
                val count = it.data!!.clipData!!.itemCount

                if(count > 5){
                    Toast.makeText(applicationContext, "사진은 5장까지 선택 가능합니다.", Toast.LENGTH_SHORT).show()

                    val drawableUri = Uri.parse(
                        ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                            + resources.getResourcePackageName(R.drawable.add_image5) + '/'
                            + resources.getResourceTypeName(R.drawable.add_image5)
                            + '/' + resources.getResourceEntryName(R.drawable.add_image5))

                    imageList.add(drawableUri)

                    for (index in 0 until 5) {
                        val imageUri = it.data!!.clipData!!.getItemAt(index).uri
                        imageList.add(imageUri)
                    }
                    imageNum = 5
                }else{
                    lateinit var drawableUri : Uri
                    imageNum = count

                    when(count){
                        1 -> drawableUri = Uri.parse(
                            ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                                + resources.getResourcePackageName(R.drawable.add_image1) + '/'
                                + resources.getResourceTypeName(R.drawable.add_image1)
                                + '/' + resources.getResourceEntryName(R.drawable.add_image1))

                        2 -> drawableUri = Uri.parse(
                            ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                                + resources.getResourcePackageName(R.drawable.add_image2) + '/'
                                + resources.getResourceTypeName(R.drawable.add_image2)
                                + '/' + resources.getResourceEntryName(R.drawable.add_image2))

                        3 -> drawableUri = Uri.parse(
                            ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                                + resources.getResourcePackageName(R.drawable.add_image3) + '/'
                                + resources.getResourceTypeName(R.drawable.add_image3)
                                + '/' + resources.getResourceEntryName(R.drawable.add_image3))

                        4 -> drawableUri = Uri.parse(
                            ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                                + resources.getResourcePackageName(R.drawable.add_image4) + '/'
                                + resources.getResourceTypeName(R.drawable.add_image4)
                                + '/' + resources.getResourceEntryName(R.drawable.add_image4))

                        5 -> drawableUri = Uri.parse(
                            ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                                + resources.getResourcePackageName(R.drawable.add_image5) + '/'
                                + resources.getResourceTypeName(R.drawable.add_image5)
                                + '/' + resources.getResourceEntryName(R.drawable.add_image5))
                    }

                    imageList.add(drawableUri)


                    for (index in 0 until count) {
                        val imageUri = it.data!!.clipData!!.getItemAt(index).uri

                        imageList.add(imageUri)
                    }

                }

            } else { //단일 선택

            }
            binding.addImageBtn.visibility = View.GONE
            binding.guidanceText.visibility = View.VISIBLE
            multiImageAdapter.notifyDataSetChanged()
        }
    }

    @SuppressLint("NewApi")
    fun saveLocation(){
        binding.saveBtn.setOnClickListener{
            var isGoToSave = true

            val name = binding.nameArea.text.toString()
            val category = binding.categorySpinner.selectedItem.toString()
            val address = binding.addressArea.text.toString()
            val memo = binding.memotArea.text.toString()

            if(name.isEmpty()){
                Toast.makeText(this, "장소 이름을 입력하세요.", Toast.LENGTH_SHORT).show()
                isGoToSave = false
            }

            if(category == "카테고리를 선택하세요."){
                Toast.makeText(this, "카테고리를 선택하세요.", Toast.LENGTH_SHORT).show()
                isGoToSave = false
            }

            if(isGoToSave){
                writeNewContent(name, category, address, memo)
                startActivity(Intent(this, NaviActivity::class.java))
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun writeNewContent(name : String, category : String, address : String, memo : String){
        val myPlaceData = MyPlaceData(name, category, categoryPos,
            address, latitude, longitude, memo, date, markerColor, target, imageNum)

        firestore.collection("Users").document(uid)
            .collection("locationList").document(date)
            .set(myPlaceData)

        storage.child("userPhoto/$uid/contentImage/$date").delete()

        for(i in 1..imageList.size-1){
            storage.child("userPhoto/$uid/contentImage/$date/$i.png").putFile(imageList[i])
                .addOnSuccessListener {
                        taskSnapshot -> // 업로드 정보를 담는다
                    taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener {
                            it->
                        var imageUrl = it.toString()
                        firestore.collection("Users").document(uid)
                            .collection("locationList").document(date)
                            .update("image$i", imageUrl)
                    }
                }
        }

    }

    fun markerColor(){
        binding.markerColorRed!!.setOnClickListener(this)
        binding.markerColorOrange!!.setOnClickListener(this)
        binding.markerColorYellow!!.setOnClickListener(this)
        binding.markerColorGreen!!.setOnClickListener(this)
        binding.markerColorBlue!!.setOnClickListener(this)
        binding.markerColorViolet!!.setOnClickListener(this)
        binding.markerColorUserBtn!!.setOnClickListener(this)

    }

    @SuppressLint("ResourceType")
    override fun onClick(v: View?) {
        target = "null"

        when(v?.id){
            R.id.markerColor_red ->{
                markerColor = "#FB8383"
                target = "Red"
            }
            R.id.markerColor_orange ->{
                markerColor = "#FFC47E"
                target = "Orange"
            }
            R.id.markerColor_yellow ->{
                markerColor = "#FFFE8B"
                target = "Yellow"
            }
            R.id.markerColor_green ->{
                markerColor = "#B1FF7F"
                target = "Green"
            }
            R.id.markerColor_blue ->{
                markerColor = "#8C9BFF"
                target = "Blue"
            }
            R.id.markerColor_violet ->{
                markerColor = "#A085FF"
                target = "Violet"
            }
            R.id.markerColor_userBtn ->{

            }

        }

        for(i in 0..markerList.size-1){
            if(markerList[i].tag.toString() == target){
                val layoutParams = markerList[i].layoutParams
                layoutParams.width = 60
                layoutParams.height = 60
                markerList[i].layoutParams = layoutParams
            }else{
                val layoutParams = markerList[i].layoutParams
                layoutParams.width = 90
                layoutParams.height = 90
                markerList[i].layoutParams = layoutParams
            }
        }
    }

    fun categorySpinner(){
        val spin = binding.categorySpinner
        spin.adapter = ArrayAdapter.createFromResource(this, R.array.categoryArray,
            android.R.layout.simple_spinner_dropdown_item)

        binding.categorySpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                categoryPos = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }

    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        val focusView = currentFocus
        if (focusView != null && ev != null) {
            val rect = Rect()
            focusView.getGlobalVisibleRect(rect)
            val x = ev.x.toInt()
            val y = ev.y.toInt()

            if (!rect.contains(x, y)) {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm?.hideSoftInputFromWindow(focusView.windowToken, 0)
                focusView.clearFocus()
            }
        }
        return super.dispatchTouchEvent(ev)
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