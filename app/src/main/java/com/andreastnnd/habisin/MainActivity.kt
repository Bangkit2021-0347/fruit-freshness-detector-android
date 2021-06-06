package com.andreastnnd.habisin

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.andreastnnd.habisin.databinding.ActivityMainBinding
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), UploadRequestBody.UploadCallback {

    companion object {
        private const val REQUEST_IMAGE_PICKER_CODE = 1
        private const val REQUEST_CAMERA_CODE = 2
        private const val PERMISSION_CAMERA_CODE = 102
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var currentPhotoPath: String

    private var selectedImage: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            btnCamera.setOnClickListener { askCameraPermission() }
            btnGallery.setOnClickListener { openImageChooser() }
            btnUpload.setOnClickListener { uploadImage() }
        }
    }

    private fun uploadImage() {
        if (selectedImage == null) {
            binding.root.snackbar("Select an Image First")
            return
        }

        val parcelFileDescriptor =
            contentResolver.openFileDescriptor(selectedImage!!, "r", null) ?: return
        val file = File(cacheDir, contentResolver.getFileName(selectedImage!!))
        val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)

        binding.progressBar.progress = 0
        val body = UploadRequestBody(file, "image", this)

        Api().uploadImage(
            MultipartBody.Part.createFormData("image", file.name, body)
        ).enqueue(object : Callback<UploadResponse> {
            override fun onResponse(
                call: Call<UploadResponse>,
                response: Response<UploadResponse>
            ) {
                binding.progressBar.progress = 100
                Log.d("upload", "freshness_level: {${response.body()?.freshness_level}}")
                Log.d("upload", "price: {${response.body()?.price}}")

                val freshnessLevel = response.body()?.freshness_level.toString()
                val price = response.body()?.price.toString()

                Intent(this@MainActivity, DetailActivity::class.java).also {
                    it.putExtra(DetailActivity.EXTRA_IMAGE, selectedImage.toString())
                    it.putExtra(DetailActivity.EXTRA_FRESHNESS_LEVEL, freshnessLevel)
                    it.putExtra(DetailActivity.EXTRA_PRICE, price)
                    startActivity(it)
                }
            }

            override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                binding.root.snackbar(t.message!!)
                Log.d("upload", "onFailure: ${t.message}")
            }
        })
    }

    private fun openImageChooser() {
        Intent(Intent.ACTION_PICK).also {
            it.type = "image/*"
            val mimeTypes = arrayOf("image/jpeg", "image/jpg", "image/png")
            it.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            startActivityForResult(it, REQUEST_IMAGE_PICKER_CODE)
        }
    }

    private fun askCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_CODE
            )
        } else {
            openCamera()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CAMERA_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(
                    this,
                    "Camera Permission is Required to User Camera",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    @Throws(IOException::class)
    private fun createPhotoFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply { currentPhotoPath = absolutePath }
    }

    private fun openCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { imageCaptureIntent ->
            imageCaptureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createPhotoFile()
                } catch (e: IOException) {
                    // Error occurred while creating the File
                    null
                }

                photoFile?.also {
                    selectedImage = FileProvider.getUriForFile(
                        this,
                        "com.andreastnnd.android.fileprovider",
                        it
                    )

                    imageCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, selectedImage)
                    startActivityForResult(imageCaptureIntent, REQUEST_CAMERA_CODE)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_PICKER_CODE -> {
                    selectedImage = data?.data
                    binding.imageView.setImageURI(selectedImage)
                }
                REQUEST_CAMERA_CODE -> {
                    binding.imageView.setImageURI(selectedImage)
                }
            }
        }
    }

    override fun onProgressUpdate(percentage: Int) {
        binding.progressBar.progress = percentage
    }
}
