package com.example.clientchatsystem.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.InputStream

object BitmapUtils {
    /**
     * 从输入流中解码图片，并按照指定的宽高进行缩放
     *
     * @param inputStream 图片输入流
     * @param width       缩放后的宽度
     * @param height      缩放后的高度
     * @return 缩放后的图片Bitmap对象
     */
    fun decodeSampledBitmapFromStream(inputStream: InputStream, width: Int, height: Int): Bitmap? {
        // 先获取图片的宽高信息
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeStream(inputStream, null, options)
        // 计算采样率
        options.inSampleSize = calculateInSampleSize(options, width, height)
        // 解码图片
        options.inJustDecodeBounds = false
        val bitmap = BitmapFactory.decodeStream(inputStream, null, options)
        // 回收Bitmap对象的内存
        inputStream.close()
        return bitmap
    }

    /**
     * 计算采样率
     *
     * @param options   图片信息
     * @param reqWidth  缩放后的宽度
     * @param reqHeight 缩放后的高度
     * @return 采样率
     */
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
            val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }
        return inSampleSize
    }
}
