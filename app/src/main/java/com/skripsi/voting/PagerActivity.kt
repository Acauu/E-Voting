package com.skripsi.voting

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.database.*
import com.skripsi.voting.Adapter.MyAdapter
import com.skripsi.voting.Listener.IFirebaseLoadDone
import com.skripsi.voting.Transformer.DepthPageTransformer
import com.skripsi.voting.model.Person
import com.skripsi.voting.model.isVote
import kotlinx.android.synthetic.main.activity_page.*
import kotlinx.android.synthetic.main.view_pager_item.*

class PagerActivity: AppCompatActivity(), IFirebaseLoadDone {

    val positiveButtonClick = { dialog: DialogInterface, which: Int ->
        Toast.makeText(
            applicationContext,
            android.R.string.yes, Toast.LENGTH_SHORT
        ).show()

        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase!!.reference
        var obj = view_pager.getCurrentItem()
        Log.d("key", obj.toString())
        refhasil = FirebaseDatabase.getInstance().getReference("Candidate").child(obj.toString()).child("Vote")
        Log.d("namanya", refhasil.toString())

        refhasil.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val y = snapshot.getValue(Integer::class.java)!!
                val x = y.toInt()+1
                Log.d("vote", y.toString())
                Log.d("vote", x.toString())
                refhasil.setValue(x)

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        ref = FirebaseDatabase.getInstance().getReference("hasVote")
        val sharedPreferences = getSharedPreferences("myKey", MODE_PRIVATE)
        val value = sharedPreferences.getString("value", "")
        Log.d("nik", value.toString())

        var id=value.toString()
        var model = isVote(value.toString())
        ref.child(id).setValue(model)

        val final = Intent(this, Sukses::class.java)
        startActivity(final)
    }

    val negativeButtonClick = { dialog: DialogInterface, which: Int ->
        Toast.makeText(
            applicationContext,
            android.R.string.no, Toast.LENGTH_SHORT
        ).show()
    }

    lateinit var refhasil:DatabaseReference
    lateinit var ref: DatabaseReference
    lateinit var adapter:MyAdapter
    lateinit var kandidat:DatabaseReference
    lateinit var IFirebaseLoadDone : IFirebaseLoadDone

    var firebaseDatabase: FirebaseDatabase? = null
    var databaseReference: DatabaseReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page)
        kandidat = FirebaseDatabase.getInstance().getReference("Candidate")
        IFirebaseLoadDone = this

        loadData()
        view_pager.setPageTransformer(true,DepthPageTransformer())

    }

    override fun onDataLoadSuccess(dataList: List<Person>) {
        adapter = MyAdapter(this,dataList)
        view_pager.adapter = adapter
    }

    override fun onDataLoadFailed(message: String) {
        Toast.makeText(this, ""+message, Toast.LENGTH_SHORT).show()
    }

    private fun loadData(){
        kandidat.addListenerForSingleValueEvent(object :ValueEventListener{
            var kandidat : MutableList<Person> = ArrayList()

            override fun onCancelled(p0: DatabaseError) {
                IFirebaseLoadDone.onDataLoadFailed(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                for(data in p0.children)
                {
                    val data = data.getValue(Person::class.java)
                    kandidat.add(data!!)
                }
                IFirebaseLoadDone.onDataLoadSuccess(kandidat)
            }
        })
    }

    fun kirim1(view: View?) {
        basicAlert()
    }

    private fun basicAlert() {
        if (this !=null) {
            val builder = AlertDialog.Builder(this@PagerActivity, R.style.MyDialogTheme)
            with(builder)
            {
                setTitle("Message")
                setMessage("Apakah anda ingin mencoblos ${namakandidat?.text.toString()}?")
                setPositiveButton(
                    "OK",
                    DialogInterface.OnClickListener(function = positiveButtonClick)
                )
                setNegativeButton(android.R.string.no, negativeButtonClick)
//            show()
                val dialog = builder.create()
                dialog.show()

                val button = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                with(button) {
                    setTextColor(ContextCompat.getColor(this@PagerActivity, R.color.colorAccent))
                }
                val button2 = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                with(button2) {
                    setTextColor(ContextCompat.getColor(this@PagerActivity, R.color.colorAccent))
                }

            }
        }
    }

    fun AlertDialog.makeButtonTextGrey(){
        this.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
        this.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
    }



}