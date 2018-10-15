package com.wedrive.welink.appstore.app.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import org.json.JSONArray;
import org.json.JSONObject;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;
import com.mapbar.android.model.ActivityInterface;
import com.mapbar.android.model.FilterObj;
import com.mapbar.android.model.OnProviderListener;
import com.mapbar.android.model.ProviderResult;
import com.mapbar.android.statistics.api.MapbarMobStat;
import com.wedrive.welink.appstore.Configs;
import com.wedrive.welink.appstore.MainActivity;
import com.wedrive.welink.appstore.MainApplication;
import com.wedrive.welink.appstore.R;
import com.wedrive.welink.appstore.app.model.AppInfo;
import com.wedrive.welink.appstore.app.model.AppInstall;
import com.wedrive.welink.appstore.app.model.AppVersion;
import com.wedrive.welink.appstore.app.provider.SearchProvider;
import com.wedrive.welink.appstore.app.util.AppUtil;
import com.wedrive.welink.appstore.app.util.CommonUtil;

public class HomePageMannger implements OnClickListener {

	private ActivityInterface mAif;
	private View mView;
	private Context mContext;
	private TextView txt_update;

	private HashMap<String,AppInfo> infoMaps=new HashMap<String,AppInfo>();
	private ArrayList<AppInstall> appInstalls = new ArrayList<AppInstall>();
	private ArrayList<AppVersion> appVersions = new ArrayList<AppVersion>();
	private ArrayList<AppInfo> appUnInstalls = new ArrayList<AppInfo>();

	private boolean isLoader = false;
	private boolean isUnSend = false;
	private boolean isUpSend = false;

