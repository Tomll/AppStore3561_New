package com.wedrive.welink.appstore.app.view;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import com.mapbar.android.model.ActivityInterface;
import com.mapbar.android.model.BasePage;
import com.mapbar.android.statistics.api.MapbarMobStat;
import com.wedrive.welink.appstore.Configs;
import com.wedrive.welink.appstore.MainActivity;
import com.wedrive.welink.appstore.R;

public class ManngerContactPage extends BasePage implements OnClickListener{

	private final static String TAG = "ManngerContactPage";
	private Context mContext;
	private ActivityInterface mAif;
	private ClipboardManager cm;

	public ManngerContactPage(Context context, View view, ActivityInterface aif)
	{
		super(context, view, aif);
		
		mContext = context;
		mAif = aif;		
		initview(view);
	}
	
	@Override
	public void viewWillAppear(int flag) {
		super.viewWillAppear(flag);		
		MainActivity.mMainActivity.setFirstAndSecondTitle("联系我们","管理");
		MapbarMobStat.onPageStart(mContext,Configs.AppStore_Interface_ContactUsPage);
	}
	
	@Override
	public void viewWillDisappear(int flag) {
		super.viewWillDisappear(flag);
		MapbarMobStat.onPageEnd(mContext,Configs.AppStore_Interface_ContactUsPage);
	}

	@Override
	public void onResume() {
		super.onResume();
		//MapbarMobStat.onPageStart(mContext,Configs.AppStore_Interface_ContactUsPage);
	}

	@Override
	public void onPause() {
		super.onPause();
		//MapbarMobStat.onPageEnd(mContext,Configs.AppStore_Interface_ContactUsPage);
	}

	private void initview(View view) {
		cm = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
		TextView tv_net_address = (TextView) view.findViewById(R.id.tv_net_address);
		TextView tv_qq_num = (TextView) view.findViewById(R.id.tv_qq_num);
		TextView tv_weixin = (TextView) view.findViewById(R.id.tv_weixin);

		tv_net_address.setOnClickListener(this);
		tv_qq_num.setOnClickListener(this);
		tv_weixin.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_net_address:
			try {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(Configs.CONTACT_NET));
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mContext.getApplicationContext().startActivity(intent);
			} catch (Exception e) {
				mAif.showAlert("主人，请前安装浏览器然后再试试");
			}
			break;
		case R.id.tv_qq_num:
			cm.setText(Configs.CONTACT_QQ);
			mAif.showAlert("已将QQ号复制到剪切板");
			break;
		case R.id.tv_weixin:
			cm.setText(Configs.CONTACT_WX);
			mAif.showAlert("已将电话号复制到剪切板");
			break;
		}
	}
	
	
	@Override
	public void goBack()
	{
		mAif.showPrevious(null);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			goBack();
		}
		return true;
	}
	
	@Override
	public int getMyViewPosition() {
		return Configs.VIEW_POSITION_MANNGER_CONTACT;
	}
}
