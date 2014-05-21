/** KangaCoders Ltd. (most code borrowed from appcelerator titanium) **/
package com.kangacoders.tidetailscroll;

import java.util.HashMap;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.TiDimension;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.view.TiCompositeLayout;
import org.appcelerator.titanium.view.TiCompositeLayout.LayoutArrangement;
import org.appcelerator.titanium.view.TiUIView;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ScrollView;

public class KangaScrollView extends TiUIView {
	private static final String TAG = "KangaScrollView";
	private int offsetX = 0, offsetY = 0;
	private boolean setInitialOffset = false;
	private boolean mScrollingEnabled = true;

	public class TiScrollViewLayout extends TiCompositeLayout {
		private static final int AUTO = Integer.MAX_VALUE;
		private int parentWidth = 0;
		private int parentHeight = 0;
		private boolean canCancelEvents = true;

		public TiScrollViewLayout(Context context, LayoutArrangement arrangement) {
			super(context, arrangement, proxy);
		}

		public void setParentWidth(int width) {
			parentWidth = width;
		}

		public void setParentHeight(int height) {
			parentHeight = height;
		}

		public void setCanCancelEvents(boolean value) {
			canCancelEvents = value;
		}

		@Override
		public boolean dispatchTouchEvent(MotionEvent ev) {
			// If canCancelEvents is false, then we want to prevent the scroll
			// view from canceling the touch
			// events of the child view
			if (!canCancelEvents) {
				requestDisallowInterceptTouchEvent(true);
			}

			return super.dispatchTouchEvent(ev);
		}

		private int getContentProperty(String property) {
			Object value = getProxy().getProperty(property);
			if (value != null) {
				if (value.equals(TiC.SIZE_AUTO)) {
					return AUTO;
				} else if (value instanceof Number) {
					return ((Number) value).intValue();
				} else {
					int type = 0;
					TiDimension dimension;
					if (TiC.PROPERTY_CONTENT_HEIGHT.equals(property)) {
						type = TiDimension.TYPE_HEIGHT;
					} else if (TiC.PROPERTY_CONTENT_WIDTH.equals(property)) {
						type = TiDimension.TYPE_WIDTH;
					}
					dimension = new TiDimension(value.toString(), type);
					return dimension.getUnits() == TiDimension.COMPLEX_UNIT_AUTO ? AUTO
							: dimension.getIntValue();
				}
			}
			return AUTO;
		}

		@Override
		protected int getWidthMeasureSpec(View child) {
			int contentWidth = getContentProperty(TiC.PROPERTY_CONTENT_WIDTH);
			if (contentWidth == AUTO) {
				return MeasureSpec.UNSPECIFIED;
			} else {
				return super.getWidthMeasureSpec(child);
			}
		}

		@Override
		protected int getHeightMeasureSpec(View child) {
			int contentHeight = getContentProperty(TiC.PROPERTY_CONTENT_HEIGHT);
			if (contentHeight == AUTO) {
				return MeasureSpec.UNSPECIFIED;
			} else {
				return super.getHeightMeasureSpec(child);
			}
		}

		@Override
		protected int getMeasuredWidth(int maxWidth, int widthSpec) {
			int contentWidth = getContentProperty(TiC.PROPERTY_CONTENT_WIDTH);
			if (contentWidth == AUTO) {
				contentWidth = maxWidth; // measuredWidth;
			}

			// Returns the content's width when it's greater than the
			// scrollview's width
			if (contentWidth > parentWidth) {
				return contentWidth;
			} else {
				return resolveSize(maxWidth, widthSpec);
			}
		}

		@Override
		protected int getMeasuredHeight(int maxHeight, int heightSpec) {
			int contentHeight = getContentProperty(TiC.PROPERTY_CONTENT_HEIGHT);
			if (contentHeight == AUTO) {
				contentHeight = maxHeight; // measuredHeight;
			}

			// Returns the content's height when it's greater than the
			// scrollview's height
			if (contentHeight > parentHeight) {
				return contentHeight;
			} else {
				return resolveSize(maxHeight, heightSpec);
			}
		}
	}

	// same code, different super-classes
	public class TiVerticalScrollView extends ScrollView {
		private TiScrollViewLayout layout;
		private ScrollViewListener scrollViewListener = null;

		public TiVerticalScrollView(Context context,
				LayoutArrangement arrangement) {
			super(context);
			setScrollBarStyle(SCROLLBARS_INSIDE_OVERLAY);

			layout = new TiScrollViewLayout(context, arrangement);
			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.MATCH_PARENT);
			layout.setLayoutParams(params);
			super.addView(layout, params);
		}

