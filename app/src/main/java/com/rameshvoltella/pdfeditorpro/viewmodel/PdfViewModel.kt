package com.rameshvoltella.pdfeditorpro.viewmodel

import android.content.Context
import android.graphics.PointF
import android.os.Build
import android.text.Html
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.artifex.mupdfdemo.Annotation
import com.artifex.mupdfdemo.MuPDFCore
import com.artifex.mupdfdemo.MuPDFView
import com.google.gson.Gson
import com.rameshvoltella.pdfeditorpro.AcceptMode
import com.rameshvoltella.pdfeditorpro.constants.PdfConstants
import com.rameshvoltella.pdfeditorpro.data.AnnotationOperationResult
import com.rameshvoltella.pdfeditorpro.data.database.DatabaseRepository
import com.rameshvoltella.pdfeditorpro.data.dto.TtsModel
import com.rameshvoltella.pdfeditorpro.data.local.LocalRepository
import com.rameshvoltella.pdfeditorpro.database.PdfAnnotation
import com.rameshvoltella.pdfeditorpro.database.PdfDrawAnnotation
import com.rameshvoltella.pdfeditorpro.database.data.QuadDrawPointsAndType
import com.rameshvoltella.pdfeditorpro.database.data.QuadPointsAndType
import com.rameshvoltella.pdfeditorpro.database.getQuadPoints
import com.rameshvoltella.pdfeditorpro.ui.base.BaseViewModel
import com.rameshvoltella.pdfeditorpro.utils.TextToSpeechHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.io.File
import javax.inject.Inject
@HiltViewModel
class PdfViewModel@Inject
constructor(
   private val pdfDatabaseRepository: DatabaseRepository,private val localRepository: LocalRepository
):BaseViewModel() {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    private val annotationResponsePrivate = MutableLiveData<AnnotationOperationResult>()
    val annotationResponse: LiveData<AnnotationOperationResult> get() = annotationResponsePrivate

    private val annotationInsertDeletePrivate = MutableLiveData<Boolean>()
    private val annotationPerPagePrivate = MutableLiveData<List<QuadPointsAndType>>()
    val annotationPerPage: LiveData<List<QuadPointsAndType>> get() = annotationPerPagePrivate

    val annotationInsertDelete: LiveData<Boolean> get() = annotationInsertDeletePrivate

    private val annotationDrawPerPagePrivate = MutableLiveData<List<QuadDrawPointsAndType>>()
    val annotationDrawPerPage: LiveData<List<QuadDrawPointsAndType>> get() = annotationDrawPerPagePrivate

    private val ttsOutPutPrivate = MutableLiveData<TtsModel>()
    val ttsOutPut: LiveData<TtsModel> get() = ttsOutPutPrivate


    private val comfortListPrivate = MutableLiveData<List<String>>()
    val comfortList: LiveData<List<String>> get() = comfortListPrivate

    var canLoadMore by mutableStateOf(true)

    fun getAnnotations(pdfName:String,page:Int)
    {
//        pdfDatabaseRepository.getQuadPointsAndTypeByPage(pdfName,1)

        viewModelScope.launch {

            pdfDatabaseRepository.getQuadPointsAndTypeByPage(pdfName,page).collect {
                annotationPerPagePrivate.value = it

            }
        }
    }

    fun getDrawAnnotations(pdfName:String,page:Int)
    {
//        pdfDatabaseRepository.getQuadPointsAndTypeByPage(pdfName,1)

        viewModelScope.launch {

            pdfDatabaseRepository.getDrawQuadPointsAndTypeByPage(pdfName,page).collect {
                annotationDrawPerPagePrivate.value = it

            }
        }
    }

    fun addAnnotation(muPDFView: MuPDFView?,mAcceptMode: AcceptMode?,pdfName:String)
    {

        muPDFView?.let { pageView ->
            Log.d("muPDFViewunda","<>current<>"+muPDFView?.page)

            var success = false
            var annotationPointList: ArrayList<PointF> = ArrayList()
            when (mAcceptMode) {
                AcceptMode.CopyText -> {
                    success = pageView.copySelection()
                    annotationResponsePrivate.value=AnnotationOperationResult(success,mAcceptMode)


                }

                AcceptMode.Highlight -> {
                    annotationPointList = pageView.markupSelection(Annotation.Type.HIGHLIGHT)
                    annotationResponsePrivate.value=AnnotationOperationResult(success,mAcceptMode)
//                    success = pageView.markupHardcodeSelection(Annotation.Type.HIGHLIGHT)
                    if(annotationPointList.size>0)
                    {
                        viewModelScope.launch {

                            pdfDatabaseRepository.insertAnnotation(PdfAnnotation(
                                pdfname = pdfName,
                                pagenumber = muPDFView.page,
                                quadPoints = annotationPointList,
                                type = PdfConstants.HIGHLIGHT,
                                color = 0
                            )).collect {
                                annotationInsertDeletePrivate.value = it

                            }
                        }

                    }


                }

                AcceptMode.Underline -> {
                    annotationPointList = pageView.markupSelection(Annotation.Type.UNDERLINE)
                    annotationResponsePrivate.value=AnnotationOperationResult(success,mAcceptMode)
                    if(annotationPointList.size>0)
                    {
                        viewModelScope.launch {

                            pdfDatabaseRepository.insertAnnotation(PdfAnnotation(
                                pdfname = pdfName,
                                pagenumber = muPDFView.page,
                                quadPoints = annotationPointList,
                                type = PdfConstants.UNDERLINE,
                                color = 0
                            )).collect {
                                annotationInsertDeletePrivate.value = it

                            }
                        }

                    }

                }

                AcceptMode.StrikeOut -> {
                    annotationPointList = pageView.markupSelection(Annotation.Type.STRIKEOUT)
                    annotationResponsePrivate.value=AnnotationOperationResult(success,mAcceptMode)
                    if(annotationPointList.size>0)
                    {
                        viewModelScope.launch {

                            pdfDatabaseRepository.insertAnnotation(PdfAnnotation(
                                pdfname = pdfName,
                                pagenumber = muPDFView.page,
                                quadPoints = annotationPointList,
                                type = PdfConstants.STRIKEOUT,
                                color = 0
                            )).collect {
                                annotationInsertDeletePrivate.value = it

                            }
                        }

                    }

                }

                AcceptMode.Ink -> {
                   val savedPointF:Array<Array<PointF>> = pageView.saveDraw()
                    annotationResponsePrivate.value=AnnotationOperationResult(true,mAcceptMode)

                    if(savedPointF!=null)
                    {

                        viewModelScope.launch {
                            val pdfAnnotationNew = PdfDrawAnnotation(
                                pdfname = pdfName,
                                pagenumber = muPDFView.page,
                                type = PdfConstants.INK,
                                color = 0 // Yellow color
                                , quadPoints = Gson().toJson(savedPointF)
                            )
////        this.quadPoints = gson.toJson(points)
// Set the quadPoints as JSON
//                            pdfAnnotationNew.setQuadPoints(savedPointF)
                            pdfDatabaseRepository.insertDrawAnnotation(pdfAnnotationNew

                            ).collect {
                                annotationInsertDeletePrivate.value = it

                            }
                        }
                    }
//                    annotationResponsePrivate.value=AnnotationOperationResult(success,mAcceptMode)

                }

                else -> {

                }

            }

        }

    }

    fun deleteAnnotation(muPDFView: MuPDFView?,pdfName:String)
    {
        muPDFView?.let { pageView ->
            val deleteRect=pageView.getRectToDelete()
            if(deleteRect!=null) {
                viewModelScope.launch {

                    pdfDatabaseRepository.getQuadPointsAndTypeByPageToDelete(
                        pdfname = pdfName, pagenumber = muPDFView.page, selectedRect = deleteRect!!
                    ).collect {
                        if(it)
                        {
                            pageView.clearDeleteRect();
                        }
                        annotationInsertDeletePrivate.value = it

                    }
                }
            }
            pageView.deleteSelectedAnnotation()
    }
    }

    fun addDrawAnnotationFromDatabase(muPDFView: MuPDFView?, quadPoints: Array<Array<PointF>>)
    {
        muPDFView?.let { pageView ->

            pageView.saveDrawFromDb(quadPoints)
        }

    }

    fun addAnnotationFromDatabase(muPDFView: MuPDFView?,quadPoints:QuadPointsAndType )
    {

        muPDFView?.let { pageView ->
            Log.d("muPDFViewunda","<>current<>"+muPDFView?.page)

            var success = false
            when (quadPoints.type) {


                PdfConstants.HIGHLIGHT -> {
                    success = pageView.markupFromDbSelection(Annotation.Type.HIGHLIGHT,quadPoints?.quadPoints!!)
                    annotationResponsePrivate.value=AnnotationOperationResult(success,AcceptMode.Highlight,true)
//                    success = pageView.markupHardcodeSelection(Annotation.Type.HIGHLIGHT)




                }

                PdfConstants.UNDERLINE -> {
                    success = pageView.markupFromDbSelection(Annotation.Type.UNDERLINE,quadPoints?.quadPoints)
                    annotationResponsePrivate.value=AnnotationOperationResult(success,AcceptMode.Underline,true)


                }

                PdfConstants.STRIKEOUT -> {
                    success = pageView.markupFromDbSelection(Annotation.Type.STRIKEOUT,quadPoints?.quadPoints)
                    annotationResponsePrivate.value=AnnotationOperationResult(success,AcceptMode.StrikeOut,true)


                }



                else -> {

                }

            }

        }

    }

    suspend fun setDrawingAnnotation(
        muPDFView: MuPDFView?,
        pdfDrawAnnotations: List<QuadDrawPointsAndType>
    ) {
        withContext(Dispatchers.IO)
        {
            muPDFView?.let {
                for (annotation in pdfDrawAnnotations) {
                    val quadPoints = getQuadPoints(annotation.quadPoints)

                    addDrawAnnotationFromDatabase(muPDFView, quadPoints)
                }
            }
        }
    }

    fun getComfortModeData(lastPageNumber: Int,totalPages: Int,muPDFCore: MuPDFCore)
    {
        viewModelScope.launch {

            localRepository.getPageText(lastPageNumber,totalPages,muPDFCore
            ).collect {
//                annotationInsertDeletePrivate.value = it
                comfortListPrivate.value = it

            }
        }
    }

    fun readThePageOrLine(context: Context, textToConvert: String?=null,muPDFCore: MuPDFCore?=null,pageNumber: Int=-1)
    {
        var textToSpeech=textToConvert
        if(muPDFCore!=null)
        {
            viewModelScope.launch {

                localRepository.getPageText(pageNumber,pageNumber,muPDFCore, isSinglePage = true
                ).collect {
//                annotationInsertDeletePrivate.value = it
                    if(it.size>0) {
                        val document = Jsoup.parse(it.get(0))
                        textToSpeech = document.text()
                        ttsConversion(context,textToSpeech)

                    }
                }
            }



        }else {
            if (textToSpeech != null) {
                Log.d("SADKKE","YOOOOOgoinggggggg");

                ttsConversion(context,textToSpeech)
            } else {
                ttsOutPutPrivate.value = TtsModel(null, false)

            }
        }
    }

    private fun ttsConversion(context: Context,textToSpeech:String?) {
        Log.d("SADKKE","YOOOOOfirst");

        val outputDir = context.cacheDir // You can specify another directory if needed
        val outputFile = File(outputDir, "tts_output.wav")

        val ttsHelper = TextToSpeechHelper(context)
        ttsHelper.convertTextToSpeech(textToSpeech!!, outputFile) { success ->
            if (success) {
                Log.d("SADKKE","YOOOOO");
                // Audio file is ready, play it
                ttsOutPutPrivate.value = TtsModel(outputFile, true)
            } else {
                Log.d("SADKKE","YOOOOOFFFFF");

                // Handle failure
                ttsOutPutPrivate.value = TtsModel(null, false)

            }
        }
    }

    fun CoroutineScope.readThePagdeOrLine(
       context: Context,
       textToConvert: String? = null,
       muPDFCore: MuPDFCore? = null,
       pageNumber: Int = -1
   ) {
       // Launch the coroutine
       launch {
           var textToSpeech = textToConvert

           // Perform I/O operations like extracting text in Dispatchers.IO
           withContext(Dispatchers.IO) {
               if (muPDFCore != null) {
                   val extractedText = String(muPDFCore.html(pageNumber), Charsets.UTF_8)
                   val document = Jsoup.parse(extractedText)
                   textToSpeech = document.text()
               }
           }

           // Continue with the Text-to-Speech conversion
           if (textToSpeech != null) {
               // Switching back to I/O context to handle file operations
               withContext(Dispatchers.IO) {
                   val outputDir = context.cacheDir // You can specify another directory if needed
                   val outputFile = File(outputDir, "tts_output.wav")

                   val ttsHelper = TextToSpeechHelper(context)

                   // Text-to-Speech conversion (could happen on any thread, not necessarily IO)
                   ttsHelper.convertTextToSpeech(textToSpeech!!, outputFile) { success ->
                       // Switch to the Main thread to update the UI or LiveData
                       launch(Dispatchers.Main) {
                           if (success) {
                               // Audio file is ready, handle playback
                               ttsOutPutPrivate.value = TtsModel(outputFile, true)
                           } else {
                               // Handle failure
                               ttsOutPutPrivate.value = TtsModel(null, false)
                           }
                       }
                   }
               }
           } else {
               // Handle when textToSpeech is null (must happen on the main thread)
               withContext(Dispatchers.Main) {
                   ttsOutPutPrivate.value = TtsModel(null, false)
               }
           }
       }
   }
}