/** KangaCoders Ltd. (most code borrowed from appcelerator titanium) **/
package com.kangacoders.tidetailscroll;

import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.AsyncResult;
import org.appcelerator.kroll.common.TiMessenger;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiUIView;

import com.kangacoders.tidetailscroll.KangaScrollView;
import com.kangacoders.tidetailscroll.KangaScrollView.TiVerticalScrollView;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;

@Kroll.proxy(creatableInModule=TidetailscrollModule.class, propertyAccessors = {
	TiC.PROPERTY_CONTENT_HEIGHT, TiC.PROPERTY_CONTENT_WIDTH,
	TiC.PROPERTY_SHOW_VERTICAL_SCROLL_INDICATOR,
	TiC.PROPERTY_CONTENT_OFFSET,
	TiC.PROPERTY_CAN_CANCEL_EVENTS,
	TiC.PROPERTY_OVER_SCROLL_MODE
})
public class KangaScrollViewProxy extends TiViewProxy
	implements Handler.Callback
{
	private static final int MSG_FIRST_ID = KrollProxy.MSG_LAST_ID + 1;

	private static final int MSG_SCROLL_TO = MSG_FIRST_ID + 100;
	private static final int MSG_SCROLL_TO_BOTTOM = MSG_FIRST_ID + 101;
	protected static final int MSG_LAST_ID = MSG_FIRST_ID + 999;

	public KangaScrollViewProxy()
	{
		super();
		defaultValues.put(TiC.PROPERTY_OVER_SCROLL_MODE, 0);
	}

	public KangaScrollViewProxy(TiContext context)
	{
		this();
	}

	@Override
	public TiUIView createView(Activity activity) {
		return new KangaScrollView(this);
	}

	public KangaScrollView getScrollView() {
		return (KangaScrollView) getOrCreateView();
	}

	@Kroll.method
	public void scrollTo(int x, int y) {
		if (!TiApplication.isUIThread()) {
			TiMessenger.sendBlockingMainMessage(getMainHandler().obtainMessage(MSG_SCROLL_TO, x, y), getActivity());


			//TiApplication.getInstance().getMessageQueue().sendBlockingMessage(getMainHandler().obtainMessage(MSG_SCROLL_TO, x, y), getActivity());
			//sendBlockingUiMessage(MSG_SCROLL_TO, getActivity(), x, y);
		} else {
			handleScrollTo(x,y);
		}
	}
	
	@Kroll.setProperty @Kroll.method
	public void setScrollingEnabled(Object enabled)
	{
		getScrollView().setScrollingEnabled(enabled);
	}

	@Kroll.getProperty @Kroll.method
	public boolean getScrollingEnabled()
	{
		return getScrollView().getScrollingEnabled();
	}

	@Kroll.method
	public void scrollToBottom() {
		if (!TiApplication.isUIThread()) {
			TiMessenger.sendBlockingMainMessage(getMainHandler().obtainMessage(MSG_SCROLL_TO_BOTTOM), getActivity());

			//TiApplication.getInstance().getMessageQueue().sendBlockingMessage(getMainHandler().obtainMessage(MSG_SCROLL_TO_BOTTOM), getActivity());
			//sendBlockingUiMessage(MSG_SCROLL_TO_BOTTOM, getActivity());
		} else {
			handleScrollToBottom();
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		if (msg.what == MSG_SCROLL_TO) {
			handleScrollTo(msg.arg1, msg.arg2);
			AsyncResult result = (AsyncResult) msg.obj;
			result.setResult(null); // signal scrolled
			return true;
		} else if (msg.what == MSG_SCROLL_TO_BOTTOM) {
			handleScrollToBottom();
			AsyncResult result = (AsyncResult) msg.obj;
			result.setResult(null); // signal scrolled
			return true;
		}
		return super.handleMessage(msg);
	}

	public void handleScrollTo(int x, int y) {
		getScrollView().scrollTo(x, y);
	}
	
	public void handleScrollToBottom() {
		getScrollView().scrollToBottom();
	}

	public TiVerticalScrollView getNativeView() {
		return (TiVerticalScrollView) getScrollView().getNativeView();
	}
}