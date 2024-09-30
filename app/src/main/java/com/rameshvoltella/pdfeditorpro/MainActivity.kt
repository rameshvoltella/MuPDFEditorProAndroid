package com.rameshvoltella.pdfeditorpro

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.nareshchocha.filepickerlibrary.models.DocumentFilePickerConfig
import com.nareshchocha.filepickerlibrary.utilities.appConst.Const
import com.rameshvoltella.pdfeditorpro.constants.Constants
import com.rameshvoltella.pdfeditorpro.ui.component.PdfEditorProActivity
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import com.nareshchocha.filepickerlibrary.ui.FilePicker
class MainActivity : ComponentActivity() {

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                // Use the uri to load the image
                val uri = it.data?.data!!
                // Use the file path to set image or upload
                val filePath= it.data!!.getStringExtra(Const.BundleExtras.FILE_PATH)
                copyFileFromAssetsToInternal(this, filePath!!)
                val fileName=File(filePath).name
                startActivity(Intent(this@MainActivity, PdfEditorProActivity::class.java).apply {
                    putExtra(Constants.PDF_FILE_PATH, "${filesDir.path}/"+fileName)
                    putExtra(Constants.DOC_ID, -1L)
                    putExtra(Constants.DIRECT_DOC_EDIT_OPEN, false)
                    putExtra(Constants.DOC_NAME, fileName)
                })
//                finish()

            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



//        copyFileFromAssetsToInternal(this, "example.pdf")

    }

    private fun copyFileFromAssetsToInternal(context: Context, filePath: String): String? {
        val inputStream: InputStream
        val outputStream: OutputStream
        try {
            inputStream =  FileInputStream(File(filePath))
            val outputFile = File(context.filesDir, File(filePath).name)
            outputStream = FileOutputStream(outputFile)
            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }
            outputStream.flush()
            outputStream.close()
            inputStream.close()
            return outputFile.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun requestManageExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                // Permission is granted, proceed with file operations
                launcher.launch(
                    FilePicker.Builder(this)
                        .pickDocumentFileBuild(
                            DocumentFilePickerConfig(
                                allowMultiple = true,
                                mMimeTypes = listOf("application/pdf"),
                            ),
                        ),
                )
            } else {
                // Request permission
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse("package:${"com.rameshvoltella.pdfeditorpro"}")
                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requestManageExternalStoragePermission()

    }

}