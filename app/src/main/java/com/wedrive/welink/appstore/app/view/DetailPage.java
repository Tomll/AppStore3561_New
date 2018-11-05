package com.wedrive.welink.appstore.app.view;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.TextView;

import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.util.LogUtils;
import com.mapbar.android.model.ActivityInterface;
import com.mapbar.android.model.BasePage;
import com.mapbar.android.model.CommandInfo;
import com.mapbar.android.model.FilterObj;
import com.mapbar.android.model.OnDialogListener;
import com.mapbar.android.model.OnProviderListener;
import com.mapbar.android.model.ProviderResult;
import com.mapbar.android.net.MyHttpHandler;
import com.mapbar.android.provider.Provider;
import com.mapbar.android.statistics.api.MapbarMobStat;
import com.wedrive.welink.appstore.Configs;
import com.wedrive.welink.appstore.MainActivity;
import com.wedrive.welink.appstore.MainApplication;
import com.wedrive.welink.appstore.R;
import com.wedrive.welink.appstore.app.download.DownloadInfo;
import com.wedrive.welink.appstore.app.download.DownloadManager;
import com.wedrive.welink.appstore.app.model.AppBean;
import com.wedrive.welink.appstore.app.model.AppDetails;
import com.wedrive.welink.appstore.app.model.AppInfo;
import com.wedrive.welink.appstore.app.provider.SearchProvider;
import com.wedrive.welink.appstore.app.util.AppUtil;
import com.wedrive.welink.appstore.app.util.CommonUtil;

import org.json.JSONObject;

import java.io.File;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.List;

public class DetailPage extends BasePage implements OnClickListener {

	private final static String TAG = "DetailPage";
	private Context mContext;
	private ActivityInterface mAif;
	private View mView;

	private View lv_detail;
	private View lv_common;

	private View currentMenu;
	private View currentContent;

	private DetailPageDetail mDetailPageDetail;
	private DetailPageCommon mDetailPageCommon;

	private RadioButton txt_detail, txt_common;
	private ImageView imageView_app_icon;
	private ProgressBar lcb_download_progress;
	private RatingBar rtb_app_class;
	private TextView tv_details_appTitle, tv_details_download_times, tv_details_app_size;

