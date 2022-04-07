package com.skripsi.voting

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.skripsi.voting.Adapter.Adapter
import com.skripsi.voting.model.Person

class VotelistActivity: AppCompatActivity() {

    private lateinit var dbref : DatabaseReference
    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userArrayList: ArrayList<Person>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        userRecyclerView = findViewById(R.id.voteList)
        userRecyclerView.layoutManager=LinearLayoutManager(this)
        userRecyclerView.setHasFixedSize(true)

        userArrayList= arrayListOf<Person>()
        getUserData()
    }

    private fun getUserData(){
        dbref = FirebaseDatabase.getInstance().getReference("Candidate")
        dbref.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(data in snapshot.children){
                        val user= data.getValue(Person::class.java)
                        userArrayList.add(user!!)
                    }
                    userRecyclerView.adapter = Adapter(userArrayList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}