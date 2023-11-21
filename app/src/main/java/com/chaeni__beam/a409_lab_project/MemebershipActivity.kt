package com.chaeni__beam.a409_lab_project

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.WindowCompat
import androidx.databinding.DataBindingUtil
import com.chaeni__beam.a409_lab_project.databinding.ActivityJoinBinding
import com.chaeni__beam.a409_lab_project.databinding.ActivityMemebershipBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MemebershipActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var database: DatabaseReference

    private lateinit var binding : ActivityMemebershipBinding

    private lateinit var firestore: FirebaseFirestore

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_memebership)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_memebership)

        auth = Firebase.auth

        database = FirebaseDatabase.getInstance().getReference().child("Users")
        firestore = Firebase.firestore

        setStatusBarTransparent()

        joinBtnClick()

        backBtnClick()

    }

    fun backBtnClick(){
        binding.backBtn.setOnClickListener{
            startActivity(Intent(this, JoinActivity::class.java))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun joinBtnClick(){

        binding.joinBtn.setOnClickListener{
            var isGoToJoin = true

            val email = binding.emailArea.text.toString()
            val password1 = binding.passwordArea.text.toString()
            val password2 = binding.passwordCheckArea.text.toString()
            val name = binding.nameArea.text.toString()

            if(name.isEmpty()){
                Toast.makeText(this, "이름을 입력하세요.", Toast.LENGTH_SHORT).show()
                isGoToJoin = false
            }

            if(email.isEmpty()){
                Toast.makeText(this, "이메일을 입력하세요.", Toast.LENGTH_SHORT).show()
                isGoToJoin = false
            }

            if(password1.isEmpty()){
                Toast.makeText(this, "비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show()
                isGoToJoin = false
            }

            if(password2.isEmpty()) {
                Toast.makeText(this, "비밀번호를 확인하세요.", Toast.LENGTH_SHORT).show()
                isGoToJoin = false
            }

            if(!password1.equals(password2)){
                Toast.makeText(this, "비밀번호를 똑같이 입력하세요.", Toast.LENGTH_SHORT).show()
                isGoToJoin = false
            }

            if(password1.length < 6){
                Toast.makeText(this, "비밀번호를 6자리 이상 입력하세요", Toast.LENGTH_SHORT).show()
                isGoToJoin = false
            }

            //회원가입
            if(isGoToJoin){
                auth.createUserWithEmailAndPassword(email, password1) //신규 사용자 가입
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            writeNewUser(name, email)
                            Toast.makeText(this, "회원가입 성공", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MyPageActivity::class.java))
                        } else {
                            Toast.makeText(this, "회원가입 실패", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun writeNewUser(name : String, email : String){
        val date = LocalDate.now()

        val user = User(name, email, date.toString())

        firestore.collection("Users").document(auth.currentUser!!.uid)
            .collection("userInfo").document("userProfile")
            .set(user)
            .addOnSuccessListener {  }
            .addOnFailureListener { Log.d("tttt", "또실패냐") }

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