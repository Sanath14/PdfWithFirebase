package com.example.pdfviewerfirebase

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.pdfviewerfirebase.databinding.ActivityShowBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ShowActivity : AppCompatActivity(), PdfCardClickListener {

    private lateinit var binding: ActivityShowBinding

    private lateinit var databaseRef: DatabaseReference
    private lateinit var adapter: PdfFilesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityShowBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseRef = FirebaseDatabase.getInstance().getReference("pdfs")
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
        adapter = PdfFilesAdapter(this)
        binding.recyclerView.adapter = adapter
        getAllPdfs()
    }

    private fun getAllPdfs() {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val tempList = mutableListOf<PdfFile>()
                snapshot.children.forEach {
                    it.getValue(PdfFile::class.java)?.let { it1 -> tempList.add(it1) }
                }
                binding.progressBar.visibility = View.GONE
                adapter.submitList(tempList)
                if (tempList.isEmpty())
                    Snackbar.make(binding.mainLayout, "Nothing Found", Snackbar.LENGTH_SHORT).show()
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@ShowActivity, error.message, Toast.LENGTH_SHORT).show()
            }

        })
    }

    override fun onPdfCardClicked(pdfFile: PdfFile) {
        val intent = Intent(this, PdfActivity::class.java)
        intent.putExtra("downloadUrl", pdfFile.downloadUrl)
        intent.putExtra("fileName", pdfFile.fileName)
        startActivity(intent)
    }


}