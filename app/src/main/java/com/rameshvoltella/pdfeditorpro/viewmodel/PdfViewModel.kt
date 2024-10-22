package com.rameshvoltella.pdfeditorpro.viewmodel

import android.content.Context
import android.graphics.PointF
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
import com.rameshvoltella.pdfeditorpro.data.dto.ForceDrawPointsAnnotation
import com.rameshvoltella.pdfeditorpro.data.dto.ForceQuadPointsAnnotation
import com.rameshvoltella.pdfeditorpro.data.dto.TtsModel
import com.rameshvoltella.pdfeditorpro.data.local.LocalRepository
import com.rameshvoltella.pdfeditorpro.database.PdfAnnotation
import com.rameshvoltella.pdfeditorpro.database.PdfDrawAnnotation
import com.rameshvoltella.pdfeditorpro.database.data.QuadDrawPointsAndType
import com.rameshvoltella.pdfeditorpro.database.data.QuadPointsAndType
import com.rameshvoltella.pdfeditorpro.database.getQuadPoints
import com.rameshvoltella.pdfeditorpro.ui.base.BaseViewModel
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

    private val annotationForcePerPagePrivate = MutableLiveData<ForceQuadPointsAnnotation>()
    val annotationForcePerPage: LiveData<ForceQuadPointsAnnotation> get() = annotationForcePerPagePrivate

    private val drawForcePerPagePrivate = MutableLiveData<ForceDrawPointsAnnotation>()
    val drawForcePerPage: LiveData<ForceDrawPointsAnnotation> get() = drawForcePerPagePrivate



    val annotationInsertDelete: LiveData<Boolean> get() = annotationInsertDeletePrivate

    private val annotationDrawPerPagePrivate = MutableLiveData<List<QuadDrawPointsAndType>>()
    val annotationDrawPerPage: LiveData<List<QuadDrawPointsAndType>> get() = annotationDrawPerPagePrivate

    private val ttsOutPutPrivate = MutableLiveData<TtsModel>()
    val ttsOutPut: LiveData<TtsModel> get() = ttsOutPutPrivate


    private val comfortListPrivate = MutableLiveData<List<String>>()
    val comfortList: LiveData<List<String>> get() = comfortListPrivate

    var canLoadMore by mutableStateOf(true)

    fun getAnnotations(pdfName:String,page:Int,isForceView: Boolean=false)
    {
//        pdfDatabaseRepository.getQuadPointsAndTypeByPage(pdfName,1)

        viewModelScope.launch {

            pdfDatabaseRepository.getQuadPointsAndTypeByPage(pdfName,page).collect {
                if(isForceView)
                {
                    annotationForcePerPagePrivate.value=ForceQuadPointsAnnotation(it,page)
                }else {
                    annotationPerPagePrivate.value = it
                }

            }
        }
    }

    fun getDrawAnnotations(pdfName:String,page:Int,isForceView: Boolean=false)
    {
//        pdfDatabaseRepository.getQuadPointsAndTypeByPage(pdfName,1)

        viewModelScope.launch {

            pdfDatabaseRepository.getDrawQuadPointsAndTypeByPage(pdfName,page).collect {

               if(isForceView)
               {
                   drawForcePerPagePrivate.value=ForceDrawPointsAnnotation(it,page)
               }else {
                   annotationDrawPerPagePrivate.value = it
               }

            }
        }
    }

    fun addAnnotation(muPDFView: MuPDFView?,mAcceptMode: AcceptMode?,pdfName:String)
    {
        Log.d("muPDFViewunda","<>current<>beginninganno")


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
        Log.d("muPDFViewunda","<>current<>beginning")

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

            localRepository.getPageText(lastPageNumber,totalPages,muPDFCore,false
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
            Log.d("SAKKKi","11111>>>")
            Log.d("SAKKKi","11111>>>pageNumber"+pageNumber)

            viewModelScope.launch {

                localRepository.getPageText(pageNumber,pageNumber,muPDFCore, isSinglePage = true
                ).collect {
//                annotationInsertDeletePrivate.value = it

                    if(it.size>0) {
                        val document = Jsoup.parse(it.get(0))
                        textToSpeech = document.text()

                        ttsOutPutPrivate.value = TtsModel(textToSpeech, true)

//                        ttsConversion(context,textToSpeech)

                    }
                }
            }



        }else {
            if (textToSpeech != null) {
                Log.d("SADKKE","YOOOOOgoinggggggg");

                ttsOutPutPrivate.value = TtsModel(textToSpeech, true)

            } else {
                ttsOutPutPrivate.value = TtsModel(null, false)

            }
        }
    }



}