package com.example.pdfviewerfirebase

data class PdfFile(val fileName : String , val downloadUrl : String){
    constructor() : this("","")
}
