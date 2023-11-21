package com.chaeni__beam.a409_lab_project

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.chaeni__beam.a409_lab_project.databinding.ActivityImageBinding
import com.chaeni__beam.a409_lab_project.databinding.ActivityLocationDialogBinding

class ImageActivity : AppCompatActivity() {

    val binding by lazy { ActivityImageBinding.inflate(layoutInflater) }

    lateinit var image : Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        image = intent.getStringExtra("image")!!.toUri()

        Glide.with(this)
            .load(image) // 불러올 이미지 url
            //.placeholder(defaultImage) // 이미지 로딩 시작하기 전 표시할 이미지
            //.error(defaultImage) // 로딩 에러 발생 시 표시할 이미지
            //.fallback(defaultImage) // 로드할 url 이 비어있을(null 등) 경우 표시할 이미지
            .into(binding.image) // 이미지를 넣을 뷰


    }
}