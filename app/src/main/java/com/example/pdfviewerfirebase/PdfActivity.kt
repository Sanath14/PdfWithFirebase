package com.example.pdfviewerfirebase

import android.media.SoundPool
import android.media.SoundPool.OnLoadCompleteListener
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.example.pdfviewerfirebase.databinding.ActivityPdfBinding
import com.github.barteksc.pdfviewer.listener.OnRenderListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL


class PdfActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPdfBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val downloadUrl = intent.extras?.getString("downloadUrl")

        lifecycleScope.launch(Dispatchers.IO) {

            val input = URL(downloadUrl).openStream()

            withContext(Dispatchers.Main) {

                binding.pdfView.fromStream(input).onRender { nbPages, pageWidth, pageHeight ->
                    if (nbPages >= 1) {
                        binding.progressBar.visibility = View.GONE
                    }
                }.load()
            }

        }


    }
}