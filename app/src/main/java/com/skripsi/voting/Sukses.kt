package com.skripsi.voting

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity


class Sukses: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sukses)


    }

    fun home(view: View?){
        val a = Intent(this,Navbar::class.java)
//        a.putExtra("Email", firebaseAuth!!.currentUser!!.email)
        startActivity(a)
    }

    fun keluar(view: View?){
        finish()
        moveTaskToBack(true)
    }
}