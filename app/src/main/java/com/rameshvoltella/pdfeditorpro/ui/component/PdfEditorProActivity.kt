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
import com.rameshvoltella.pdfeditorpro.TopBarMode
import com.rameshvoltella.pdfeditorpro.ViewEditPdfActivity
import com.rameshvoltella.pdfeditorpro.constants.PdfConstants
import com.rameshvoltella.pdfeditorpro.databinding.PdfvieweditorlayoutBinding
import com.rameshvoltella.pdfeditorpro.ui.base.BaseActivity
import com.rameshvoltella.pdfeditorpro.viewmodel.PdfViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class PdfEditorProActivity:BaseActivity<PdfvieweditorlayoutBinding,PdfViewModel> (),
    OnPageChangeListener {
    private var muPDFCore: MuPDFCore? = null
    private var currentDocId: Long? = null
    var currentMode: String = PdfConstants.VERTICAL
    var currentTheme: String = PdfConstants.LIGHT
    var currentPageMode: String = PdfConstants.PAGE_MODE
    var currentBgColor = Color.WHITE

    private var mTopBarMode: TopBarMode = TopBarMode.Main
    private var mAcceptMode: AcceptMode? = null

    override fun getViewModelClass() = PdfViewModel::class.java

    override fun getViewBinding() = PdfvieweditorlayoutBinding.inflate(layoutInflater)

    override fun observeViewModel() {
    }

    override fun observeActivity() {

        if(intent.extras!!.containsKey(Constants.PDF_FILE_PATH)) {
            openPdfFile(intent.getStringExtra(Constants.PDF_FILE_PATH))
            initPdfCore()
        }else
        {
            finish()
        }

        binding.highlighterIv.setOnClickListener {

            if(binding.pdfReaderRenderView.userSelectedText)
            {
                mAcceptMode = AcceptMode.Highlight
                selectAnnotationMode()

            }else
            {
                Toast.makeText(applicationContext,"No text Selected",1).show()
            }
        }
        binding.strikethroughIv.setOnClickListener {

            if(binding.pdfReaderRenderView.userSelectedText)
            {
                mAcceptMode = AcceptMode.StrikeOut
                selectAnnotationMode()

            }else
            {
                Toast.makeText(applicationContext,"No text Selected",1).show()
            }
        }
        binding.underlineIv.setOnClickListener {

            if(binding.pdfReaderRenderView.userSelectedText)
            {
                mAcceptMode = AcceptMode.Underline
                selectAnnotationMode()

            }else
            {
                Toast.makeText(applicationContext,"No text Selected",1).show()
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
                if (ViewEditPdfActivity.currentMode == Constants.HORIZONTAL) {
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
            binding.pdfReaderRenderView?.setBackgroundColor(ViewEditPdfActivity.currentBgColor)
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

                toggleOptionState()


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

    private fun toggleOptionState(forceShow:Boolean=false) {

        if(forceShow) {
            binding.titleBar.visibility = View.VISIBLE
            binding.border.visibility = View.VISIBLE
            binding.bottomOptions.visibility = View.VISIBLE
        }else {
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
        //val pageView = muPDFReaderViewN?.displayedView as MuPDFView
        getPageViewMupdf()?.let { pageView ->
            var success = false
            when (mAcceptMode) {
                AcceptMode.CopyText -> {
                    success = pageView.copySelection()
                    // mTopBarMode = TopBarMode.Accept
//                    showInfo(
//                        if (success) getString(com.artifex.mupdfdemo.R.string.copied_to_clipboard) else getString(
//                            com.artifex.mupdfdemo.R.string.no_text_selected
//                        )
//                    )
                }

                AcceptMode.Highlight -> {
                    success = pageView.markupSelection(Annotation.Type.HIGHLIGHT)
                    //    mTopBarMode = TopBarMode.Accept
                    if (success) {


                    }
                }

                AcceptMode.Underline -> {
                    success = pageView.markupSelection(Annotation.Type.UNDERLINE)
//                    hideEditingViews()
//                    if (!success) showInfo(getString(com.artifex.mupdfdemo.R.string.no_text_selected))
                }

                AcceptMode.StrikeOut -> {
                    success = pageView.markupSelection(Annotation.Type.STRIKEOUT)
                    //   mTopBarMode = TopBarMode.Accept
//                    hideEditingViews()
//                    if (!success) showInfo(getString(com.artifex.mupdfdemo.R.string.no_text_selected))
                }

                AcceptMode.Ink -> {
                    success = pageView.saveDraw()
//                    hideEditingViews()
//                    // mTopBarMode = TopBarMode.Accept
//                    if (!success) showInfo(getString(com.artifex.mupdfdemo.R.string.nothing_to_save))
                }

                else -> {

                }
            }
        }

        binding.pdfReaderRenderView?.setMode(MuPDFReaderView.Mode.Viewing)
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
}