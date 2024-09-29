package com.rameshvoltella.pdfeditorpro.data.local

import android.util.Log
import com.artifex.mupdfdemo.MuPDFCore
import com.artifex.mupdfdemo.PDFTextExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LocalData@Inject constructor():LocalDataSource {
     suspend fun getPageTexdt(lastPageNumber: Int,totalPages: Int,muPDFCore: MuPDFCore): ArrayList<String> {
        var pageStringData = ArrayList<String>()


        if(lastPageNumber<totalPages){
            for (i in lastPageNumber until (lastPageNumber+3)) {
//                println("Index: $i, Value: ${list[i]}")
                if(i<totalPages) {
                    val extractedText = PDFTextExtractor().extractText(muPDFCore, pageNumber = i)
                    if(extractedText!=null) {
                        pageStringData.add(extractedText)
                    }
                }else {
                    break;
                }

            }
        }

        return pageStringData

    }

    override suspend fun getPageText(lastPageNumber: Int, totalPages: Int, muPDFCore: MuPDFCore,isSinglePage:Boolean): ArrayList<String> {
        return withContext(Dispatchers.IO) {  // Switch context to IO
            val pageStringData = ArrayList<String>()
Log.d("las","lalal"+totalPages+"<>"+lastPageNumber)
//            Log.d("andi",""+String(muPDFCore.html(4), Charsets.UTF_8))
            if (isSinglePage)
            {
                val extractedText = String(muPDFCore.html(lastPageNumber), Charsets.UTF_8)
                if (extractedText != null) {
                    pageStringData.add(extractedText)
                } else {
                    pageStringData.add("")
                }
            }else {
                if (lastPageNumber < totalPages) {
                    for (i in lastPageNumber until (lastPageNumber + 3)) {
                        Log.d("looping,", "<><><>" + i)
                        if (i < totalPages) {
                            val extractedText = String(muPDFCore.html(i), Charsets.UTF_8)
                            if (extractedText != null) {
                                pageStringData.add(extractedText)
                            } else {
                                pageStringData.add("")
                            }
                        } else {
                            break
                        }
                    }
                }
            }

            pageStringData
        }
    }

}