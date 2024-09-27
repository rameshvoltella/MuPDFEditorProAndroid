package com.rameshvoltella.pdfeditorpro.ui.component

import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.artifex.mupdfdemo.MuPDFCore
import com.artifex.mupdfdemo.OutlineActivityData
import com.rameshvoltella.pdfeditorpro.constants.Constants
import com.rameshvoltella.pdfeditorpro.databinding.PdfComfortViewActivityBinding
import com.rameshvoltella.pdfeditorpro.ui.base.BaseActivity
import com.rameshvoltella.pdfeditorpro.ui.component.adaptor.ComfortModeAdaptor
import com.rameshvoltella.pdfeditorpro.utils.observe
import com.rameshvoltella.pdfeditorpro.viewmodel.PdfViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ComfortReadingModeActivity: BaseActivity<PdfComfortViewActivityBinding, PdfViewModel>() {

    private lateinit var adapter: ComfortModeAdaptor
    private var canLoadMore = true
    private val comfortModeItemList = ArrayList<String>()
    private var muPDFCore: MuPDFCore? = null

    override fun getViewModelClass() = PdfViewModel::class.java

    override fun getViewBinding() = PdfComfortViewActivityBinding.inflate(layoutInflater)
    override fun observeViewModel() {
        observe(viewModel.comfortList,::handleComfortModeList)
    }

    private fun handleComfortModeList(comfortModeList: List<String>) {

        if(comfortModeList.isNotEmpty())
        {
            comfortModeItemList.addAll(comfortModeList)
        }
        adapter.notifyDataSetChanged()

        if (comfortModeItemList.size >= muPDFCore!!.countPages()) {
            canLoadMore = false
            Log.d("MainActivity", "Load more disabled.")
        }

    }

    override fun observeActivity() {

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ComfortModeAdaptor(comfortModeItemList)
        binding.recyclerView.adapter = adapter

        binding.activityBackBtn.setOnClickListener {
            finish()
        }


        muPDFCore=openFile(intent.getStringExtra(Constants.PDF_FILE_PATH)!!)
        if(muPDFCore!=null) {
            loadPageData()
        }
       binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1) && canLoadMore) {
                    loadPageData()

                }
            }
        })

    }

    private fun loadPageData() {
        viewModel.getComfortModeData(comfortModeItemList.size, muPDFCore!!.countPages(), muPDFCore!!)

    }

    private fun openFile(path: String): MuPDFCore? {
        val lastSlashPos = path.lastIndexOf('/')
        val fileName = if (lastSlashPos == -1) path else path.substring(lastSlashPos + 1)
        Log.e("openFile", "filename = $fileName")
        Log.e("openFile", "Trying to open $path")
        try {
            val muPDFCore = MuPDFCore(this, path)
            // New: delete old directory data
            OutlineActivityData.set(null)
            return muPDFCore
        } catch (e: Exception) {
            Log.e("openFile", "Error opening file", e)
            return null
        } catch (e: OutOfMemoryError) {
            //  out of memory is not an Exception, so we catch it separately.
            Log.e("openFile", "OutOfMemoryError", e)
            return null
        }
    }
}