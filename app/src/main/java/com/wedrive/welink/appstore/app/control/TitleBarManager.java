package com.wedrive.welink.appstore.app.control;

import android.app.Instrumentation;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.mapbar.android.model.ActivityInterface;
import com.mapbar.android.model.FilterObj;
import com.mapbar.android.model.PageObject;
import com.wedrive.welink.appstore.Configs;
import com.wedrive.welink.appstore.MainActivity;
import com.wedrive.welink.appstore.R;
import com.wedrive.welink.appstore.app.widget.CircularImage;
import com.wedrive.welink.appstore.app.widget.ImageCache;

public class TitleBarManager implements OnClickListener {
	
	private Context mContext;
	private ActivityInterface mAif;
	private View mView;
	private View mCustomView;
	private ImageCache mImageCache;

	public TitleBarManager(Context context, ActivityInterface aif) {
		mContext=context;
		mAif = aif;
		mImageCache = new ImageCache(mContext);
	}

	public TitleBarManager(Context context, View view, ActivityInterface aif) {
		mContext=context;
		mAif = aif;
		mCustomView = view;
		view.findViewById(R.id.fv_left).setOnClickListener(this);
		mImageCache = new ImageCache(mContext);
	}

	public TitleBarManager(Context context, View view, ActivityInterface aif,View customView) {
		mContext=context;
		mAif = aif;
		mView = view;
		mCustomView = customView;
		customView.findViewById(R.id.fv_left).setOnClickListener(this);
		mImageCache = new ImageCache(mContext);
	}

	/**
	 * 
	 * <p>功能描述</p>设置左标题
	 * @param title
	 * @author wangzhichao
	 * @date 2015年11月30日
	 */
	
	public void setFirstTitle(String title) {
		mCustomView.setVisibility(View.VISIBLE);
		mCustomView.findViewById(R.id.tv_first_title).setVisibility(View.VISIBLE);
		mCustomView.findViewById(R.id.tv_second_title).setVisibility(View.GONE);
		mCustomView.findViewById(R.id.tv_third_title).setVisibility(View.GONE);
		mCustomView.findViewById(R.id.iv_login).setVisibility(View.GONE);
		((TextView) mCustomView.findViewById(R.id.tv_first_title)).setText(title);
	}
	
	/**
	 * 
	 * <p>功能描述</p>第一第二标题
	 * @param firstTitle
	 * @param secondTitle
	 * @author wangzhichao
	 * @date 2015年12月15日
	 */

	public void setFirstAndSecondTitle(String firstTitle, String secondTitle) {
		mCustomView.setVisibility(View.VISIBLE);
		mCustomView.findViewById(R.id.tv_first_title).setVisibility(View.VISIBLE);
		mCustomView.findViewById(R.id.tv_second_title).setVisibility(View.GONE);
		mCustomView.findViewById(R.id.tv_third_title).setVisibility(View.GONE);
		mCustomView.findViewById(R.id.iv_login).setVisibility(View.GONE);
		((TextView) mCustomView.findViewById(R.id.tv_first_title)).setText(firstTitle);
//		((TextView) mCustomView.findViewById(R.id.tv_second_title)).setText(secondTitle);
	}
	
	/**
	 * 
	 * <p>功能描述</p>设置完整标题
	 * @param firstTitle
	 * @param secondTitle
	 * @param thirdTitle
	 * @author wangzhichao
	 * @date 2015年12月15日
	 */
	
	public void setAllTitle(String firstTitle, String secondTitle, String thirdTitle) {
		mCustomView.setVisibility(View.VISIBLE);
		mCustomView.findViewById(R.id.tv_first_title).setVisibility(View.VISIBLE);
		mCustomView.findViewById(R.id.tv_second_title).setVisibility(View.GONE);
		mCustomView.findViewById(R.id.tv_third_title).setVisibility(View.VISIBLE);
		mCustomView.findViewById(R.id.iv_login).setVisibility(View.GONE);
		((TextView) mCustomView.findViewById(R.id.tv_first_title)).setText(firstTitle);
		//((TextView) mCustomView.findViewById(R.id.tv_second_title)).setText(secondTitle);
		((TextView) mCustomView.findViewById(R.id.tv_third_title)).setText(thirdTitle);
	}
	
