package com.rameshvoltella.pdfeditorpro.viewmodel

import com.rameshvoltella.pdfeditorpro.ui.base.BaseViewModel
import com.rameshvoltella.pdfeditorpro.utils.ContextModule
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
@HiltViewModel
class PdfViewModel@Inject
constructor(
    private val contextModule: ContextModule
):BaseViewModel() {
}