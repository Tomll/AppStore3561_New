/**
 * 
 */
package com.wedrive.welink.appstore;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.exception.DbException;
import com.mapbar.android.model.CommandInfo;
import com.mapbar.android.statistics.api.MapbarMobStat;
import com.mapbar.scale.ScaleCalculator;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.wedrive.welink.appstore.app.download.DownloadInfo;
import com.wedrive.welink.appstore.app.download.DownloadManager;
import com.wedrive.welink.appstore.app.download.DownloadService;
import com.wedrive.welink.appstore.app.model.APPSearch;
import com.wedrive.welink.appstore.app.model.APPSearchHistory;
import com.wedrive.welink.appstore.app.model.APPSearchRecom;
import com.wedrive.welink.appstore.app.model.AppBannger;
import com.wedrive.welink.appstore.app.model.AppBean;
import com.wedrive.welink.appstore.app.model.AppComment;
import com.wedrive.welink.appstore.app.model.AppDetails;
import com.wedrive.welink.appstore.app.model.AppInfo;
import com.wedrive.welink.appstore.app.model.AppInstall;
import com.wedrive.welink.appstore.app.model.AppPreferce;
import com.wedrive.welink.appstore.app.model.AppVersion;
import com.wedrive.welink.appstore.app.model.HisbillboardBean;
import com.wedrive.welink.appstore.app.model.WeekbillboardBean;
import com.wedrive.welink.appstore.app.util.FileUtil;
import com.wedrive.welink.appstore.app.util.SdcardUtil;
import com.wedrive.welink.appstore.app.widget.ImageLoader;

import java.io.File;

public class MainApplication extends Application implements DbUtils.DbUpgradeListener {

	public static AppPreferce mAppPreferce;
	public static ImageLoader imageLoader;
	public static DbUtils dbUtils;
	public static DownloadManager downloadManager;	
	public static IWXAPI api;
	public static SharedPreferences mSharedPreferences;
	public static boolean appIsAccessGiven = false;
	public static boolean isRootAvailable = false;
	public static boolean isFirst=true;
	public static boolean isBanngerFirst=true;
	public static boolean isAppFirst=true;
	public static boolean isSearchFirst=true;
	public static boolean isListFirst=true;
	public static String downloadRootPath;
	public static String imageDownloadPath;
	public static String apkDownloadPath;
	public static String dataBaseloadPath;
	
	private static MainApplication instance;
	
	public static MainApplication getInstance() {
		return instance;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		ScaleCalculator.init(this, 0, 1024, 600, 1.5f);
		// 初始化统计服务
		MapbarMobStat.prestrain(this);
		MapbarMobStat.setServerIp("http://1.202.136.11:8004");
		//MapbarMobStat.readyToStatistic(this);//调用此方法才会将统计数据持久化存储，比较耗时，建议放在空闲时操作
		initPreferce();
		initFile();
		initData();
		mSharedPreferences=getSharedPreferences("SettingsConfig", 0);
		imageLoader = new ImageLoader(this);
		downloadManager = DownloadService.getDownloadManager(this);
		api = WXAPIFactory.createWXAPI(this, Configs.THIRED_LOGIN_WEIXIN_APP_ID,  true); 
		api.registerApp(Configs.THIRED_LOGIN_WEIXIN_APP_ID);
	}

