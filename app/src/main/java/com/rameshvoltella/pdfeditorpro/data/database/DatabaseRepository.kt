package com.rameshvoltella.pdfeditorpro.data.database

import android.graphics.RectF
import com.rameshvoltella.pdfeditorpro.database.PdfAnnotation
import com.rameshvoltella.pdfeditorpro.database.data.QuadPointsAndType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseRepository @Inject constructor(
    val databaseData: DatabaseData
):DatabaseRepositorySource {
    override suspend fun insertAnnotation(annotation: PdfAnnotation): Flow<Boolean> {
        return flow {emit(databaseData.insertAnnotation(annotation))  }

    }

    override suspend fun getQuadPointsAndTypeByPage(
        pdfname: String,
        pagenumber: Int
    ): Flow<List<QuadPointsAndType>> {
        return flow {emit(databaseData.getQuadPointsAndTypeByPage(pdfname,pagenumber))  }
    }

    override suspend fun deleteAnnotationById(id: Int): Flow<Boolean> {
        return flow {emit(databaseData.deleteAnnotationById(id))  }
    }

    override suspend fun getQuadPointsAndTypeByPageToDelete(
        pdfname: String,
        pagenumber: Int,
        selectedRect: RectF
    ): Flow<Boolean> {
        return flow {emit(databaseData.getQuadPointsAndTypeByPageToDelete(pdfname,pagenumber,selectedRect))  }

    }

}