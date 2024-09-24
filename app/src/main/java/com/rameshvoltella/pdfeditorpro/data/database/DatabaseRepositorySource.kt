package com.rameshvoltella.pdfeditorpro.data.database

import android.graphics.RectF
import com.rameshvoltella.pdfeditorpro.database.PdfAnnotation
import com.rameshvoltella.pdfeditorpro.database.PdfDrawAnnotation
import com.rameshvoltella.pdfeditorpro.database.data.QuadDrawPointsAndType
import com.rameshvoltella.pdfeditorpro.database.data.QuadPointsAndType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

interface DatabaseRepositorySource {
    suspend fun insertAnnotation(annotation: PdfAnnotation): Flow<Boolean>
    suspend fun insertDrawAnnotation(annotation: PdfDrawAnnotation): Flow<Boolean>

    suspend fun getQuadPointsAndTypeByPage(pdfname: String, pagenumber: Int): Flow<List<QuadPointsAndType>>
    suspend fun deleteAnnotationById(id: Int) : Flow<Boolean>
    suspend fun getQuadPointsAndTypeByPageToDelete(pdfname: String, pagenumber: Int,selectedRect: RectF): Flow<Boolean>
    suspend fun getDrawQuadPointsAndTypeByPage(pdfname: String, pagenumber: Int):Flow< List<QuadDrawPointsAndType>>

}