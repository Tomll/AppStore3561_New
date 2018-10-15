/**
 * Created by wangzhichao on 2015年9月14日.
 * Copyright (c) 2015 北京图为先科技有限公司. All rights reserved.
 */
package com.wedrive.welink.appstore.app.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.widget.ImageView;

import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;
import com.lidroid.xutils.bitmap.callback.DefaultBitmapLoadCallBack;
import com.wedrive.welink.appstore.MainApplication;
import com.wedrive.welink.appstore.R;

public class ImageLoader {

    public BitmapUtils bitmapUtils;
    private ColorDrawable TRANSPARENT_DRAWABLE = new ColorDrawable(android.R.color.transparent);

    public ImageLoader(Context context) {
        bitmapUtils = new BitmapUtils(context, MainApplication.imageDownloadPath, 1024 * 1024);
        bitmapUtils.configDefaultBitmapMaxSize(150, 150);
        bitmapUtils.configDefaultLoadFailedImage(R.drawable.img_df_pic);//加载失败图片
        bitmapUtils.configDefaultBitmapConfig(Bitmap.Config.RGB_565);//设置图片压缩类型
        bitmapUtils.configMemoryCacheEnabled(true);
        bitmapUtils.configDiskCacheEnabled(true);
        bitmapUtils.configDefaultReadTimeout(5 * 1000);
        bitmapUtils.configDefaultConnectTimeout(5 * 1000);
    }

    /**
     * <p>功能描述</p>清空缓存
     *
     * @author wangzhichao
     * @date 2015年9月28日
     */

    public void clearCache() {
//		bitmapUtils.cancel();
        bitmapUtils.clearCache();
        bitmapUtils.clearDiskCache();
        bitmapUtils.clearMemoryCache();
    }

    /**
     * @author sunglasses
     * @category 图片回调函数
     */
    public class CustomBitmapLoadCallBack extends DefaultBitmapLoadCallBack<ImageView> {


        @Override
        public void onLoading(ImageView container, String uri,
                              BitmapDisplayConfig config, long total, long current) {
            super.onLoading(container, uri, config, total, current);
        }


        @Override
        public void onLoadCompleted(ImageView container, String uri,
                                    Bitmap bitmap, BitmapDisplayConfig config, BitmapLoadFrom from) {
            fadeInDisplay(container, bitmap);

        }


        @Override
        public void onLoadFailed(ImageView container, String uri, Drawable drawable) {
            super.onLoadFailed(container, uri, drawable);
        }
    }


    /**
     * @param imageView
     * @param bitmap
     * @author sunglasses
     * @category 图片加载效果
     */
    private void fadeInDisplay(ImageView imageView, Bitmap bitmap) {//目前流行的渐变效果
        TransitionDrawable transitionDrawable = new TransitionDrawable(
                new Drawable[]{TRANSPARENT_DRAWABLE, new BitmapDrawable(imageView.getResources(), bitmap)});
        imageView.setImageDrawable(transitionDrawable);
        transitionDrawable.startTransition(500);
    }

    public void display(ImageView container, String url) {//外部接口函数
        bitmapUtils.display(container, url, new CustomBitmapLoadCallBack());
    }

    public void display(ImageView container, String url, BitmapDisplayConfig config) {//外部接口函数
        bitmapUtils.display(container, url, config, new CustomBitmapLoadCallBack());
    }
}
