package com.rameshvoltella.pdfeditorpro.ui.component.adaptor


import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rameshvoltella.pdfeditorpro.R

class ComfortModeAdaptor(private val items: ArrayList<String>) : RecyclerView.Adapter<ComfortModeAdaptor.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val comfortWebView: WebView = itemView.findViewById(R.id.htmlTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.pdf_comfort_view_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.comfortWebView.loadDataWithBaseURL(
            "", items[position], "text/html",
            "UTF-8",
            null
        )
        holder.comfortWebView.isHapticFeedbackEnabled = false
        holder.comfortWebView.setOnLongClickListener { true }
        holder.comfortWebView.setWebViewClient(object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)

            }
            override fun onPageFinished(view: WebView, url: String) {
//                holder.binding.
//                placeholder.setVisibility(View.GONE)
//                holder.binding.
//                htmlTextView.setVisibility(View.VISIBLE)
            }
        });
    }

    override fun getItemCount(): Int = items.size

    fun addItems(newItems: List<String>) {
        val startPosition = items.size
        items.addAll(newItems)
        notifyItemRangeInserted(startPosition, newItems.size)
    }
}
