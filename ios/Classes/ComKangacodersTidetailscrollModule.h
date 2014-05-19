/**
 * Your Copyright Here
 *
 * Appcelerator Titanium is Copyright (c) 2009-2010 by Appcelerator, Inc.
 * and licensed under the Apache Public License (version 2)
 */
#import "TiModule.h"
#import "TiUIViewProxy.h"
#import "TiUIView.h"
#import "TiUIScrollViewProxy.h"
#import "TiUIScrollView.h"

@interface ComKangacodersTidetailscrollModule : TiModule <UIScrollViewDelegate>
{
    TiViewProxy* resize_view;
    TiViewProxy* blur_view;
    BOOL blur;
    float minimum;
    float maximum;
}

@end
