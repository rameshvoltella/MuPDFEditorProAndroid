package com.rameshvoltella.pdfeditorpro.viewmodel

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.artifex.mupdfdemo.Annotation
import com.artifex.mupdfdemo.MuPDFView
import com.rameshvoltella.pdfeditorpro.AcceptMode
import com.rameshvoltella.pdfeditorpro.data.AnnotationOperationResult
import com.rameshvoltella.pdfeditorpro.ui.base.BaseViewModel
import com.rameshvoltella.pdfeditorpro.utils.ContextModule
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
@HiltViewModel
class PdfViewModel@Inject
constructor(
    private val contextModule: ContextModule
):BaseViewModel() {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    private val annotationResponsePrivate = MutableLiveData<AnnotationOperationResult>()
    val annotationResponse: LiveData<AnnotationOperationResult> get() = annotationResponsePrivate

    fun addAnnotation(muPDFView: MuPDFView,mAcceptMode: AcceptMode)
    {


        muPDFView?.let { pageView ->
            var success = false
            when (mAcceptMode) {
                AcceptMode.CopyText -> {
                    success = pageView.copySelection()

                }

                AcceptMode.Highlight -> {
                    success = pageView.markupSelection(Annotation.Type.HIGHLIGHT)

                }

                AcceptMode.Underline -> {
                    success = pageView.markupSelection(Annotation.Type.UNDERLINE)

                }

                AcceptMode.StrikeOut -> {
                    success = pageView.markupSelection(Annotation.Type.STRIKEOUT)

                }

                AcceptMode.Ink -> {
                    success = pageView.saveDraw()
                }

                else -> {

                }

            }
            annotationResponsePrivate.value=AnnotationOperationResult(success,mAcceptMode)

        }

    }
}