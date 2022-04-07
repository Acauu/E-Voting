package com.skripsi.voting.Adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.skripsi.voting.R
import com.skripsi.voting.model.Person

class MyAdapter(internal var context: Context, internal var data:List<Person>): PagerAdapter(){
    internal var layoutInflater:LayoutInflater
    init{
        layoutInflater=LayoutInflater.from(context)
    }

    override fun getCount(): Int {
        return data.size
    }

    override fun isViewFromObject(p0: View, p1: Any): Boolean {
        return p0==p1
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        (container as ViewPager).removeView(`object` as View)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = layoutInflater.inflate(R.layout.view_pager_item,container,false)
        val gambar = view.findViewById<View>(R.id.fotokandidat) as ImageView
        val nama = view.findViewById<View>(R.id.namakandidat) as TextView



//        Picasso.get().load(data[position].gambar).into(gambar)
        var strbit= StringToBitMap(data[position].gambar)
        gambar.setImageBitmap(strbit)
        nama.text = data[position].nama.toString()
        nama.setText(data[position].nama.toString())
        view.setOnClickListener{
            Toast.makeText(context, ""+data[position].nama, Toast.LENGTH_SHORT).show()
        }
        container.addView(view)
        return view
    }

    fun StringToBitMap(encodedString: String?): Bitmap? {
        val encodeByte: ByteArray = Base64.decode(encodedString, Base64.DEFAULT)
        var poto = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
        return poto
    }
}