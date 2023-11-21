package com.chaeni__beam.a409_lab_project

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.bumptech.glide.Glide
import com.chaeni__beam.a409_lab_project.databinding.ActivityNaviBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.InfoWindow
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import java.io.IOException
import java.util.Locale


private const val TAG_Map = "NaviActivity"
private const val TAG_MyPlace = "MyPlaceActivity"
private const val TAG_MyPage = "MyPageActivity"

class NaviActivity : AppCompatActivity(), OnMapReadyCallback{

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    var menu : Boolean = true
    var dialog : Boolean = false

    private lateinit var menuBtn: ImageView
    private lateinit var myPlaceBtn: ImageView
    private lateinit var myPageBtn: ImageView
    private lateinit var moveCurrentLocationBtn: ImageView

    private lateinit var naverMap : NaverMap
    private lateinit var fusedLocationClient : FusedLocationProviderClient
    private lateinit var locationSource: FusedLocationSource
    lateinit var locationCallBack : LocationCallback

    val LOCATION_PERMISSION_REQUEST_CODE = 1000

    val permission_request = 99
    var permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION)

    private lateinit var binding : ActivityNaviBinding

    private lateinit var selectedAddress : String
    private lateinit var selectedLatitude : String
    private lateinit var selectedLongitude : String

    private lateinit var myPlaceList : MutableList<MyPlaceData>
    private lateinit var markerList : MutableList<Marker>

    private lateinit var target : String


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNaviBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        firestore = Firebase.firestore

        setStatusBarTransparent()

        menuBtn = binding.menuBtn
        myPlaceBtn = binding.myPlaceBtn
        myPageBtn = binding.myPageBtn
        moveCurrentLocationBtn = binding.currentLocationBtn

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        myPlaceList = mutableListOf<MyPlaceData>()
        myPlaceList.clear()
        markerList = mutableListOf<Marker>()
        markerList.clear()


        setListner()

        setActivity()

        requestPermissions()

        moveCurrentLocation()

        startProcess()

        savedLocationMarker()

        dialogOn()



        var string = "대한민국 서울특별시 양천구 목동중앙본로 30길"

        Log.d("tttt", string.length.toString())

    }


    fun dialogOn(){
        binding.locationInfoDialog.setOnClickListener{
            val intent = Intent(this, LocationDialog::class.java)
            for(i in 0..myPlaceList.size-1){
                if(myPlaceList[i].dateCreated == target){
                    intent.putExtra("date", myPlaceList[i])
                }
            }
            startActivity(intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun savedLocationMarker(){
        val currentUser = auth.currentUser
        if(currentUser != null)
        {
            firestore.collection("Users").document(auth.currentUser!!.uid)
                .collection("locationList")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot != null) {
                        for (dc in querySnapshot.documentChanges) {
                            if (dc.type == DocumentChange.Type.ADDED) {
                                var getData = dc.document.toObject<MyPlaceData>()

                                myPlaceList.add(getData)
                            }
                        }
                        updateMarker(myPlaceList)
                    }
                }

        }else{
            startActivity(Intent(this, JoinActivity::class.java))
            Toast.makeText(this, "로그인 후 이용해주세요.", Toast.LENGTH_SHORT).show()
        }

    }

    @SuppressLint("ResourceType")
    @RequiresApi(Build.VERSION_CODES.M)
    fun updateMarker(myPlaceList: MutableList<MyPlaceData>) {

        for(i in 0 .. myPlaceList.size-1){
            val marker = Marker()
            marker.position = LatLng(myPlaceList[i].latitude.toDouble(), myPlaceList[i].longitude.toDouble())
            marker.map = naverMap
            marker.tag = myPlaceList[i].dateCreated
            marker.icon = MarkerIcons.BLACK
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                marker.iconTintColor = when(myPlaceList[i].markerColorTag){
                    "Red" -> ContextCompat.getColor(applicationContext, R.color.red_marker)
                    "Orange" -> ContextCompat.getColor(applicationContext, R.color.orange_marker)
                    "Yellow" -> ContextCompat.getColor(applicationContext, R.color.yellow_marker)
                    "Green" -> ContextCompat.getColor(applicationContext, R.color.green_marker)
                    "Blue" -> ContextCompat.getColor(applicationContext, R.color.blue_marker)
                    "Violet" -> ContextCompat.getColor(applicationContext, R.color.violet_marker)
                    else -> Log.d("tttt", "else")
                }
            }
            if(myPlaceList[i].locationName.length > 12){
                try{
                    marker.captionText = myPlaceList[i].locationName.split(' ')[0].plus("\n")
                        .plus(myPlaceList[i].locationName.split(' ')[1])
                }catch (e : IndexOutOfBoundsException){
                    marker.captionText = myPlaceList[i].locationName.substring(0 until 10).plus("\n")
                        .plus(myPlaceList[i].locationName.substring(10))
                }
            }else{
                marker.captionText = myPlaceList[i].locationName
            }

            //marker.captionText = myPlaceList[i].locationName

            markerList.add(marker)

            marker.onClickListener = markerListener
        }

    }

    val markerListener = Overlay.OnClickListener { overlay ->

        popDialog(overlay.tag as String)
        target = overlay.tag as String

        true
    }


    private fun setActivity() {

        myPlaceBtn.setOnClickListener{
            val currentUser = auth.currentUser
            if (currentUser != null) { //로그인 되어 있음
                startActivity(Intent(this, MyPlaceActivity::class.java))
            }else{
                startActivity(Intent(this, JoinActivity::class.java))
                Toast.makeText(this, "로그인 후 이용해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        myPageBtn.setOnClickListener{
            val currentUser = auth.currentUser
            if (currentUser != null) { //로그인 되어 있음
                startActivity(Intent(this, MyPageActivity::class.java))
            }else{
                startActivity(Intent(this, JoinActivity::class.java))

            }
        }

    }

    fun moveCurrentLocation(){
        moveCurrentLocationBtn.setOnClickListener{
            startProcess()
        }
    }

    private fun requestPermissions() {
        // 내장 위치 추적 기능 사용
        locationSource =
            FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE)
        }else{
            startProcess()
        }

    }

    //네이버 맵 동적으로 불러오기
    fun startProcess(){
        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map_fragment) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map_fragment, it).commit()
            }
        mapFragment.getMapAsync(this)

    }

    private val clickMarker = Marker()
    val infoWindow = InfoWindow()
    @UiThread
    override fun onMapReady(naverMap: NaverMap){
        this.naverMap = naverMap
        //내장 위치 추적 기능
        naverMap.locationSource = locationSource

        var currentLocation : Location?

        //권한 재확인
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                currentLocation = location
                //현재위치 파란색 점
                naverMap.locationOverlay.run{
                    isVisible = true
                    position = LatLng(currentLocation!!.latitude, currentLocation!!.longitude)
                }

                val cameraUpdate = CameraUpdate.scrollAndZoomTo(
                    LatLng(currentLocation!!.latitude, currentLocation!!.longitude), 15.0)
                    .animate(CameraAnimation.Fly, 2000)

                naverMap.moveCamera(cameraUpdate)
            }
        //지도가 클릭 되면 클릭된 좌표에 마커 찍기, 주소 표시
        naverMap.setOnMapClickListener{ point, coord ->
            if(dialog){ //dialog가 켜져있음
                popDialog("")
            }else{ //dialog가 꺼져있음
                marker(coord.latitude, coord.longitude)
            }
        }
        //마커 클릭 시 장소 저장 액티비티로 넘어감
        clickMarker.onClickListener = clickMarkerListener

    }

    val clickMarkerListener = Overlay.OnClickListener { overlay ->
        val intent = Intent(this, AddLocationActivity::class.java)
        intent.putExtra("address", selectedAddress)
        intent.putExtra("latitude", selectedLatitude)
        intent.putExtra("longitude", selectedLongitude)
        startActivity(intent)

        true
    }

    private fun marker(latitude: Double, longitude: Double) {
        clickMarker.position = LatLng(latitude, longitude)
        clickMarker.map = naverMap

        infoWindow.adapter = object : InfoWindow.DefaultTextAdapter(this) {
            override fun getText(infoWindow: InfoWindow): CharSequence {
                selectedAddress = getAddress(latitude, longitude)
                selectedLatitude = latitude.toString()
                selectedLongitude = longitude.toString()

                return selectedAddress
            }
        }
        infoWindow.open(clickMarker)
        clickMarker.captionText = "저장하기"

    }



    private fun getAddress(lat: Double, lng: Double): String {
        val geoCoder = Geocoder(this, Locale.KOREA)
        val address: ArrayList<Address>
        var addressResult = "주소를 가져 올 수 없습니다."
        try {
            //세번째 파라미터는 좌표에 대해 주소를 리턴 받는 갯수로
            //한좌표에 대해 두개이상의 이름이 존재할수있기에 주소배열을 리턴받기 위해 최대갯수 설정
            address = geoCoder.getFromLocation(lat, lng, 1) as ArrayList<Address>
            if (address.size > 0) {
                // 주소 받아오기
                val currentLocationAddress = address[0].getAddressLine(0)
                    .toString()
                addressResult = currentLocationAddress

            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
        return addressResult
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK){
            when(requestCode){

            }
        }
    }


    //여기부터 레이아웃//여기부터 레이아웃//여기부터 레이아웃//여기부터 레이아웃//여기부터 레이아웃

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

    private fun setListner(){
        menuBtn.setOnClickListener{
            popupMenu()
        }
    }

    private fun popupMenu(){

        var px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80f, this.resources.displayMetrics)

        if (menu) {
            var animation = AnimationUtils.loadAnimation(this, R.anim.rotate_menu_close)
            menuBtn.animation = animation
            menuBtn.startAnimation(animation)

            var writeAnimator = ObjectAnimator.ofFloat(myPlaceBtn, "translationY", 0f, -px)
            writeAnimator.duration = 400
            writeAnimator.interpolator = OvershootInterpolator()
            writeAnimator.target = myPlaceBtn
            writeAnimator.start()

            var photoAnimator = ObjectAnimator.ofFloat(myPageBtn, "translationY", 0f, -px*2)
            photoAnimator.duration = 500
            photoAnimator.interpolator = OvershootInterpolator()
            photoAnimator.target = myPageBtn
            photoAnimator.start()

            menu = !menu
        } else {
            var animation = AnimationUtils.loadAnimation(this, R.anim.rotate_menu_plus)
            menuBtn.animation = animation
            menuBtn.startAnimation(animation)

            var writeAnimator = ObjectAnimator.ofFloat(myPlaceBtn, "translationY", -px, 0f)
            writeAnimator.duration = 400
            writeAnimator.interpolator = OvershootInterpolator()
            writeAnimator.target = myPlaceBtn
            writeAnimator.start()

            var photoAnimator = ObjectAnimator.ofFloat(myPageBtn, "translationY", -px*2, 0f)
            photoAnimator.duration = 500
            photoAnimator.interpolator = OvershootInterpolator()
            photoAnimator.target = myPageBtn
            photoAnimator.start()

            menu = !menu
        }
    }

    private fun popDialog(tag : String){
        var px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 180f, this.resources.displayMetrics)
        val locationInfoDialog = binding.locationInfoDialog

        if (dialog) { //dialog가 켜져있음
            if(tag == ""){ //dialog 닫기
                var dialogAnimator = ObjectAnimator.ofFloat(locationInfoDialog, "translationY", -px, 0f)
                dialogAnimator.duration = 400
                dialogAnimator.interpolator = OvershootInterpolator()
                dialogAnimator.target = locationInfoDialog
                dialogAnimator.start()

                dialog = !dialog
            }else{
                setInfo(tag)
            }
        } else { //dialog가 꺼져있음
            var dialogAnimator = ObjectAnimator.ofFloat(locationInfoDialog, "translationY", 0f, -px)
            dialogAnimator.duration = 400
            dialogAnimator.interpolator = OvershootInterpolator()
            dialogAnimator.target = locationInfoDialog
            dialogAnimator.start()

            clickMarker.map = null
            setInfo(tag)

            dialog = !dialog

        }

    }

    fun setInfo(tag : String){
        for(i in 0 .. myPlaceList.size-1){
            if(myPlaceList[i].dateCreated == tag){
                if(myPlaceList[i].locationName.length > 11){
                    binding.locationName.text = myPlaceList[i].locationName.substring(0 until 11)
                        .plus("..")
                }else{
                    binding.locationName.text = myPlaceList[i].locationName
                }
                binding.category.text = myPlaceList[i].category
                if(myPlaceList[i].address.length > 25){
                    binding.address.text = myPlaceList[i].address.substring(0 until 25)
                        .plus("..")
                }else{
                    binding.address.text = myPlaceList[i].address
                }
                if(myPlaceList[i].memo.length > 17){
                    binding.content.text = myPlaceList[i].memo.substring(0 until 17)
                        .plus("..")
                }else{
                    binding.content.text = myPlaceList[i].memo
                }
                binding.dateCreated.text = myPlaceList[i].dateCreated
                Glide.with(this)
                    .load(myPlaceList[i].image1) // 불러올 이미지 url
                    //.placeholder(defaultImage) // 이미지 로딩 시작하기 전 표시할 이미지
                    //.error(defaultImage) // 로딩 에러 발생 시 표시할 이미지
                    //.fallback(defaultImage) // 로드할 url 이 비어있을(null 등) 경우 표시할 이미지
                    .into(binding.locationImage) // 이미지를 넣을 뷰

                val cameraUpdate = CameraUpdate.scrollAndZoomTo(
                    LatLng(myPlaceList[i].latitude.toDouble(), myPlaceList[i].longitude.toDouble()), 15.0)
                    .animate(CameraAnimation.Fly, 1000)

                naverMap.moveCamera(cameraUpdate)

                break
            }
        }
    }

}