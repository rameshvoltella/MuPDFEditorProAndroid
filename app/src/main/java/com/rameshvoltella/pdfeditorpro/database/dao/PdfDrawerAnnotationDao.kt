package com.rameshvoltella.pdfeditorpro.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.rameshvoltella.pdfeditorpro.database.PdfDrawAnnotation
import com.rameshvoltella.pdfeditorpro.database.data.QuadDrawPointsAndType

@Dao
interface PdfDrawerAnnotationDao {

    @Insert
    suspend fun insertAnnotation(annotation: PdfDrawAnnotation)

//    @Query("SELECT quadPoints, type FROM pdf_draw_annotations WHERE pdfname = :pdfname AND pagenumber = :pagenumber")
//    suspend fun getQuadDrawdPointsAndTypeByPage(pdfname: String, pagenumber: Int): List<QuadDrawPointsAndType>

//    @Query("SELECT * FROM pdf_draw_annotations WHERE pdfname = :pdfname AND pagenumber = :pagenumber")
//    suspend fun getQuadDrawPointsAndTypeByPage(
//        pdfname: String,
//        pagenumber: Int
//    ): List<PdfDrawAnnotation>
@Query("SELECT quadPoints, type FROM pdf_draw_annotations WHERE pdfname = :pdfname AND pagenumber = :pagenumber")
suspend fun getQuadDrawPointsAndTypeByPage(
    pdfname: String,
    pagenumber: Int
): List<QuadDrawPointsAndType>
    @Query("DELETE FROM pdf_draw_annotations WHERE id = :id")
    suspend fun deleteAnnotationById(id: Int)


    @Query("SELECT * FROM pdf_draw_annotations WHERE pdfname = :pdfname AND pagenumber = :pagenumber")
    suspend fun getDrawAnnotationsByPage(
        pdfname: String,
        pagenumber: Int
    ): List<PdfDrawAnnotation>
}


