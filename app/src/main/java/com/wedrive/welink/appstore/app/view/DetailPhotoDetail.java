package com.wedrive.welink.appstore.app.view;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.core.BitmapSize;
import com.wedrive.welink.appstore.MainApplication;
import com.wedrive.welink.appstore.R;
import com.wedrive.welink.appstore.app.util.CommonUtil;
import com.wedrive.welink.appstore.app.widget.IndicatorView;

public class DetailPhotoDetail implements OnPageChangeListener {
	
	private static final String TAG = "DetailPhotoDetail";
	private Context mContext;
	private Dialog dialog;
	private RelativeLayout layout;
	private ViewPager mViewPager;
	private IndicatorView mIndicator;
	private MyPagerAdapter mPagerAdapter;

	private String[] imgUrlPath;
	private int currSelectPosition;
	private List<ImageView> mViewCache = new ArrayList<ImageView>();

	public void setCurrSelectPosition(int position) {
		this.currSelectPosition = position;
		if (dialog != null) {
			if (mIndicator != null) {
				mIndicator.setSelection(currSelectPosition);
			}
			mViewPager.setCurrentItem(currSelectPosition);
		}
	}

	public DetailPhotoDetail(Context mContext, String[] imgUrlPath, int currSelectPosition) {
		super();
		this.mContext = mContext;
		this.currSelectPosition = currSelectPosition;
		this.imgUrlPath = imgUrlPath;
		init();
	}

	public void show() {
		if (dialog != null) {
			dialog.show();
		}
	}

	public void dismiss() {
		if (dialog != null) {
			dialog.dismiss();
		}
	}

	private void init() {
		dialog = new Dialog(mContext, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
		
		for (int i = 0; i < imgUrlPath.length; i++) {
			ImageView imageView = (ImageView) View.inflate(mContext, R.layout.item_detail_dialog_image_view, null);
			BitmapDisplayConfig config=new BitmapDisplayConfig();
			BitmapSize size=new BitmapSize(1024,600);
			config.setBitmapMaxSize(size);
			config.setLoadFailedDrawable(mContext.getResources().getDrawable(R.drawable.img_df_pic));
			MainApplication.imageLoader.display(imageView,imgUrlPath[i],config);
			mViewCache.add(imageView);
		}
		
		// 设置view
		layout = new RelativeLayout(mContext);
		layout.setBackgroundColor(mContext.getResources().getColor(R.color.page_bg));
		AbsListView.LayoutParams al = new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		layout.setLayoutParams(al);

		mViewPager = new ViewPager(mContext);
		RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		mViewPager.setLayoutParams(rl);
		mPagerAdapter = new MyPagerAdapter();
		mViewPager.setAdapter(mPagerAdapter);
		if (imgUrlPath.length > 1) {
			mIndicator = new IndicatorView(mContext);
			mIndicator.setCount(imgUrlPath.length);
			// 设置点和点之间的间隙
			mIndicator.setInterval(CommonUtil.dip2px(mContext, 5));
			// 设置点的图片
			mIndicator.setIndicatorDrawable(mContext.getResources().getDrawable(R.drawable.indicator));
			rl = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			rl.addRule(RelativeLayout.CENTER_HORIZONTAL);
			rl.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			rl.setMargins(0, 0, 0, 20);
			mIndicator.setLayoutParams(rl);
			mIndicator.setSelection(currSelectPosition);
		}
		mViewPager.setCurrentItem(currSelectPosition);
		mViewPager.setOnPageChangeListener(this);

		layout.addView(mViewPager);
		if (mIndicator != null)
			layout.addView(mIndicator);
		dialog.setContentView(layout);
	}

	public boolean isShowing() {
		if (dialog != null) {
			return dialog.isShowing();

		}
		return false;
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	@Override
	public void onPageSelected(int position) {
		if (mIndicator != null)
			mIndicator.setSelection(position);
	}

	class MyPagerAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return mViewCache.size();
		}

		@Override
		public boolean isViewFromObject(View view, Object o) {
			return view == o;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			 container.removeView(mViewCache.get(position));
		}
		
		@Override   
        public int getItemPosition(Object object) {   
            return super.getItemPosition(object);   
        }  

		@Override
		public Object instantiateItem(ViewGroup container, int position) {			
			ImageView imageView = mViewCache.get(position);
			imageView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dismiss();
				}
			});
			container.addView(imageView); 
			return imageView;
		}

	}

}
