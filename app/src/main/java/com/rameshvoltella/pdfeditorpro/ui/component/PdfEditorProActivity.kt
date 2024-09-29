package com.rameshvoltella.pdfeditorpro.ui.component

import android.content.Intent
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.MediaItem
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.SeekBar
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.Player
import com.rameshvoltella.pdfeditorpro.tts.TextToSpeechHelper
import com.artifex.mupdfdemo.Hit
import com.artifex.mupdfdemo.MuPDFCore
import com.artifex.mupdfdemo.MuPDFPageAdapter
import com.artifex.mupdfdemo.MuPDFReaderView
import com.artifex.mupdfdemo.MuPDFReaderViewListener
import com.artifex.mupdfdemo.MuPDFView
import com.artifex.mupdfdemo.PageActionListener
import com.artifex.mupdfdemo.OutlineActivityData
import com.artifex.mupdfdemo.ReaderView
import com.artifex.mupdfdemo.SearchTaskResult
import com.artifex.mupdfdemo.util.SearchTask
import com.google.android.material.snackbar.Snackbar
import com.rameshvoltella.pdfeditorpro.AcceptMode
import com.rameshvoltella.pdfeditorpro.R
import com.rameshvoltella.pdfeditorpro.constants.Constants
import com.rameshvoltella.pdfeditorpro.constants.PdfConstants
import com.rameshvoltella.pdfeditorpro.data.AnnotationOperationResult
import com.rameshvoltella.pdfeditorpro.data.dto.TtsModel
import com.rameshvoltella.pdfeditorpro.database.data.QuadDrawPointsAndType
import com.rameshvoltella.pdfeditorpro.database.data.QuadPointsAndType
import com.rameshvoltella.pdfeditorpro.database.getQuadPoints
import com.rameshvoltella.pdfeditorpro.databinding.PdfViewProEditorLayoutBinding
import com.rameshvoltella.pdfeditorpro.setOnSingleClickListener
import com.rameshvoltella.pdfeditorpro.ui.base.BaseActivity
import com.rameshvoltella.pdfeditorpro.utils.hideKeyboardFromView
import com.rameshvoltella.pdfeditorpro.utils.observe
import com.rameshvoltella.pdfeditorpro.utils.showKeyboardFromView
import com.rameshvoltella.pdfeditorpro.viewmodel.PdfViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class PdfEditorProActivity : BaseActivity<PdfViewProEditorLayoutBinding, PdfViewModel>(),
    PageActionListener {
    private var muPDFCore: MuPDFCore? = null
    private var currentDocId: Long? = null
    var currentMode: String = PdfConstants.VERTICAL
    var currentTheme: String = PdfConstants.LIGHT
    var currentPageMode: String = PdfConstants.PAGE_MODE
    var currentBgColor = Color.WHITE
    private var mSearchTask: SearchTask? = null // Coroutine-based search task
    var resultt: SearchTaskResult?= null
    var oldScrollPosition=0;
    private var handler: Handler? = null
    private var updateSeekBarRunnable: Runnable? = null
    private var mAcceptMode: AcceptMode? = null

    private var addedAnnotationPages = ArrayList<Int>()
    private lateinit var exoPlayer: ExoPlayer
    private var isPlaying = false
    override fun getViewModelClass() = PdfViewModel::class.java

    override fun getViewBinding() = PdfViewProEditorLayoutBinding.inflate(layoutInflater)

    override fun observeViewModel() {
        observe(viewModel.annotationResponse, ::handleAddCommentResponse)
        observe(viewModel.annotationPerPage, ::handleAnnotationPerPage)
        observe(viewModel.annotationDrawPerPage,::handleDrawAnnotationPerPage)
        observe(viewModel.annotationInsertDelete,::handleOnInsertionDeletion)
        observe(viewModel.ttsOutPut,::handleTTSData)

    }

    private fun handleTTSData(ttsModel: TtsModel) {
if(ttsModel.status&&ttsModel.outPutString!=null) {
    Toast.makeText(applicationContext,"Yoooooo"+ttsModel.outPutString,1).show()

    TextToSpeechHelper
        .getInstance(this)
        .registerLifecycle( this as LifecycleOwner)
        .saveAsAudio(ttsModel.outPutString)
        .onDone {
            Log.d("poda", "speak: done")
            runOnUiThread{
                playAudioFile()
            }


//                    Toast.makeText(applicationContext,"OVERR",Toast.LENGTH_SHORT).show()

        }
        .onError {
            Log.d("poda", "speak: error")

//                    Toast.makeText(applicationContext,"Playback doesn't support in this device.",Toast.LENGTH_SHORT).show()

        }
}else
{
    Toast.makeText(applicationContext,"TTSFAILED",1).show()
}

    }


    override fun observeActivity() {

        if (intent.extras!!.containsKey(Constants.PDF_FILE_PATH)) {
            openPdfFile(intent.getStringExtra(Constants.PDF_FILE_PATH))
            initPdfCore()
            settingClicksToSearch()
            if(muPDFCore!=null) {
                binding.movableView.progressMax =
                    muPDFCore!!.countPages() // Set maximum progress to 200

                binding.movableView.onProgressChanged = { progress ->
                    // Handle the progress change
                    Log.d("Progress", "Progress: $progress%")
                    if(oldScrollPosition!=progress)
                    {
                        oldScrollPosition=progress;
                        binding.pdfReaderRenderView.setDisplayedViewIndex(progress - 1)

                    }
                }

                binding.movableView.onTopReached={
//                    Toast.makeText(applicationContext,"TOP RECHED",1).show()
                    binding.pdfReaderRenderView.setDisplayedViewIndex(0)
//                    binding.movableView.setProgress(0)

                }
                binding.movableView.onBottomReached={
//                    Toast.makeText(applicationContext,"TOP RECHED",1).show()
//                    if(muPDFCore!=null) {
                        binding.pdfReaderRenderView.setDisplayedViewIndex(binding.pdfReaderRenderView.adaptorCount)
//                    }
//                    binding.movableView.setProgress(0)

                }


            }

            exoPlayer = ExoPlayer.Builder(applicationContext).build()
            // Play/Pause Button Click Listener
            binding.playerbase.playerControlButton.setOnClickListener {
                if (isPlaying) {
                    exoPlayer.pause()
                    binding.playerbase.playerControlButton.setImageResource(R.drawable.plus_ic)
                    stopSeekBarUpdate()
                } else {
                    exoPlayer.play()
                    binding.playerbase.playerControlButton.setImageResource(R.drawable.minus_ic)
                    startSeekBarUpdate() // Start seek bar update only when playback starts
                }
                isPlaying = !isPlaying
            }
            // Make sure to release the player when playback finishes
            exoPlayer.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        binding.playerbase.playerProgressBar.max = exoPlayer.duration.toInt()
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (isPlaying) {
                        binding.playerbase.playerControlButton.setImageResource(R.drawable.minus_ic)
                        startSeekBarUpdate() // Ensure the handler runs when playback starts
                    } else {
                        binding.playerbase.playerControlButton.setImageResource(R.drawable.plus_ic)
                        stopSeekBarUpdate()
                    }
                }
            })

            // SeekBar Change Listener
            binding.playerbase.playerProgressBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        exoPlayer.seekTo(progress.toLong())
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        } else {
            finish()
        }

        binding.bookmarkBtn.setOnClickListener {
//            viewModel.deleteAnnotation(getPageViewMupdf(),"test.pdf")
//            viewModel.getComfortModeData(0,binding.pdfReaderRenderView.adaptorCount,muPDFCore!!)
        }

        binding.comfortBtn.setOnClickListener {
            startActivity(Intent(applicationContext,ComfortReadingModeActivity::class.java).putExtra(Constants.PDF_FILE_PATH,intent.getStringExtra(Constants.PDF_FILE_PATH)))
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
        binding.listenIv.setOnClickListener {

            if (binding.pdfReaderRenderView.userSelectedText) {
                Toast.makeText(applicationContext, binding.pdfReaderRenderView.selectedText+"<><", 1).show()

                viewModel.readThePageOrLine(applicationContext,binding.pdfReaderRenderView.selectedText)

            } else {
                viewModel.readThePageOrLine(applicationContext, muPDFCore = muPDFCore, pageNumber = binding.pdfReaderRenderView.displayedViewIndex)
            }

            binding.pdfReaderRenderView?.setMode(MuPDFReaderView.Mode.Viewing)


        }

        binding.acceptBtn.setOnClickListener {

            mAcceptMode = AcceptMode.Ink
            selectAnnotationMode()

        }

     /*   binding.bookmarkBtn.setOnClickListener {

            // Inside a CoroutineScope, like in a ViewModel or Activity/Fragment
            lifecycleScope.launch {
                val extractedText = PDFTextExtractor().extractText(muPDFCore,  pageNumber = 1)
                Log.d("Extracted Text", extractedText ?: "No text extracted")
            }
        }*/

        binding.searchAction.searchClose.setOnClickListener {
            toggleOptionState(true)
            searchModeOff()
        }

        binding.searchBtn.setOnClickListener {

            if(binding.searchAction.searchViewCl.visibility==View.GONE)
            {

                searchModeOn()
                searchTaskClicks()

            }

        }


        binding.drawerIv.setOnClickListener {

            binding.basicLl.visibility = View.GONE;
            binding.acceptModeLl.visibility = View.VISIBLE
            binding.bottomOptions.visibility = View.GONE
            if(binding.searchAction.searchViewCl.visibility==View.VISIBLE) {
                searchModeOff()
            }
            drawTextOnPage()

        }

        binding.activityBackBtn.setOnClickListener {

            if (binding.basicLl.visibility == View.VISIBLE) {
                finish()

            }
            else if (binding.searchAction.searchViewCl.visibility == View.VISIBLE) {
                binding.searchAction.searchViewCl.visibility=View.GONE
                searchModeOff()
                toggleOptionState(true)
            }
            else {
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
                if(item==Hit.Annotation) {

//                    Toast.makeText(applicationContext, "Clickedon Annotaion", 1).show()
                }

            }

            override fun onLongPress() {
                Log.i("LONGPRESS", "onLongPress: hitting in Activity")
                if(binding.searchAction.searchViewCl.visibility==View.VISIBLE) {
                    searchModeOff()
                }
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
            binding.movableView.setProgress(page)

            if (!addedAnnotationPages.contains(getPageViewMupdf()?.page)) {
                Log.d("Pageis","CurrentPageIS goint :${getPageViewMupdf()?.page}")
                viewModel.getAnnotations("test.pdf", getPageViewMupdf()?.page!!)
                viewModel.getDrawAnnotations("test.pdf", getPageViewMupdf()?.page!!)

            }
        }

    }

    override fun onPageChanged(page: Int, currentPageView: View?) {
    }

    override fun onDeleteSelectedAnnotation() {
        viewModel.deleteAnnotation(getPageViewMupdf(),"test.pdf")

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

    private fun handleOnInsertionDeletion(status: Boolean) {


    }
    private fun handleDrawAnnotationPerPage(pdfDrawAnnotations: List<QuadDrawPointsAndType>) {
        for (annotation in pdfDrawAnnotations) {
            val quadPoints = getQuadPoints(annotation.quadPoints)

            viewModel.addDrawAnnotationFromDatabase(getPageViewMupdf(),quadPoints)
//            viewModel.setDrawingAnnotation(pdfDrawAnnotations)
        }
//        lifecycleScope.launch {
//            viewModel.setDrawingAnnotation(getPageViewMupdf(), pdfDrawAnnotations)
//        }

    }
    private fun handleAnnotationPerPage(quadPointsAndTypes: List<QuadPointsAndType>) {

        Log.d("Pageis","CurrentPageIS goint :${getPageViewMupdf()?.page}"+"<>"+quadPointsAndTypes.size)

        for (pointsData in quadPointsAndTypes) {
//            Log.d("dbdata", "" + index.type + "<>" + index.quadPoints);
            viewModel.addAnnotationFromDatabase( getPageViewMupdf(),pointsData)

        }
        if (getPageViewMupdf() != null) {
            addedAnnotationPages.add(getPageViewMupdf()?.page!!)
        }


    }

    fun handleAddCommentResponse(annotationOperationResult: AnnotationOperationResult) {

        if (annotationOperationResult.status) {
            Log.d("TAG", "handleAddCommentResponse:")
        }
        if(!annotationOperationResult.isFromDb) {
            if (annotationOperationResult.acceptMode == AcceptMode.Ink) {
                mAcceptMode = AcceptMode.None;
                toggleOptionState(true)
            }
        }
        binding.pdfReaderRenderView?.setMode(MuPDFReaderView.Mode.Viewing)
    }


    private fun searchTaskClicks() {
        // Create a coroutine-based SearchTask
        mSearchTask = object : SearchTask(this, muPDFCore) {
            override fun onTextFound(result: SearchTaskResult) {
                // Handle the search result when text is found
                resultt = result
                SearchTaskResult.set(result)

                Log.i("TAG", "TotalWordCount: ${result.txt.count()}")
                // Move to the resulting page
                binding.pdfReaderRenderView?.displayedViewIndex = result.pageNumber
                Log.e("TAG", "onTextFound: ${result.pageNumber}")
                // Reset the ReaderView to reflect changes in SearchTaskResult
                binding.pdfReaderRenderView?.resetupChildren()
            }

            override fun onTextNotFound(result: String) {
                // Handle the case where text is not found
                Snackbar.make(binding.border,"No text found", Snackbar.LENGTH_LONG).show()
            }
        }

        // Start the search task with the search term and direction


        // Disable search buttons while search is in progress

    }
    private fun search(direction: Int) {
        hideKeyboardFromView(applicationContext,binding.searchAction.searchText)

//        hideKeyboard()

//        val searchText = "Your search query"
//        val currentDisplayPage = binding.pdfReaderRenderView?.displayedViewIndex ?: 0

   /*     mSearchTask?.go(
            text = searchText,
            direction = 1,  // Forward direction, can be -1 for backward
            displayPage = currentDisplayPage,
            searchPage = -1 // You can customize this if needed
        )*/
        val displayPage = binding.pdfReaderRenderView?.displayedViewIndex
        val r = SearchTaskResult.get()
        val searchPage = r?.pageNumber ?: -1
        displayPage?.let { page ->
            mSearchTask?.go(
                binding.searchAction.searchText.text.toString(), direction, page, searchPage
            )
            Log.i("TAG", "search: $page")

        }
    }

    private fun settingClicksToSearch() {
        var haveText = false
        binding.searchAction.searchText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                haveText = s.toString().isNotEmpty()

                //Remove any previous search results
                if (SearchTaskResult.get() != null && binding.searchAction.searchText.text.toString() != SearchTaskResult.get().txt) {
                    SearchTaskResult.set(null)
                    binding.pdfReaderRenderView?.resetupChildren()
                }
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int, count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int, before: Int, count: Int
            ) {
            }
        })
        //React to Done button on keyboard
        binding.searchAction.searchText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE && haveText) search(1)

            false
        }
