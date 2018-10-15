/**
 * Created by wangzhichao on 2015年12月2日.
 * Copyright (c) 2015 北京图为先科技有限公司. All rights reserved.
 */
package com.wedrive.welink.appstore.app.util;

import android.content.Context;

public class DensityUtil {
	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}
	
	public static int dp2px(Context context, float dp) { 
	     final float scale = context.getResources().getDisplayMetrics().density; 
	     return (int) (dp * scale + 0.5f); 
	 } 
	  
	 public static int px2dp(Context context, float px) { 
	     final float scale = context.getResources().getDisplayMetrics().density; 
	     return (int) (px / scale + 0.5f); 
	 } 

}
