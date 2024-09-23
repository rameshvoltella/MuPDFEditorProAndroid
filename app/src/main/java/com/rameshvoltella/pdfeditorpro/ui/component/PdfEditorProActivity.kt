package com.rameshvoltella.pdfeditorpro.ui.component

import android.content.ClipboardManager
import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.Toast
import com.artifex.mupdfdemo.Annotation
import com.artifex.mupdfdemo.Hit
import com.artifex.mupdfdemo.MuPDFCore
import com.artifex.mupdfdemo.MuPDFPageAdapter
import com.artifex.mupdfdemo.MuPDFReaderView
import com.artifex.mupdfdemo.MuPDFReaderViewListener
import com.artifex.mupdfdemo.MuPDFView
import com.artifex.mupdfdemo.OnPageChangeListener
import com.artifex.mupdfdemo.OutlineActivityData
import com.rameshvoltella.pdfeditorpro.AcceptMode
import com.rameshvoltella.pdfeditorpro.constants.Constants
import com.rameshvoltella.pdfeditorpro.ViewEditPdfActivity
import com.rameshvoltella.pdfeditorpro.constants.PdfConstants
import com.rameshvoltella.pdfeditorpro.data.AnnotationOperationResult
import com.rameshvoltella.pdfeditorpro.database.data.QuadPointsAndType
import com.rameshvoltella.pdfeditorpro.databinding.PdfViewProEditorLayoutBinding
import com.rameshvoltella.pdfeditorpro.ui.base.BaseActivity
import com.rameshvoltella.pdfeditorpro.utils.observe
import com.rameshvoltella.pdfeditorpro.viewmodel.PdfViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class PdfEditorProActivity : BaseActivity<PdfViewProEditorLayoutBinding, PdfViewModel>(),
    OnPageChangeListener {
    private var muPDFCore: MuPDFCore? = null
    private var currentDocId: Long? = null
    var currentMode: String = PdfConstants.VERTICAL
    var currentTheme: String = PdfConstants.LIGHT
    var currentPageMode: String = PdfConstants.PAGE_MODE
    var currentBgColor = Color.WHITE

    private var mAcceptMode: AcceptMode? = null

    private var addedAnnotationPages = ArrayList<Int>()

    override fun getViewModelClass() = PdfViewModel::class.java

    override fun getViewBinding() = PdfViewProEditorLayoutBinding.inflate(layoutInflater)

    override fun observeViewModel() {
        observe(viewModel.annotationResponse, ::handleAddCommentResponse)
        observe(viewModel.annotationPerPage, ::handleAnnotationPerPage)

    }


    override fun observeActivity() {

        if (intent.extras!!.containsKey(Constants.PDF_FILE_PATH)) {
            openPdfFile(intent.getStringExtra(Constants.PDF_FILE_PATH))
            initPdfCore()
        } else {
            finish()
        }

        binding.highlighterIv.setOnClickListener {

            if (binding.pdfReaderRenderView.userSelectedText) {
                mAcceptMode = AcceptMode.Highlight
                selectAnnotationMode()

            } else {
                Toast.makeText(applicationContext, "No text Selected", 1).show()
            }
        }
        binding.strikethroughIv.setOnClickListener {

            if (binding.pdfReaderRenderView.userSelectedText) {
                mAcceptMode = AcceptMode.StrikeOut
                selectAnnotationMode()

            } else {
                Toast.makeText(applicationContext, "No text Selected", 1).show()
            }
        }
        binding.underlineIv.setOnClickListener {

            if (binding.pdfReaderRenderView.userSelectedText) {
                mAcceptMode = AcceptMode.Underline
                selectAnnotationMode()

            } else {
                Toast.makeText(applicationContext, "No text Selected", 1).show()
            }
        }

        binding.acceptBtn.setOnClickListener {
            mAcceptMode = AcceptMode.Ink
            selectAnnotationMode()

        }


        binding.drawerIv.setOnClickListener {

            binding.basicLl.visibility = View.GONE;
            binding.acceptModeLl.visibility = View.VISIBLE
            binding.bottomOptions.visibility = View.GONE
            drawTextOnPage()

        }

        binding.activityBackBtn.setOnClickListener {

            if (binding.basicLl.visibility == View.VISIBLE) {
                finish()

            } else {
                cancellAllEdit()
            }
        }

    }


    private var mPageSliderRes = 0
    private fun initPdfCore() {
        muPDFCore?.let { muPDFCore ->
            if (muPDFCore.countPages() > 0) {
//                sideSeekHandling()

            }
            // Set up the page slider
            val smax = Math.max(muPDFCore.countPages() - 1, 1)
            mPageSliderRes = (10 + smax - 1) / smax * 2
            Log.d("PROGRESSSEEK", "sliderInitial:$mPageSliderRes")
            // Update page number text and slider
            binding.pdfReaderRenderView?.let { readerView ->
                if (currentMode == Constants.HORIZONTAL) {
                    readerView.setHorizontalScrolling(true)
                } else {
                    readerView.setHorizontalScrolling(false)
                }
                val index: Int = readerView.displayedViewIndex
//                updatePageNumView(index * mPageSliderRes)


            }

            setListenerToReaderView()
            //last commented code will be here
            binding.pdfReaderRenderView?.adapter = MuPDFPageAdapter(this, muPDFCore, this)
            //Set the background color of the view
            binding.pdfReaderRenderView?.setBackgroundColor(currentBgColor)
//                ContextCompat.getColor(this, R.color.muPDFReaderView_bg)
//            )
        }?.run {
            return
        }
        //for deselection
        //  onDeleteButtonClick()
    }


    private fun setListenerToReaderView() {


        binding.pdfReaderRenderView?.setListener(object : MuPDFReaderViewListener {
            override fun onMoveToChild(i: Int) {
                if (muPDFCore == null) return

            }

            override fun onTapMainDocArea() {
                Log.d("DELETEANNOT", "onHit:  ")
                if (mAcceptMode != AcceptMode.Ink) {
                    toggleOptionState()
                }


            }

            override fun onDocMotion() {
                Log.e("DELETEANNOT", "onHit:  ")

            }

            override fun onHit(item: Hit) {

            }

            override fun onLongPress() {
                Log.i("LONGPRESS", "onLongPress: hitting in Activity")
                toggleOptionState(true)


            }

        })

    }

    private fun toggleOptionState(forceShow: Boolean = false) {
        if (binding.basicLl.visibility == View.GONE) {

            binding.basicLl.visibility = View.VISIBLE;
            binding.acceptModeLl.visibility = View.GONE
        }

        if (forceShow) {
            binding.titleBar.visibility = View.VISIBLE
            binding.border.visibility = View.VISIBLE
            binding.bottomOptions.visibility = View.VISIBLE
        } else {
            if (binding.titleBar.visibility == View.VISIBLE) {
                binding.titleBar.visibility = View.GONE
                binding.border.visibility = View.GONE
                binding.bottomOptions.visibility = View.GONE
            } else {
                binding.titleBar.visibility = View.VISIBLE
                binding.border.visibility = View.VISIBLE
                binding.bottomOptions.visibility = View.VISIBLE


            }
        }
    }

    override fun onPageChanged(page: Int) {
//        onPageChanged
        Log.e("PageIs", "CurrentPageIS:${getPageViewMupdf()?.page}"+addedAnnotationPages)

        if (getPageViewMupdf() != null) {
            if (!addedAnnotationPages.contains(getPageViewMupdf()?.page)) {
                Log.d("Pageis","CurrentPageIS goint :${getPageViewMupdf()?.page}")
                viewModel.getAnnotations("test.pdf", getPageViewMupdf()?.page!!)
            }
        }

    }

    override fun onPageChanged(page: Int, currentPageView: View?) {
    }

    private fun openPdfFile(pathGet: String?) {
        pathGet?.let { path ->
            Log.i("FILEPATH", "getIntentDataOpenPdf:$path")
            displayPdf(path)

        }
    }

    private fun displayPdf(path: String) {
        val file = File(path)
        if (file.exists()) {
            muPDFCore = openFile(path)
        }


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

    private fun selectAnnotationMode() {
        viewModel.addAnnotation(getPageViewMupdf(), mAcceptMode, "test.pdf")

    }

    private fun getPageViewMupdf(): MuPDFView? {
        return try {
            binding.pdfReaderRenderView?.let {
                val pageView = it.displayedView
                if (pageView != null && pageView is MuPDFView) {
                    return pageView
                }
            }
            null
        } catch (e: Exception) {
            // Handle any other exceptions that may occur
            Log.i("error", "getPageViewMupdf:${e.message.toString()} ")
            null
        }

    }

    private fun drawTextOnPage() {
        mAcceptMode = AcceptMode.Ink
        binding.pdfReaderRenderView?.setMode(MuPDFReaderView.Mode.Drawing)


    }

    private fun cancellAllEdit() {
        // val pageView = muPDFReaderViewN?.displayedView as MuPDFView
        getPageViewMupdf()?.let { pageView ->
            if (pageView != null) {
                pageView.deselectText()
                pageView.cancelDraw()
            }
        }
        binding.pdfReaderRenderView?.setMode(MuPDFReaderView.Mode.Viewing)
        toggleOptionState(true)
    }

    private fun handleAnnotationPerPage(quadPointsAndTypes: List<QuadPointsAndType>) {

        Log.d("Pageis","CurrentPageIS goint :${getPageViewMupdf()?.page}"+"<>"+quadPointsAndTypes.size)

        for (index in quadPointsAndTypes) {
            Log.d("dbdata", "" + index.type + "<>" + index.quadPoints);
        }
        if (getPageViewMupdf() != null) {
            addedAnnotationPages.add(getPageViewMupdf()?.page!!)
        }

    }

    fun handleAddCommentResponse(annotationOperationResult: AnnotationOperationResult) {

        if (annotationOperationResult.status) {
            Log.d("TAG", "handleAddCommentResponse:")
        }
        if (annotationOperationResult.acceptMode == AcceptMode.Ink) {
            mAcceptMode = AcceptMode.None;
            toggleOptionState(true)
        }
        binding.pdfReaderRenderView?.setMode(MuPDFReaderView.Mode.Viewing)
    }
}


