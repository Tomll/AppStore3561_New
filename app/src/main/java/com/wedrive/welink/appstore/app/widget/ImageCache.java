package com.wedrive.welink.appstore.app.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.widget.ImageView;

import com.mapbar.android.net.HttpHandler;
import com.mapbar.android.net.HttpHandler.CacheType;
import com.mapbar.android.net.HttpHandler.HttpRequestType;
import com.mapbar.android.net.MyHttpHandler;
import com.wedrive.welink.appstore.app.util.CommonUtil;

import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Hashtable;

public class ImageCache {

	private Context mContext;
	private Hashtable<String, ImageTask> mHt_ImgTask = new Hashtable<String, ImageTask>();
	private Hashtable<String, Integer> mHt_ImgWaiting = new Hashtable<String, Integer>();
	private boolean clearBackground = false;

	public ImageCache(Context context) {
		this.mContext = context;
		this.clearBackground = false;
	}

	public ImageCache(Context context, OnMyListViewTefeshListener listener) {
		this.mContext = context;
		this.clearBackground = false;
		this.onMyListViewTefeshListener = listener;
	}

	public ImageCache(Context context, boolean clearBackground) {
		this.mContext = context;
		this.clearBackground = clearBackground;
	}
	
	public void setDrawable(String urlImg, ImageView iv) {
		if (!mHt_ImgWaiting.containsKey(urlImg)) {
			mHt_ImgWaiting.put(urlImg, 0);
			ImageTask task = new ImageTask(iv, false);
			mHt_ImgTask.put(urlImg, task);
			task.execute(urlImg);
		}
	}
	
	public void setDrawable(String urlImg, ImageView iv,int ResId,int width,int height) {	
		mHt_ImgWaiting.put(urlImg, 0);
		ImageTask task = new ImageTask(iv, false,ResId);
		mHt_ImgTask.put(urlImg, task);
		task.execute(urlImg, width, height);
	}

	/**
	 * 联网获取图片
	 * 
	 * @param urlImg
	 *            图片url
	 * @param iv
	 *            图片控件
	 * @return 图片BitmapDrawable
	 */
	public void setDrawableCache(String urlImg, ImageView iv) {
		if (DataContainer.containsKey(urlImg)) {
			BitmapDrawable bd = (BitmapDrawable) DataContainer.get(urlImg);
			if (bd != null && bd.getBitmap() != null && !bd.getBitmap().isRecycled()) {
				if (clearBackground) {
					iv.setBackgroundDrawable(null);
				}
				iv.setImageDrawable(bd);
				iv.postInvalidate();
			}
		} else {
			if (!mHt_ImgWaiting.containsKey(urlImg)) {
				mHt_ImgWaiting.put(urlImg, 0);
				ImageTask task = new ImageTask(iv, false);
				mHt_ImgTask.put(urlImg, task);
				task.execute(urlImg);
			}
		}
	}
	
	public void setDrawableCache(String urlImg, ImageView iv,int ResId) {
		if (DataContainer.containsKey(urlImg)) {
			BitmapDrawable bd = (BitmapDrawable) DataContainer.get(urlImg);
			if (bd != null && bd.getBitmap() != null && !bd.getBitmap().isRecycled()) {
				if (clearBackground) {
					iv.setBackgroundDrawable(null);
				}
				iv.setImageDrawable(bd);
			}else{
				iv.setImageResource(ResId);
			}
			iv.postInvalidate();
		} else {
			if (!mHt_ImgWaiting.containsKey(urlImg)) {
				mHt_ImgWaiting.put(urlImg, 0);
				ImageTask task = new ImageTask(iv, false,ResId);
				mHt_ImgTask.put(urlImg, task);
				task.execute(urlImg);
			}
		}
	}
	
	public void setDrawableCache(String urlImg, ImageView iv,int ResId,int width,int height) {
		if (DataContainer.containsKey(urlImg)) {
			BitmapDrawable bd = (BitmapDrawable) DataContainer.get(urlImg);
			if (bd != null && bd.getBitmap() != null && !bd.getBitmap().isRecycled()) {
				if (clearBackground) {
					iv.setBackgroundDrawable(null);
				}
				iv.setImageDrawable(bd);
			}else{
				iv.setImageResource(ResId);
			}
			iv.postInvalidate();
		} else {
			if (!mHt_ImgWaiting.containsKey(urlImg)) {
				mHt_ImgWaiting.put(urlImg, 0);
				ImageTask task = new ImageTask(iv, false,ResId);
				mHt_ImgTask.put(urlImg, task);
				task.execute(urlImg, width, height);
			}
		}
	}

