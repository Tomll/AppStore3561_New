package com.wedrive.welink.appstore.app.view;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;

import com.mapbar.android.model.ActivityInterface;
import com.mapbar.android.model.BasePage;
import com.mapbar.android.statistics.api.MapbarMobStat;
import com.wedrive.welink.appstore.Configs;
import com.wedrive.welink.appstore.MainActivity;

public class ManngerHelpPage extends BasePage {

	private final static String TAG = "ManngerHelpPage";
	private Context mContext;
	private ActivityInterface mAif;

	
	public ManngerHelpPage(Context context, View view, ActivityInterface aif) {
		super(context, view, aif);
		mContext = context;
		mAif = aif;
	}
	
	
	@Override
	public void viewWillAppear(int flag) {
		super.viewWillAppear(flag);		
		MainActivity.mMainActivity.setFirstAndSecondTitle("帮助", "管理");
		//9MapbarMobStat.onPageStart(mContext,Configs.AppStore_Interface_HelpPage);
	}
	
	@Override
	public void viewWillDisappear(int flag) {
		super.viewWillDisappear(flag);
		//9MapbarMobStat.onPageEnd(mContext,Configs.AppStore_Interface_HelpPage);
	}

	@Override
	public void onResume() {
		super.onResume();
		//8MapbarMobStat.onPageStart(mContext,Configs.AppStore_Interface_HelpPage);
	}

	@Override
	public void onPause() {
		super.onPause();
		//8MapbarMobStat.onPageEnd(mContext,Configs.AppStore_Interface_HelpPage);
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

	@Override
	public int getMyViewPosition() {
		return Configs.VIEW_POSITION_MANNGER_HELP;
	}
	

}
