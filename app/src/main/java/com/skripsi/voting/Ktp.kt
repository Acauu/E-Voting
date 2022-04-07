package com.skripsi.voting

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.skripsi.voting.permissionkit.askPermissions
import kotlinx.android.synthetic.main.content_ktp.*
import org.jetbrains.anko.toast
import org.michaelbel.bottomsheet.BottomSheet
import timber.log.Timber
import java.util.regex.Pattern
import com.marchinram.rxgallery.RxGallery
import java.io.ByteArrayOutputStream
import java.io.IOException

class Ktp : AppCompatActivity(){

    lateinit var uriPath: Uri
    lateinit var ref: DatabaseReference
    private lateinit var textoutput: TextView
    private var foto: ImageView? = null
    private var bitmap: Bitmap?= null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_ktp)
//        setContentView(R.layout.activity_main_camera)
        textoutput = findViewById(R.id.textoutput)
        foto = findViewById(R.id.foto)

        ref = FirebaseDatabase.getInstance().getReference("NIK")
        foto?.setOnClickListener {
            showBottomView()
        }
        deteksi.setOnClickListener {
            if (::uriPath.isInitialized) {
                startOCR()
            }
        }
//        liveCheck.setOnClickListener {
//            startActivity(intentFor<LivePreviewActivity>())
//
//        }
        reqPermission()
    }

    fun reqPermission() {
        askPermissions(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) {
            onGranted {
                toast("granted")
            }
            onDenied {
                toast("wajib")
            }
            onShowRationale {
                toast("wajib")
            }
            onNeverAskAgain {
                toast("wajib")
            }
        }
    }

    fun startOCR() {
        FirebaseApp.initializeApp(this)
        val detector = FirebaseVision.getInstance().onDeviceTextRecognizer
        val image = FirebaseVisionImage.fromFilePath(this, uriPath)
        detector.processImage(image)
            .addOnSuccessListener { firebaseVisionText ->
                for (blockText in firebaseVisionText.textBlocks) {
                    Timber.d(blockText.text)
                    val regexKtpPattern = "[0-9]{8,16}"
                    val pattern = Pattern.compile(regexKtpPattern)
                    val matcher = pattern.matcher(blockText.text)
                    if (matcher.find()) {
                        textoutput.text = matcher.group()
                        var bundle = Bundle()
                        bundle.putString("nik",matcher.group())
                        val c = Intent(this,Regis::class.java)
                        c.putExtras(bundle)
                        startActivity(c)


                    }
                }
            }
            .addOnFailureListener {
                Timber.e(it)
                toast("Failed")
            }
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

    fun showBottomView() {
        val items = arrayOf("Camera", "Galery")
        val builder = BottomSheet.Builder(this)
        builder.setDarkTheme(false)
        builder.setWindowDimming(80)
        builder.setDividers(false)
        builder.setFullWidth(false)
        builder.setItems(
            items
        ) { dialog, which ->
            when (which) {
                0 -> {
                    RxGallery.photoCapture(this).subscribe({ uriPhoto ->
                        Timber.d(uriPhoto.toString())
                        uriPath = uriPhoto
                        val img = uriToBitmap(uriPath)
                        bitmap = img?.let { compressBitmap(it,50) }
                        foto!!.setImageBitmap(bitmap)
                    }, { failed ->
                        failed.message?.let { toast(it) }
                    })
                }
                1 -> {
                    RxGallery.gallery(this, false, RxGallery.MimeType.IMAGE).subscribe({ uriPhoto ->
                        Timber.d(uriPhoto.toString())
                        uriPath = uriPhoto[0]
                        val img = uriToBitmap(uriPath)
                        bitmap = img?.let { compressBitmap(it,50) }
                        foto!!.setImageBitmap(bitmap)
                    }, { failed ->
                        failed.message?.let { toast(it) }
                    })
                }
            }
        }
        builder.show()
    }




}