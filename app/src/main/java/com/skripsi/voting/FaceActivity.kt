package com.skripsi.voting

import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Base64.NO_WRAP
import android.util.Base64.URL_SAFE
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.skripsi.voting.FaceDetection.MTCNN
import com.skripsi.voting.FaceRecognition.FaceNet
import kotlinx.android.synthetic.main.face.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException

class FaceActivity : AppCompatActivity() {
    private var img: Bitmap? = null
    private var strtoimg: Bitmap? = null
    private var image: Bitmap? = null
    private var image2: Bitmap? = null
    private var imageView: ImageView? = null
    private var imageView2: ImageView? = null
    private var button: Button? = null
    private var next: Button? =null
    private var textView: TextView? = null
    private var keterangan: TextView?= null
    lateinit var ref: DatabaseReference
    var image_uri: Uri? = null

    //crop bitmap dengan rect
    private fun cropFace(bitmap: Bitmap, mtcnn: MTCNN): Bitmap? {
        var croppedBitmap: Bitmap? = null
        try {
            val boxes = mtcnn.detectFaces(bitmap, 10)
            Log.i("MTCNN", "Jumlah muka terdeteksi" + boxes.size)
            val left = boxes[0].left()
            val top = boxes[0].top()
            val x = boxes[0].left()
            val y = boxes[0].top()
            var width = boxes[0].width()
            var height = boxes[0].height()
            if (y + height >= bitmap.height) height -= y + height - (bitmap.height - 1)
            if (x + width >= bitmap.width) width -= x + width - (bitmap.width - 1)
            Log.i("MTCNN", "Final x:" + (x + width).toString())
            Log.i("MTCNN", "Width: " + bitmap.width)
            Log.i("MTCNN", "Final y: " + (y + width).toString())
            Log.i("MTCNN", "Height: " + bitmap.width)
            croppedBitmap = Bitmap.createBitmap(bitmap, x, y, width, height)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return croppedBitmap
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.face)
        imageView = findViewById(R.id.wajah)
        imageView2 = findViewById(R.id.imageView2)
        button = findViewById(R.id.similiarity)
        textView = findViewById(R.id.textView)
//        keterangan = findViewById(R.id.keterangan)
//        next = findViewById(R.id.next)
        if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED || checkSelfPermission(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_DENIED
        ) {
            val permission =
                arrayOf(
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            requestPermissions(permission, 112)
        }

        imageView?.setOnLongClickListener{
            if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED || checkSelfPermission(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_DENIED
            ) {
                val permission = arrayOf(
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                requestPermissions(permission, MY_CAMERA_PERMISSION_CODE)
            } else {
                openCamera()
            }
            false
        }
        ref = FirebaseDatabase.getInstance().getReference("Users")
        val sharedPreferences = getSharedPreferences("myKey", MODE_PRIVATE)
        val value = sharedPreferences.getString("value", "")
        Log.d("id", value.toString())
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                CoroutineScope(Dispatchers.IO).launch {
                    var img2 = snapshot.child(value.toString()).child("gambar").value
                    Log.d("gambar", img2.toString())
                    image2=StringToBitMap(img2.toString())
                    Log.d("bitmap", image2.toString())

                    withContext(Dispatchers.Main) {
                        imageView2?.setImageBitmap(image2)
                    }
                }

            }


            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })


//                imageView2!!.setOnClickListener{ view: View? ->
//            val photoPickerIntent = Intent(Intent.ACTION_PICK)
//            photoPickerIntent.type ="image/*"
//            startActivityForResult(photoPickerIntent,PICK_IMAGEVIEW2_CONTENT)
//        }
//        next?.setOnClickListener{view : View? ->
//            val next = Intent(this@FaceActivity,Vote2::class.java)
//            startActivity(next)
//        }
        button?.setOnClickListener{ view: View? ->
            if(image== null || image2 == null){
                Toast.makeText(applicationContext, "Wajah tidak terdaftar", Toast.LENGTH_SHORT).show()
            }else{
                val mtcnn = MTCNN(assets)
                val wajah1= cropFace(image!!,mtcnn)
                val wajah2= cropFace(image2!!,mtcnn)
                mtcnn.close()
                var facenet: FaceNet? = null
                try{
                    facenet = FaceNet(assets)
                }catch (e: IOException){
                    e.printStackTrace()
                }
                if (wajah1 != null) imageView?.setImageBitmap(wajah1) else Log.i(
                    "deteksi", "Tidak bisa crop foto 1"
                )
                if (wajah2 != null) imageView2?.setImageBitmap(wajah2) else Log.i(
                    "deteksi","Tidak bisa crop foto 2"
                )
                if(wajah1!=null && wajah2!= null){
                    val skor = facenet!!.getSimilarityScore(wajah1,wajah2)
//                    val verified = facenet!!.getSimilarityKeterangan(wajah1,wajah2)
                    Log.i("Score", skor.toString())
//                    Log.i("Keterangan",verified.toString())
                    val text = String.format("Similarity score = $skor")
//                    val ket = String.format("Keterangan= $verified")
                    textView?.setText(text)
//                    keterangan?.setText(ket)

                    val sharedPreferences = getSharedPreferences("mythresh", MODE_PRIVATE)
                    val thresh = sharedPreferences.getString("thresh", "")
                    Log.d("thresh", thresh.toString())
                    if((skor.toString()).compareTo(thresh.toString())<0){
                        val i = Intent(this@FaceActivity,PagerActivity::class.java)
                        startActivity(i)
                    }else{
                        Toast.makeText(this@FaceActivity, "Wajah tidak terverifikasi", Toast.LENGTH_SHORT).show()
                    }
                }
                facenet!!.close()
            }
        }

    }

    fun StringToBitMap(encodedString: String?): Bitmap? {
            val encodeByte: ByteArray = Base64.decode(encodedString, Base64.DEFAULT)
            var poto = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
            return poto
    }

    private fun openCamera() {

        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        image_uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == IMAGE_CAPTURE_CODE && resultCode== RESULT_OK){
            if (requestCode == IMAGE_CAPTURE_CODE && resultCode == RESULT_OK){
                try{
                CoroutineScope(Dispatchers.IO).launch{
                    img = uriToBitmap(image_uri)
                    image = compressBitmap(img!!,50)
                    withContext(Dispatchers.Main){
                        imageView!!.setImageBitmap(image)
                    }
                }

            }catch (e: ActivityNotFoundException){
                    e.printStackTrace()
                    Toast.makeText(this@FaceActivity, "No Activity", Toast.LENGTH_SHORT).show()

                }
            }

        }
