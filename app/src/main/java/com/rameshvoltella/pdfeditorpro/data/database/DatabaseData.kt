package com.rameshvoltella.pdfeditorpro.data.database

import android.graphics.PointF
import android.graphics.RectF
import com.rameshvoltella.pdfeditorpro.database.PdfAnnotation
import com.rameshvoltella.pdfeditorpro.database.PdfDrawAnnotation
import com.rameshvoltella.pdfeditorpro.database.dao.PdfAnnotationDao
import com.rameshvoltella.pdfeditorpro.database.dao.PdfDrawerAnnotationDao
import com.rameshvoltella.pdfeditorpro.database.data.QuadPointsAndType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DatabaseData @Inject constructor(
    private val pdfAnnotationDao: PdfAnnotationDao,private val pdfDrawerAnnotationDao: PdfDrawerAnnotationDao
) :DatabaseDataSource{
    override suspend fun insertAnnotation(annotation: PdfAnnotation): Boolean {
        return withContext(Dispatchers.IO){
            pdfAnnotationDao.insertAnnotation(annotation)
            true
        }

    }

    override suspend fun insertDrawAnnotation(annotation: PdfDrawAnnotation): Boolean {
        return withContext(Dispatchers.IO){
            pdfDrawerAnnotationDao.insertAnnotation(annotation)
            true
        }    }

    override suspend fun getQuadPointsAndTypeByPage(pdfname: String, pagenumber: Int): List<QuadPointsAndType> {
        return withContext(Dispatchers.IO){ pdfAnnotationDao.getQuadPointsAndTypeByPage(pdfname, pagenumber)}
    }

    override suspend fun deleteAnnotationById(id: Int): Boolean {
        return withContext(Dispatchers.IO){ pdfAnnotationDao.deleteAnnotationById(id)
        true}
    }

    override suspend fun getQuadPointsAndTypeByPageToDelete(
        pdfname: String,
        pagenumber: Int,selectedRect: RectF
    ): Boolean {
        return withContext(Dispatchers.IO){


            val annotationInList=pdfAnnotationDao.getAnnotationsByPage(pdfname, pagenumber)

            for(annotation in annotationInList){
                println("konacheck---------------------------------"+selectedRect)

                println("konacheckfirstList"+annotation.quadPoints)

                for(point in annotation.quadPoints){
                    println("konacheckPoints---pointx>"+point.x+"-pointy>"+point.y)

                    if(selectedRect.contains(point.x,point.y)){
                        println("konacheckGOTPoints---pointx>"+point.x+"-pointy>"+point.y)

                        pdfAnnotationDao.deleteAnnotationById(annotation.id)
                        break
                    }
                }
                println("----------------OVER-----------------")


            }


            val result = pdfDrawerAnnotationDao.getQuadPointsAndTypeByPage(pdfname, pagenumber)


            // Check each annotation's quadPoints
            for (annotation in result) {
                val quadPoints = annotation.getQuadPoints() // Get the PointF[][]
                println("konacheckiop---------------------------------"+quadPoints)

                // Check each PointF in the quadPoints
                for (points in quadPoints) {
                    println("konacheckiop---------------points------------------"+points)

                    for (point in points) {
                        println("konacheckiop---------------points-point------------------"+point)

                        if (selectedRect.contains(point.x, point.y)) {
                            println("konacheckiop---------------Contains------------------"+point)

                            pdfAnnotationDao.deleteAnnotationById(annotation.id)
                            break

                        }
                    }
                    println("----------------OVER-----------------")

                }
            }

            true

        }
    }
}
