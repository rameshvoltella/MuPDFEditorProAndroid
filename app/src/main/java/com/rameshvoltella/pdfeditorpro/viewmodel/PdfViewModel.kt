package com.rameshvoltella.pdfeditorpro.viewmodel

import android.graphics.PointF
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.artifex.mupdfdemo.Annotation
import com.artifex.mupdfdemo.MuPDFView
import com.rameshvoltella.pdfeditorpro.AcceptMode
import com.rameshvoltella.pdfeditorpro.constants.PdfConstants
import com.rameshvoltella.pdfeditorpro.data.AnnotationOperationResult
import com.rameshvoltella.pdfeditorpro.data.database.DatabaseData
import com.rameshvoltella.pdfeditorpro.data.database.DatabaseRepository
import com.rameshvoltella.pdfeditorpro.database.PdfAnnotation
import com.rameshvoltella.pdfeditorpro.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class PdfViewModel@Inject
constructor(
   private val pdfDatabaseRepository: DatabaseRepository
):BaseViewModel() {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    private val annotationResponsePrivate = MutableLiveData<AnnotationOperationResult>()
    val annotationResponse: LiveData<AnnotationOperationResult> get() = annotationResponsePrivate

    private val annotationInsertPrivate = MutableLiveData<Boolean>()
    val annotationInsert: LiveData<Boolean> get() = annotationInsertPrivate

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
//                    annotationResponsePrivate.value=AnnotationOperationResult(success,mAcceptMode)
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
                                annotationInsertPrivate.value = it

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
                                annotationInsertPrivate.value = it

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
                                annotationInsertPrivate.value = it

                            }
                        }

                    }

                }

                AcceptMode.Ink -> {
                    success = pageView.saveDraw()
                    annotationResponsePrivate.value=AnnotationOperationResult(success,mAcceptMode)

                }

                else -> {

                }

            }

        }

    }
}