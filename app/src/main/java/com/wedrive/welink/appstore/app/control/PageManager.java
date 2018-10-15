package com.wedrive.welink.appstore.app.control;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.mapbar.android.model.ActivityInterface;
import com.mapbar.android.model.BasePage;
import com.mapbar.android.model.PageObject;
import com.wedrive.welink.appstore.Configs;
import com.wedrive.welink.appstore.R;
import com.wedrive.welink.appstore.app.view.CameraPage;
import com.wedrive.welink.appstore.app.view.CropPage;
import com.wedrive.welink.appstore.app.view.DetailPage;
import com.wedrive.welink.appstore.app.view.HomePage;
import com.wedrive.welink.appstore.app.view.LoginPage;
import com.wedrive.welink.appstore.app.view.ManngerContactPage;
import com.wedrive.welink.appstore.app.view.ManngerHelpPage;
import com.wedrive.welink.appstore.app.view.ManngerLoadPage;
import com.wedrive.welink.appstore.app.view.ManngerSetingPage;
import com.wedrive.welink.appstore.app.view.ManngerUninstalPage;
import com.wedrive.welink.appstore.app.view.ManngerUpdatePage;

public class PageManager
{
	private Context mContext;
	private LayoutInflater mInflater;
	private ActivityInterface mActivityInterface;
	
	public PageManager(Context context, ActivityInterface activityInterface)
	{
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mActivityInterface = activityInterface;
	}
	
	public PageObject createPage(int index)
	{
		BasePage page = null;
		View view = null;
		switch(index)
		{
			case Configs.VIEW_POSITION_HOME:
			{
				view = mInflater.inflate(R.layout.layout_home, null);
				page = new HomePage(mContext, view, mActivityInterface);
				break;
			}
			case Configs.VIEW_POSITION_DETAIL:
			{
				view = mInflater.inflate(R.layout.layout_detail, null);
				page = new DetailPage(mContext, view, mActivityInterface);
				break;
			}
			case Configs.VIEW_POSITION_MANNGER_LOAD:
			{
				view = mInflater.inflate(R.layout.layout_mannger_load, null);
				page = new ManngerLoadPage(mContext, view, mActivityInterface);
				break;
			}
			case Configs.VIEW_POSITION_MANNGER_UPDATE:
			{
				view = mInflater.inflate(R.layout.layout_mannger_update, null);
				page = new ManngerUpdatePage(mContext, view, mActivityInterface);
				break;
			}
			case Configs.VIEW_POSITION_MANNGER_UNINSTAL:
			{
				view = mInflater.inflate(R.layout.layout_mannger_uninstal, null);
				page = new ManngerUninstalPage(mContext, view, mActivityInterface);
				break;
			}
			case Configs.VIEW_POSITION_MANNGER_SETING:
			{
				view = mInflater.inflate(R.layout.layout_mannger_seting, null);
				page = new ManngerSetingPage(mContext, view, mActivityInterface);
				break;
			}
			case Configs.VIEW_POSITION_MANNGER_HELP:
			{
				view = mInflater.inflate(R.layout.layout_mannger_help, null);
				page = new ManngerHelpPage(mContext, view, mActivityInterface);
				break;
			}
			case Configs.VIEW_POSITION_MANNGER_CONTACT:
			{
				view = mInflater.inflate(R.layout.layout_mannger_contact, null);
				page = new ManngerContactPage(mContext, view, mActivityInterface);
				break;
			}
			case Configs.VIEW_POSITION_MANNGER_LOGIN:
			{
				view = mInflater.inflate(R.layout.layout_mannger_login, null);
				page = new LoginPage(mContext, view, mActivityInterface);
				break;
			}
			case Configs.VIEW_POSITION_MANNGER_CAMERA:
			{
				view = mInflater.inflate(R.layout.layout_mannger_camera, null);
				page = new CameraPage(mContext, view, mActivityInterface);
				break;
			}
			case Configs.VIEW_POSITION_MANNGER_CROP:
			{
				view = mInflater.inflate(R.layout.layout_mannger_crop, null);
				page = new CropPage(mContext, view, mActivityInterface);
				break;
			}
		}
		if(page == null || view == null){
			Log.e("message","the Page is null or the View is null");
			throw new IllegalArgumentException("the Page is null or the View is null.");		
		}
		return new PageObject(index, view, page);
	}
}
