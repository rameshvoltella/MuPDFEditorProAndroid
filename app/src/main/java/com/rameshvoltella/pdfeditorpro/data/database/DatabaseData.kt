package com.rameshvoltella.pdfeditorpro.data.database

import com.rameshvoltella.pdfeditorpro.database.PdfAnnotation
import com.rameshvoltella.pdfeditorpro.database.dao.PdfAnnotationDao
import com.rameshvoltella.pdfeditorpro.database.data.QuadPointsAndType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DatabaseData @Inject constructor(
    private val pdfAnnotationDao: PdfAnnotationDao
) :DatabaseDataSource{
    override suspend fun insertAnnotation(annotation: PdfAnnotation): Boolean {
        return withContext(Dispatchers.IO){
            pdfAnnotationDao.insertAnnotation(annotation)
            true
        }

    }

    override suspend fun getQuadPointsAndTypeByPage(pdfname: String, pagenumber: Int): List<QuadPointsAndType> {
        return withContext(Dispatchers.IO){ pdfAnnotationDao.getQuadPointsAndTypeByPage(pdfname, pagenumber)}
    }

    override suspend fun deleteAnnotationById(id: Int): Boolean {
        return withContext(Dispatchers.IO){ pdfAnnotationDao.deleteAnnotationById(id)
        true}
    }
}
