package co.candyhouse.app.tabs.devices.ssm2.setting.angle

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import co.candyhouse.app.R
import co.candyhouse.app.tabs.devices.ssm2.ssmBotUIParcer
import co.candyhouse.sesame.open.device.CHSesame2Intention
import co.candyhouse.sesame.open.device.CHSesameBot

class SSMBotCellView @JvmOverloads constructor(
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

    fun setLockImage(ssm: CHSesameBot) {
        ssmImg = ContextCompat.getDrawable(context, ssmBotUIParcer(ssm))!!.toBitmap()
        invalidate()
    }

    fun setLock(ssm: CHSesameBot) {
        if (ssm.mechStatus == null) {
//            L.d("hcia", "bot mechStatus:" + ssm.mechStatus)
            return
        }

//        L.d("hcia", "ssm.intention:" + ssm.intention)
        when (ssm.intention) {
            CHSesame2Intention.locking, CHSesame2Intention.unlocking -> {
                startAnimation(AnimationUtils.loadAnimation(context, R.anim.shake_forever))
            }
            else -> {
                clearAnimation()
            }
        }
    }
}