	/**
	 * 
	 * <p>功能描述</p>设置标题点击事件
	 * @param leftOnClickListener
	 * @param rightOnClickListener
	 * @author wangzhichao
	 * @date 2015年12月15日
	 */
	
	public void setTitleOnClickListener(OnClickListener leftOnClickListener,OnClickListener rightOnClickListener) {
//		mCustomView.findViewById(R.id.tv_second_title).setOnClickListener(leftOnClickListener);
		mCustomView.findViewById(R.id.fv_left).setOnClickListener(leftOnClickListener);
		mCustomView.findViewById(R.id.tv_third_title).setOnClickListener(rightOnClickListener);
	}
	
	
	/**
	 * 
	 * <p>功能描述</p>设置用户头像
	 * @param isVisibile
	 * @param drawable
	 * @author wangzhichao
	 * @date 2015年12月15日
	 */
	
	public void setTitleLoginVisibile(boolean isVisibile,Drawable drawable) {
		final CircularImage login = (CircularImage) mCustomView.findViewById(R.id.iv_login);
		if(isVisibile){
			login.setVisibility(View.VISIBLE);
			login.setImageDrawable(drawable);
			login.setOnClickListener(new OnClickListener() {			
				@Override
				public void onClick(View v) {
				    login.setVisibility(View.GONE);
					//((MainActivity)mAif).onClick_View_OnClick_Event(mContext,Configs.AppStore_OnClick_Login);
					FilterObj filter = new FilterObj();
					mAif.showPage(Configs.VIEW_POSITION_HOME, Configs.VIEW_POSITION_MANNGER_LOGIN, filter,true,null,null);		
				}
			});
		}else{
			login.setVisibility(View.INVISIBLE);
		}
	}
	
	/**
	 * 
	 * <p>功能描述</p>在线设置用户头像
	 * @param isVisibile
	 * @param iconUrl
	 * @author wangzhichao
	 * @date 2015年12月15日
	 */
	
	public void setTitleLoginUserIcon(boolean isVisibile,String iconUrl) {
		final CircularImage login = (CircularImage) mCustomView.findViewById(R.id.iv_login);
		if(isVisibile){
			login.setVisibility(View.VISIBLE);	
			mImageCache.setDrawableCache(iconUrl, login,R.drawable.iv_user_logout,50,50);
			login.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					login.setVisibility(View.INVISIBLE);
					//((MainActivity)mAif).onClick_View_OnClick_Event(mContext,Configs.AppStore_OnClick_Login);
					FilterObj filter = new FilterObj();
				    mAif.showPage(Configs.VIEW_POSITION_HOME, Configs.VIEW_POSITION_MANNGER_LOGIN, filter,true,null,null);
				}
			});
		}else{
			login.setVisibility(View.INVISIBLE);
		}


	}
	
	/**
	 * 
	 * <p>功能描述</p>清除用户头像缓存
	 * @author wangzhichao
	 * @date 2016年1月21日
	 */
	
	public void recycleLoginUserIcon(){
		if(mImageCache!=null){
			mImageCache.recycle();
		}
	}
	
	/**
	 * 
	 * <p>功能描述</p>是否显示标题栏分割线
	 * @param isVisibile
	 * @author wangzhichao
	 * @date 2015年12月17日
	 */
	
	public void setTitleDividerVisibile(boolean isVisibile) {
		View divider = mCustomView.findViewById(R.id.img_divider);
		divider.setVisibility(isVisibile ? View.VISIBLE : View.GONE);
	}
	
	/**
	 * 
	 * <p>功能描述</p>隐藏标题栏
	 * @author wangzhichao
	 * @date 2015年12月17日
	 */
	
	public void dismissTitle() {
		mCustomView.setVisibility(View.GONE);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.fv_left:
			mCustomView.setVisibility(View.GONE);
			PageObject page = mAif.getCurrentPageObj();
			switch(page.getPosition()){
			case Configs.VIEW_POSITION_HOME:
			case Configs.VIEW_POSITION_MANNGER_LOGIN:
				onBack();
				break;
				default:
				mAif.showPrevious(null);	
				break;				
			}
			break;
		}
	}

	/**
	 * 模拟返回按键
	 */
	public void onBack() {
		new Thread() {
			public void run() {
				try {
					Instrumentation inst = new Instrumentation();
					inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
				} catch (Exception e) {
					Log.e("Exception when onBack", e.toString());
				}
			}
		}.start();
	}

}
