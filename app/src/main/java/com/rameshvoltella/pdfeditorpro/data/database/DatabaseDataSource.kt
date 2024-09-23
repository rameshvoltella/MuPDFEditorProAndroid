package com.rameshvoltella.pdfeditorpro.data.database

import com.rameshvoltella.pdfeditorpro.database.PdfAnnotation
import com.rameshvoltella.pdfeditorpro.database.data.QuadPointsAndType
import kotlinx.coroutines.flow.Flow

interface DatabaseDataSource {
    suspend fun insertAnnotation(annotation: PdfAnnotation): Boolean

    suspend fun getQuadPointsAndTypeByPage(pdfname: String, pagenumber: Int): List<QuadPointsAndType>
    suspend fun deleteAnnotationById(id: Int) : Boolean
}