package org.kreal.hkshare

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import android.view.animation.Animation
import android.widget.FrameLayout
import android.widget.ImageView
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix


class QRActivity : AppCompatActivity() {
    lateinit private var imageView: ImageView
    private var info: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
//        overridePendingTransition(Animation.RELATIVE_TO_PARENT,Animation.ABSOLUTE)
        super.onCreate(savedInstanceState)
//        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        try {
            info = intent.getStringExtra(INFO)
        } catch (e: Exception) {
            throw Error("error intent")
        }
        val view = FrameLayout.inflate(baseContext, R.layout.activity_show_ftpserver_qr, null)
        view.setOnClickListener {
            finish()
        }
        imageView = view.findViewById(R.id.QRimage)
        imageView.setImageBitmap(bitMatrixToBitmap(MultiFormatWriter().encode(info, BarcodeFormat.QR_CODE, 800, 800)))
        setContentView(view)
    }
    companion object {
        private val INFO = "QRActivity_info"

        fun intent(context: Context, info: String):Intent{
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
