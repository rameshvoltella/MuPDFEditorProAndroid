package com.artifex.mupdfdemo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.content.Context
import android.util.Log

class PDFTextExtractor {

    suspend fun extractText(
        core: MuPDFCore?,
        pdfPassword: String="",pageNumber:Int
    ): String? = withContext(Dispatchers.IO) {  // Use IO dispatcher for blocking operations
        try {
            if(pdfPassword.length>0) {
                core!!.authenticatePassword(pdfPassword)
            }
            SearchTaskResult.set(null)
        } catch (x: Exception) {
            return@withContext null
        }

        try {
            // Extract text as HTML from the first page (index 1)
            val pageData = core!!.textPerPage(pageNumber)
            Log.d("pager", ">>"+pageData)

//            core!!.onDestroy()

            return@withContext pageData
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
//            core?.onDestroy()  // Ensure the core is destroyed in case of an exception
        }
        return@withContext null
    }


}
