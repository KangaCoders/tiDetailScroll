package com.kangacoders.tidetailscroll;

import com.kangacoders.tidetailscroll.KangaScrollView.TiVerticalScrollView;

public interface ScrollViewListener {
	void onScrollChanged(TiVerticalScrollView scrollView, int x, int y,
			int oldx, int oldy);
}