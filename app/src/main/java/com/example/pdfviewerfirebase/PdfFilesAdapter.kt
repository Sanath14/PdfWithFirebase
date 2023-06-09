package com.example.pdfviewerfirebase

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pdfviewerfirebase.databinding.EachPdfItemBinding

class PdfFilesAdapter(private val listener: PdfCardClickListener) :
    ListAdapter<PdfFile, PdfFilesAdapter.PdfFilesViewHolder>(DiffCallback()) {

    inner class PdfFilesViewHolder(private val binding: EachPdfItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                listener.onPdfCardClicked(getItem(adapterPosition).downloadUrl)
            }
        }

        fun bind(pdfFile: PdfFile) {
            binding.fileName.text = pdfFile.fileName
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfFilesViewHolder {
        val binding = EachPdfItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PdfFilesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PdfFilesViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

interface PdfCardClickListener {
    fun onPdfCardClicked(downloadUrl: String)
}

private class DiffCallback : DiffUtil.ItemCallback<PdfFile>() {

    override fun areItemsTheSame(
        oldItem: PdfFile,
        newItem: PdfFile
    ) = oldItem.downloadUrl == newItem.downloadUrl


    override fun areContentsTheSame(
        oldItem: PdfFile,
        newItem: PdfFile
    ) = oldItem == newItem

}