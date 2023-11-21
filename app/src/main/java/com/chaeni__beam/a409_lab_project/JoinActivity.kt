package com.chaeni__beam.a409_lab_project

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.databinding.DataBindingUtil
import com.chaeni__beam.a409_lab_project.databinding.ActivityJoinBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase

class JoinActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var binding : ActivityJoinBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_join)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_join)
        auth = Firebase.auth

        setStatusBarTransparent()

        membershipBtnClick()

        joinBtnClick()

    }

    private fun joinBtnClick(){

        binding.joinBtn.setOnClickListener{
            var isGoToJoin = true

            val email = binding.emailArea.text.toString()
            val password = binding.passwordArea.text.toString()

            if(email.isEmpty()){
                Toast.makeText(this, "이메일을 입력하세요.", Toast.LENGTH_SHORT).show()
                isGoToJoin = false
            }

            if(password.isEmpty()){
                Toast.makeText(this, "비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show()
                isGoToJoin = false
            }

            if(isGoToJoin){
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) { //로그인 성공
                            startActivity(Intent(this, MyPageActivity::class.java))
                        } else {
                            Toast.makeText(this, "이메일 또는 비밀번호를 다시 확인하세요.", Toast.LENGTH_SHORT,).show()
                        }
                    }
            }
        }

    }

    private fun membershipBtnClick(){
        binding.membershipBtn.setOnClickListener{
            val intent = Intent(this, MemebershipActivity::class.java)
            startActivity(intent)
        }
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