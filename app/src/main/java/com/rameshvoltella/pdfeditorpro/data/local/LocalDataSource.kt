package com.rameshvoltella.pdfeditorpro.data.local

import com.artifex.mupdfdemo.MuPDFCore


interface LocalDataSource {
    suspend fun getPageText(lastPageNumber: Int,totalPages: Int,muPDFCore: MuPDFCore): ArrayList<String>

}