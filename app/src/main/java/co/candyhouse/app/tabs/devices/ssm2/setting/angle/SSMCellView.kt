package co.candyhouse.app.tabs.devices.ssm2.setting.angle

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import co.candyhouse.app.R
import co.candyhouse.app.tabs.devices.ssm2.ssmUIParcer
import co.candyhouse.sesame.open.device.CHSesame2
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class SSMCellView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var anim: ValueAnimator? = null
    private var ssmImg: Bitmap
    private var midx: Float? = null
    private var midy: Float? = null
    private var angle: Float? = null

    val handMoveInterval: Long = 500
    val motorMoveSpeed: Long = 10
    var ssmWidth: Int = 0
    var ssmMargin: Int = 0
    var lockWidth: Int = 0
    var lockMargin: Int = 0
    var lockCenter: Float = 0f
    var dotPaint: Paint

    init {

        ssmImg = ContextCompat.getDrawable(context, R.drawable.icon_nosignal)!!.toBitmap()
        dotPaint = Paint()
        dotPaint.color = ContextCompat.getColor(context, R.color.clear)
        dotPaint.style = Paint.Style.FILL
        dotPaint.isAntiAlias = true
        dotPaint.isDither = true
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        midx = width / 2.toFloat()
        midy = height / 2.toFloat()

        ssmWidth = width * 8 / 10
        ssmMargin = (width - ssmWidth) / 2

        lockWidth = width / 30
        lockMargin = ssmWidth / 2
        lockCenter = midx!! // must  x = y
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(ssmImg, Rect(0, 0, 0 + ssmImg.width, 0 + ssmImg.width), Rect(ssmMargin, ssmMargin, ssmMargin + ssmWidth, ssmMargin + ssmWidth), null)
        val lockdeg: Double = angle?.toDG() ?: 0.0
        val lockMarginX = lockCenter + cos(lockdeg) * (lockMargin)
        val lockMarginY = lockCenter - sin(lockdeg) * (lockMargin)
        canvas.drawCircle(lockMarginX.toFloat(), lockMarginY.toFloat(), lockWidth.toFloat(), dotPaint)
    }

    fun setLockImage(ssm: CHSesame2) {
        ssmImg = ContextCompat.getDrawable(context, ssmUIParcer(ssm))!!.toBitmap()
        invalidate()
    }

    fun setLock(ssm: CHSesame2) {
        if (ssm.mechStatus == null) {
            return
        }

        dotPaint.setColor(ContextCompat.getColor(context, if (ssm.mechStatus!!.isInLockRange) R.color.lock_red else R.color.unlock_blue))
        val degree = (ssm.mechStatus!!.position.toFloat() * 360 / 1024)
        val toTarget = ssm.mechStatus!!.target.toFloat() * 360 / 1024

        if (anim?.isRunning == true) {
            return
        }
        if (ssm.mechStatus!!.target.toInt() == -32768) {
            anim = ValueAnimator.ofFloat(angle ?: degree, degree)
            anim!!.duration = handMoveInterval
            anim!!.addUpdateListener { animation ->
                val currentValue = animation.animatedValue as Float
                angle = currentValue
                invalidate()
            }
            anim!!.start()
        } else {
            anim = ValueAnimator.ofFloat(angle ?: toTarget, toTarget)
            anim!!.duration = abs(toTarget.toLong() - (angle?.toLong()
                    ?: toTarget.toLong())) * motorMoveSpeed
            anim!!.addUpdateListener { animation ->
                val currentValue = animation.animatedValue as Float
                angle = currentValue
                invalidate()
            }
            anim!!.start()
        }
    }
}

