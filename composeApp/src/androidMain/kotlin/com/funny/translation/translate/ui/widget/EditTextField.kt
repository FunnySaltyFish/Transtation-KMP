package com.funny.translation.translate.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatEditText
import com.funny.translation.helper.BitmapUtil.getBitmapFromResources
import com.funny.translation.kmp.R

/*来源https://github.com/opprime/EditTextField
	修改byFunnySaltyFish 2020.2.8
	*/
/**
 * Created by opprime on 16-7-21.
 */
class EditTextField : AppCompatEditText {
    private var mContext: Context? = null
    private var mClearButton: Bitmap? = null
    private var mPaint: Paint? = null
    val isShowing = false

    //按钮显示方式
    private var mClearButtonMode: ClearButtonMode? = null

    //初始化输入框右内边距
    private var mInitPaddingRight = 0

    //按钮的左右内边距，默认为3dp
    private var mButtonPadding = dp2px(3f)

    //按钮的Rect
    private var mButtonRect: Rect? = null
    private var mClearButtonGravity = 0
    private var viewWidth = 0
    private var viewHeight = 0
    private var clearButtonTint = 0

    /**
     * 按钮显示方式
     * NEVER   不显示清空按钮
     * ALWAYS  始终显示清空按钮
     * WHILEEDITING   输入框内容不为空且有获得焦点
     * UNLESSEDITING  输入框内容不为空且没有获得焦点
     */
    enum class ClearButtonMode {
        NEVER,
        ALWAYS,
        WHILEEDITING,
        UNLESSEDITING
    }

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    /**
     * 初始化
     */
    private fun init(context: Context, attributeSet: AttributeSet?) {
        mContext = context
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.EditTextField)
        mClearButtonMode =
            when (typedArray.getInteger(R.styleable.EditTextField_clearButtonMode, 0)) {
                1 -> ClearButtonMode.ALWAYS
                2 -> ClearButtonMode.WHILEEDITING
                3 -> ClearButtonMode.UNLESSEDITING
                else -> ClearButtonMode.NEVER
            }
        mClearButtonGravity = typedArray.getInt(R.styleable.EditTextField_clearButtonGravity, 0)
        clearButtonTint =
            typedArray.getColor(R.styleable.EditTextField_clearButtonTint, Color.WHITE)
        val clearButton = typedArray.getResourceId(
            R.styleable.EditTextField_clearButtonDrawable,
            android.R.drawable.ic_delete
        )
        typedArray.recycle()