//        binding.searchText.setOnEditorActionListener { textView, actionId, keyEvent ->
//            if(actionId == EditorInfo.IME_ACTION_NEXT && haveText) search(1)
//            false
//        }
        binding.searchAction.searchText.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) search(
                1
            )
            false
        }
        // Activate search invoking buttons
        binding.searchAction.searchBack.setOnSingleClickListener({
            try {
                search(-1)
            } catch (ex: Exception) {
                // Log.e(TAG, "settingClickToSearch: $ex")
            }
        })
        binding.searchAction.searchForward.setOnSingleClickListener({
            try {
                search(1)
            } catch (ex: Exception) {

            }

        })


    }

    private fun searchModeOff() {
        hideKeyboardFromView(applicationContext,binding.searchAction.searchText)
        binding.searchAction.searchText.text = null
        binding.searchAction.searchText.hint = "Enter Text"
        SearchTaskResult.set(null)
        binding.searchAction.searchViewCl.visibility=View.GONE
        // Make the ReaderView act on the change to mSearchTaskResult
        // via overridden onChildSetup method.
        binding.pdfReaderRenderView?.resetupChildren()
    }

    private fun searchModeOn() {

            //Focus on EditTextWidget
        binding.searchAction.searchViewCl.visibility=View.VISIBLE
        binding.basicLl.visibility=View.GONE
            binding.searchAction.searchText.requestFocus()
            showKeyboardFromView(applicationContext,binding.searchAction.searchText)

    }

    override fun onPause() {
        super.onPause()
        if (mSearchTask != null) {
            mSearchTask?.stop()
        }
    }
    var word = ""

    private fun extractText() {
        binding.pdfReaderRenderView?.let { readerView ->
            muPDFCore?.let {
                val textWord = it.textLines(binding.pdfReaderRenderView.displayedViewIndex)
                for (z in textWord.indices) {
                    for (j in textWord[z].indices) {
                        word += "${textWord[z][j].w} "

                    }
//                    Log.d("kaskd")
                }
                //  Log.i("CUURENTTEXT", "extractText:$word")
            }
        }


    }

    fun playAudioFile() {
stopSeekBarUpdate()
        val uri = Uri.fromFile(File(
            filesDir.absolutePath
                    + "/pdfAudio/audio.wav"
        ))

        val mediaItem = MediaItem.fromUri(uri)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
        startSeekBarUpdate()

    }
    // Function to start the SeekBar update
    private fun startSeekBarUpdate() {
        handler = Handler(Looper.getMainLooper())
        updateSeekBarRunnable = object : Runnable {
            override fun run() {
                if (exoPlayer.isPlaying) {
                    binding.playerbase.playerProgressBar.progress = exoPlayer.currentPosition.toInt()
                    handler?.postDelayed(this, 1000)
                }
            }
        }
        handler?.post(updateSeekBarRunnable!!)
    }

    // Function to stop the SeekBar update
    private fun stopSeekBarUpdate() {
        handler?.removeCallbacks(updateSeekBarRunnable!!)
    }


    override fun onDestroy() {
        super.onDestroy()
        stopSeekBarUpdate() // Ensure handler stops when activity is destroyed
        exoPlayer?.release() // Release player
        if (binding.pdfReaderRenderView != null) {
            binding.pdfReaderRenderView.applyToChildren(object : ReaderView.ViewMapper() {
                override fun applyToView(view: View) {
                    (view as MuPDFView).releaseBitmaps()
                }
            })
        }
        if (muPDFCore != null) muPDFCore?.onDestroy()
        /* if (mAlertTask != null) {
             mAlertTask.cancel(true)
             mAlertTask = null
         }*/
        muPDFCore = null
    }
}


