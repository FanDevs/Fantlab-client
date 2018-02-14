package ru.fantlab.android.ui.widgets

import android.content.Context
import android.support.annotation.AttrRes
import android.support.annotation.StyleRes
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import butterknife.BindView
import butterknife.ButterKnife
import cn.gavinliu.android.lib.shapedimageview.ShapedImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import ru.fantlab.android.R

/**
 * Created by Kosh on 14 Nov 2016, 7:59 PM
 */

class CoverLayout : FrameLayout {

	@BindView(R.id.image)
	lateinit var cover: ShapedImageView

	constructor(context: Context) : super(context)

	constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

	constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(context, attrs, defStyleAttr)

	constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int, @StyleRes defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

	override fun onFinishInflate() {
		super.onFinishInflate()
		View.inflate(context, R.layout.image_layout, this)
		if (isInEditMode) return
		ButterKnife.bind(this)
		setBackgroundResource(R.drawable.rect_shape)
		cover.setShape(ShapedImageView.SHAPE_MODE_ROUND_RECT, 15f)
	}

	fun setUrl(url: String?) {
		Glide.with(context)
				.load(url)
				// todo добавить заглушку
				//.fallback(ContextCompat.getDrawable(context, R.drawable.ic_fantlab_mascot))
				.diskCacheStrategy(DiskCacheStrategy.ALL)
				.dontAnimate()
				.into(cover)
	}
}