	/**
	 * 
	 * <p>
	 * 功能描述
	 * </p>
	 * 初始还数据表
	 * 
	 * @author wangzhichao
	 * @date 2015年9月6日
	 */
	private void initData() {
		try {
			dbUtils = DbUtils.create(instance,dataBaseloadPath+File.separator+Configs.DB_NAME,Configs.DB_VERSION,this);
			dbUtils.configAllowTransaction(true);// 开启事务
			dbUtils.createTableIfNotExist(AppInfo.class);
			dbUtils.createTableIfNotExist(AppBannger.class);
			dbUtils.createTableIfNotExist(AppBean.class);
			dbUtils.createTableIfNotExist(AppDetails.class);
			dbUtils.createTableIfNotExist(AppVersion.class);
			dbUtils.createTableIfNotExist(AppInstall.class);
			dbUtils.createTableIfNotExist(AppComment.class);
			dbUtils.createTableIfNotExist(APPSearch.class);
			dbUtils.createTableIfNotExist(APPSearchHistory.class);
			dbUtils.createTableIfNotExist(APPSearchRecom.class);
			dbUtils.createTableIfNotExist(HisbillboardBean.class);
			dbUtils.createTableIfNotExist(WeekbillboardBean.class);
			dbUtils.createTableIfNotExist(DownloadInfo.class);
		} catch (DbException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onUpgrade(DbUtils dbUtils, int oldVersion, int newVersion) {
		try{
			if(newVersion > oldVersion){
				dbUtils.dropDb();
				FileUtil.delAllFile(apkDownloadPath);
				dbUtils.createTableIfNotExist(AppInfo.class);
				dbUtils.createTableIfNotExist(AppBannger.class);
				dbUtils.createTableIfNotExist(AppBean.class);
				dbUtils.createTableIfNotExist(AppDetails.class);
				dbUtils.createTableIfNotExist(AppVersion.class);
				dbUtils.createTableIfNotExist(AppInstall.class);
				dbUtils.createTableIfNotExist(AppComment.class);
				dbUtils.createTableIfNotExist(APPSearch.class);
				dbUtils.createTableIfNotExist(APPSearchHistory.class);
				dbUtils.createTableIfNotExist(APPSearchRecom.class);
				dbUtils.createTableIfNotExist(HisbillboardBean.class);
				dbUtils.createTableIfNotExist(WeekbillboardBean.class);
				dbUtils.createTableIfNotExist(DownloadInfo.class);
			}
		}catch(Exception e){
			Log.e("message","onUpgrade message:"+e.getMessage());
		}

	}

		/**
         *
         * <p>
         * 功能描述
         * </p>
         * 初始化属性数据
         *
         * @author wangzhichao
         * @date 2015年9月6日
         */
	private void initPreferce() {
		mAppPreferce = new AppPreferce();
		SharedPreferences mSharedPreferences = getSharedPreferences("SettingsConfig", 0);
		mAppPreferce.setNetWorkAllow(mSharedPreferences.getBoolean("netWorkAllow", true));
		mAppPreferce.setClearLoadAPK(mSharedPreferences.getBoolean("clearLoadAPK", true));
		mAppPreferce.setRecomBanngerLoadTime(mSharedPreferences.getLong("recomBanngerLoadTime", 0));
		mAppPreferce.setRecomPageLoadTime(mSharedPreferences.getLong("recomPageLoadTime", 0));
		mAppPreferce.setSearchPageLoadTime(mSharedPreferences.getLong("searchPageLoadTime", 0));
		mAppPreferce.setListPageLoadTime(mSharedPreferences.getLong("listPageLoadTime", 0));
	}

	/**
	 * 
	 * <p>
	 * 功能描述
	 * </p>
	 * 初始化文件资源
	 * 
	 * @author wangzhichao
	 * @date 2015年9月6日
	 */

	private void initFile() {
		String rootPath= SdcardUtil.getExtDataPath(instance);
		if(TextUtils.isEmpty(rootPath)){
			Toast.makeText(this,"无法识别的设备的存储路径",Toast.LENGTH_SHORT).show();
			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(0);
			return;
		}
		File root = new File(rootPath);//依据手机获取内置或外置存储卡路径

		File downloadRootDir = new File(root.getAbsolutePath() + File.separator + Configs.DOWNLOAD_ROOT_DIR + File.separator);
		if (!downloadRootDir.exists()) {
			downloadRootDir.mkdirs();
		}
		downloadRootPath = downloadRootDir.getAbsolutePath();

		File cacheDownloadDirFile = new File(root.getAbsolutePath() + File.separator + Configs.DOWNLOAD_IMAGE_DIR + File.separator);
		if (!cacheDownloadDirFile.exists()) {
			cacheDownloadDirFile.mkdirs();
		}
		imageDownloadPath = cacheDownloadDirFile.getAbsolutePath();

		File fileDownloadDirFile = new File(root.getAbsolutePath() + File.separator + Configs.DOWNLOAD_APK_DIR + File.separator);
		if (!fileDownloadDirFile.exists()) {
			fileDownloadDirFile.mkdirs();
		}
		apkDownloadPath = fileDownloadDirFile.getAbsolutePath();
		
		File dataBaseDirFile = new File(root.getAbsolutePath() + File.separator + Configs.DOWNLOAD_DB_DIR + File.separator);
		if (!dataBaseDirFile.exists()) {
			dataBaseDirFile.mkdirs();
		}
		dataBaseloadPath=dataBaseDirFile.getAbsolutePath();
	}
	
	public void onCommandReceive(Context context, CommandInfo command) {	
		if (command == null)
			return;
		if (mOnActivityListener != null) {
			mOnActivityListener.onCommandReceive(context,command);
		}
	}


	private OnActivityListener mOnActivityListener;

	public void setOnActivityListener(OnActivityListener listener) {
		mOnActivityListener = listener;
	}

	public interface OnActivityListener {
		void onCommandReceive(Context context, CommandInfo ci);
	}

}
