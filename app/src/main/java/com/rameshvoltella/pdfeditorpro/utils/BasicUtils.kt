package com.rameshvoltella.pdfeditorpro.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
public fun hideKeyboardFromView(context: Context,view: View) {
    val imm = context.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

public fun showKeyboardFromView(context: Context,view: View)  {
    val imm = context.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(view, 0)
}



suspend fun generateNormalThumbnail(pdfFilePath: String): Bitmap? {
    return withContext(Dispatchers.IO) {
        var pdfRenderer: PdfRenderer? = null
        var currentPage: PdfRenderer.Page? = null
        try {
            val file = File(pdfFilePath)
            if (!file.exists()) {
                return@withContext null
            }

            val fileDescriptor =
                ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            pdfRenderer = PdfRenderer(fileDescriptor)

            // Use the first page to generate the thumbnail
            currentPage = pdfRenderer.openPage(0)
            val bitmap: Bitmap = Bitmap.createBitmap(
                currentPage.width / 4, currentPage.height / 4, Bitmap.Config.ARGB_8888
            )
            currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

            return@withContext bitmap
        } catch (ex: OutOfMemoryError) {
            // Log.e("ThumbnailGenerator", "Memory issue generating thumbnail", ex)
            return@withContext null
        } catch (ex: Exception) {
            // Log.e("ThumbnailGenerator", "Error generating thumbnail", ex)
            return@withContext null
        } finally {
            currentPage?.close()
            pdfRenderer?.close()
        }
    }
}
