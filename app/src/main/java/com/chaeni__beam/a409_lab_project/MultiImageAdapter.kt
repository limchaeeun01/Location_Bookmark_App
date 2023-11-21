package com.chaeni__beam.a409_lab_project

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MultiImageAdapter() : RecyclerView.Adapter<MultiImageAdapter.ViewHolder>(){

    lateinit var imageList : ArrayList<Uri>
    lateinit var context : Context

    var itemClick: ItemClick? = null

    interface ItemClick{ //인터페이스
        fun onClick(view: View, position: Int)
    }

    constructor(imageList : ArrayList<Uri>, context: Context) : this(){
        this.imageList = imageList
        this.context = context
    }

    class ViewHolder(v : View) : RecyclerView.ViewHolder(v){
        val imageItem = v.findViewById<ImageView>(R.id.imageItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater : LayoutInflater = LayoutInflater.from(parent.context)
        val view : View = inflater.inflate(R.layout.content_image_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = imageList.size

    override fun onBindViewHolder(holder: MultiImageAdapter.ViewHolder, position: Int) {
        Glide.with(context)
            .load(imageList[position])
            .into(holder.imageItem)

        if(itemClick != null){

            holder.itemView.setOnClickListener(View.OnClickListener {
                itemClick?.onClick(it, position)
            })
        }

        holder.itemView.setOnClickListener{
            val intent = Intent(holder.itemView?.context, ImageActivity::class.java)
            intent.putExtra("image", imageList[position].toString())
            ContextCompat.startActivity(holder.itemView.context, intent, null)
        }

    }

}
