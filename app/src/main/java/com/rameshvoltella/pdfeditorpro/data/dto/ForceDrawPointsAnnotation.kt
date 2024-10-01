package com.rameshvoltella.pdfeditorpro.data.dto

import com.rameshvoltella.pdfeditorpro.database.data.QuadDrawPointsAndType
import com.rameshvoltella.pdfeditorpro.database.data.QuadPointsAndType

data class ForceDrawPointsAnnotation(val list:List<QuadDrawPointsAndType>, val pageNumber:Int)