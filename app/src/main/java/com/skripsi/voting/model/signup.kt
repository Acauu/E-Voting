package com.skripsi.voting.model

import android.graphics.Bitmap

class signup(){

    lateinit var nama:String
    lateinit var nik:String
    lateinit var pass: String
    lateinit var gambar: String
    lateinit var id:String
    constructor(nama:String,nik:String,pass:String,gambar: String, id:String) : this(){
        this.nama = nama
        this.nik = nik
        this.pass = pass
        this.gambar = gambar
        this.id = id
    }
}