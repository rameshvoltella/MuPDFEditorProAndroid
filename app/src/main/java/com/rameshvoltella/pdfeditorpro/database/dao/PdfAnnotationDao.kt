package com.rameshvoltella.pdfeditorpro.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.rameshvoltella.pdfeditorpro.database.PdfAnnotation
import com.rameshvoltella.pdfeditorpro.database.data.QuadPointsAndType

@Dao
interface PdfAnnotationDao {

    @Insert
    suspend fun insertAnnotation(annotation: PdfAnnotation)

    @Query("SELECT quadPoints, type FROM pdf_annotations WHERE pdfname = :pdfname AND pagenumber = :pagenumber")
    suspend fun getQuadPointsAndTypeByPage(
        pdfname: String,
        pagenumber: Int
    ): List<QuadPointsAndType>

    @Query("DELETE FROM pdf_annotations WHERE id = :id")
    suspend fun deleteAnnotationById(id: Int)
}
