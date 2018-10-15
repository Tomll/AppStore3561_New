package com.wedrive.welink.appstore.app.widget;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

//import com.mapbar.android.mapbarmap.option.bean.EDriveItemInfo;
//import com.mapbar.android.mapbarmap.option.bean.EDriverResult;

public class DataContainer {
	
//	 private static Hashtable<String, EDriverResult> mHt_ChannelContainer = new Hashtable<String, EDriverResult>();
	    private final static int maxSize = 35;
    
//	    public static Vector<EDriveItemInfo> mNearbyInfos;

	    public synchronized static void recycle()
	    {
//	    	mHt_ChannelContainer.clear();
//		    for(Enumeration<String> e = DataContainer.mHt_ImgCache.keys(); e.hasMoreElements();)
//	        {
//	            String key = e.nextElement();
//	            Drawable d = DataContainer.mHt_ImgCache.get(key);
//	            d.setCallback(null);
//	            if(d instanceof BitmapDrawable)
//	            {
//	            	if(((BitmapDrawable)d).getBitmap() != null)
//	            		((BitmapDrawable)d).getBitmap().recycle();
//	        		if(Configs.isDebug)
//	        			System.out.println("[DataContainer]recycle=>"+key);
//	            }
//	            d = null;
//	            DataContainer.mHt_ImgCache.remove(key);
//	        }
//		    DataContainer.mHt_ImgCache.clear();

//	        mNearbyInfos = null;
//	        mHt_ChannelContainer.clear();

	        Set<Entry<String, Drawable>> set = mHt_ImgCache.entrySet();
	        Iterator<Entry<String, Drawable>> it = set.iterator();
	        while(it.hasNext())
	        {
	            Entry<String, Drawable> entry = it.next();
	            Drawable d = entry.getValue();
	            if(d == null)
	                continue;
	            if(d instanceof BitmapDrawable)
	            {
	                // 从内存中移除该图片
	                try
	                {
	                    Bitmap bmp = ((BitmapDrawable)d).getBitmap();
	                    if(bmp != null && !bmp.isRecycled())
	                        bmp.recycle();
	                    bmp = null;
	                 
	                }
	                catch(Exception ex)
	                {
	                    ex.printStackTrace();
	                }
	            }
	            d = null;
	        }
	        mHt_ImgCache.clear();

	        Set<Entry<String, Drawable>> setAbs = mHt_AbsImgCache.entrySet();
	        Iterator<Entry<String, Drawable>> itAbs = setAbs.iterator();
	        while(itAbs.hasNext())
	        {
	            Entry<String, Drawable> entry = itAbs.next();
	            Drawable d = entry.getValue();
	            if(d == null)
	                continue;
	            if(d instanceof BitmapDrawable)
	            {
	                // 从内存中移除该图片
	                try
	                {
	                    Bitmap bmp = ((BitmapDrawable)d).getBitmap();
	                    if(bmp != null && !bmp.isRecycled())
	                        bmp.recycle();
	                    bmp = null;
	                    
	                }
	                catch(Exception ex)
	                {
	                    ex.printStackTrace();
	                }
	            }
	            d = null;
	        }
	        mHt_AbsImgCache.clear();
	        
	        System.gc();
	    }

	    public synchronized static void put(String key, Drawable value)
	    {
	        checkMemory();
//			mHt_ImgCache.put(key, value);
	    }

	    public synchronized static Drawable get(String key)
	    {
	        if(mHt_ImgCache.containsKey(key))
	            return mHt_ImgCache.get(key);
	        return null;
	    }

	    public static boolean containsKey(String key)
	    {
	        return mHt_ImgCache.containsKey(key);
	    }

	    public synchronized static void putAbsDrawable(String key, Drawable value)
	    {
	        checkMemory();
//	        mHt_AbsImgCache.put(key, value);
	    }

	    public synchronized static Drawable getAbsDrawable(String key)
	    {
	        if(mHt_AbsImgCache.containsKey(key))
	            return mHt_AbsImgCache.get(key);
	        return null;
	    }

	    public static boolean containsAbsKey(String key)
	    {
	        return mHt_AbsImgCache.containsKey(key);
	    }

	    private static Map<String, Drawable> mHt_ImgCache = Collections.synchronizedMap(
	            new LinkedHashMap<String, Drawable>(101, .75F, true)
	            {
	                private static final long serialVersionUID = 504454218163194296L;

					@SuppressWarnings("rawtypes")
					protected boolean removeEldestEntry(Map.Entry eldest)
	                {
	                    if(size() < maxSize)
	                        return false;
	                    Drawable d = (Drawable)eldest.getValue();
	                    if(d == null)
	                        return false;
	                    d.setCallback(null);
	                    if(d instanceof BitmapDrawable)
	                    {
	                        // 从内存中移除该图片
	                        try
	                        {
	                            mHt_ImgCache.remove((String)eldest.getKey());
	                            Bitmap bmp = ((BitmapDrawable)d).getBitmap();
	                            if(bmp != null && !bmp.isRecycled())
	                                bmp.recycle();
	                            bmp = null;
	                         
	                        }
	                        catch(Exception ex)
	                        {
	                            ex.printStackTrace();
	                        }
	                    }
	                    d = null;
	                    return false;
	                }
	            });

	    private static Map<String, Drawable> mHt_AbsImgCache = Collections.synchronizedMap(
	            new LinkedHashMap<String, Drawable>(101, .75F, true)
	    {
	        private static final long serialVersionUID = 1L;

			@SuppressWarnings("rawtypes")
			protected boolean removeEldestEntry(Map.Entry eldest)
	        {
	            if(size() < 30)
	                return false;
	            Drawable d = (Drawable)eldest.getValue();
	            if(d == null)
	                return false;
	            d.setCallback(null);
	            if(d instanceof BitmapDrawable)
	            {
	                // 从内存中移除该图片
	                try
	                {
	                	mHt_AbsImgCache.remove((String)eldest.getKey());
	                    Bitmap bmp = ((BitmapDrawable)d).getBitmap();
	                    if(bmp != null && !bmp.isRecycled())
	                    {
	                        bmp.recycle();
	                    }
	                    bmp = null;
	                  
	                }
	                catch(Exception ex)
	                {
	                    ex.printStackTrace();
	                }
	            }
	            d = null;
	            return false;
	        }
	    });

	    public static void checkMemory()
	    {
	    	/**
	    	 * 
	    	try
			{
				long bmpMem = VMRuntime.getRuntime().getExternalBytesAllocated();
				long heapMem = Runtime.getRuntime().totalMemory();
//				long freeMem = Runtime.getRuntime().freeMemory();
				long maxMem = Runtime.getRuntime().maxMemory();
				
				long leafMem = (maxMem - heapMem - bmpMem)/1024;
				
				if(Configs.isDebug)
				System.out.println("[DataContainer]checkMemory=>leafMem="+leafMem+"K");
				
//				System.out.println("[freeMem]" + (freeMem/1024)
//						+ "K[HeapMem]" + (heapMem/1024)
//						+ "K[TotMem]" + ((heapMem + bmpMem)/1024) + "K"
//						+ "[MaxMem]" + ((maxMem/1024)) + "K"); 
				
				if(leafMem < 1500)
				{
					recycle();
				}
			}
			catch(Exception e)
			{
			}
			 */
	    }
	    
		
	   
	  
	    
//	    public static void clear()
//	    {
//	    	mHt_ChannelContainer.clear();
//	    }
	    
	   
	  
	}
