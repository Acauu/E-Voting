package com.skripsi.voting

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_login.*


class Login : AppCompatActivity() {

    lateinit var ref: DatabaseReference
    private var nik: EditText? = null
    private var pass: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        nik = findViewById<View>(R.id.nik) as EditText
        pass = findViewById<View>(R.id.passlogin) as EditText

//        buttonlogin.setOnClickListener {
//            login()
//        }
    }


        fun login(v: View?){
            ref = FirebaseDatabase.getInstance().getReference("Users")
            var niktxt = nik!!.text.toString()
            var passtxt = pass!!.text.toString()


            if (niktxt.isEmpty() || passtxt.isEmpty()) {
                Toast.makeText(this@Login, "Kolom wajib diisi", Toast.LENGTH_SHORT).show()
            } else {
                ref.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.hasChild(niktxt)){
                            var getpassword=snapshot.child(niktxt).child("pass").value.toString()
                            if(getpassword.equals(passtxt)){
                                val sharedPref = getSharedPreferences("myKey", MODE_PRIVATE)
                                val editor = sharedPref.edit()
                                editor.putString("value", niktxt)
                                editor.apply()
//                                val userInfo = isVote()
//                                userInfo.setNik(niktxt)
//                                val i = Intent(this@Login,Vote2::class.java)
//                                val b = Bundle()
//                                b.putSerializable("serializable",userInfo)
//                                i.putExtras(b)

//                                val i = Intent(this@Login,Vote2::class.java)
//                                i.putExtras(bundle)
                                Log.d("niktxt", niktxt)
                                Toast.makeText(this@Login, "Successfull", Toast.LENGTH_SHORT).show()
                                val a = Intent(this@Login,Navbar::class.java)
                                startActivity(a)
                            }else{
                                Toast.makeText(this@Login, "Password salah", Toast.LENGTH_SHORT).show()
                            }
                        }else{
                            Toast.makeText(this@Login, "NIK belum terdaftar", Toast.LENGTH_SHORT).show()
                        }


                    }


                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
            }


        }

}




//    private fun isEmailExist(nik: String, pass: String){
//        ref
//            .addValueEventListener(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    var list = ArrayList<signup>()
//                    var isemailexist = false
//                    for(postsnapshot in snapshot.children){
//                        var value = postsnapshot.getValue<signup>()
//                        if(value!!.nik == nik && value!!.pass == pass){
//                            isemailexist = true
//                    }
//                    list.add(value!! as signup)
//                }
//                if(isemailexist){
//                    Toast.makeText(this@Login, "Login Succesfull", Toast.LENGTH_SHORT).show()
//                }else{
//                    Toast.makeText(this@Login, "NIK/password salah", Toast.LENGTH_SHORT).show()
//                }
//                override fun onCancelled(error: DatabaseError) {
//                    TODO("Not yet implemented")
//                }
//            })
//    }
//    }

//    fun buttonlogin(V: View?){
//        var nik = nik?.text.toString().trim()
//        var pass = pass?.text.toString().trim()
//        if(nik.isEmpty()||pass.isEmpty()){
//            Toast.makeText(this@Login, "Wajib diisi", Toast.LENGTH_SHORT).show()
//        }else{
//                isEmailExist(nik,pass)
//        }
//
//    }
//}