package com.artifex.mupdfdemo.util

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.RectF
import android.os.Handler
import com.artifex.mupdfdemo.MuPDFCore
import com.artifex.mupdfdemo.R
import com.artifex.mupdfdemo.SearchTaskResult
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class ProgressDialogX(context: Context) : ProgressDialog(context) {
    private var mCancelled = false

    fun isCancelled(): Boolean {
        return !mCancelled
    }

    override fun cancel() {
        mCancelled = true
        super.cancel()
    }
}

abstract class SearchTask(private val mContext: Context, private val mCore: MuPDFCore?) : CoroutineScope {

    private val SEARCH_PROGRESS_DELAY = 200L
    private val mHandler = Handler()
    private val mAlertBuilder: AlertDialog.Builder = AlertDialog.Builder(mContext)
    private var searchJob: Job? = null

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + SupervisorJob()

    protected abstract fun onTextFound(result: SearchTaskResult)
    protected abstract fun onTextNotFound(result: String)

    fun stop() {
        searchJob?.cancel()
        searchJob = null
    }

    fun go(text: String, direction: Int, displayPage: Int, searchPage: Int) {
        if (mCore == null) return

        stop()
        val increment = direction
        val startIndex = if (searchPage == -1) displayPage else searchPage + increment

        val progressDialog = ProgressDialogX(mContext).apply {
            setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            setTitle(mContext.getString(R.string.searching))
            setOnCancelListener { stop() }
            max = mCore.countPages()
        }

        searchJob = launch {
            val result = withContext(Dispatchers.IO) {
                var index = startIndex
                while (index in 0 until mCore.countPages() && isActive) {
                    withContext(Dispatchers.Main) {
                        progressDialog.progress = index
                    }
                    val searchHits: Array<RectF>? = mCore.searchPage(index, text)
                    if (searchHits != null && searchHits.isNotEmpty()) {
                        return@withContext SearchTaskResult(text, index, searchHits)
                    }
                    index += increment
                }
                null
            }

            progressDialog.cancel()

            if (result != null) {
                onTextFound(result)
            } else {
                val message = if (SearchTaskResult.get() == null) {
                    "Content not found" // Use your string resource
                } else {
                    "No further occurrences found" // Use your string resource
                }
                onTextNotFound(message)
            }
        }

        mHandler.postDelayed({
            if (progressDialog.isCancelled()) {
                progressDialog.show()
                progressDialog.progress = startIndex
            }
        }, SEARCH_PROGRESS_DELAY)
    }
}
