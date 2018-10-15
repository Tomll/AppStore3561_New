package com.wedrive.welink.appstore.app.control;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.mapbar.android.control.AppActivity;
import com.mapbar.android.model.FilterObj;
import com.mapbar.android.model.OnDialogListener;
import com.mapbar.android.model.OnProviderListener;
import com.mapbar.android.model.PageObject;
import com.mapbar.android.model.ProviderResult;
import com.mapbar.android.net.MyHttpHandler;
import com.wedrive.welink.appstore.Configs;
import com.wedrive.welink.appstore.MainActivity;
import com.wedrive.welink.appstore.MainApplication;
import com.wedrive.welink.appstore.R;
import com.wedrive.welink.appstore.app.download.DownloadInfo;
import com.wedrive.welink.appstore.app.provider.SearchProvider;
import com.wedrive.welink.appstore.app.util.AppUtil;
import com.wedrive.welink.appstore.app.util.CommonUtil;
import com.wedrive.welink.appstore.app.view.HomePage;

public class MainController {
	
	private AppActivity mBaseActivity;
	private PageManager mPageManager;
	private TitleBarManager mTitleBarManager;

	private boolean isFinishInitView = false;
	private boolean isFinishInit = false;
	private boolean isDetailPage = false;
	private boolean isShowDetail = false;
	private List<String> objs=new ArrayList<String>();
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 10:
				objs=(List<String>)msg.obj;
				if(isFinishInit && objs!=null) {
					showDetail();
				}
				break;
			case 11:
				postDelayed(new Runnable() {
					@Override
					public void run() {
						initView();
					}
				}, 500);
				break;
			}
		}
	};

	public MainController(AppActivity activity) {
		this.mBaseActivity = activity;
	}

	
	/**
	 * 
	* @Title: initView 
	* @Description: 正常加载home界面
	* @return void :
	* @author : wangzc
	* @date 2016年9月8日
	 */
	
	private void initView(){
		mBaseActivity.setContentView(R.layout.layout_main);
		mPageManager = new PageManager(mBaseActivity, mBaseActivity);
		mTitleBarManager = new TitleBarManager(mBaseActivity,mBaseActivity.findViewById(R.id.ind_title),mBaseActivity);
		if(isDetailPage){
			FilterObj filter = new FilterObj();		
			View homeView = LayoutInflater.from(mBaseActivity).inflate(R.layout.layout_home, null);
			HomePage homePage = new HomePage(mBaseActivity, homeView, mBaseActivity);
			PageObject homeObject = new PageObject(Configs.VIEW_POSITION_HOME,homeView,homePage);
			mBaseActivity.pushPage(homeObject, Configs.VIEW_FLAG_NONE, filter);
			if(objs!=null){
				showDetail();
			}
		}else{
			FilterObj filter = new FilterObj();
			mBaseActivity.showPage(Configs.VIEW_POSITION_NONE,Configs.VIEW_POSITION_HOME, filter,null,null);
		}
		isFinishInit = true;
	}

	/**
	 * 方法名称：showDetail
	 * 方法描述：显示详情数据界面
	 * 方法参数：
	 * 返回类型：
	 * 创建人：wangzc
	 * 创建时间：2017/5/8 15:03
	*/

	private void showDetail(){
		if(isShowDetail){
			return;
		}
		if(Configs.VIEW_POSITION_DETAIL==mBaseActivity.getCurrentPagePosition()){//如果在详情界面直接把参数信息发送给详情界面
			mBaseActivity.sendToPage(Configs.VIEW_POSITION_DETAIL,10, objs);
		}else{//如果不在详情界面通过跳转方式传递参数
			FilterObj detail = new FilterObj();
			detail.setFlag(10);
			detail.setTag(objs);
			mBaseActivity.showPage(mBaseActivity.getCurrentPagePosition(), Configs.VIEW_POSITION_DETAIL, detail,true,null,null);
		}
		isShowDetail = true;
		objs=null;
	}


	public void onResume(int flag) {
		if (!isFinishInitView) {
			isFinishInitView = true;
			if (!isDetailPage && CommonUtil.isNetworkAvailable(mBaseActivity) && MainApplication.isFirst) {//首次启动检查版本更新
				MainApplication.isFirst=false;
				checkVersion();
			}else{
				initView();
			}
		}
	}

	public void onNewIntent(Intent intent){
		if(intent != null && intent.hasExtra("package_name")){//接收第三方发送来的包名参数
			String package_name = intent.getStringExtra("package_name");
			if(!TextUtils.isEmpty(package_name)){//如果包名参数不为空，依据包名加载该应用的版本号
				isDetailPage = true;
				isShowDetail = false;
				objs=null;
				getAppDetail(package_name);
			}
		}
	}
	
	public void onResume() {

	}

	public void onPause() {

	}

	public PageObject createPage(int index) {
		return mPageManager.createPage(index);
	}
	
	public void setTitle(View view, int titleID) {		
		mTitleBarManager = new TitleBarManager(mBaseActivity, view,mBaseActivity);
	}

	public void setTitleCustomView(View view, View customView) {
		mTitleBarManager = new TitleBarManager(mBaseActivity, view, mBaseActivity, customView);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (!isFinishInit){
			return true;
		}			
		return false;
	}

	public void dismissTitle(){
		if(mTitleBarManager != null) mTitleBarManager.dismissTitle();
	}

	public void setFirstTitle(String title) {
		if(mTitleBarManager != null) mTitleBarManager.setFirstTitle(title);
	}

	public void setFirstAndSecondTitle(String firstTitle, String secondTitle) {
		if(mTitleBarManager != null) mTitleBarManager.setFirstAndSecondTitle(firstTitle, secondTitle);
	}
	
	public void setAllTitle(String firstTitle, String secondTitle, String thirdTitle) {
		if(mTitleBarManager != null) mTitleBarManager.setAllTitle(firstTitle, secondTitle,thirdTitle);
	}
	
	public void setTitleOnClickListener(OnClickListener leftOnClickListener,OnClickListener rightOnClickListener) {
		if(mTitleBarManager != null) mTitleBarManager.setTitleOnClickListener(leftOnClickListener,rightOnClickListener);
	}

	public void setTitleDividerVisibile(boolean b) {
		if(mTitleBarManager != null) mTitleBarManager.setTitleDividerVisibile(b);
	}
	
	public void setTitleLoginVisibile(boolean isVisibile,Drawable drawable) {
		if(mTitleBarManager != null) mTitleBarManager.setTitleLoginVisibile(isVisibile,drawable);
	}
	
	public void setTitleLoginUserIcon(boolean isVisibile,String iconUrl) {
		if(mTitleBarManager != null) mTitleBarManager.setTitleLoginUserIcon(isVisibile, iconUrl);
	}
	
	public void recycleLoginUserIcon(){
		if(mTitleBarManager != null) mTitleBarManager.recycleLoginUserIcon();
	}
	
	/**
	 * 
	* @Title: getAppDetail 
	* @Description: 获取应用详情信息
	* @return void :
	* @author : wangzc
	* @date 2016年3月29日
	 */
	
	private void getAppDetail(String packageName){
		if (CommonUtil.isNetworkAvailable(mBaseActivity)) {
			SearchProvider provider = new SearchProvider(mBaseActivity);
			provider.setOnProviderListener(mAppsListener);
			LinkedHashMap<String, String> paramMap = new LinkedHashMap<String, String>();
			paramMap.put("os_version", "" + android.os.Build.VERSION.SDK_INT);
			LinkedHashMap<String, String> paramMap2 = new LinkedHashMap<String, String>();
			paramMap2.put("package_name", packageName);
			MyHttpHandler myHttpHandler = provider.loadInstalApps(paramMap, paramMap2);
			mBaseActivity.showProgressDialog(myHttpHandler,R.string.dialog_loading_data, true);
			provider.loadInstalApps(paramMap, paramMap2);
		}else{
			Message message = mHandler.obtainMessage();
			message.what = 10;
			message.obj = new ArrayList<String>();
			mHandler.sendMessage(message);
		}
	}
	
	/**
	 * 
	 * <p>功能描述</p>检查更新功能
	 * @author wangzhichao
	 * @date 2015年9月10日
	 */

	private void checkVersion(){
		try {
			PackageManager manager = mBaseActivity.getPackageManager();
			PackageInfo info = manager.getPackageInfo(mBaseActivity.getPackageName(), 0);
			SearchProvider provider = new SearchProvider(mBaseActivity);
			provider.setOnProviderListener(mAppsListener);
			LinkedHashMap<String, String> paramMap = new LinkedHashMap<String, String>();
			paramMap.put("os_version", ""+android.os.Build.VERSION.SDK_INT);
			paramMap.put("version_no", "" + info.versionCode);
			paramMap.put("package_name", info.packageName);
			final MyHttpHandler myHttpHandler = provider.checkVersion(paramMap);
			((MainActivity) mBaseActivity).showProgressDialog(R.string.check_update_version,new OnKeyListener() {
				@Override
				public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent evnet) {
					if (keyCode == KeyEvent.KEYCODE_BACK) {
						if(myHttpHandler != null) myHttpHandler.cancel(true);
						mBaseActivity.hideProgressDialog();
						initView();
						return false;
					}
					return false;
				}
			},true);
		} catch (Exception e) {
			mBaseActivity.hideProgressDialog();
			initView();
		}

	}
	
	/**
	 * 
	 * <p>功能描述</p>执行更新功能
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
				if(mBaseActivity!=null && !mBaseActivity.isFinishing()){
					((MainActivity) mBaseActivity).showProgressDialog(R.string.apk_version_update,new DialogInterface.OnKeyListener(){
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
			}

			public void onCancelled() {
				HttpHandler<File> handler = downloadInfo.getHandler();
				if (handler != null) {
					downloadInfo.setState(handler.getState());
				}
				if(mBaseActivity!=null && !mBaseActivity.isFinishing()){
					mBaseActivity.hideProgressDialog();
					mBaseActivity.finish();
				}

			}

			@Override
			public void onSuccess(ResponseInfo<File> result) {
				HttpHandler<File> handler = downloadInfo.getHandler();
				if (handler != null) {
					downloadInfo.setState(handler.getState());
				}
				if(mBaseActivity!=null && !mBaseActivity.isFinishing()){
					mBaseActivity.hideProgressDialog();
					File file=result.result;
					if(file!=null && file.exists()){
						if(CommonUtil.verifyFileMD5(file,apk_md5)){//校验通过
							AppUtil.installApp(mBaseActivity, file);
							mBaseActivity.finish();
						}else{//校验失败
							file.delete();
							final RequestCallBack callBack=this;
							((MainActivity) mBaseActivity).showDialog("温馨提示", "应用商店安装包已损坏，是否继续下载?", "重新下载", "取消", new OnDialogListener() {

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
									mBaseActivity.finish();
								}

							},new DialogInterface.OnKeyListener() {
								@Override
								public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
									if (keyCode == KeyEvent.KEYCODE_BACK) {
										dialog.dismiss();
										mBaseActivity.finish();
										return true;
									}
									return false;
								}
							});
						}
					}
				}
			}

			@Override
			public void onFailure(HttpException arg0, String arg) {
				HttpHandler<File> handler = downloadInfo.getHandler();
				if (handler != null) {
					downloadInfo.setState(handler.getState());
				}
				if(mBaseActivity!=null && !mBaseActivity.isFinishing()){
					mBaseActivity.hideProgressDialog();
					mBaseActivity.showAlert(R.string.apk_version_failure);
					mBaseActivity.finish();
				}
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
			if (mBaseActivity != null && !mBaseActivity.isFinishing()) {
				mBaseActivity.hideProgressDialog();
			}
			//BUG号：WELM-3258：bug链接：https://wdjira.mapbar.com/browse/WELM-3258
			if (result == null || TextUtils.isEmpty(result.getResponseStr()))
				return;
			Message message = mHandler.obtainMessage();
			switch (requestCode) {
				case Configs.REQUEST_CODE_APPSTOR_CHECK_VERSION:
					message.what = 11;
					try {
						JSONObject obj = new JSONObject(result.getResponseStr());
						if (obj.has("data") && !obj.isNull("data")) {
							obj = obj.getJSONObject("data");
							final String apk_path = obj.getString("apk_path");
							final String apk_md5 = obj.getString("md5");

							MainActivity mainActivity = (MainActivity) mBaseActivity;
							Resources resources = mainActivity.getResources();
							String content = "";
							if (CommonUtil.isWifiAvailable(mBaseActivity)) { //wifi下载
								content = resources.getString(R.string.net_version_update);
							} else { //当用户使用2G/3G/4G网络下载
								content = MainApplication.mAppPreferce.netWorkAllow ? resources.getString(R.string.net_version_update_workAllow_open)
										: resources.getString(R.string.net_version_update_workAllow_close);
							}
							mainActivity.showDialog("温馨提示", content, "确定", "取消", new OnDialogListener() {
								@Override
								public void onCancel() {
									initView();
								}

								@Override
								public void onOk() {
									updateVersion(apk_path, apk_md5);
								}
							}, new DialogInterface.OnKeyListener() {
								@Override
								public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
									if (keyCode == KeyEvent.KEYCODE_BACK) {
										initView();
										dialog.dismiss();
										return true;
									}
									return false;
								}
							});
						} else {
							mHandler.sendMessage(message);
						}
					} catch (Exception e1) {
						Log.e("message", "exception:" + e1.getMessage());
						mHandler.sendMessage(message);
					}
					break;
				case Configs.REQUEST_CODE_INSTAL_APPS_LIST:
					message.what = 10;
					message.obj = new ArrayList<String>();
					try {
						JSONObject obj = new JSONObject(result.getResponseStr());
						if (obj.has("data") && !obj.isNull("data")) {
							JSONArray jsonArr = obj.getJSONArray("data");
							obj = jsonArr.getJSONObject(0);
							List<String> objs = new ArrayList<String>();
							objs.add(obj.getString("app_id"));
							objs.add(obj.getString("version_id"));
							message.obj = objs;
						}
					} catch (JSONException e) {
						Log.e("message", "exception:" + e.getMessage());
					} finally {
						mHandler.sendMessage(message);
					}
					break;
			}
		}

		@Override
		public void onReadResponse(int arg0, int arg1) {

		}
	};

}