		public TiScrollViewLayout getLayout() {
			return layout;
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_MOVE
					&& !mScrollingEnabled) {
				return false;
			}
			// There's a known Android bug (version 3.1 and above) that will
			// throw an exception when we use 3+ fingers to touch the
			// scrollview.
			// Link: http://code.google.com/p/android/issues/detail?id=18990
			try {
				return super.onTouchEvent(event);
			} catch (IllegalArgumentException e) {
				return false;
			}
		}

		@Override
		public boolean onInterceptTouchEvent(MotionEvent event) {
			if (mScrollingEnabled) {
				return super.onInterceptTouchEvent(event);
			}

			return false;
		}

		@Override
		public void addView(View child,
				android.view.ViewGroup.LayoutParams params) {
			layout.addView(child, params);
		}

		public void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			// setting offset once when this view is visible
			if (!setInitialOffset) {
				scrollTo(offsetX, offsetY);
				setInitialOffset = true;
			}

		}

		public void setScrollViewListener(ScrollViewListener scrollViewListener) {
			this.scrollViewListener = scrollViewListener;
		}

		@Override
		protected void onScrollChanged(int l, int t, int oldl, int oldt) {
			super.onScrollChanged(l, t, oldl, oldt);

			KrollDict data = new KrollDict();
			data.put(TiC.EVENT_PROPERTY_X, l);
			data.put(TiC.EVENT_PROPERTY_Y, t);
			setContentOffset(l, t);
			getProxy().fireEvent(TiC.EVENT_SCROLL, data);

			if (scrollViewListener != null) {
				scrollViewListener.onScrollChanged(this, l, t, oldl, oldt);
			}

		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			layout.setParentHeight(MeasureSpec.getSize(heightMeasureSpec));
			layout.setParentWidth(MeasureSpec.getSize(widthMeasureSpec));
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);

			// This is essentially doing the same logic as if you did
			// setFillViewPort(true). In native Android, they
			// don't measure the child again if measured height of content view
			// < scrollViewheight. But we want to do
			// this in all cases since we allow the content view height to be
			// greater than the scroll view. We force
			// this to allow fill behavior: TIMOB-8243.
			if (getChildCount() > 0) {
				final View child = getChildAt(0);
				int height = getMeasuredHeight();
				final FrameLayout.LayoutParams lp = (LayoutParams) child
						.getLayoutParams();

				int childWidthMeasureSpec = getChildMeasureSpec(
						widthMeasureSpec, getPaddingLeft() + getPaddingRight(),
						lp.width);
				height -= getPaddingTop();
				height -= getPaddingBottom();

				// If we measure the child height to be greater than the parent
				// height, use it in subsequent
				// calculations to make sure the children are measured correctly
				// the second time around.
				height = Math.max(child.getMeasuredHeight(), height);
				int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
						height, MeasureSpec.EXACTLY);
				child.measure(childWidthMeasureSpec, childHeightMeasureSpec);

			}
		}
	}

	public KangaScrollView(TiViewProxy proxy) {
		// we create the view after the properties are processed
		super(proxy);
		getLayoutParams().autoFillsHeight = true;
		getLayoutParams().autoFillsWidth = true;
	}

	public void setContentOffset(int x, int y) {
		KrollDict offset = new KrollDict();
		offsetX = x;
		offsetY = y;
		offset.put(TiC.EVENT_PROPERTY_X, offsetX);
		offset.put(TiC.EVENT_PROPERTY_Y, offsetY);
		getProxy().setProperty(TiC.PROPERTY_CONTENT_OFFSET, offset);
	}

	public void setContentOffset(Object hashMap) {
		if (hashMap instanceof HashMap) {
			HashMap contentOffset = (HashMap) hashMap;
			offsetX = TiConvert.toInt(contentOffset, TiC.PROPERTY_X);
			offsetY = TiConvert.toInt(contentOffset, TiC.PROPERTY_Y);
		} else {
			Log.e(TAG, "ContentOffset must be an instance of HashMap");
		}
	}

	@Override
	public void propertyChanged(String key, Object oldValue, Object newValue,
			KrollProxy proxy) {
		if (Log.isDebugModeEnabled()) {
			Log.d(TAG, "Property: " + key + " old: " + oldValue + " new: "
					+ newValue, Log.DEBUG_MODE);
		}
		if (key.equals(TiC.PROPERTY_CONTENT_OFFSET)) {
			setContentOffset(newValue);
			scrollTo(offsetX, offsetY);
		}
		if (key.equals(TiC.PROPERTY_CAN_CANCEL_EVENTS)) {
			View view = getNativeView();
			boolean canCancelEvents = TiConvert.toBoolean(newValue);
			((TiVerticalScrollView) view).getLayout().setCanCancelEvents(
					canCancelEvents);
		}
		if (TiC.PROPERTY_SCROLLING_ENABLED.equals(key)) {
			setScrollingEnabled(newValue);
		}
		if (TiC.PROPERTY_OVER_SCROLL_MODE.equals(key)) {
			if (Build.VERSION.SDK_INT >= 9) {
				getNativeView().setOverScrollMode(
						TiConvert.toInt(newValue, View.OVER_SCROLL_ALWAYS));
			}
		}
		super.propertyChanged(key, oldValue, newValue, proxy);
	}

	@Override
	public void processProperties(KrollDict d) {
		boolean showVerticalScrollBar = false;

		if (d.containsKey(TiC.PROPERTY_SCROLLING_ENABLED)) {
			setScrollingEnabled(d.get(TiC.PROPERTY_SCROLLING_ENABLED));
		}

		if (d.containsKey(TiC.PROPERTY_SHOW_VERTICAL_SCROLL_INDICATOR)) {
			showVerticalScrollBar = TiConvert.toBoolean(d,
					TiC.PROPERTY_SHOW_VERTICAL_SCROLL_INDICATOR);
		}

		if (d.containsKey(TiC.PROPERTY_CONTENT_OFFSET)) {
			Object offset = d.get(TiC.PROPERTY_CONTENT_OFFSET);
			setContentOffset(offset);
		}

		// we create the view here since we now know the potential widget type
		View view = null;
		LayoutArrangement arrangement = LayoutArrangement.DEFAULT;
		TiScrollViewLayout scrollViewLayout;
		if (d.containsKey(TiC.PROPERTY_LAYOUT)
				&& d.getString(TiC.PROPERTY_LAYOUT).equals(TiC.LAYOUT_VERTICAL)) {
			arrangement = LayoutArrangement.VERTICAL;
		} else if (d.containsKey(TiC.PROPERTY_LAYOUT)
				&& d.getString(TiC.PROPERTY_LAYOUT).equals(
						TiC.LAYOUT_HORIZONTAL)) {
			arrangement = LayoutArrangement.HORIZONTAL;
		}

		view = new TiVerticalScrollView(getProxy().getActivity(), arrangement);
		scrollViewLayout = ((TiVerticalScrollView) view).getLayout();

		if (d.containsKey(TiC.PROPERTY_CAN_CANCEL_EVENTS)) {
			((TiScrollViewLayout) scrollViewLayout)
					.setCanCancelEvents(TiConvert.toBoolean(d,
							TiC.PROPERTY_CAN_CANCEL_EVENTS));
		}

		boolean autoContentWidth = (scrollViewLayout
				.getContentProperty(TiC.PROPERTY_CONTENT_WIDTH) == scrollViewLayout.AUTO);
		boolean wrap = !autoContentWidth;
		if (d.containsKey(TiC.PROPERTY_HORIZONTAL_WRAP) && wrap) {
			wrap = TiConvert.toBoolean(d, TiC.PROPERTY_HORIZONTAL_WRAP, true);
		}
		scrollViewLayout.setEnableHorizontalWrap(wrap);

		if (d.containsKey(TiC.PROPERTY_OVER_SCROLL_MODE)) {
			if (Build.VERSION.SDK_INT >= 9) {
				view.setOverScrollMode(TiConvert.toInt(
						d.get(TiC.PROPERTY_OVER_SCROLL_MODE),
						View.OVER_SCROLL_ALWAYS));
			}
		}

		setNativeView(view);

		nativeView.setVerticalScrollBarEnabled(showVerticalScrollBar);

		super.processProperties(d);
	}

	public TiScrollViewLayout getLayout() {
		View nativeView = getNativeView();
		return ((TiVerticalScrollView) nativeView).layout;
	}

	@Override
	protected void setOnClickListener(View view) {
		View targetView = view;
		// Get the layout and attach the listeners to it
		targetView = ((TiVerticalScrollView) nativeView).layout;
		super.setOnClickListener(targetView);
	}

	public void setScrollingEnabled(Object value) {
		try {
			mScrollingEnabled = TiConvert.toBoolean(value);
		} catch (IllegalArgumentException e) {
			mScrollingEnabled = true;
		}
	}

	public boolean getScrollingEnabled() {
		return mScrollingEnabled;
	}

	public void scrollTo(int x, int y) {
		getNativeView().scrollTo(x, y);
		getNativeView().computeScroll();
	}

	public void scrollToBottom() {
		TiVerticalScrollView scrollView = (TiVerticalScrollView) getNativeView();
		scrollView.fullScroll(View.FOCUS_DOWN);
	}

	@Override
	public void add(TiUIView child) {
		super.add(child);

		if (getNativeView() != null) {
			getLayout().requestLayout();
			if (child.getNativeView() != null) {
				child.getNativeView().requestLayout();
			}
		}
	}

	@Override
	public void remove(TiUIView child) {
		if (child != null) {
			View cv = child.getOuterView();
			if (cv != null) {
				View nv = getLayout();
				if (nv instanceof ViewGroup) {
					((ViewGroup) nv).removeView(cv);
					children.remove(child);
					child.setParent(null);
				}
			}
		}
	}

	@Override
	public void resort() {
		View v = getLayout();
		if (v instanceof TiCompositeLayout) {
			((TiCompositeLayout) v).resort();
		}
	}

}