	/**
	 * 联网获取图片
	 * 
	 * @param urlImg
	 *            图片url
	 * @param iv
	 *            图片控件
	 * @return 图片BitmapDrawable
	 */
	public void setDrawableCache(String urlImg, ImageView iv, int width, int height) {
		if (DataContainer.containsKey(urlImg)) {
			BitmapDrawable bd = (BitmapDrawable) DataContainer.get(urlImg);
			if (bd != null && bd.getBitmap() != null && !bd.getBitmap().isRecycled()) {
				if (clearBackground) {
					iv.setBackgroundDrawable(null);
				}
				iv.setImageDrawable(bd);
				iv.postInvalidate();
			}
		} else {
			if (!mHt_ImgWaiting.containsKey(urlImg)) {
				mHt_ImgWaiting.put(urlImg, 0);
				ImageTask task = new ImageTask(iv, false);
				mHt_ImgTask.put(urlImg, task);
				task.execute(urlImg, width, height);
			}
		}
	}

	/**
	 * 联网获取图片
	 * 
	 * @param urlImg
	 *            图片url
	 * @param iv
	 *            图片控件
	 * @return 图片BitmapDrawable
	 */
	public void setAddListenerDrawableCache(String urlImg, ImageView iv, boolean isAddListener) {
		if (DataContainer.containsKey(urlImg)) {
			BitmapDrawable bd = (BitmapDrawable) DataContainer.get(urlImg);
			if (bd != null && bd.getBitmap() != null && !bd.getBitmap().isRecycled()) {
				if (clearBackground) {
					iv.setBackgroundDrawable(null);
				}
				iv.setImageDrawable(bd);
				iv.postInvalidate();
			}
		} else {
			if (!mHt_ImgWaiting.containsKey(urlImg)) {
				mHt_ImgWaiting.put(urlImg, 0);
				ImageTask task = new ImageTask(iv, false, isAddListener);
				mHt_ImgTask.put(urlImg, task);
				task.execute(urlImg);
			}
		}
	}

	/**
	 * 联网获取图片
	 * 
	 * @param urlImg
	 *            图片url
	 * @param iv
	 *            图片控件
	 * @return 图片BitmapDrawable
	 */
	public void setDrawableAbsCache(String urlImg, ImageView iv) {
		if (DataContainer.containsAbsKey(urlImg)) {
			BitmapDrawable bd = (BitmapDrawable) DataContainer.getAbsDrawable(urlImg);
			if (bd != null && bd.getBitmap() != null && !bd.getBitmap().isRecycled()) {
				iv.setImageDrawable(bd);
				iv.postInvalidate();
			}
		} else {
			if (!mHt_ImgWaiting.containsKey(urlImg)) {
				mHt_ImgWaiting.put(urlImg, 0);
				ImageTask task = new ImageTask(iv, true);
				mHt_ImgTask.put(urlImg, task);
				task.execute(urlImg);
			}
		}
	}

	private Handler mImageHandler = new Handler();

	/**
	 * 联网获取图片类，包含对ImageView的设置
	 * 
	 * @author chenlh
	 * 
	 */
	private class ImageTask {
		private String imgUrl;
		private HttpHandler httpHandler;
		private ImageView imgView;
		private int ResID;
		private boolean isAbsolute = false;
		private boolean mIsAddListener = false;

		public ImageTask(ImageView iv, boolean isAbs) {
			imgView = iv;
			isAbsolute = isAbs;
		}
		
		public ImageTask(ImageView iv, boolean isAbs,int resID) {
			imgView = iv;
			isAbsolute = isAbs;
			ResID=resID;
		}

		public ImageTask(ImageView iv, boolean isAbs, boolean isAddListener) {
			imgView = iv;
			isAbsolute = isAbs;
			mIsAddListener = isAddListener;
		}

		public void clean() {
			imgUrl = null;
			httpHandler = null;
		}

		public void execute(String url) {
			imgUrl = url;
			//判断url是否是有效的url，如果是无效的则直接返回
			if(!CommonUtil.isLegalURL(url)){
				if(ResID!=0) imgView.setImageResource(ResID);
				return;
			}
			httpHandler = new MyHttpHandler(mContext);
			httpHandler.setCache(CacheType.NOCACHE);
			httpHandler.setRequest(url, HttpRequestType.GET);
			httpHandler.setHttpHandlerListener(new HttpHandler.HttpHandlerListener() {
				@Override
				public void onResponse(int resCode, String responseStatusLine, byte[] data) {
					if (data != null) {
						BitmapDrawable d = null;
						try {
							byte[] result = (byte[]) data;
							try {
								d = (BitmapDrawable) byteToDrawable(result, 1);
							} catch (Exception e) {
								if(ResID!=0) imgView.setImageResource(ResID);
							}
							if (d != null) {
								if (isAbsolute)
									DataContainer.putAbsDrawable(imgUrl, d);
								else
									DataContainer.put(imgUrl, d);
								removeItem(imgUrl);
								if (imgView != null) {
									final BitmapDrawable bd = d;
									mImageHandler.post(new Runnable() {
										@Override
										public void run() {
											if (bd != null && bd.getBitmap() != null && !bd.getBitmap().isRecycled()) {
												if (imgView instanceof MyImageView)
													((MyImageView) imgView).setFinishGet(true);
												if (clearBackground) {
													imgView.setBackgroundDrawable(null);
												}
												imgView.setImageDrawable(bd);
												imgView.refreshDrawableState();
												imgView.postInvalidate();
												ResID=0;
												if (onMyListViewTefeshListener != null && mIsAddListener) {
													onMyListViewTefeshListener.onRefeshListView(true);
												}
											}else{
												if(ResID!=0) imgView.setImageResource(ResID);
											}
										}
									});
								}
							}else{
								if(ResID!=0) imgView.setImageResource(ResID);
							}
						} catch (Exception e) {
							if(ResID!=0) imgView.setImageResource(ResID);
						}
					}else{
						if(ResID!=0) imgView.setImageResource(ResID);
					}
				}
			});
			httpHandler.execute();
		}

