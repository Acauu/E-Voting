package com.skripsi.voting.model

class Person {
    var nama: String? = null
    var gambar: String? = null
    var Vote: Long?=null

    constructor() {}
    constructor(gambar: String?, nama: String?,Vote:Long?) {
        this.nama = nama
        this.gambar = gambar
        this.Vote = Vote

    }
}