//        else if(requestCode == PICK_IMAGEVIEW2_CONTENT && resultCode == RESULT_OK){
//            try {
//                CoroutineScope(Dispatchers.IO).launch {
//                    val imageUri = data!!.data
//                    Log.d("image", imageUri.toString())
//                    val imageStream = contentResolver.openInputStream(imageUri!!)
//                    img2 = BitmapFactory.decodeStream(imageStream)
//                    image2 = compressBitmap(img2!!, 50)
//                    withContext(Dispatchers.Main) {
//                        imageView2!!.setImageBitmap(image2)
//                    }
//                }
//            }catch(e:FileNotFoundException){
//                e.printStackTrace()
//                Toast.makeText(this@FaceActivity, "Error loading data gambar", Toast.LENGTH_SHORT).show()
//            }
//        }
    }

    private fun uriToBitmap(selectedFileUri: Uri?): Bitmap?{
        try{
            val parcelFileDescriptor = contentResolver.openFileDescriptor(
                selectedFileUri!!,"r"
            )
            val fileDescriptor = parcelFileDescriptor!!.fileDescriptor
            val photo = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor.close()
            return photo
        } catch(e: IOException){
            e.printStackTrace()
        }
        return null
    }
    private fun compressBitmap(bitmap: Bitmap,quality:Int): Bitmap{
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,quality,stream)
        val byteArray = stream.toByteArray()
        return BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
    }

    companion object{
        private const val PICK_IMAGEVIEW2_CONTENT=2
        private const val MY_CAMERA_PERMISSION_CODE = 100
        const val IMAGE_CAPTURE_CODE =654
    }
}

