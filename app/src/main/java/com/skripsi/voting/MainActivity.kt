package com.skripsi.voting

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun regis(v:View?){
        val a = Intent(this@MainActivity,Ktp::class.java)
        startActivity(a)
    }
    fun login(v: View?){
        val b = Intent(this@MainActivity,Login::class.java)
        startActivity(b)
    }
}