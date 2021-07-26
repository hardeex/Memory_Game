package com.hardextech.memorygame.bitmap_scaler

import android.graphics.Bitmap
import androidx.core.graphics.drawable.toIcon

object BitmapScaler {
    fun scaleToFitWidth ( b: Bitmap, width:Int): Bitmap{
        val factor = width/b.width.toFloat()
        return Bitmap.createScaledBitmap(b, width, (b.height*factor).toInt() , true)
    }

    fun scaleToFitHeight (b:Bitmap, height: Int): Bitmap{
        val factor = height/b.width.toFloat()
        return Bitmap.createScaledBitmap(b,  (b.width *factor).toInt() , height, true)
    }

}
