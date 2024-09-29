package com.rameshvoltella.pdfeditorpro.data.local

import com.artifex.mupdfdemo.MuPDFCore
import com.rameshvoltella.pdfeditorpro.database.PdfAnnotation
import kotlinx.coroutines.flow.Flow

interface LocalRepositorySource {
    suspend fun getPageText(lastPageNumber: Int,totalPages: Int,muPDFCore: MuPDFCore,isSinglePage:Boolean=false): Flow<ArrayList<String>>


}