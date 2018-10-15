package com.wedrive.welink.appstore.app.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;


public class MyImageView extends com.mapbar.scale.ScaleImageView
{
	private ImageCache mImageCache;
	private String mImgUrl;
	private boolean isGetSelf = true;
	private boolean isAbsolute = false;
	private String mResString = "";
	
	public MyImageView(Context context)
	{
		this(context, null, 0x101008a);
	}
	
	public MyImageView(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0x101008a);
	}
	
	public MyImageView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}
	
	public void setImageCache(ImageCache cache, String imgUrl)
	{
		if(this.getDrawable() != null)
		{
			mResString = this.getDrawable().toString();
		}
		isAbsolute = true;
		mImageCache = cache;
		mImgUrl = imgUrl;
	}
	
	public void setAbsImageCache(ImageCache cache, String imgUrl)
	{
		if(this.getDrawable() != null)
		{
			mResString = this.getDrawable().toString();
		}
		mImageCache = cache;
		mImgUrl = imgUrl;
	}
	
	private boolean isFinished = false;
	
	public boolean isFinishGet()
	{
		return isFinished;
	}
	
	public void setFinishGet(boolean finish)
	{
		isFinished = finish;
	}
	
	@Override
	public void onDraw(Canvas canvas)
	{
		Drawable drawable = this.getDrawable();
		if(drawable != null)
		{
			if(drawable instanceof BitmapDrawable)
			{
				Bitmap bmp = ((BitmapDrawable)drawable).getBitmap();
				if(bmp != null && !bmp.isRecycled())
				{
					isGetSelf = true;
					super.onDraw(canvas);
					bmp = null;//创建对象 系统进行回收
				}
				else
				{
					if(isGetSelf)
					{
						isGetSelf = false;
						if(mImageCache != null)
						{
							if(isAbsolute)
								mImageCache.setDrawableAbsCache(mImgUrl, this);
							else
								mImageCache.setDrawableCache(mImgUrl, this);
						}
					}
				}
			}
		}
	}

	public void recycle()
	{
		Drawable drawable = this.getDrawable();
		if(drawable != null)
		{
			if(mResString.equals(drawable.toString()))
			{
				return;
			}
			if(drawable instanceof BitmapDrawable)
			{
				Bitmap bmp = ((BitmapDrawable)drawable).getBitmap();
				if(bmp != null && !bmp.isRecycled())
				{
					bmp.recycle();
					bmp = null;//null对象 以便回收
				}
			}
		}
	}
	
	
}
