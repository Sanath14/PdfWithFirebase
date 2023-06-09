package com.example.pdfviewerfirebase

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.pdfviewerfirebase.databinding.ActivityMainBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var pdfFileUri: Uri? = null
    private lateinit var storageRef: StorageReference
    private lateinit var databaseRef: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storageRef = FirebaseStorage.getInstance().reference
        databaseRef = FirebaseDatabase.getInstance().getReference("pdfs")


        binding.floatingActionButton.setOnClickListener {
            launcher.launch("application/pdf")
        }

        binding.showAllBtn.setOnClickListener {
            startActivity(Intent(this, ShowActivity::class.java))
        }

        binding.uploadBtn.setOnClickListener {

            if (pdfFileUri != null) {
                uploadPdfToFirebase()
            } else {
                Toast.makeText(this, "Please Select PDF first", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun uploadPdfToFirebase() {
        val fileName = binding.fileName.text.toString()
        val mStorageRef = storageRef.child("pdfs/${System.currentTimeMillis()}/ $fileName")
        pdfFileUri?.let {
            mStorageRef.putFile(it).addOnSuccessListener {
                mStorageRef.downloadUrl.addOnSuccessListener { downLoadUrl ->
                    val pdfFile = PdfFile(fileName, downLoadUrl.toString())
                    databaseRef.push().key
                        ?.let { it1 -> databaseRef.child(it1).setValue(pdfFile) }
                        ?.addOnSuccessListener {
                            Toast.makeText(this, "Uploaded Successfully", Toast.LENGTH_SHORT).show()
                            pdfFileUri = null
                        }
                }

            }.addOnProgressListener { uploadTaskSnapshot ->
                val percent =
                    uploadTaskSnapshot.bytesTransferred * 100 / uploadTaskSnapshot.totalByteCount
                binding.progressBar.progress = percent.toInt()
                if (percent.toInt() == 100) {
                    binding.progressBar.visibility = View.GONE
                    binding.fileName.text = resources.getString(R.string.no_file)
                } else {
                    if (!binding.progressBar.isShown)
                        binding.progressBar.visibility = View.VISIBLE
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this, exception.message.toString(), Toast.LENGTH_SHORT).show()
            }

        }
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { pdfUri ->

                pdfFileUri = pdfUri
                val fileName = getFileName(this, pdfUri)
                binding.fileName.text = fileName

            }
        }

    @SuppressLint("Range")
    private fun getFileName(context: Context, uri: Uri): String {

        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor.use {
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        return cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    }
                }
            }
        }

        return uri.path?.substring(uri.path!!.lastIndexOf('/') + 1) ?: uri.toString()
    }
}