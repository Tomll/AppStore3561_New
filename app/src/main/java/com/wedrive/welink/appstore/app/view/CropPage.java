/**
 * Created by wangzhichao on 2015年12月4日.
 * Copyright (c) 2015 北京图为先科技有限公司. All rights reserved.
 */
package com.wedrive.welink.appstore.app.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;

import com.mapbar.android.model.ActivityInterface;
import com.mapbar.android.model.BasePage;
import com.mapbar.android.model.FilterObj;
import com.wedrive.welink.appstore.Configs;
import com.wedrive.welink.appstore.MainActivity;
import com.wedrive.welink.appstore.R;
import com.wedrive.welink.appstore.app.widget.CropImageView;

public class CropPage extends BasePage {
	
	private final static String TAG = "CameraPage";
	private Context mContext;
	private ActivityInterface mAif;
	private View mView;
	
	private final static int CROP_IMAGE_WIDTH=200;
	private final static int CROP_IMAGE_HEIGHT=200;
	
	private CropImageView mCropImageView;
	
	public CropPage(Context context, View view, ActivityInterface aif) {
		super(context, view, aif);
		mContext = context;
		mView = view;
		mAif = aif;
		initView(view);
	}
	
	@Override
	public void viewWillAppear(int arg0) {
		super.viewWillAppear(arg0);
		MainActivity.mMainActivity.setFirstAndSecondTitle("裁切头像", "管理");
		MainActivity.mMainActivity.setTitleDividerVisibile(true);
	}
	
	private void initView(View view){
		mCropImageView=(CropImageView) view.findViewById(R.id.crop_image);
		view.findViewById(R.id.crop_submit).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FilterObj filter = new FilterObj();
				filter.setTag(mCropImageView.getCropImage());
				mAif.showJumpPrevious(Configs.VIEW_POSITION_MANNGER_CROP, Configs.VIEW_POSITION_MANNGER_LOGIN, filter);
			}
		});
		
		view.findViewById(R.id.crop_reset).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mAif.showPrevious(null);
			}
		});
		
	}
	
	@Override
	public void setFilterObj(int flag, FilterObj filter) {
		super.setFilterObj(flag, filter);
		Drawable mDrawable=(Drawable) filter.getTag();
		if(mDrawable!=null){
			mCropImageView.setDrawable(mDrawable, CROP_IMAGE_WIDTH, CROP_IMAGE_HEIGHT);
		}
	}
	
	@Override
	public void goBack() {
		mAif.showPrevious(null);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			goBack();
		}
		return true;
	}

}
