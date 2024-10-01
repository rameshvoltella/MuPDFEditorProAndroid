package com.rameshvoltella.pdfeditorpro.ui.base


import android.app.AlertDialog
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.text.Html
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity

import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding



/**
 * Created by Ramesh on 23/09/24.
 *   @auther Ramesh M Nair
 */
abstract class BaseActivity<VBinding : ViewBinding, ViewModel : BaseViewModel> : ComponentActivity() {

    protected lateinit var viewModel: ViewModel
    protected abstract fun getViewModelClass(): Class<ViewModel>
    protected lateinit var binding: VBinding
    protected abstract fun getViewBinding(): VBinding

   // private lateinit var progressDialog : ProgressDialog
    var builder:AlertDialog.Builder?=null
    var dialog: Dialog? = null

    abstract fun observeViewModel()
    abstract fun observeActivity()
    open fun observeActivityWithInstance(savedInstanceState: Bundle?){}
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        setContentView(binding.root)
        setUpViews()
        observeViewModel()
        observeActivity()
    }
    private fun init() {
        binding = getViewBinding()
        viewModel = ViewModelProvider(this)[getViewModelClass()]
        //progressDialog = ProgressDialog(this, R.style.MyDialogStyle)
    }



    open fun setUpViews() {}

    open fun observeView() {}




//    override fun setTitle(resId: CharSequence) {
//        if (supportActionBar != null) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                supportActionBar!!.title =
//                    Html.fromHtml(resId.toString(), Html.FROM_HTML_MODE_LEGACY)
//            } else {
//                supportActionBar!!.title = Html.fromHtml(resId.toString())
//            }
//            supportActionBar!!.setDisplayShowTitleEnabled(true)
//        }
//    }





}