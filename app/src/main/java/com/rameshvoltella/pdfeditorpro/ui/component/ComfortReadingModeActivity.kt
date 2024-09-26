package com.rameshvoltella.pdfeditorpro.ui.component

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rameshvoltella.pdfeditorpro.databinding.PdfComfortViewActivityBinding
import com.rameshvoltella.pdfeditorpro.databinding.PdfViewProEditorLayoutBinding
import com.rameshvoltella.pdfeditorpro.ui.base.BaseActivity
import com.rameshvoltella.pdfeditorpro.viewmodel.PdfViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ComfortReadingModeActivity: BaseActivity<PdfComfortViewActivityBinding, PdfViewModel>() {
    override fun getViewModelClass() = PdfViewModel::class.java

    override fun getViewBinding() = PdfComfortViewActivityBinding.inflate(layoutInflater)
    override fun observeViewModel() {
    }

    override fun observeActivity() {
    }
}