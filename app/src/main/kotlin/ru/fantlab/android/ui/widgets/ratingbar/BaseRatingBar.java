package ru.fantlab.android.ui.widgets.ratingbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ru.fantlab.android.R;

public class BaseRatingBar extends LinearLayout implements SimpleRatingBar {

	public interface OnRatingChangeListener {
		void onRatingChange(BaseRatingBar ratingBar, float rating);
	}

	public interface OnRatingDoneListener {
		void onRatingDone(float mRating);
	}

	public static final String TAG = "RatingBar";

	private int mNumStars;
	private int mPadding = 20;
	private int mStarWidth;
	private int mStarHeight;
	private float mMinimumStars = 0;
	private float mRating = -1;
	private float mStepSize = 1f;
	private float mPreviousRating = 0;

	private boolean mIsIndicator = false;
	private boolean mIsScrollable = true;
	private boolean mIsClickable = true;
	private boolean mClearRatingEnabled = true;

	private float mStartX;
	private float mStartY;

	private Drawable mEmptyDrawable;
	private Drawable mFilledDrawable;

	private OnRatingChangeListener mOnRatingChangeListener;
	private OnRatingDoneListener mOnRatingDoneListener;

	protected List<PartialView> mPartialViews;

	public BaseRatingBar(Context context) {
		this(context, null);
	}

	/* Call by xml layout */
	public BaseRatingBar(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	/**
	 * @param context      context
	 * @param attrs        attributes from XML => app:mainText="mainText"
	 * @param defStyleAttr attributes from default style (Application theme or activity theme)
	 */
	public BaseRatingBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BaseRatingBar);
		final float rating = typedArray.getFloat(R.styleable.BaseRatingBar_srb_rating, 0);

