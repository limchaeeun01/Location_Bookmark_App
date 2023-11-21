package com.chaeni__beam.a409_lab_project

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class RvAdapter(private val context : Context) : RecyclerView.Adapter<RvAdapter.ViewHolder>(){

    private var contentsList = mutableListOf<MyPlaceData>()
    var latitude = "null"
    var longitude = "null"


    fun setListData(data:MutableList<MyPlaceData>){
        contentsList = data
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RvAdapter.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_list,parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RvAdapter.ViewHolder, position: Int) {
        val myPlaceData : MyPlaceData = contentsList[position]
        if(myPlaceData.locationName.length > 13){
            holder.locationName.text = myPlaceData.locationName.substring(0 until 13)
                .plus("..")
        }else{
            holder.locationName.text = myPlaceData.locationName
        }

        holder.category.text = myPlaceData.category

        if(myPlaceData.address.length > 27){
            holder.address.text = myPlaceData.address.substring(0 until 27)
                .plus("..")
        }else{
            holder.address.text = myPlaceData.address
        }

        if(myPlaceData.memo.length > 18){
            holder.content.text = myPlaceData.memo.substring(0 until 18)
                .plus("..")
        }else{
            holder.content.text = myPlaceData.memo
        }

        holder.dateCreated.text = myPlaceData.dateCreated

        Glide.with(holder.itemView.context)
            .load(myPlaceData.image1)
            .into(holder.locationImage)
        latitude = myPlaceData.latitude
        longitude = myPlaceData.longitude

        holder.itemView.setOnClickListener{
            val intent = Intent(holder.itemView?.context, LocationDialog::class.java)
            intent.putExtra("date", myPlaceData)
            ContextCompat.startActivity(holder.itemView.context, intent, null)
        }



    }

    override fun getItemCount(): Int {
        return contentsList.size
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val locationName : TextView = itemView.findViewById(R.id.locationName)
        val category : TextView = itemView.findViewById(R.id.category)
        val address : TextView = itemView.findViewById(R.id.address)
        val content : TextView = itemView.findViewById(R.id.content)
        val dateCreated : TextView = itemView.findViewById(R.id.dateCreated)
        val locationImage : ImageView = itemView.findViewById(R.id.locationImage)


    }



}