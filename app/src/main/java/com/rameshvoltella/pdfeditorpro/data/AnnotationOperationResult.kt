package com.rameshvoltella.pdfeditorpro.data

import com.rameshvoltella.pdfeditorpro.AcceptMode

data class AnnotationOperationResult (val status:Boolean,val acceptMode: AcceptMode,val isFromDb:Boolean=false)