        //按钮的图片
        val targetWidth: Int
        val targetHeight: Int
        targetHeight = lineHeight
        targetWidth = targetHeight //ApplicationUtil.sp2px(mContext,(int)getPaint().getTextSize());
        mClearButton = getBitmapFromResources(
            resources,
            clearButton,
            targetWidth,
            targetHeight
        ) //(getDrawableCompat(clearButton)).getBitmap();
        mPaint = Paint()
        mPaint!!.isAntiAlias = true
        mInitPaddingRight = paddingRight
    }

    /**
     * 按钮状态管理
     *
     * @param canvas onDraw的Canvas
     */
    private fun buttonManager(canvas: Canvas) {
        when (mClearButtonMode) {
            ClearButtonMode.ALWAYS -> {
                mButtonRect = getRect(true)
                drawBitmap(canvas, mButtonRect)
            }

            ClearButtonMode.WHILEEDITING -> {
                mButtonRect = getRect(hasFocus() && text!!.length > 0)
                drawBitmap(canvas, mButtonRect)
            }

            ClearButtonMode.UNLESSEDITING -> {}
            else -> {
                mButtonRect = getRect(false)
                drawBitmap(canvas, mButtonRect)
            }
        }
    }

    /**
     * 设置输入框的内边距
     *
     * @param isShow 是否显示按钮
     */
    fun setPadding(isShow: Boolean) {
        val paddingRight =
            mInitPaddingRight + if (isShow) mClearButton!!.width + mButtonPadding else 0
        setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
        //System.out.println("setPadding");
    }

    val isShow: Boolean
        get() {
            var isShow = false
            when (mClearButtonMode) {
                ClearButtonMode.ALWAYS -> isShow = true
                ClearButtonMode.WHILEEDITING -> isShow = hasFocus() && text!!.length > 0
                ClearButtonMode.UNLESSEDITING -> {}
                else -> {}
            }
            return isShow
        }

    /**
     * 取得显示按钮与不显示按钮时的Rect
     *
     * @param isShow 是否显示按钮
     */
    private fun getRect(isShow: Boolean): Rect {
        val left: Int
        val top: Int
        val right: Int
        val bottom: Int
        if (!isShow) { //不展示
            bottom = 0
            left = bottom
            top = left
            right = top
        } else {
            right = viewWidth + scrollX - mButtonPadding
            left = right - mClearButton!!.width
            val line = lineCount
            val textHeight = (line * lineHeight * lineSpacingMultiplier).toInt()
            //System.out.printf("line:%d height:%d\n",line,textHeight);
            top = if (line == 1) {
                (measuredHeight - mClearButton!!.height) / 2
            } else {
                textHeight - mClearButton!!.height
            }
            bottom = top + mClearButton!!.height
        }
        //更新输入框内边距
        //setPadding(isShow);
        return Rect(left, top, right, bottom)
    }

    /**
     * 绘制按钮图片
     *
     * @param canvas onDraw的Canvas
     * @param rect   图片位置
     */
    private fun drawBitmap(canvas: Canvas, rect: Rect?) {
        if (rect != null) {
            //创建一个新的bitmap在上面绘制出指定的颜色的配色，mode使用默认值
            val filter: ColorFilter = PorterDuffColorFilter(clearButtonTint, PorterDuff.Mode.SRC_IN)
            //创建画笔及设置过滤器
            val paint = Paint()
            paint.setColorFilter(filter)
            canvas.drawBitmap(mClearButton!!, null, rect, paint)
        }
    }

    private fun isClickBitmap(x: Int, y: Int): Boolean {
        val lineCount = lineCount
        val maxShowCount = maxLines
        return if (lineCount <= maxShowCount) {
            mButtonRect!!.contains(x, y)
        } else {
            mButtonRect!!.contains(x, y + scrollY)
            //System.out.printf("x:%d,y:%d",x,y);
            //int bottom=(int)(lineCount*getLineHeight()*getLineSpacingMultiplier());
            //System.out.printf("left:%d ,right:%d ,bottom:%d ,top:%d",mButtonRect.left,mButtonRect.right,bottom,bottom-mClearButton.getHeight());
            //return (x>=mButtonRect.left&&x<=mButtonRect.right&&y<=bottom&&y>=bottom-mClearButton.getHeight());
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        buttonManager(canvas)
        canvas.restore()
        //System.out.println("onDraw()");
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        viewWidth = MeasureSpec.getSize(widthMeasureSpec)
        viewHeight = MeasureSpec.getSize(heightMeasureSpec)
        setPadding(isShow)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_UP ->                 //判断是否点击到按钮所在的区域
                if (isClickBitmap(event.x.toInt(), event.y.toInt())) {
                    error = null
                    this.setText("")
                }
        }
        return super.onTouchEvent(event)
    }

    /**
     * 获取Drawable
     *
     * @param resourseId 资源ID
     */
    private fun getDrawableCompat(resourseId: Int): BitmapDrawable? {
        //Bitmap b=BitmapUtil.getBigBitmapFromResources(getResources(),resourseId,targetWidth,targetHeight);
        //return new BitmapDrawable(b);
        return null
    }

    /**
     * 设置按钮左右内边距
     *
     * @param buttonPadding 单位为dp
     */
    fun setButtonPadding(buttonPadding: Int) {
        mButtonPadding = dp2px(buttonPadding.toFloat())
    }

    /**
     * 设置按钮显示方式
     *
     * @param clearButtonMode 显示方式
     */
    fun setClearButtonMode(clearButtonMode: ClearButtonMode?) {
        mClearButtonMode = clearButtonMode
    }

    fun dp2px(dipValue: Float): Int {
        val scale = resources.displayMetrics.density
        return (dipValue * scale + 0.5f).toInt()
    }

    companion object {
        var CLEAR_BUTTON_GRAVITY_BOTTOM = 0
    }
}
