/**
 * Your Copyright Here
 *
 * Appcelerator Titanium is Copyright (c) 2009-2010 by Appcelerator, Inc.
 * and licensed under the Apache Public License (version 2)
 */
#import "ComKangacodersTidetailscrollModule.h"
#import "TiBase.h"
#import "TiHost.h"
#import "TiUtils.h"

@implementation ComKangacodersTidetailscrollModule

#pragma mark Internal

// this is generated for your module, please do not change it
-(id)moduleGUID
{
	return @"51f0dc7e-8720-4b26-a863-1711450be854";
}

// this is generated for your module, please do not change it
-(NSString*)moduleId
{
	return @"com.kangacoders.tidetailscroll";
}

#pragma mark Lifecycle

-(void)startup
{
	// this method is called when the module is first loaded
	// you *must* call the superclass
	[super startup];
	
	NSLog(@"[INFO] %@ loaded",self);
}

-(void)shutdown:(id)sender
{
	// this method is called when the module is being unloaded
	// typically this is during shutdown. make sure you don't do too
	// much processing here or the app will be quit forceably
	
	// you *must* call the superclass
	[super shutdown:sender];
}

#pragma mark Cleanup 

-(void)unbind_views
{
    if (resize_view) {
        [resize_view forgetSelf];
        RELEASE_TO_NIL(resize_view);
    }
    if (blur_view) {
        RELEASE_TO_NIL(blur_view);
    }
}

-(void)dealloc
{
	// release any resources that have been retained by the module
	[super dealloc];
}

-(UIScrollView*)to_scroll_view:(id)view
{
    UIScrollView* scrollView = nil;
    if ([view respondsToSelector:@selector(scrollView)]) {
        scrollView = [view scrollView];
    }
    
    return scrollView;
}

#pragma mark Internal Memory Management

-(void)didReceiveMemoryWarning:(NSNotification*)notification
{
	// optionally release any resources that can be dynamically
	// reloaded once memory is available - such as caches
	[super didReceiveMemoryWarning:notification];
}

#pragma mark Listener Notifications

-(void)_listenerAdded:(NSString *)type count:(int)count
{
	if (count == 1 && [type isEqualToString:@"my_event"])
	{
		// the first (of potentially many) listener is being added 
		// for event named 'my_event'
	}
}

-(void)_listenerRemoved:(NSString *)type count:(int)count
{
	if (count == 0 && [type isEqualToString:@"my_event"])
	{
		// the last listener called for event named 'my_event' has
		// been removed, we can optionally clean up any resources
		// since no body is listening at this point for that event
	}
}

#pragma Public APIs

-(void)bind_views:(id)args
{
    ENSURE_UI_THREAD_1_ARG(args);
    ENSURE_SINGLE_ARG_OR_NIL(args, NSDictionary);
    
    TiViewProxy *_scroll_view = [args valueForKey:(@"scroll_view")];
    TiViewProxy *_resize_view = [args valueForKey:(@"resize_view")];
    
    ENSURE_CLASS(_scroll_view, [TiViewProxy class]);
    ENSURE_CLASS(_resize_view, [TiViewProxy class]);
    
    
    [_scroll_view rememberSelf];
    UIScrollView* scroll = [self to_scroll_view:_scroll_view.view];
    scroll.delegate = self;
    [_resize_view rememberSelf];
    UIView* resize = [_resize_view view];
    
    [self unbind_views];
    resize_view = _resize_view;
    
    blur = [args valueForKey:(@"blur_wrapper")] != NULL;
    minimum = [TiUtils floatValue:@"minimum" properties:args];
    maximum = [TiUtils floatValue:@"maximum" properties:args];
    
    if(blur){
//        TiViewProxy *_blur_wrapper = [args valueForKey:(@"blur_wrapper")];
//        ENSURE_CLASS(_blur_wrapper, [TiViewProxy class]);
//        
//        [_blur_wrapper rememberSelf];
//        UIView* blur_wrapper = [_blur_wrapper view];
//    
//        blur_view = [[UIToolbar alloc] initWithFrame:blur_wrapper.frame];
//        [blur_view setBarStyle:UIBarStyleDefault];
//        [blur_view setAlpha:0.0];
//        [blur_view setBarTintColor:[UIColor blueColor]];
//        [blur_wrapper addSubview:blur_view];
        TiViewProxy *_blur_wrapper = [args valueForKey:(@"blur_wrapper")];
        ENSURE_CLASS(_blur_wrapper, [TiViewProxy class]);
        [_blur_wrapper rememberSelf];
        blur_view = _blur_wrapper;

    }
    
}

- (void)resize_view:(float)_new_height
{
    ENSURE_UI_THREAD_0_ARGS;
    UIView* _view = [resize_view view];
    CGRect _view_rect = _view.frame;
    _view_rect.size.height = _new_height;
    [_view setFrame:_view_rect];
}

- (void)fade_blur:(float)_fade_level
{
    ENSURE_UI_THREAD_0_ARGS;
    UIView* _view = [blur_view view];
    [_view setAlpha:_fade_level];
//    NSLog([NSString stringWithFormat:@"%f", _fade_level]);
//    [blur_view setAlpha:_fade_level];
}

#pragma UIScrollView Delegate

- (void)scrollViewDidScroll:(UIScrollView *)_scroll_view
{
    float _new_height = maximum - _scroll_view.contentOffset.y;
    if(_new_height >= minimum && _new_height <= maximum){
        [self resize_view:_new_height];
        [self fade_blur:(((maximum - minimum) - (_new_height - minimum)) / (maximum - minimum) * 1.0)];
    }
}

@end
