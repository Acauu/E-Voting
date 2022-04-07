package com.skripsi.voting

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class Navbar :AppCompatActivity (), NavigationView.OnNavigationItemSelectedListener {
    val positiveButtonClick = { dialog: DialogInterface, which: Int ->
        Toast.makeText(
            applicationContext,
            android.R.string.yes, Toast.LENGTH_SHORT
        ).show()
    }
    val negativeButtonClick = { dialog: DialogInterface, which: Int ->
        Toast.makeText(
            applicationContext,
            android.R.string.no, Toast.LENGTH_SHORT
        ).show()
    }

    lateinit var ref: DatabaseReference
    lateinit var refdate:DatabaseReference
    lateinit var reftime:DatabaseReference
    lateinit var refthresh: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navbar)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        val toggle = ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer.setDrawerListener(toggle)
        toggle.syncState()
        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)

        refthresh = FirebaseDatabase.getInstance().getReference("Settings").child("Threshold")
        refthresh.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var thresh = snapshot.value.toString()
                val sharedPref = getSharedPreferences("mythresh", MODE_PRIVATE)
                val editor = sharedPref.edit()
                editor.putString("thresh", thresh)
                editor.apply()

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

            override fun onBackPressed() {
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.navhome, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        return if (id == R.id.action_settings) {
            true
        } else super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.nav_home) {
            //tidak ada ji aktivitas
        }
//        else if (id == R.id.nav_camera) {
//            val a = Intent(this, question::class.java)
//            startActivity(a)
//        }
        else if (id == R.id.nav_slideshow) {
                finish()
                moveTaskToBack(true)

        }
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    fun live(view: View?) {
        val date = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val time = SimpleDateFormat("HHmmss", Locale.getDefault())
        val currentDate: String = date.format(Date())
        val currentTime: String = time.format(Date())
        refdate = FirebaseDatabase.getInstance().getReference("Settings").child("Date")
        refdate.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var tanggal = snapshot.value.toString()
                Log.d("tanggal", tanggal)

                reftime = FirebaseDatabase.getInstance().getReference("Settings").child("Time")
                reftime.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var waktu = snapshot.value.toString()
                        Log.d("waktu", waktu)
                        if (waktu.compareTo(currentTime) < 0 && tanggal == currentDate) {
                            val d = Intent(this@Navbar, VotelistActivity::class.java)
                            startActivity(d)
                        }else{
                            Toast.makeText(this@Navbar, "Hasil Pemilihan belum dapat diakses", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
            }


            override fun onCancelled(error:DatabaseError){
                TODO("Not yet implemented")
            }

        })
    }



    fun pilih(view: View?) {
//        val currentTime: Date = Calendar.getInstance().time
        val sdf = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
        val date = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val time = SimpleDateFormat("HHmmss", Locale.getDefault())
//        Log.d("time", sdf.toString())
//        val currentDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
//        Log.d("time1", currentDate.toString())
//        val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
//        Log.d("time2", currentTime.toString())
        val currentDate: String = date.format(Date())
        val currentTime: String = time.format(Date())
        Log.d("date", currentDate)
        Log.d("time", currentTime)
//        Log.d("tanggal", sdf.toString())


        refdate = FirebaseDatabase.getInstance().getReference("Settings").child("Date")
        refdate.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var tanggal = snapshot.value.toString()
                Log.d("tanggal", tanggal)

                reftime = FirebaseDatabase.getInstance().getReference("Settings").child("Time")
                reftime.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var waktu=snapshot.value.toString()
                        Log.d("waktu",waktu)


                        ref = FirebaseDatabase.getInstance().getReference("hasVote")
                        val sharedPreferences = getSharedPreferences("myKey", MODE_PRIVATE)
                        val value = sharedPreferences.getString("value", "")

                        Log.d("id", value.toString())
                        ref.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if(waktu.compareTo(currentTime)<0){
                                    Toast.makeText(this@Navbar, "Pemilihan sudah ditutup", Toast.LENGTH_SHORT).show()
                                }else if (snapshot.hasChild(value.toString())   ){
                                    basicAlert()
                                } else if(tanggal.equals(currentDate)){
                                    isvote()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }
                        })
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
            }

        override fun onCancelled(error:DatabaseError){
            TODO("Not yet implemented")
            }

        })
    }

        private fun basicAlert() {

        val builder = AlertDialog.Builder(this@Navbar)
        with(builder)
        {
            setTitle("Alert")
            setMessage("Suaramu sudah terekam")
            setPositiveButton("OK", DialogInterface.OnClickListener(function = positiveButtonClick))
            setNegativeButton(android.R.string.no, negativeButtonClick)
            show()
        }
    }

    fun isvote(){
//        val d = Intent(this,Vote2::class.java)
//        startActivity(d)
        val d = Intent(this,FaceActivity::class.java)
        startActivity(d)
    }

}
