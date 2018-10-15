package com.wedrive.welink.appstore.app.view;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.format.Formatter;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.mapbar.android.model.ActivityInterface;
import com.mapbar.android.model.BasePage;
import com.mapbar.android.model.OnDialogListener;
import com.mapbar.android.model.OnProviderListener;
import com.mapbar.android.model.ProviderResult;
import com.mapbar.android.net.MyHttpHandler;
import com.mapbar.android.statistics.api.MapbarMobStat;
import com.wedrive.welink.appstore.Configs;
import com.wedrive.welink.appstore.MainActivity;
import com.wedrive.welink.appstore.MainApplication;
import com.wedrive.welink.appstore.R;
import com.wedrive.welink.appstore.app.control.DataCleanManager;
import com.wedrive.welink.appstore.app.download.DownloadInfo;
import com.wedrive.welink.appstore.app.provider.SearchProvider;
import com.wedrive.welink.appstore.app.util.AppUtil;
import com.wedrive.welink.appstore.app.util.CommonUtil;
import com.wedrive.welink.appstore.app.widget.LoadingDialog;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ManngerSetingPage extends BasePage implements OnClickListener {

	private String TAG = "ManngerSetingPage";
	private Context mContext;
	private ActivityInterface mAif;
	private LoadingDialog mLoadingDialog;
	private TextView tv_version_name, tv_cache_num;
	private CheckBox cb_net_btn, img_clear_btn;

	public ManngerSetingPage(Context context, View view, ActivityInterface aif) {
		super(context, view, aif);

		mContext = context;
		mAif = aif;
		initView(view);
	}

	private void initView(View view) {
		tv_version_name = (TextView) view.findViewById(R.id.tv_version_name);
		tv_cache_num = (TextView) view.findViewById(R.id.tv_cache_num);
		cb_net_btn = (CheckBox) view.findViewById(R.id.cb_net_btn);
		img_clear_btn = (CheckBox) view.findViewById(R.id.img_clear_btn);
		tv_version_name.setText(CommonUtil.getVersion(mContext));
		setCacheSize();

		view.findViewById(R.id.ral_mannger_setting_net).setOnClickListener(this);
		view.findViewById(R.id.ral_mannger_setting_clear_apk).setOnClickListener(this);
		view.findViewById(R.id.ral_mannger_setting_clear_cache).setOnClickListener(this);
		view.findViewById(R.id.ral_mannger_setting_check_version).setOnClickListener(this);

		cb_net_btn.setChecked(MainApplication.mAppPreferce.netWorkAllow);
		img_clear_btn.setChecked(MainApplication.mAppPreferce.clearLoadAPK);

		cb_net_btn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				MainApplication.mAppPreferce.netWorkAllow = isChecked;
				MainApplication.mSharedPreferences.edit().putBoolean("netWorkAllow", isChecked).commit();
				if (isChecked) {
					mAif.showAlert(R.string.seting_net_open);
				} else {
					mAif.showAlert(R.string.seting_net_close);
				}
			}
		});
		img_clear_btn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				MainApplication.mAppPreferce.clearLoadAPK = isChecked;
				MainApplication.mSharedPreferences.edit().putBoolean("clearLoadAPK", isChecked).commit();
				if (isChecked) {
					mAif.showAlert(R.string.seting_clear_apk_open);
				} else {
					mAif.showAlert(R.string.seting_clear_apk_close);
				}
			}
		});
	}
	
	/**
	 * 
	* @Title: setCacheSize 
	* @Description: 设置缓存大小
	* @return void :
	* @author : wangzc
	* @date 2016年4月14日
	 */
	
	private void setCacheSize(){
		String cacheSize=getCacheSize();
		if(cacheSize.contains("KB")){
			String data=cacheSize.substring(0, cacheSize.length()-2);
			Double value=Double.parseDouble(data);
			if(value<10){
				tv_cache_num.setText("0 KB");
				return;
			}
		}
		tv_cache_num.setText(cacheSize);
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.ral_mannger_setting_net:
				// TODO: 2018/9/26 设置界面 2G、3G、4G网络下载开关---
				MapbarMobStat.onEvent(mContext,"F0142","2G、3G、4G网络下载开关");
				if(cb_net_btn.isChecked()){
					cb_net_btn.setChecked(false);
				}else{
					cb_net_btn.setChecked(true);
				}
				break;
			case R.id.ral_mannger_setting_clear_apk:
				// TODO: 2018/9/26 设置界面 安装完成自动清理安装包开关---
				MapbarMobStat.onEvent(mContext,"F0143","安装完成自动清理安装包开关");
				if(img_clear_btn.isChecked()){
					img_clear_btn.setChecked(false);
				}else{
					img_clear_btn.setChecked(true);
				}
				break;
			case R.id.ral_mannger_setting_clear_cache:
				// TODO: 2018/9/26 设置界面 清除缓存---
				MapbarMobStat.onEvent(mContext,"F0144","清除缓存");
				mAif.showProgressDialog(R.string.dialog_loading_clear);
				new ClearCacheTask().execute(null, null, null);
				break;
			case R.id.ral_mannger_setting_check_version:
				// TODO: 2018/9/26 设置界面 检查更新---
				MapbarMobStat.onEvent(mContext,"F0145","检查更新");
				if (CommonUtil.isNetworkAvailable(mContext)) {
					checkVersion();
				}else{
					mAif.showAlert(R.string.net_unconnect_imp_label);
				}
			break;
		default:
			break;
		}
	}
	
	@Override
	public void viewWillAppear(int flag) {
		super.viewWillAppear(flag);		
		MainActivity.mMainActivity.setFirstAndSecondTitle("设置", "管理");
		MapbarMobStat.onPageStart(mContext,Configs.AppStore_Interface_SettingsPage);
	}
	
	@Override
	public void viewWillDisappear(int flag) {
		super.viewWillDisappear(flag);
		MapbarMobStat.onPageEnd(mContext,Configs.AppStore_Interface_SettingsPage);
	}

	@Override
	public void onResume() {
		super.onResume();
		//MapbarMobStat.onPageStart(mContext,Configs.AppStore_Interface_SettingsPage);
	}

	@Override
	public void onPause() {
		super.onPause();
		//MapbarMobStat.onPageEnd(mContext,Configs.AppStore_Interface_SettingsPage);
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

	private String getCacheSize() {
		long useSpace = mContext.getCacheDir().length();
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			useSpace += mContext.getExternalCacheDir().length();
		}
//		useSpace += getDirSize(new File(MainApplication.apkDownloadPath));
		useSpace += getDirSize(new File(MainApplication.imageDownloadPath));
		String temp = Formatter.formatFileSize(mContext, useSpace);
		return temp;
	}

	public static long getDirSize(File file) {
		// 判断文件是否存在
		if (file.exists()) {
			// 如果是目录则递归计算其内容的总大小
			if (file.isDirectory()) {
				File[] children = file.listFiles();
				long size = 0;
				for (File f : children)
					size += getDirSize(f);
				return size;
			} else {// 如果是文件则直接返回其大小,以“兆”为单位
				long size = file.length();
				return size;
			}
		} else {
			System.out.println("文件或者文件夹不存在，请检查路径是否正确！");
			return 0;
		}
	}

	private static void deleteFilesByDirectory(File directory) {
		if (directory != null && directory.exists() && directory.isDirectory()) {
			List<String> apkPath = new ArrayList<String>();
			File[] listFiles = directory.listFiles();
			int downloadInfoListCount = MainApplication.downloadManager.getDownloadInfoListCount();

			if (downloadInfoListCount == 0) {
				for (File file : listFiles) {
					file.delete();
				}
				return;
			}

			if (downloadInfoListCount > 0) {
				for (int i = 0; i < downloadInfoListCount; i++) {
					DownloadInfo downloadInfo = MainApplication.downloadManager.getDownloadInfo(i);
					apkPath.add(downloadInfo.getFileSavePath());
				}
			}

			for (int i = 0; i < listFiles.length; i++) {
				boolean isContains = false;
				File file = listFiles[i];// sd
				for (String str : apkPath) {
					if (str.contains(file.getName())) {
						isContains = true;
						break;
					}
				}
				if (!isContains) {
					file.delete();
				}
			}
		}
	}

	class ClearCacheTask extends AsyncTask<Object, Object, Object> {

		@Override
		protected Object doInBackground(Object... params) {
			DataCleanManager.cleanInternalCache(mContext);// 清除本应用内部缓存(/data/data/com.xxx.xxx/cache)
			DataCleanManager.cleanExternalCache(mContext);// mnt/sdcard/android/data/com.xxx.xxx/cache	
			DataCleanManager.cleanCustomCache(MainApplication.imageDownloadPath);// 清除storage/sdcard/mapbar/wediver/appstore/images
			
			deleteFilesByDirectory(new File(MainApplication.apkDownloadPath));// 清除storage/sdcard/mapbar/wediver/appstore/apks
			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			mAif.hideProgressDialog();
			setCacheSize();
			mAif.showAlert(R.string.seting_clear_dialog);
		}

	}

	/**
	 * 
	 * <p>
	 * 功能描述
	 * </p>
	 * 检查更新功能
	 * 
	 * @author wangzhichao
	 * @date 2015年9月10日
	 */

