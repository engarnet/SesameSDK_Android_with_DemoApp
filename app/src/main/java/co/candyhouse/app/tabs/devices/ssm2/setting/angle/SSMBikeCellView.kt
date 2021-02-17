package co.candyhouse.app.tabs.devices.ssm2.setting.angle

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import co.candyhouse.app.R
import co.candyhouse.app.tabs.devices.ssm2.ssmBikeUIParcer
import co.candyhouse.sesame.open.device.CHSesameBike

class SSMBikeCellView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var ssmImg: Bitmap

    var ssmWidth: Int = 0
    var ssmMargin: Int = 0

    init {

        ssmImg = ContextCompat.getDrawable(context, R.drawable.icon_nosignal)!!.toBitmap()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        ssmWidth = width * 8 / 10
        ssmMargin = (width - ssmWidth) / 2
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(ssmImg, Rect(0, 0, 0 + ssmImg.width, 0 + ssmImg.width), Rect(ssmMargin, ssmMargin, ssmMargin + ssmWidth, ssmMargin + ssmWidth), null)
    }

    fun setLockImage(ssm: CHSesameBike) {
        ssmImg = ContextCompat.getDrawable(context, ssmBikeUIParcer(ssm))!!.toBitmap()
        invalidate()
    }
}