		public void execute(String url, int width, int height) {
			imgUrl = url;
			//判断url是否是有效的url，如果是无效的则直接返回
			if(!CommonUtil.isLegalURL(url)){
				return;
			}
			if (url.contains("?"))
				url = url + "&width=" + width + "&height=" + height + "&position=top";
			else
				url = url + "?width=" + width + "&height=" + height + "&position=top";
			
			
			httpHandler = new MyHttpHandler(mContext);
			httpHandler.setCache(CacheType.NOCACHE);
			httpHandler.setRequest(url, HttpRequestType.GET);
			httpHandler.setHttpHandlerListener(new HttpHandler.HttpHandlerListener() {
				@Override
				public void onResponse(int resCode, String responseStatusLine, byte[] data) {
					if (data != null) {
						BitmapDrawable d = null;
						try {
							byte[] result = (byte[]) data;
							try {
								d = (BitmapDrawable) byteToDrawable(result, 1);
							} catch (Exception e) {
								if(ResID!=0) imgView.setImageResource(ResID);
							}
							if (d != null) {
								if (isAbsolute)
									DataContainer.putAbsDrawable(imgUrl, d);
								else
									DataContainer.put(imgUrl, d);
								removeItem(imgUrl);
								if (imgView != null) {
									final BitmapDrawable bd = d;
									mImageHandler.post(new Runnable() {
										@Override
										public void run() {
											if (bd != null && bd.getBitmap() != null && !bd.getBitmap().isRecycled()) {
												if (imgView instanceof MyImageView)
													((MyImageView) imgView).setFinishGet(true);
												if (clearBackground) {
													imgView.setBackgroundDrawable(null);
												}
												imgView.setImageDrawable(bd);
												imgView.refreshDrawableState();
												imgView.postInvalidate();
												if (onMyListViewTefeshListener != null && mIsAddListener) {
													onMyListViewTefeshListener.onRefeshListView(true);
												}
											}else{
												if(ResID!=0) imgView.setImageResource(ResID);
											}
										}
									});
								}
							}else{
								if(ResID!=0) imgView.setImageResource(ResID);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}else{
						if(ResID!=0) imgView.setImageResource(ResID);
					}
				}
			});
			httpHandler.execute();
		}

		public void cancel(boolean ca) {
			if (httpHandler != null) {
				httpHandler.cancel(ca);
			}
		}
	}

	public void recycle() {
		for (Enumeration<String> e = mHt_ImgTask.keys(); e.hasMoreElements();) {
			String key = e.nextElement();
			ImageTask task = mHt_ImgTask.get(key);
			if (task == null)
				continue;
			task.cancel(true);
			task.clean();
			mHt_ImgTask.remove(key);
		}
		mHt_ImgTask.clear();
		mHt_ImgWaiting.clear();
		System.gc();
	}

	private void removeItem(String key) {
		ImageTask task = mHt_ImgTask.get(key);
		if (task != null) {
			// task.cancel(true);
			task.clean();
		}
		mHt_ImgTask.remove(key);
		mHt_ImgWaiting.remove(key);
	}

	public void setClearBackground(boolean clear) {
		this.clearBackground = clear;
	}

	private OnMyListViewTefeshListener onMyListViewTefeshListener;

	public interface OnMyListViewTefeshListener {
		public void onRefeshListView(boolean isRefesh);
	}

	public final static Drawable byteToDrawable(byte[] bytes, int inSampleSize) {
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = inSampleSize;
			Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
			try {
				Method setDensity = bm.getClass().getMethod("setDensity", Integer.TYPE);
				if (setDensity != null) {
					setDensity.invoke(bm, 0);
				}
			} catch (Exception xe) {
			}
			return new BitmapDrawable(bm);
		} catch (Exception e) {
		} catch (OutOfMemoryError outofmemoryerror) {
			// System.out.println("============out of memory=============");
		}
		return null;
	}
}