//	private void checkVersion() {
//		try {
//			PackageManager manager = mContext.getPackageManager();
//			PackageInfo info = manager.getPackageInfo(mContext.getPackageName(), 0);
//			SearchProvider provider = new SearchProvider(mContext);
//			provider.setOnProviderListener(mAppsListener);
//			LinkedHashMap<String, String> paramMap = new LinkedHashMap<String, String>();
//			paramMap.put("os_version", "" + android.os.Build.VERSION.SDK_INT);
//			paramMap.put("version_no", "" + info.versionCode);
//			paramMap.put("package_name", info.packageName);
//			MyHttpHandler myHttpHandler = provider.checkVersion(paramMap);
//			mAif.showProgressDialog(myHttpHandler, R.string.dialog_loading_data, true);
//		} catch (NameNotFoundException e) {
//			Log.e("message", "exception:" + e.getMessage());
//		}
//	}

	private void checkVersion(){
		try {
			PackageManager manager = mContext.getPackageManager();
			PackageInfo info = manager.getPackageInfo(mContext.getPackageName(), 0);
			SearchProvider provider = new SearchProvider(mContext);
			provider.setOnProviderListener(mAppsListener);
			LinkedHashMap<String, String> paramMap = new LinkedHashMap<String, String>();
			paramMap.put("os_version", ""+android.os.Build.VERSION.SDK_INT);
			paramMap.put("version_no", "" + info.versionCode);
			paramMap.put("package_name", info.packageName);
			final MyHttpHandler myHttpHandler = provider.checkVersion(paramMap);

			mLoadingDialog = new LoadingDialog(mContext);
			mLoadingDialog.setTip(mContext.getResources().getString(R.string.check_update_version));
			mLoadingDialog.setIndeterminate(true);
			mLoadingDialog.setCancelable(false);
			mLoadingDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
				@Override
				public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent evnet) {
					if (keyCode == KeyEvent.KEYCODE_BACK) {
						if(myHttpHandler != null) myHttpHandler.cancel(true);
						if(mLoadingDialog != null) mLoadingDialog.dismiss();
						return false;
					}
					return false;
				}
			});
			mLoadingDialog.show();
		} catch (Exception e) {
			if(mLoadingDialog != null) mLoadingDialog.dismiss();
		}

	}

	/**
	 * 
	 * <p>
	 * 功能描述
	 * </p>
	 * 执行更新功能
	 * 
	 * @param url
	 * @author wangzhichao
	 * @date 2015年9月10日
	 */

	private void updateVersion(final String apk_path,final String apk_md5){
		final DownloadInfo downloadInfo = new DownloadInfo();
		final String savePath=MainApplication.downloadRootPath+apk_path.substring(apk_path.lastIndexOf("/"));
		File file=new File(savePath);
		if(file.exists()) file.delete();
		downloadInfo.setDownloadUrl(apk_path);
		downloadInfo.setFileSavePath(savePath);

		RequestCallBack<File> requestCallBack=new RequestCallBack<File>() {

			public void onStart() {
				HttpHandler<File> handler = downloadInfo.getHandler();
				if (handler != null) {
					downloadInfo.setState(handler.getState());
				}
				((MainActivity) mAif).showProgressDialog(R.string.apk_version_update,new DialogInterface.OnKeyListener(){
					@Override
					public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent evnet){
						if (keyCode == KeyEvent.KEYCODE_BACK) {
							dialog.dismiss();
							HttpHandler<File> handler = downloadInfo.getHandler();
							if (handler != null && !handler.isCancelled()) {
								handler.cancel();
							}
							return true;
						}
						return false;
					}
				},false);
			}

			public void onCancelled() {
				HttpHandler<File> handler = downloadInfo.getHandler();
				if (handler != null) {
					downloadInfo.setState(handler.getState());
				}
				mAif.hideProgressDialog();
			}

			@Override
			public void onSuccess(ResponseInfo<File> result) {
				HttpHandler<File> handler = downloadInfo.getHandler();
				if (handler != null) {
					downloadInfo.setState(handler.getState());
				}
				mAif.hideProgressDialog();
				File file=result.result;
				if(file!=null && file.exists()) {
					if (CommonUtil.verifyFileMD5(file, apk_md5)) {//校验通过
						AppUtil.installApp(mContext, file);
					} else {//校验失败
						file.delete();
						final RequestCallBack callBack=this;
						((MainActivity) mAif).showDialog("温馨提示", "应用商店安装包已损坏，是否继续下载?", "重新下载", "取消", new OnDialogListener() {

							@Override
							public void onOk() {
								HttpUtils http = new HttpUtils();
								HttpHandler<File> handler = http.download(downloadInfo.getDownloadUrl(),
										downloadInfo.getFileSavePath(),false,false,callBack);
								downloadInfo.setHandler(handler);
								downloadInfo.setState(handler.getState());
							}

							@Override
							public void onCancel() {

							}

						}, new DialogInterface.OnKeyListener() {
							@Override
							public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
								if (keyCode == KeyEvent.KEYCODE_BACK) {
									dialog.dismiss();
									return true;
								}
								return false;
							}
						});
					}
				}
			}

			@Override
			public void onFailure(HttpException arg0, String arg) {
				HttpHandler<File> handler = downloadInfo.getHandler();
				if (handler != null) {
					downloadInfo.setState(handler.getState());
				}
				mAif.hideProgressDialog();
				mAif.showAlert(R.string.apk_version_failure);
			}

			@Override
			public void onLoading(long total, long current, boolean isUploading) {
				HttpHandler<File> handler = downloadInfo.getHandler();
				if (handler != null) {
					downloadInfo.setState(handler.getState());
				}
			}

		};

		HttpUtils http = new HttpUtils();
		HttpHandler<File> handler = http.download(downloadInfo.getDownloadUrl(), downloadInfo.getFileSavePath(),false,false,requestCallBack);
		downloadInfo.setHandler(handler);
		downloadInfo.setState(handler.getState());
	}

	private OnProviderListener mAppsListener = new OnProviderListener() {

		@Override
		public void onProviderResponse(int requestCode, int responseCode, ProviderResult result) {
			if(mLoadingDialog != null) mLoadingDialog.dismiss();
			if (responseCode == -1) {
				mAif.showAlert(R.string.net_unconnect_imp_label);
			} else {
				if (Configs.REQUEST_CODE_APPSTOR_CHECK_VERSION == requestCode) {
					try {
						JSONObject obj = new JSONObject(result.getResponseStr());
						int status = obj.getInt("status");
						if (status != 200) {
							if (status == 206) {
								mAif.showAlert(R.string.seting_version_alert);
							} else {
								if (obj.has("msg")) {
									mAif.showAlert(obj.getString("msg"));
								}
							}
						}else{
							if(obj.has("data") && !obj.isNull("data")) {
								obj = obj.getJSONObject("data");
								final String apk_path=obj.getString("apk_path");
								final String apk_md5=obj.getString("md5");

								Resources resources = mContext.getResources();
								String content = "";
								if (CommonUtil.isWifiAvailable(mContext)) { //wifi下载
									content =  resources.getString(R.string.net_version_update);
								} else { //当用户使用2G/3G/4G网络下载
									content = MainApplication.mAppPreferce.netWorkAllow ? resources.getString(R.string.net_version_update_workAllow_open)
											: resources.getString(R.string.net_version_update_workAllow_close);
								}
								((MainActivity) mAif).showDialog("温馨提示", content, "确定", "取消", new OnDialogListener() {
									@Override
									public void onOk() {
										updateVersion(apk_path,apk_md5);
									}

									@Override
									public void onCancel() {

									}
								}, new DialogInterface.OnKeyListener() {
									@Override
									public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
										if (keyCode == KeyEvent.KEYCODE_BACK) {
											dialog.dismiss();
											return true;
										}
										return false;
									}
								});
							}
						}
					} catch (Exception e1) {
						mAif.showAlert("解析数据出错！");
					}
				}
			}
		}

		@Override
		public void onReadResponse(int arg0, int arg1) {

		}
	};
	
	@Override
	public int getMyViewPosition() {
		return Configs.VIEW_POSITION_MANNGER_SETING;
	}

}
