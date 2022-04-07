package com.skripsi.voting

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import com.google.firebase.database.*
import com.skripsi.voting.model.signup
import java.io.ByteArrayOutputStream
import java.io.IOException

class Regis : AppCompatActivity() {
    private lateinit var textoutputt: EditText
    private var imageView : ImageView? = null
    private var image: Bitmap?= null
    var image_uri: Uri?= null
//    private var emailreg: EditText? = null
    private var name: EditText? = null
    private var passreg: EditText? = null
    private var passconfirm: EditText?=null



    lateinit var ref: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_regis)
        imageView = findViewById(R.id.foto1)
//        emailreg = findViewById<View>(R.id.emailreg) as EditText
        passreg = findViewById<View>(R.id.passreg) as EditText
        passconfirm = findViewById<View>(R.id.passconfirm) as EditText
        name = findViewById<View>(R.id.name) as EditText
        textoutputt = findViewById(R.id.textoutputt)
        if (intent.extras != null) {
            val bundle = intent.extras
            textoutputt.setText(bundle?.getString("nik"))

        }
        ref = FirebaseDatabase.getInstance().getReference("Users")

        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED || checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_DENIED
        ) {
            val permission =
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            requestPermissions(permission, 112)
        }
        imageView?.setOnLongClickListener {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED || checkSelfPermission(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_DENIED
            ) {
                val permission = arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                requestPermissions(permission, MY_CAMERA_PERMISSION_CODE)
            } else {
                openCamera()
            }
            false
        }

    }

    private fun openCamera() {

        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        image_uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)
    }

     fun registrasi(V: View?) {
         var nama = name?.text.toString().trim()
         var nik = textoutputt?.text.toString().trim()
         var pass = passreg?.text.toString().trim()
         var passconfirm = passconfirm?.text.toString().trim()
         var gambar = image?.let { BitMapToString(it) }


         if (nama.isEmpty() || nik.isEmpty() || pass.isEmpty() || passconfirm.isEmpty()) {
             Toast.makeText(this@Regis, "Semua kolom wajib diisi", Toast.LENGTH_SHORT).show()
         } else if (gambar == null) {
             Toast.makeText(this@Regis, "Upload foto diri", Toast.LENGTH_SHORT).show()
         } else if (!pass.equals(passconfirm)){
             Toast.makeText(this@Regis, "Password tidak terkonfirmasi", Toast.LENGTH_SHORT).show()
         } else {
             ref.addValueEventListener(object : ValueEventListener {
                 override fun onDataChange(snapshot: DataSnapshot) {
                     if (snapshot.hasChild(nik)) {
                         Toast.makeText(this@Regis, "NIK sudah terdaftar", Toast.LENGTH_SHORT).show()
                     } else {
                         var id = nik
                         var model = signup(nama, nik, pass, gambar, id)
                         ref.child(id).setValue(model)
                         Toast.makeText(this@Regis, "Pendaftaran berhasil", Toast.LENGTH_SHORT).show()
                         startActivity(Intent(applicationContext, Login::class.java))
                         finish()
                     }
                 }

                 override fun onCancelled(error: DatabaseError) {
                     TODO("Not yet implemented")
                 }
             })
         }

     }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_CAPTURE_CODE && resultCode == RESULT_OK) {
            if (requestCode == IMAGE_CAPTURE_CODE && resultCode == RESULT_OK) {
                var img = uriToBitmap(image_uri)
                image = img?.let { compressBitmap(it,50) }
//                val img = image?.let { BitMapToString(it) }
                imageView!!.setImageBitmap(image)


            }
        }
    }

    fun BitMapToString(bitmap: Bitmap): String? {
        val ByteStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, ByteStream)
        val b = ByteStream.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }

    private fun uriToBitmap(selectedFileUri: Uri?): Bitmap? {
        try {
            val parcelFileDescriptor = contentResolver.openFileDescriptor(
                selectedFileUri!!, "r"
            )
            val fileDescriptor = parcelFileDescriptor!!.fileDescriptor
            val photo = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor.close()
            return photo
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun compressBitmap(bitmap:Bitmap, quality:Int):Bitmap{
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,quality, stream)
        val byteArray = stream.toByteArray()
        return BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
    }

    companion object {
        private const val PICK_IMAGEVIEW_CONTENT = 1
        private const val PICK_IMAGEVIEW2_CONTENT = 2
        private const val CAMERA_REQUEST = 1888
        private const val MY_CAMERA_PERMISSION_CODE = 100
        private const val RESULT_LOAD_IMAGE = 123
        const val IMAGE_CAPTURE_CODE = 654
        val REQUEST_IMAGE_CAPTURE = 1
    }

}