	private String app_id;
	private String app_version_id;
	private DownloadInfo downloadInfo;
	private AppDetails mAppDetails;
	private int detailFlag=10;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case 11:
					if(mAppDetails!=null) setAppDetail(mAppDetails);
					break;
				case 12:
					mView.findViewById(R.id.imgView_details_download).performClick();
					break;
				case 13:
					mView.findViewById(R.id.imgView_details_reset).performClick();
					break;
			}
		}
	};

	public DetailPage(Context context, View view, ActivityInterface aif) {
		super(context, view, aif);
		mContext = context;
		mView = view;
		mAif = aif;
		initView(view);
	}

	private void initView(View view) {
		view.findViewById(R.id.imgView_details_download).setOnClickListener(this);
		view.findViewById(R.id.imgView_details_pause).setOnClickListener(this);
		view.findViewById(R.id.imgView_details_reset).setOnClickListener(this);
		view.findViewById(R.id.imgView_details_start).setOnClickListener(this);
		view.findViewById(R.id.imgView_details_instal).setOnClickListener(this);
		view.findViewById(R.id.imgView_details_open).setOnClickListener(this);
		view.findViewById(R.id.imgView_details_update).setOnClickListener(this);

		txt_detail = (RadioButton) view.findViewById(R.id.txt_detail);
		txt_common = (RadioButton) view.findViewById(R.id.txt_common);

		lv_detail = view.findViewById(R.id.lv_detail);
		lv_common = view.findViewById(R.id.lv_common);

		txt_detail.setOnClickListener(this);
		txt_common.setOnClickListener(this);

		imageView_app_icon = (ImageView) view.findViewById(R.id.imageView_app_icon);
		rtb_app_class = (RatingBar) view.findViewById(R.id.rtb_app_class);
		tv_details_appTitle = (TextView) view.findViewById(R.id.tv_details_appTitle);
		tv_details_download_times = (TextView) view.findViewById(R.id.tv_details_download_times);
		tv_details_app_size = (TextView) view.findViewById(R.id.tv_details_app_size);
		lcb_download_progress = (ProgressBar) view.findViewById(R.id.lcb_download_progress);

		mDetailPageDetail = new DetailPageDetail(lv_detail, mAif, mContext);
		mDetailPageCommon = new DetailPageCommon(lv_common, mAif, mContext);

		currentMenu = txt_detail;
		currentContent = lv_detail;
		currentMenu.setSelected(true);
	}

	@Override
	public void viewWillAppear(int flag) {
		super.viewWillAppear(flag);
		//9MapbarMobStat.onPageStart(mContext,Configs.AppStore_Interface_DetailPage);
	}

	@Override
	public void viewWillDisappear(int flag) {
		super.viewWillDisappear(flag);
		//9MapbarMobStat.onPageEnd(mContext,Configs.AppStore_Interface_DetailPage);
	}

	@Override
	public void onResume() {
		super.onResume();
		//8MapbarMobStat.onPageStart(mContext,Configs.AppStore_Interface_DetailPage);
	}

	@Override
	public void onPause() {
		super.onPause();
		//8MapbarMobStat.onPageEnd(mContext,Configs.AppStore_Interface_DetailPage);
	}

	@Override
	public void setFilterObj(int flag, FilterObj filter) {
		super.setFilterObj(flag, filter);
		detailFlag=filter.getFlag();
		setDetailDetail(detailFlag);
		if (filter != null && filter.getTag() != null) {
			Object tag = filter.getTag();
			if (List.class.isInstance(tag)) {
				List<String> objs=(List<String>)tag;
				if(objs!=null && objs.size()>0){
					loadAppDetail(objs);
				}
			}
		}
	}

	@Override
	public void onReceiveData(int arg0, int code, Object obj) {
		super.onReceiveData(arg0, code, obj);
		if (arg0 == getMyViewPosition()) {
			if(code == 10 && obj != null){
				if (List.class.isInstance(obj)) {
					detailFlag=10;
					txt_detail.performClick();
					List<String> objs=(List<String>)obj;
					if(objs!=null && objs.size()>0){
						loadAppDetail(objs);
					}
				}
			}
		}
	}

	private void setDetailDetail(int flag){
		switch (flag){
			case 10:
				MainActivity.mMainActivity.setFirstAndSecondTitle("详情", "推荐");
				break;
			case 11:
				MainActivity.mMainActivity.setFirstAndSecondTitle("详情", "搜索");
				break;
			case 12:
				MainActivity.mMainActivity.setFirstAndSecondTitle("详情", "榜单");
				break;
			default:
				MainActivity.mMainActivity.setFirstAndSecondTitle("详情", "推荐");
				break;
		}
	}

	private void setDetailCommon(int flag){
		switch (flag){
			case 10:
				MainActivity.mMainActivity.setFirstAndSecondTitle("评论", "推荐");
				break;
			case 11:
				MainActivity.mMainActivity.setFirstAndSecondTitle("评论", "搜索");
				break;
			case 12:
				MainActivity.mMainActivity.setFirstAndSecondTitle("评论", "榜单");
				break;
			default:
				MainActivity.mMainActivity.setFirstAndSecondTitle("评论", "推荐");
				break;
		}
	}


	/**
	 * 在不同情况下请求加载显示数据
	 * @param objs
     */

	private void loadAppDetail(List<String> objs){
		try{
			app_id = objs.get(0);
			if(objs.size()==1){
				AppBean mAppBean = MainApplication.dbUtils.findFirst(Selector.from(AppBean.class).where("app_id", "=", app_id));
				if(mAppBean!=null) app_version_id=mAppBean.getApp_version_id();
			}

			if(objs.size()==2){
				app_version_id = objs.get(1);
			}

			if(!TextUtils.isEmpty(app_id) && !TextUtils.isEmpty(app_version_id)){
				downloadInfo = MainApplication.downloadManager.isAppLoading(app_id);
				mAppDetails = MainApplication.dbUtils.findFirst(Selector.from(AppDetails.class).where("app_id", "=", app_id));
				if(CommonUtil.isNetworkAvailable(mContext)){//网络可用状态下
					if(mAppDetails!=null){//如果详情数据不为空
						if(downloadInfo==null){//如果没有下载该应用并且在有网络的情况下，重新加载应用详情数据
							getAppDetail4Net(app_id);
						}else if(downloadInfo.isLoadSuccess() || downloadInfo.getProgress()==100){//下载完毕重新加载应用详情数据
							getAppDetail4Net(app_id);
						}else{//下载中加载缓存数据
							setAppDetail(mAppDetails);
						}
					}else{//如果详情数据为空，加载网络数据
						getAppDetail4Net(app_id);
					}
				}else{//网络不可用状态下
					if(mAppDetails!=null) setAppDetail(mAppDetails);//如果有缓存数据加载缓存数据
				}
			}
		}catch(Exception e){
			Log.e("message","loadAppDetail exception:"+e.getMessage());
		}
	}

	/**
	 * 
	 * <p>
	 * 功能描述
	 * </p>
	 * q请求应用详情信息
	 * 
	 * @param app_id
	 * @author wangzhichao
	 * @date 2015年9月16日
	 */

	public void getAppDetail4Net(String app_id) {
		SearchProvider provider = new SearchProvider(mContext);
		provider.setOnProviderListener(mAppsListener);
		LinkedHashMap<String, String> paramMap = new LinkedHashMap<String, String>();
		paramMap.put("os_version", "" + android.os.Build.VERSION.SDK_INT);
		paramMap.put("id", app_id);
		MyHttpHandler myHttpHandler = provider.loadAppDetail(paramMap);
		mAif.showProgressDialog(myHttpHandler, R.string.dialog_loading_data, true);
	}

	/**
	 * 
	 * <p>
	 * 功能描述
	 * </p>
	 * 设置显示应用的详情信息
	 * 
	 * @param bean
	 * @author wangzhichao
	 * @date 2015年9月6日
	 */

	private void setAppDetail(AppDetails bean) {
		MainApplication.imageLoader.display(imageView_app_icon, bean.app_icon_path);
		rtb_app_class.setRating(bean.getApp_score_avg());
		tv_details_appTitle.setText(bean.getApp_name());
		tv_details_download_times.setText(bean.getApp_download_c() + "次下载");
		DecimalFormat df = new DecimalFormat("0.00");
		tv_details_app_size.setText(df.format(Double.parseDouble(bean.getApp_size())) + "MB");
		if (bean.getApp_official_flag()==1) {
			mView.findViewById(R.id.imageView_official).setVisibility(View.VISIBLE);
			mView.findViewById(R.id.imgView_details_recognised).setVisibility(View.VISIBLE);
			mView.findViewById(R.id.tv_wedrive_officially_recognised).setVisibility(View.VISIBLE);
		} else {
			mView.findViewById(R.id.imageView_official).setVisibility(View.GONE);
			mView.findViewById(R.id.imgView_details_recognised).setVisibility(View.GONE);
			mView.findViewById(R.id.tv_wedrive_officially_recognised).setVisibility(View.GONE);
		}

		mDetailPageDetail.setAppDetailDetail(bean);//详情模块
		mDetailPageCommon.setAppDetailDetail(bean, this, bean.app_id);//评论模块

		// 判断按钮显示状态

		if (downloadInfo != null) {
			refershLoadState(downloadInfo);
		} else {
			PackageInfo info = CommonUtil.isAppInstalled(mContext, bean.app_package_name);
			if (info != null) {// 已安装
				int oCode = info.versionCode;// 已安装的版本号
				int nCode = Integer.valueOf(bean.app_version_no);// 详情接口获取的版本号;
				if (nCode > oCode) {// 下发的版本号大于本地版本号，显示更新
					setButtonState(Configs.APP_BUTTON_STATUS_UPDATE);
				} else {// 下发的版本号小于或等于本地版本号，显示打开
					setButtonState(Configs.APP_BUTTON_STATUS_OPEN);
				}
			} else {// 没有安装
				String path = bean.app_apk_path.substring(bean.app_apk_path.lastIndexOf("/") + 1);
				path = MainApplication.apkDownloadPath + File.separator + path;
				File apk = new File(path);
				if (apk.exists() && AppUtil.getAPKInfo(mContext, apk.getAbsolutePath()) != null) {// 没有安装,有apk文件，显示安装
					setButtonState(Configs.APP_BUTTON_STATUS_INSTALL);
				} else {// 没有安装,没有apk文件，显示下载
					setButtonState(Configs.APP_BUTTON_STATUS_DOWNLOAD);
				}
			}
		}
	}

	/**
	 * 
	 * <p>
	 * 功能描述
	 * </p>
	 * 设置按钮显示状态
	 * 
	 * @param state
	 * @author wangzhichao
	 * @date 2015年9月6日
	 */

	private void setButtonState(int state) {
		switch (state) {
		case Configs.APP_BUTTON_STATUS_DOWNLOAD:
			mView.findViewById(R.id.imgView_details_download).setVisibility(View.VISIBLE);
			mView.findViewById(R.id.imgView_details_pause).setVisibility(View.GONE);
			mView.findViewById(R.id.imgView_details_reset).setVisibility(View.GONE);
			mView.findViewById(R.id.imgView_details_start).setVisibility(View.GONE);
			mView.findViewById(R.id.imgView_details_instal).setVisibility(View.GONE);
			mView.findViewById(R.id.imgView_details_open).setVisibility(View.GONE);
			mView.findViewById(R.id.imgView_details_update).setVisibility(View.GONE);
			mView.findViewById(R.id.lcb_download_progress).setVisibility(View.GONE);
			break;
		case Configs.APP_BUTTON_STATUS_PAUSE:
			mView.findViewById(R.id.imgView_details_download).setVisibility(View.GONE);
			mView.findViewById(R.id.imgView_details_pause).setVisibility(View.VISIBLE);
			mView.findViewById(R.id.imgView_details_reset).setVisibility(View.VISIBLE);
			mView.findViewById(R.id.imgView_details_start).setVisibility(View.GONE);
			mView.findViewById(R.id.imgView_details_instal).setVisibility(View.GONE);
			mView.findViewById(R.id.imgView_details_open).setVisibility(View.GONE);
			mView.findViewById(R.id.imgView_details_update).setVisibility(View.GONE);
			mView.findViewById(R.id.lcb_download_progress).setVisibility(View.VISIBLE);
			break;
		case Configs.APP_BUTTON_STATUS_RESET:
			mView.findViewById(R.id.imgView_details_download).setVisibility(View.VISIBLE);
			mView.findViewById(R.id.imgView_details_pause).setVisibility(View.GONE);
			mView.findViewById(R.id.imgView_details_reset).setVisibility(View.GONE);
			mView.findViewById(R.id.imgView_details_start).setVisibility(View.GONE);
			mView.findViewById(R.id.imgView_details_instal).setVisibility(View.GONE);
			mView.findViewById(R.id.imgView_details_open).setVisibility(View.GONE);
			mView.findViewById(R.id.imgView_details_update).setVisibility(View.GONE);
			mView.findViewById(R.id.lcb_download_progress).setVisibility(View.GONE);
			break;
		case Configs.APP_BUTTON_STATUS_START:
			mView.findViewById(R.id.imgView_details_download).setVisibility(View.GONE);
			mView.findViewById(R.id.imgView_details_pause).setVisibility(View.GONE);
			mView.findViewById(R.id.imgView_details_reset).setVisibility(View.VISIBLE);
			mView.findViewById(R.id.imgView_details_start).setVisibility(View.VISIBLE);
			mView.findViewById(R.id.imgView_details_instal).setVisibility(View.GONE);
			mView.findViewById(R.id.imgView_details_open).setVisibility(View.GONE);
			mView.findViewById(R.id.imgView_details_update).setVisibility(View.GONE);
			mView.findViewById(R.id.lcb_download_progress).setVisibility(View.VISIBLE);
			break;
		case Configs.APP_BUTTON_STATUS_INSTALL:
			mView.findViewById(R.id.imgView_details_download).setVisibility(View.GONE);
			mView.findViewById(R.id.imgView_details_pause).setVisibility(View.GONE);
			mView.findViewById(R.id.imgView_details_reset).setVisibility(View.GONE);
			mView.findViewById(R.id.imgView_details_start).setVisibility(View.GONE);
			mView.findViewById(R.id.imgView_details_instal).setVisibility(View.VISIBLE);
			mView.findViewById(R.id.imgView_details_open).setVisibility(View.GONE);
			mView.findViewById(R.id.imgView_details_update).setVisibility(View.GONE);
			mView.findViewById(R.id.lcb_download_progress).setVisibility(View.GONE);
			break;
		case Configs.APP_BUTTON_STATUS_OPEN:
			mView.findViewById(R.id.imgView_details_download).setVisibility(View.GONE);
			mView.findViewById(R.id.imgView_details_pause).setVisibility(View.GONE);
			mView.findViewById(R.id.imgView_details_reset).setVisibility(View.GONE);
			mView.findViewById(R.id.imgView_details_start).setVisibility(View.GONE);
			mView.findViewById(R.id.imgView_details_instal).setVisibility(View.GONE);
			mView.findViewById(R.id.imgView_details_open).setVisibility(View.VISIBLE);
			mView.findViewById(R.id.imgView_details_update).setVisibility(View.GONE);
			mView.findViewById(R.id.lcb_download_progress).setVisibility(View.GONE);
			break;
		case Configs.APP_BUTTON_STATUS_UPDATE:
			mView.findViewById(R.id.imgView_details_download).setVisibility(View.GONE);
			mView.findViewById(R.id.imgView_details_pause).setVisibility(View.GONE);
			mView.findViewById(R.id.imgView_details_reset).setVisibility(View.GONE);
			mView.findViewById(R.id.imgView_details_start).setVisibility(View.GONE);
			mView.findViewById(R.id.imgView_details_instal).setVisibility(View.GONE);
			mView.findViewById(R.id.imgView_details_open).setVisibility(View.GONE);
			mView.findViewById(R.id.imgView_details_update).setVisibility(View.VISIBLE);
			mView.findViewById(R.id.lcb_download_progress).setVisibility(View.GONE);
			break;
		}
	}

	/**
	 * 
	 * <p>
	 * 功能描述
	 * </p>
	 * 下载apk文件
	 * 
	 * @param bean
	 * @author wangzhichao
	 * @date 2015年9月6日
	 */

	private void loadApk(AppDetails bean) {
		try {
			downloadInfo = new DownloadInfo();
			String fileName = bean.app_apk_path.substring(bean.app_apk_path.lastIndexOf("/") + 1);
			String path = MainApplication.apkDownloadPath + File.separator + fileName;
			Log.d(TAG, "loadApk: -----<<<>>>>>>> "+path);
			File apk = new File(path);
			if (apk.exists()) apk.delete();
			downloadInfo.setSize(Double.parseDouble(bean.app_size));
			downloadInfo.setAppId(bean.app_id);
			downloadInfo.setLogoUrl(bean.app_icon_path);
			downloadInfo.setOfficial_flag(bean.app_official_flag);
			downloadInfo.setDownloadUrl(bean.app_apk_path);
			downloadInfo.setAutoRename(false);
			downloadInfo.setAutoResume(true);
			downloadInfo.setFileName(bean.app_name);
			downloadInfo.setFileSavePath(path);
			downloadInfo.setApp_v_id(app_version_id);
			downloadInfo.setMd5(bean.getApp_md5());
			MainApplication.downloadManager.addNewDownload(downloadInfo, new DownloadRequestCallBack());
			MainApplication.dbUtils.saveOrUpdate(new AppInfo(bean.getApp_id(), bean.getApp_package_name(), bean.getApp_apk_path()));
		} catch (DbException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * <p>
	 * 功能描述
	 * </p>
	 * 刷新下载状态
	 * 
	 * @param downloadInfo
	 * @author wangzhichao
	 * @throws DbException
	 * @date 2015年9月6日
	 */

	private void refershLoadState(DownloadInfo downloadInfo) {
		if (downloadInfo.isLoadSuccess() || downloadInfo.getProgress()==100) {
			downloadInfo.setLoadSuccess(true);
			setButtonState(Configs.APP_BUTTON_STATUS_INSTALL);
		} else {
			HttpHandler.State state = downloadInfo.getState();
			if(state!=null){
				switch (state) {
					case WAITING:
					case STARTED:
					case LOADING:
						setButtonState(Configs.APP_BUTTON_STATUS_PAUSE);
						try {
							if (downloadInfo.getFileLength() > 0) {
								lcb_download_progress.setProgress((int) (downloadInfo.getProgress() * 100 / downloadInfo.getFileLength()));
							} else {
								lcb_download_progress.setProgress(0);
							}
						} catch (Exception e1) {
							e1.printStackTrace();
						}
						break;
					case CANCELLED:
						setButtonState(Configs.APP_BUTTON_STATUS_START);
						try {
							if (downloadInfo.getFileLength() > 0) {
								lcb_download_progress.setProgress((int) (downloadInfo.getProgress() * 100 / downloadInfo.getFileLength()));
							} else {
								lcb_download_progress.setProgress(0);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						break;
					case FAILURE:
						setButtonState(Configs.APP_BUTTON_STATUS_START);
						try {
							MainApplication.downloadManager.stopDownload(downloadInfo);
							if (downloadInfo.getFileLength() > 0) {
								lcb_download_progress.setProgress((int) (downloadInfo.getProgress() * 100 / downloadInfo.getFileLength()));
							} else {
								lcb_download_progress.setProgress(0);
							}
						} catch (DbException e) {
							e.printStackTrace();
						}
						break;
					case SUCCESS:
						try {
							downloadInfo.setLoadSuccess(true);
							MainApplication.dbUtils.saveOrUpdate(downloadInfo);
							setButtonState(Configs.APP_BUTTON_STATUS_INSTALL);
						} catch (Exception e) {
							e.printStackTrace();
						}
						break;
				}
			}else{
				if(downloadInfo.getProgress()==100){
					setButtonState(Configs.APP_BUTTON_STATUS_INSTALL);
				}else{
					setButtonState(Configs.APP_BUTTON_STATUS_START);
					try {
						MainApplication.downloadManager.stopDownload(downloadInfo);
						if (downloadInfo.getFileLength() > 0) {
							lcb_download_progress.setProgress((int) (downloadInfo.getProgress() * 100 / downloadInfo.getFileLength()));
						} else {
							lcb_download_progress.setProgress(0);
						}
					} catch (DbException e) {}
				}
			}

			try {
				HttpHandler<File> handler = downloadInfo.getHandler();
				if(handler==null){
					MainApplication.downloadManager.resumeDownload(downloadInfo,new DownloadRequestCallBack());
					handler = downloadInfo.getHandler();
				}
				if (handler != null) {
					RequestCallBack callBack = handler.getRequestCallBack();
					if (callBack instanceof DownloadManager.ManagerCallBack) {
						DownloadManager.ManagerCallBack managerCallBack = (DownloadManager.ManagerCallBack) callBack;
						managerCallBack.setBaseCallBack(new DownloadRequestCallBack());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}
	
	/**
	 * 
	* @Title: checkLoadApk 
	* @Description: 依据网络状态对话框提示下载apk
	* @return void :
	* @author : wangzc
	* @date 2016年8月31日
	 */
	
	private void checkLoadApk(){
		if (!CommonUtil.isNetworkAvailable(mContext)) {
			mAif.showAlert(R.string.net_unconnect_imp_label);
			return;
		}

		if(CommonUtil.isWifiAvailable(mContext)){ //wifi下载
			setButtonState(Configs.APP_BUTTON_STATUS_PAUSE);
			loadApk(mAppDetails);
		}else{ //当用户使用2G/3G/4G网络下载
			Resources resources=mContext.getResources();
			String content=MainApplication.mAppPreferce.netWorkAllow ? resources.getString(R.string.net_is_workAllow_open)
					: resources.getString(R.string.net_is_workAllow_close);
			mAif.showDialog("温馨提示", content, "确定",  "取消",new OnDialogListener(){
				@Override
				public void onOk() {
					setButtonState(Configs.APP_BUTTON_STATUS_PAUSE);
					loadApk(mAppDetails);
				}
				
				@Override
				public void onCancel() {
					
				}
			});
		}
	}
	
	/**
	 * 
	* @Title: checkstartApk 
	* @Description: 依据网络状态对话框提示开始继续下载apk
	* @return void :
	* @author : wangzc
	* @date 2016年8月31日
	 */
	
	private void checkStartApk(){
		if (!CommonUtil.isNetworkAvailable(mContext)) {
			mAif.showAlert(R.string.net_unconnect_imp_label);
			return;
		}

		if(CommonUtil.isWifiAvailable(mContext)){ //wifi下载
			try {
				setButtonState(Configs.APP_BUTTON_STATUS_PAUSE);
				MainApplication.downloadManager.resumeDownload(downloadInfo, new DownloadRequestCallBack());
			} catch (DbException e) {
				e.printStackTrace();
			}		
		}else{ //当用户使用2G/3G/4G网络下载
			Resources resources=mContext.getResources();
			String content=MainApplication.mAppPreferce.netWorkAllow ? resources.getString(R.string.net_is_workAllow_open)
					: resources.getString(R.string.net_is_workAllow_close);
			mAif.showDialog("温馨提示", content, "确定",  "取消",new OnDialogListener(){
				@Override
				public void onOk() {
					try {
						setButtonState(Configs.APP_BUTTON_STATUS_PAUSE);
						MainApplication.downloadManager.resumeDownload(downloadInfo, new DownloadRequestCallBack());
					} catch (DbException e) {
						e.printStackTrace();
					}		
				}
				
				@Override
				public void onCancel() {
					
				}
			});
		}
	}

	/**
	 *
	 * @Title: checkstartApk
	 * @Description: 检测现在的apk包是否完整可用
	 * @return void :
	 * @author : wangzc
	 * @date 2016年12月20日
	 */

	private void checkApkMd5(){
		((MainActivity)mAif).showDialog("温馨提示", downloadInfo.getFileName()+"安装包已损坏，是否重新下载?", "重新下载", "取消", new OnDialogListener() {
			@Override
			public void onOk() {
				try {
					File file = new File(downloadInfo.getFileSavePath());
					if (file.exists()) file.delete();
					downloadInfo.setProgress(0);
					downloadInfo.setLoadSuccess(false);
					setButtonState(Configs.APP_BUTTON_STATUS_PAUSE);
					MainApplication.downloadManager.resumeDownload(downloadInfo, new DownloadRequestCallBack());
				} catch (Exception e) {
					LogUtils.e(e.getMessage(), e);
				}
			}

			@Override
			public void onCancel() {
				try {
					MainApplication.downloadManager.removeDownload(downloadInfo);
					PackageInfo info = CommonUtil.isAppInstalled(mContext, mAppDetails.app_package_name);
					if (info != null) {
						setButtonState(Configs.APP_BUTTON_STATUS_UPDATE);
					}else{
						setButtonState(Configs.APP_BUTTON_STATUS_DOWNLOAD);
					}
				} catch (Exception e) {
					LogUtils.e(e.getMessage(), e);
				}
			}
		},new DialogInterface.OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction()==KeyEvent.ACTION_DOWN) {
					dialog.dismiss();
					try {
						MainApplication.downloadManager.removeDownload(downloadInfo);
						PackageInfo info = CommonUtil.isAppInstalled(mContext, mAppDetails.app_package_name);
						if (info != null) {
							setButtonState(Configs.APP_BUTTON_STATUS_UPDATE);
						}else{
							setButtonState(Configs.APP_BUTTON_STATUS_DOWNLOAD);
						}
					} catch (Exception e) {
						LogUtils.e(e.getMessage(), e);
					}
					return true;
				}
				return false;
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.txt_detail:
			if (currentMenu != v) {
				setDetailDetail(detailFlag);
				currentMenu.setSelected(false);
				currentContent.setVisibility(View.GONE);
				v.setSelected(true);
				lv_detail.setVisibility(View.VISIBLE);
				currentMenu = v;
				currentContent = lv_detail;
				//((MainActivity)mAif).onClick_View_OnClick_Event(mContext,Configs.AppStore_OnClick_Details);
			}
			break;
		case R.id.txt_common:
			if (currentMenu != v) {
				setDetailCommon(detailFlag);
				currentMenu.setSelected(false);
				currentContent.setVisibility(View.GONE);
				v.setSelected(true);
				lv_common.setVisibility(View.VISIBLE);
				currentMenu = v;
				currentContent = lv_common;
				//((MainActivity)mAif).onClick_View_OnClick_Event(mContext,Configs.AppStore_OnClick_Comments);
			}
			break;
		case R.id.imgView_details_download:
			if(mAppDetails==null) {
				mAif.showAlert(R.string.no_apk_data_info);
				return;
			}
			checkLoadApk();
			// TODO: 2018/9/26 详情界面 下载app---
			MapbarMobStat.onEvent(mContext,"F0120","下载");
			break;
		case R.id.imgView_details_pause:
			if(mAppDetails==null) {
				mAif.showAlert(R.string.no_apk_data_info);
				return;
			}
			try {
				setButtonState(Configs.APP_BUTTON_STATUS_START);
				if (downloadInfo != null) {
					MainApplication.downloadManager.stopDownload(downloadInfo);
				}
			} catch (Exception e) {}
			// TODO: 2018/9/26 详情界面 暂停下载app
			MapbarMobStat.onEvent(mContext,"F0121","暂停");
			break;
		case R.id.imgView_details_reset:
			if(mAppDetails==null) {
				mAif.showAlert(R.string.no_apk_data_info);
				return;
			}
			try {
				if (downloadInfo != null) {
					File file = new File(downloadInfo.getFileSavePath());
					if (file.exists()) file.delete();
					MainApplication.downloadManager.removeDownload(downloadInfo);
					downloadInfo=null;
				}

				PackageInfo info = CommonUtil.isAppInstalled(mContext, mAppDetails.app_package_name);
				if (info != null) {
					setButtonState(Configs.APP_BUTTON_STATUS_UPDATE);
				}else{
					setButtonState(Configs.APP_BUTTON_STATUS_DOWNLOAD);
				}
			} catch (Exception e) {
				Log.e("message","exception:"+e.getMessage());
			}
			// TODO: 2018/9/26 详情界面取消下载app---
			MapbarMobStat.onEvent(mContext,"F0123","取消");
			break;
		case R.id.imgView_details_start:
			if(mAppDetails==null) {
				mAif.showAlert(R.string.no_apk_data_info);
				return;
			}
			checkStartApk();
			// TODO: 2018/9/26 详情界面继续下载app---
			MapbarMobStat.onEvent(mContext,"F0122","开始");
			break;
		case R.id.imgView_details_instal:
			if(mAppDetails==null) {
				mAif.showAlert(R.string.no_apk_data_info);
				return;
			}
			String path = mAppDetails.app_apk_path.substring(mAppDetails.app_apk_path.lastIndexOf("/") + 1);
			path = MainApplication.apkDownloadPath + File.separator + path;
			File apk = new File(path);
			if (apk.exists()) {
				AppUtil.installApp(mContext, apk);
			}else{
				mAif.showDialog("温馨提示", "该安装包已删除，是否继续下载?", "确定", "取消", new OnDialogListener() {
					@Override
					public void onOk() {
						lcb_download_progress.setProgress(0);
						setButtonState(Configs.APP_BUTTON_STATUS_PAUSE);
						try {
							if(downloadInfo!=null){
								downloadInfo.setLoadSuccess(false);
								downloadInfo.setProgress(0);
								DownloadRequestCallBack callBack=new DownloadRequestCallBack();
								MainApplication.downloadManager.resumeDownload(downloadInfo,callBack);
							}else{
								loadApk(mAppDetails);
							}
						} catch (Exception e) {
							LogUtils.e(e.getMessage(), e);
						}
					}

					@Override
					public void onCancel() {
						try {
							if(downloadInfo!=null){
								MainApplication.downloadManager.removeDownload(downloadInfo);
							}
							PackageInfo info = CommonUtil.isAppInstalled(mContext, mAppDetails.app_package_name);
							if (info != null) {
								setButtonState(Configs.APP_BUTTON_STATUS_UPDATE);
							}else{
								setButtonState(Configs.APP_BUTTON_STATUS_DOWNLOAD);
							}
						} catch (Exception e) {
							LogUtils.e(e.getMessage(), e);
						}
					}
				});
				
			}
			break;
		case R.id.imgView_details_open:
			if(mAppDetails==null) {
				mAif.showAlert(R.string.no_apk_data_info);
				return;
			}
			String scheme = mAppDetails.app_uri;
			String packageName = mAppDetails.app_package_name;
			String appName=mAppDetails.app_name;
			if(!TextUtils.isEmpty(scheme)) scheme+=":";
			else scheme=CommonUtil.chooseUriToPackageName(packageName);
			((MainActivity)mAif).startApp_AppStoreStart_Event(mContext,packageName,scheme,appName);
			break;
		case R.id.imgView_details_update:
			if(mAppDetails==null) {
				mAif.showAlert(R.string.no_apk_data_info);
				return;
			}
			checkLoadApk();
			break;
		}
	}

	private class DownloadRequestCallBack extends RequestCallBack<File> {

		@Override
		public void onStart() {
			setButtonState(Configs.APP_BUTTON_STATUS_PAUSE);
			try {
				if (downloadInfo != null) {
					if (downloadInfo.getFileLength() > 0) {
						lcb_download_progress.setProgress((int) (downloadInfo.getProgress() * 100 / downloadInfo.getFileLength()));
					} else {
						lcb_download_progress.setProgress(0);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}			
		}
		
		public void onLoading(long total, long current, boolean isUploading) {
			setButtonState(Configs.APP_BUTTON_STATUS_PAUSE);
			try {
				lcb_download_progress.setProgress((int) (current * 100 / (double) total));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onSuccess(ResponseInfo<File> arg0) {
			setButtonState(Configs.APP_BUTTON_STATUS_INSTALL);
			try {
				if (downloadInfo != null && !downloadInfo.isLoadSuccess) {
					downloadInfo.setLoadSuccess(true);
		            MainApplication.dbUtils.saveOrUpdate(downloadInfo);
		            sendLoadSuccessLog(downloadInfo.getAppId(), downloadInfo.getApp_v_id());
		            File file = new File(downloadInfo.getFileSavePath());
					if (file.exists()) {
						if(CommonUtil.verifyFileMD5(file,downloadInfo.getMd5())){//校验通过
							AppUtil.installApp(mContext, file);
						}else{//校验失败
							file.delete();
							checkApkMd5();
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onFailure(HttpException error, String arg1) {
			if(error.getExceptionCode()==416){
				setButtonState(Configs.APP_BUTTON_STATUS_INSTALL);
				try {
					if (downloadInfo != null && !downloadInfo.isLoadSuccess) {
						downloadInfo.setLoadSuccess(true);
						MainApplication.dbUtils.saveOrUpdate(downloadInfo);
						File file = new File(downloadInfo.getFileSavePath());
						if (file.exists()) AppUtil.installApp(mContext, file);
						sendLoadSuccessLog(downloadInfo.getAppId(), downloadInfo.getApp_v_id());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else{
				setButtonState(Configs.APP_BUTTON_STATUS_START);
                //Bug #14765：若下载过程中网络错误，弹窗提示
                mAif.showAlert(R.string.dialog_loading_net_error);
				try {
					if (downloadInfo != null) {
						if (downloadInfo.getFileLength() > 0) {
							lcb_download_progress.setProgress((int) (downloadInfo.getProgress() * 100 / downloadInfo.getFileLength()));
						} else {
							lcb_download_progress.setProgress(0);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public void onCancelled() {
			setButtonState(Configs.APP_BUTTON_STATUS_START);
			try {
				if (downloadInfo != null) {
					if (downloadInfo.getFileLength() > 0) {
						lcb_download_progress.setProgress((int) (downloadInfo.getProgress() * 100 / downloadInfo.getFileLength()));
					} else {
						lcb_download_progress.setProgress(0);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	private OnProviderListener mAppsListener = new OnProviderListener() {

		@Override
		public void onProviderResponse(int requestCode, int responseCode, ProviderResult result) {
			mAif.hideProgressDialog();
			if(responseCode == Provider.RESULT_ERROR) mAif.showAlert(R.string.dialog_loading_net_error);
			switch (requestCode) {
				case Configs.REQUEST_CODE_LOAD_APP_DETAIL:
					try {
						JSONObject obj = new JSONObject(result.getResponseStr());
						int status = obj.getInt("status");
						if (status != 200) {
							mAif.showAlert(obj.getString("msg"));
						}else{
							obj = obj.getJSONObject("data");
							AppDetails bean = new AppDetails();

							if (obj.has("app_id"))
								bean.setApp_id(obj.getString("app_id"));
							if (obj.has("name"))
								bean.setApp_name(obj.getString("name"));
							if (obj.has("developer"))
								bean.setApp_developer(obj.getString("developer"));
							if (obj.has("official_flag"))
								bean.setApp_official_flag(obj.getInt("official_flag"));
							if (obj.has("update_time"))
								bean.setApp_update_time(obj.getString("update_time"));
							if (obj.has("download_c"))
								bean.setApp_comments_c(obj.getString("comments_c"));
							if (obj.has("comments_c"))
								bean.setApp_download_c(obj.getString("download_c"));
							if (obj.has("score_avg")){
								String string = obj.getString("score_avg");
								if (TextUtils.isEmpty(string)) {
									bean.setApp_score_avg(0);
								}else{
									bean.setApp_score_avg(Float.valueOf(string));
								}
							}
							if (obj.has("md5"))
								bean.setApp_md5(obj.getString("md5"));
							if (obj.has("size"))
								bean.setApp_size(obj.getString("size"));
							if (obj.has("version_no"))
								bean.setApp_version_no(obj.getString("version_no"));
							if (obj.has("version_name"))
								bean.setApp_version_name(obj.getString("version_name"));
							if (obj.has("package_name"))
								bean.setApp_package_name(obj.getString("package_name"));
							if (obj.has("apk_path"))
								bean.setApp_apk_path(obj.getString("apk_path"));
							if (obj.has("icon_path"))
								bean.setApp_icon_path(obj.getString("icon_path"));
							if (obj.has("image_path"))
								bean.setApp_image_path(obj.getString("image_path"));
							if (obj.has("language_type"))
								bean.setApp_language_type(obj.getString("language_type"));
							if (obj.has("description"))
								bean.setApp_description(obj.getString("description"));
							if (obj.has("brief_desc"))
								bean.setApp_brief_desc(obj.getString("brief_desc"));
							if (obj.has("update_desc"))
								bean.setApp_update_desc(obj.getString("update_desc"));
							if (obj.has("scheme"))
								bean.setApp_uri(obj.getString("scheme"));

							mAppDetails = bean;
							MainApplication.dbUtils.saveOrUpdate(mAppDetails);
						}
					} catch (Exception e) {
						mAif.showAlert("解析数据出错！");
					}finally {
						Message message = handler.obtainMessage();
						message.what = 11;
						handler.sendMessage(message);
					}
					break;
				case Configs.REQUEST_CODE_RECORD_LOG_LIST:
					try {
						JSONObject obj = new JSONObject(result.getResponseStr());
						if (200==obj.getInt("status")) {
							int count = Integer.parseInt(mAppDetails.getApp_download_c()) + 1;
							mAppDetails.setApp_download_c("" + count);
							MainApplication.dbUtils.saveOrUpdate(mAppDetails);
							tv_details_download_times.setText(count + "次下载");
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
			}
		}

		@Override
		public void onReadResponse(int arg0, int arg1) {

		}

	};

	@Override
	public void onCommandReceive(CommandInfo ci) {
		super.onCommandReceive(ci);
		try {
			if(mAppDetails==null) return;
			if ("addedPackage".equals(ci.getMethod()) && ci.getExtData() != null) {
				Intent intent = (Intent) ci.getExtData();
				String packageName = intent.getData().getSchemeSpecificPart();
				if (mAppDetails.app_package_name.equals(packageName)) {
					setButtonState(Configs.APP_BUTTON_STATUS_OPEN);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
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

	/**
	 * 发送下载成功的日志
	 * 
	 * @param app_v_id
	 * @param app_id
	 */
	private void sendLoadSuccessLog(String app_id, String app_v_id) {
		if (CommonUtil.isNetworkAvailable(mContext)) {
			SearchProvider provider = new SearchProvider(mContext);
			provider.setOnProviderListener(mAppsListener);
			LinkedHashMap<String, String> paramMap = new LinkedHashMap<String, String>();
			paramMap.put("app_id", app_id);
			paramMap.put("app_v_id", app_v_id);
			provider.loadRecordLogs(paramMap);
		}
	}
	
	@Override
	public int getMyViewPosition() {
		return Configs.VIEW_POSITION_DETAIL;
	}

}
