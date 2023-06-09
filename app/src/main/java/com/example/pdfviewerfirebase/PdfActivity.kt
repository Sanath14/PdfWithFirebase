package com.example.pdfviewerfirebase

import android.app.DownloadManager
import android.content.Context
import android.media.SoundPool
import android.media.SoundPool.OnLoadCompleteListener
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.example.pdfviewerfirebase.databinding.ActivityPdfBinding
import com.github.barteksc.pdfviewer.listener.OnRenderListener
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL


class PdfActivity : AppCompatActivity(), DownloadProgressUpdater.DownloadProgressListener {

    private lateinit var binding: ActivityPdfBinding
    private lateinit var downloadManager: DownloadManager
    private lateinit var progressUpdater: DownloadProgressUpdater
    private lateinit var snackBar: Snackbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPdfBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val downloadUrl = intent.extras?.getString("downloadUrl")
        val fileName = intent.extras?.getString("fileName")

        downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        snackBar = Snackbar.make(binding.mainLayout, "", Snackbar.LENGTH_INDEFINITE)

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

        binding.floatingActionButton.setOnClickListener {

            downloadPdf(downloadUrl, fileName)
        }


    }

    private fun downloadPdf(downloadUrl: String?, fileName: String?) {

        try {
            val downloadUri: Uri = Uri.parse(downloadUrl)
            val request = DownloadManager.Request(downloadUri)

            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle(fileName)
                .setMimeType("application/pdf")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    File.separator + fileName
                )
            val downloadId = downloadManager.enqueue(request)

            binding.progressBar.visibility = View.VISIBLE

            progressUpdater = DownloadProgressUpdater(downloadManager, downloadId, this)

            lifecycleScope.launch(Dispatchers.IO) {
                progressUpdater.run()
            }
            snackBar.show()

        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }

    }

    override fun updateProgress(progress: Long) {

        lifecycleScope.launch(Dispatchers.Main) {

            when (progress) {

                DOWNLOAD_COMPLETE -> {
                    snackBar.setText("Downloading ...... ${DOWNLOAD_COMPLETE.toInt()} %")
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@PdfActivity,
                        "Downloaded Successfully !!",
                        Toast.LENGTH_SHORT
                    ).show()
                    snackBar.dismiss()
                }

                DOWNLOAD_FAILED -> {
                    snackBar.dismiss()
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@PdfActivity,
                        "Please try again",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {
                    binding.progressBar.progress = progress.toInt()
                    snackBar.setText("Downloading ...... ${progress.toInt()} %")
                }
            }

        }

    }
}