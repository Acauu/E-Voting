package com.skripsi.voting.Adapter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.skripsi.voting.R
import com.skripsi.voting.model.Person

class Adapter(private val voteList: ArrayList<Person>): RecyclerView.Adapter<Adapter.MyViewHolder>() {




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView=LayoutInflater.from(parent.context).inflate(R.layout.vote_item,
        parent,false)
        return MyViewHolder(itemView)

    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentitem = voteList[position]
        holder.nama.text = currentitem.nama
        holder.Vote.text= currentitem.Vote.toString()
        var strbit= StringToBitMap(currentitem.gambar)
        holder.image.setImageBitmap(strbit)
    }

    override fun getItemCount(): Int {
        return voteList.size
    }

    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val nama : TextView = itemView.findViewById(R.id.namekandidat)
        val Vote : TextView = itemView.findViewById(R.id.skor)
        val image : ImageView = itemView.findViewById(R.id.gambarkandidat)

    }

    fun StringToBitMap(encodedString: String?): Bitmap? {
        val encodeByte: ByteArray = Base64.decode(encodedString, Base64.DEFAULT)
        var poto = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
        return poto
    }
}