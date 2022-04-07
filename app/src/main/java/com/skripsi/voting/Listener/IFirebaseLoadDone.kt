package com.skripsi.voting.Listener

import com.skripsi.voting.model.Person

interface IFirebaseLoadDone {
    fun onDataLoadSuccess(dataList:List<Person>)
    fun onDataLoadFailed(message:String)
}