		initParamsValue(typedArray, context);
		verifyParamsValue();
		initRatingView();
		setRating(rating);
	}

	private void initParamsValue(TypedArray typedArray, Context context) {
		mNumStars = typedArray.getInt(R.styleable.BaseRatingBar_srb_numStars, mNumStars);
		mStepSize = typedArray.getFloat(R.styleable.BaseRatingBar_srb_stepSize, mStepSize);
		mMinimumStars = typedArray.getFloat(R.styleable.BaseRatingBar_srb_minimumStars, mMinimumStars);
		mPadding = typedArray.getDimensionPixelSize(R.styleable.BaseRatingBar_srb_starPadding, mPadding);
		mStarWidth = typedArray.getDimensionPixelSize(R.styleable.BaseRatingBar_srb_starWidth, 0);
		mStarHeight = typedArray.getDimensionPixelSize(R.styleable.BaseRatingBar_srb_starHeight, 0);
		mEmptyDrawable = typedArray.hasValue(R.styleable.BaseRatingBar_srb_drawableEmpty) ? ContextCompat.getDrawable(context, typedArray.getResourceId(R.styleable.BaseRatingBar_srb_drawableEmpty, View.NO_ID)) : null;
		mFilledDrawable = typedArray.hasValue(R.styleable.BaseRatingBar_srb_drawableFilled) ? ContextCompat.getDrawable(context, typedArray.getResourceId(R.styleable.BaseRatingBar_srb_drawableFilled, View.NO_ID)) : null;
		mIsIndicator = typedArray.getBoolean(R.styleable.BaseRatingBar_srb_isIndicator, mIsIndicator);
		mIsScrollable = typedArray.getBoolean(R.styleable.BaseRatingBar_srb_scrollable, mIsScrollable);
		mIsClickable = typedArray.getBoolean(R.styleable.BaseRatingBar_srb_clickable, mIsClickable);
		mClearRatingEnabled = typedArray.getBoolean(R.styleable.BaseRatingBar_srb_clearRatingEnabled, mClearRatingEnabled);
		typedArray.recycle();
	}

	private void verifyParamsValue() {
		if (mNumStars <= 0) {
			mNumStars = 5;
		}

		if (mPadding < 0) {
			mPadding = 0;
		}

		if (mEmptyDrawable == null) {
			mEmptyDrawable = ContextCompat.getDrawable(getContext(), R.drawable.star_empty);
		}

		if (mFilledDrawable == null) {
			mFilledDrawable = ContextCompat.getDrawable(getContext(), R.drawable.star_filled);
		}

		if (mStepSize > 1.0f) {
			mStepSize = 1.0f;
		} else if (mStepSize < 0.1f) {
			mStepSize = 0.1f;
		}

		mMinimumStars = RatingBarUtils.INSTANCE.getValidMinimumStars(mMinimumStars, mNumStars, mStepSize);
	}

	private void initRatingView() {
		mPartialViews = new ArrayList<>();

		for (int i = 1; i <= mNumStars; i++) {
			PartialView partialView = getPartialView(i, mStarWidth, mStarHeight, mPadding, mFilledDrawable, mEmptyDrawable);
			addView(partialView);

			mPartialViews.add(partialView);
		}
	}

	private PartialView getPartialView(final int partialViewId, int starWidth, int starHeight, int padding,
									   Drawable filledDrawable, Drawable emptyDrawable) {
		PartialView partialView = new PartialView(getContext(), partialViewId, starWidth, starHeight, padding);
		partialView.setFilledDrawable(filledDrawable);
		partialView.setEmptyDrawable(emptyDrawable);
		return partialView;
	}

	protected void emptyRatingBar() {
		fillRatingBar(0);
	}

	protected void fillRatingBar(final float rating) {
		for (PartialView partialView : mPartialViews) {
			int ratingViewId = (int) partialView.getTag();
			double maxIntOfRating = Math.ceil(rating);

			if (ratingViewId > maxIntOfRating) {
				partialView.setEmpty();
				continue;
			}

			if (ratingViewId == maxIntOfRating) {
				partialView.setPartialFilled(rating);
			} else {
				partialView.setFilled();
			}
		}
	}

	@Override
	public void setNumStars(int numStars) {
		if (numStars <= 0) {
			return;
		}

		mPartialViews.clear();
		removeAllViews();

		mNumStars = numStars;
		initRatingView();
	}

	@Override
	public int getNumStars() {
		return mNumStars;
	}

	@Override
	public void setRating(float rating) {
		if (rating > mNumStars) {
			rating = mNumStars;
		}

		if (rating < mMinimumStars) {
			rating = mMinimumStars;
		}

		if (mRating == rating) {
			return;
		}

		mRating = rating;

		if (mOnRatingChangeListener != null) {
			mOnRatingChangeListener.onRatingChange(this, mRating);
		}

		fillRatingBar(rating);
	}

	@Override
	public float getRating() {
		return mRating;
	}

	@Override
	// Unit is pixel
	public void setStarWidth(@IntRange(from = 0) int starWidth) {
		mStarWidth = starWidth;
		for (PartialView partialView : mPartialViews) {
			partialView.setStarWidth(starWidth);
		}
	}

	@Override
	public int getStarWidth() {
		return mStarWidth;
	}

	@Override
	// Unit is pixel
	public void setStarHeight(@IntRange(from = 0) int starHeight) {
		mStarHeight = starHeight;
		for (PartialView partialView : mPartialViews) {
			partialView.setStarHeight(starHeight);
		}
	}

	@Override
	public int getStarHeight() {
		return mStarHeight;
	}

	@Override
	public void setStarPadding(int ratingPadding) {
		if (ratingPadding < 0) {
			return;
		}

		mPadding = ratingPadding;

		for (PartialView partialView : mPartialViews) {
			partialView.setPadding(mPadding, mPadding, mPadding, mPadding);
		}
	}

	@Override
	public int getStarPadding() {
		return mPadding;
	}

	@Override
	public void setEmptyDrawableRes(@DrawableRes int res) {
		setEmptyDrawable(Objects.requireNonNull(ContextCompat.getDrawable(getContext(), res)));
	}

	@Override
	public void setFilledDrawableRes(@DrawableRes int res) {
		setFilledDrawable(Objects.requireNonNull(ContextCompat.getDrawable(getContext(), res)));
	}

	@Override
	public void setEmptyDrawable(@NonNull Drawable drawable) {
		mEmptyDrawable = drawable;

		for (PartialView partialView : mPartialViews) {
			partialView.setEmptyDrawable(drawable);
		}
	}

	@Override
	public void setFilledDrawable(@NonNull Drawable drawable) {
		mFilledDrawable = drawable;

		for (PartialView partialView : mPartialViews) {
			partialView.setFilledDrawable(drawable);
		}
	}

	@Override
	public void setMinimumStars(@FloatRange(from = 0.0) float minimumStars) {
		mMinimumStars = RatingBarUtils.INSTANCE.getValidMinimumStars(minimumStars, mNumStars, mStepSize);
	}

	@Override
	public boolean isScrollable() {
		return mIsScrollable;
	}

	@Override
	public void setScrollable(boolean scrollable) {
		mIsScrollable = scrollable;
	}

	@Override
	public boolean isClickable() {
		return mIsClickable;
	}

	@Override
	public void setClickable(boolean clickable) {
		this.mIsClickable = clickable;
	}

	@Override
	public void setClearRatingEnabled(boolean enabled) {
		this.mClearRatingEnabled = enabled;
	}

	@Override
	public boolean isClearRatingEnabled() {
		return mClearRatingEnabled;
	}

	@Override
	public float getStepSize() {
		return mStepSize;
	}

	@Override
	public void setStepSize(@FloatRange(from = 0.1, to = 1.0) float stepSize) {
		this.mStepSize = stepSize;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		float eventX = event.getX();
		float eventY = event.getY();
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mStartX = eventX;
				mStartY = eventY;
				mPreviousRating = mRating;
				handleClickEvent(eventX);
				break;
			case MotionEvent.ACTION_MOVE:
				if (!isScrollable()) {
					return false;
				}

				handleMoveEvent(eventX);
				break;
			case MotionEvent.ACTION_UP:
				if (mOnRatingDoneListener != null && isClickable()) {
					mOnRatingDoneListener.onRatingDone(mRating);
					}
				if (!RatingBarUtils.INSTANCE.isClickEvent(mStartX, mStartY, event) || !isClickable()) {
					return false;
				}

				handleClickEvent(eventX);
		}

		getParent().requestDisallowInterceptTouchEvent(true);
		return true;
	}

	private void handleMoveEvent(float eventX) {
		for (PartialView partialView : mPartialViews) {
			if (eventX < partialView.getWidth() / 10f + (mMinimumStars * partialView.getWidth())) {
				setRating(mMinimumStars);
				return;
			}

			if (!isPositionInRatingView(eventX, partialView)) {
				continue;
			}

			float rating = RatingBarUtils.INSTANCE.calculateRating(partialView, mStepSize, eventX);

			if (mRating != rating) {
				setRating(rating);
			}
		}
	}

	private void handleClickEvent(float eventX) {
		for (PartialView partialView : mPartialViews) {
			if (!isPositionInRatingView(eventX, partialView)) {
				continue;
			}

			float rating = mStepSize == 1 ? (int) partialView.getTag() : RatingBarUtils.INSTANCE.calculateRating(partialView, mStepSize, eventX);

			if (mPreviousRating == rating && isClearRatingEnabled()) {
				setRating(mMinimumStars);
			} else {
				setRating(rating);
			}
			break;
		}
	}

	private boolean isPositionInRatingView(float eventX, View ratingView) {
		return eventX > ratingView.getLeft() && eventX < ratingView.getRight();
	}

	public void setOnRatingChangeListener(OnRatingChangeListener onRatingChangeListener) {
		mOnRatingChangeListener = onRatingChangeListener;
	}

	public void setOnRatingDoneListener(OnRatingDoneListener onRatingDoneListener) {
		mOnRatingDoneListener = onRatingDoneListener;
		}

}
