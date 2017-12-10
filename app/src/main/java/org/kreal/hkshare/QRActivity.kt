package org.kreal.hkshare

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.os.AsyncTask
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix


class QRActivity : Activity() {
    private var info: String = ""
    private var hasCreate = false
    private val loadTask = LoadTask()
    private lateinit var view: View
    private lateinit var imageView: ImageView
    private lateinit var waitView: ProgressBar
    private val wmParams: WindowManager.LayoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSPARENT)

    override fun onCreate(savedInstanceState: Bundle?) {
//        overridePendingTransition(Animation.RELATIVE_TO_PARENT,Animation.ABSOLUTE)
        super.onCreate(savedInstanceState)
//        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        try {
            info = intent.getStringExtra(INFO)
        } catch (e: Exception) {
            throw Error("error intent")
        }
        view = FrameLayout.inflate(baseContext, R.layout.activity_show_ftpserver_qr, null)
        imageView = view.findViewById(R.id.QRimage)
        waitView = view.findViewById(R.id.progressBar)
        view.setOnClickListener {
            finish()
        }
        view.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                finish()
                true
            } else
                false
        }
        imageView.setOnClickListener {}
        windowManager.addView(view, wmParams)
        hasCreate = false
    }

    override fun onResume() {
        super.onResume()
        if (!hasCreate) {
            loadTask.execute(info)
        }
    }

    override fun onPause() {
        super.onPause()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
//        logi("destory")
        windowManager.removeViewImmediate(view)
        loadTask.cancel(true)
    }

    inner class LoadTask : AsyncTask<String, Void, Bitmap>() {
        override fun doInBackground(vararg info: String): Bitmap? {
            if (isCancelled)
                return null
            return bitMatrixToBitmap(MultiFormatWriter().encode(info[0], BarcodeFormat.QR_CODE, 800, 800))
        }

        override fun onPreExecute() {
            super.onPreExecute()
            imageView.visibility = View.INVISIBLE
            waitView.visibility = View.VISIBLE
        }

        override fun onPostExecute(result: Bitmap?) {
            super.onPostExecute(result)
            imageView.setImageBitmap(result)
            imageView.visibility = View.VISIBLE
            waitView.visibility = View.INVISIBLE
            hasCreate = true
        }
    }

    companion object {
        private val INFO = "QRActivity_info"

        fun intent(context: Context, info: String): Intent {
            val intent = Intent(context, QRActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            return intent.putExtra(INFO, info)
        }

        private fun bitMatrixToBitmap(bitMatrix: BitMatrix): Bitmap {
            val width = bitMatrix.width
            val height = bitMatrix.height
            val pixels = IntArray(width * height)
            for (y in 0 until width) {
                for (x in 0 until height) {
                    pixels[y * width + x] = if (bitMatrix.get(x, y)) -0x1000000 else -0x1 // black pixel
                }
            }
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bmp.setPixels(pixels, 0, width, 0, 0, width, height)
            return bmp
        }
    }
}
