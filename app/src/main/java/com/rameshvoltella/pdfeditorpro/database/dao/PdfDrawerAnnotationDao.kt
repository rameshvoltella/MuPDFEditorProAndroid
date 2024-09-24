package com.rameshvoltella.pdfeditorpro.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.rameshvoltella.pdfeditorpro.database.PdfAnnotation
import com.rameshvoltella.pdfeditorpro.database.PdfDrawAnnotation
import com.rameshvoltella.pdfeditorpro.database.data.QuadPointsAndType

@Dao
interface PdfDrawerAnnotationDao {

    @Insert
    suspend fun insertAnnotation(annotation: PdfDrawAnnotation)

    @Query("SELECT quadPoints, type FROM pdf_draw_annotations WHERE pdfname = :pdfname AND pagenumber = :pagenumber")
    suspend fun getQuadPointsAndTypeByPage(pdfname: String, pagenumber: Int): List<PdfDrawAnnotation>

    @Query("DELETE FROM pdf_draw_annotations WHERE id = :id")
    suspend fun deleteAnnotationById(id: Int)
}