	private String faileMessage = "网络不可用，请稍候再试";

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 11:
				if(isLoader){
					txt_update.setText("更新应用（" + appVersions.size() + "）");
					if(isUpSend) mAif.sendToPage(Configs.VIEW_POSITION_MANNGER_UPDATE,10, appVersions);
					if(isUnSend) mAif.sendToPage(Configs.VIEW_POSITION_MANNGER_UNINSTAL,10, appUnInstalls);
				}
				break;
			case 12:
				if(!isLoader){
					txt_update.setText("更新应用");
					faileMessage = (String) msg.obj;
					if(isUpSend) mAif.sendToPage(Configs.VIEW_POSITION_MANNGER_UPDATE,11, faileMessage);
					if(isUnSend) mAif.sendToPage(Configs.VIEW_POSITION_MANNGER_UNINSTAL,11, faileMessage);
				}
				break;
			}

		}
	};

	public HomePageMannger(View view, ActivityInterface aif, Context context) {
		mView=view;
		mAif = aif;
		mContext = context;
		initView(view);
	}

	private void initView(View view) {
		view.findViewById(R.id.txt_mannger_load).setOnClickListener(this);
		txt_update = (TextView) view.findViewById(R.id.txt_mannger_update);
		view.findViewById(R.id.txt_mannger_uninstal).setOnClickListener(this);
		view.findViewById(R.id.txt_mannger_seting).setOnClickListener(this);
		view.findViewById(R.id.txt_mannger_help).setOnClickListener(this);
		view.findViewById(R.id.txt_mannger_contact).setOnClickListener(this);
		txt_update.setOnClickListener(this);
	}


	/**
	 * 方法名称：updateCacheData
	 * 方法描述：更新缓存数据
	 * 方法参数：
	 * 返回类型：
	 * 创建人：wangzc
	 * 创建时间：2017/5/9 14:21
	*/

	public void updateCacheData(String packageName){
		try {
			MainApplication.dbUtils.delete(AppVersion.class,WhereBuilder.b("app_package_name", "=", packageName));
			MainApplication.dbUtils.delete(AppInstall.class,WhereBuilder.b("app_package_name", "=", packageName));
			appVersions = MainApplication.dbUtils.findAll(AppVersion.class);
			appInstalls = MainApplication.dbUtils.findAll(AppInstall.class);
			infoMaps.remove(packageName);

			Iterator<AppInfo> iterator = appUnInstalls.iterator();
			while (iterator.hasNext()){
				String pkgName = iterator.next().getPackageName();
				if(packageName.equals(pkgName)){
					iterator.remove();
					break;
				}
			}
		} catch (DbException e) {
			e.printStackTrace();
		}
	}


	/**
	 * <p>
	 * 功能描述
	 * </p>
	 * 更新显示更新应用数量
	 *
	 * @author wangzhichao
	 * @date 2015年9月15日
	 */

	public void refershUpdateApp() {
		try {
			if(isLoader){
				txt_update.setText("更新应用（" + appVersions.size() + "）");
				appVersions = MainApplication.dbUtils.findAll(AppVersion.class);
				appInstalls = MainApplication.dbUtils.findAll(AppInstall.class);
			}else{
				txt_update.setText("更新应用");
				appVersions.clear();
				appInstalls.clear();
			}
		} catch (Exception e) {
			Log.e("message","exception:"+e.getMessage());
		}
	}

	/**
	 *
	 * <p>
	 * 功能描述
	 * </p>
	 * 获取已经按照的应用信息
	 *
	 * @author wangzhichao
	 * @date 2015年10月22日
	 */

	public void loadAppInstal() {
		try {
			isLoader = false;
			isUnSend = false;
			isUpSend = false;
			infoMaps=AppUtil.getInstalApkMaps(mContext);
			if (CommonUtil.isNetworkAvailable(mContext)) {
				StringBuffer packageNames = new StringBuffer();
				for (String key : infoMaps.keySet()) {
					packageNames.append(key).append(",");
				}
				SearchProvider provider = new SearchProvider(mContext);
				provider.setOnProviderListener(mAppsListener);
				LinkedHashMap<String, String> paramMap = new LinkedHashMap<String, String>();
				paramMap.put("os_version", "" + android.os.Build.VERSION.SDK_INT);
				LinkedHashMap<String, String> paramMap2 = new LinkedHashMap<String, String>();
				paramMap2.put("package_name", packageNames.toString());
				provider.loadInstalApps(paramMap, paramMap2);
			}else{
				txt_update.setText("更新应用");
				Message message = handler.obtainMessage();
				message.obj = "网络不可用，请稍候再试";
				message.what=12;
				handler.sendMessage(message);
			}
		} catch (Exception e) {
			Log.e("message","exception:"+e.getMessage());
		}
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.txt_mannger_load:
			//((MainActivity)mAif).onClick_View_OnClick_Event(mContext,Configs.AppStore_OnClick_DownloadCenter);
            // TODO: 2018/9/26  管理界面 下载中心---
            MapbarMobStat.onEvent(mContext,"F0131","下载中心");
			mAif.showPage(Configs.VIEW_POSITION_HOME, Configs.VIEW_POSITION_MANNGER_LOAD,null,true,null,null);
			break;
		case R.id.txt_mannger_update:
            //((MainActivity)mAif).onClick_View_OnClick_Event(mContext,Configs.AppStore_OnClick_UpdateApp);
            // TODO: 2018/9/26  管理界面 更新应用---
            MapbarMobStat.onEvent(mContext,"F0132","更新应用");
			isUpSend = true;
			FilterObj update = new FilterObj();
			if(isLoader){
				update.setTag(appVersions);
			}else{
				update.setTag(faileMessage);
			}
			mAif.showPage(Configs.VIEW_POSITION_HOME, Configs.VIEW_POSITION_MANNGER_UPDATE, update,true,null,null);
			break;
		case R.id.txt_mannger_uninstal:
			//((MainActivity)mAif).onClick_View_OnClick_Event(mContext,Configs.AppStore_OnClick_UninstallApp);
            // TODO: 2018/9/26  管理界面 卸载应用---
            MapbarMobStat.onEvent(mContext,"F0133","卸载应用");
			isUnSend = true;
			FilterObj filter = new FilterObj();
			if(isLoader){
				filter.setTag(appUnInstalls);
			}else{
				filter.setTag(faileMessage);
			}
			mAif.showPage(Configs.VIEW_POSITION_HOME, Configs.VIEW_POSITION_MANNGER_UNINSTAL, filter,true,null,null);
			break;
		case R.id.txt_mannger_seting:
			//((MainActivity) mAif).onClick_View_OnClick_Event(mContext, Configs.AppStore_OnClick_Settings);
            // TODO: 2018/9/26  管理界面 设置---
            MapbarMobStat.onEvent(mContext,"F0134","设置");
			mAif.showPage(Configs.VIEW_POSITION_HOME, Configs.VIEW_POSITION_MANNGER_SETING, null, true, null, null);
			break;
		case R.id.txt_mannger_help:
			//((MainActivity)mAif).onClick_View_OnClick_Event(mContext,Configs.AppStore_OnClick_Help);
            // TODO: 2018/9/26  管理界面 帮助---
            MapbarMobStat.onEvent(mContext,"F0135","帮助");
			mAif.showPage(Configs.VIEW_POSITION_HOME, Configs.VIEW_POSITION_MANNGER_HELP, null,true,null,null);
			break;
		case R.id.txt_mannger_contact:
			//((MainActivity)mAif).onClick_View_OnClick_Event(mContext,Configs.AppStore_OnClick_ContactUs);
            // TODO: 2018/9/26  管理界面 联系我们---
            MapbarMobStat.onEvent(mContext,"F0136","联系我们");
			mAif.showPage(Configs.VIEW_POSITION_HOME, Configs.VIEW_POSITION_MANNGER_CONTACT, null,true,null,null);
			break;
		}
	}

	private OnProviderListener mAppsListener = new OnProviderListener() {

		@Override
		public void onProviderResponse(int requestCode, int responseCode, ProviderResult result) {
			mAif.hideProgressDialog();
			if (responseCode == -1) {
				Message message = handler.obtainMessage();
				message.obj = "网络不可用，请稍候再试";
				message.what = 12;
				handler.sendMessage(message);
			} else {
				switch (requestCode) {
				case Configs.REQUEST_CODE_INSTAL_APPS_LIST:
					Message message = handler.obtainMessage();
					message.what = 11;

					try {
						JSONObject obj = new JSONObject(result.getResponseStr());
						if(obj.has("data") && !obj.isNull("data")){
							JSONArray jsonArr = obj.getJSONArray("data");
							ArrayList<AppInstall> is = new ArrayList<AppInstall>();
							ArrayList<AppVersion> vs = new ArrayList<AppVersion>();
							ArrayList<AppInfo> us = new ArrayList<AppInfo>();

							for (int i = 0; i < jsonArr.length(); i++) {
								JSONObject listobj = jsonArr.getJSONObject(i);

								AppVersion version = new AppVersion();
								AppInstall install = new AppInstall();

								if (listobj.has("app_id")) {
									version.setApp_id(listobj.getString("app_id"));
									install.setApp_id(listobj.getString("app_id"));
								}

								if (listobj.has("name")) {
									version.setApp_name(listobj.getString("name"));
									install.setApp_name(listobj.getString("name"));
								}

								if (listobj.has("size")) {
									version.setApp_size(listobj.getString("size"));
									install.setApp_size(listobj.getString("size"));
								}

								if (listobj.has("version_id")) {
									version.setApp_version_id(listobj.getString("version_id"));
									install.setApp_version_id(listobj.getString("version_id"));
								}

								if (listobj.has("version_no")) {
									version.setApp_version_no(listobj.getString("version_no"));
									install.setApp_version_no(listobj.getString("version_no"));
								}

								if (listobj.has("official_flag")) {
									version.setOfficial_flag(listobj.getInt("official_flag"));
									install.setOfficial_flag(listobj.getInt("official_flag"));
								}

								if (listobj.has("description")) {
									version.setDescription(listobj.getString("description"));
									install.setDescription(listobj.getString("description"));
								}

								if (listobj.has("apk_path")) {
									version.setApk_path(listobj.getString("apk_path"));
									install.setApk_path(listobj.getString("apk_path"));
								}

								if (listobj.has("icon_path")) {
									version.setIcon_path(listobj.getString("icon_path"));
									install.setIcon_path(listobj.getString("icon_path"));
								}

								if (listobj.has("package_name")) {
									version.setApp_package_name(listobj.getString("package_name"));
									install.setApp_package_name(listobj.getString("package_name"));
								}

								if (listobj.has("md5")){
									version.setApp_md5(listobj.getString("md5"));
									install.setApp_md5(listobj.getString("md5"));
								}

								is.add(install);

								PackageInfo info = CommonUtil.isAppInstalled(mContext, version.getApp_package_name());
								if (info != null) {
									int ins_no = info.versionCode;
									int ver_no = Integer.parseInt(version.app_version_no);
									if (ver_no > ins_no) {
										vs.add(version);
									}
									us.add(infoMaps.get(version.getApp_package_name()));
								}
							}

							appInstalls = is;
							appVersions = vs;
							appUnInstalls=us;

							MainApplication.dbUtils.deleteAll(AppInstall.class);
							MainApplication.dbUtils.deleteAll(AppVersion.class);

							MainApplication.dbUtils.saveOrUpdateAll(appInstalls);
							MainApplication.dbUtils.saveOrUpdateAll(appVersions);
						}
						isLoader = true;
					} catch (Exception e) {
						message.obj = "解析数据出错";
						message.what = 12;
					}finally {
						handler.sendMessage(message);
					}
					break;
				}
			}
		}

		@Override
		public void onReadResponse(int arg0, int arg1) {

		}

	};


}
