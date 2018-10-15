/*
 * Copyright (C) 2012 www.amsoft.cn
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wedrive.welink.appstore.app.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.wedrive.welink.appstore.Configs;
import com.wedrive.welink.appstore.MainApplication;
import com.wedrive.welink.appstore.R;
import com.wedrive.welink.appstore.app.RootShell.RootShell;
import com.wedrive.welink.appstore.app.RootShell.execution.Command;
import com.wedrive.welink.appstore.app.model.AppBean;
import com.wedrive.welink.appstore.app.model.AppInfo;
import com.wedrive.welink.appstore.app.model.AppVersion;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class AppUtil {
	public static final String TAG = "AppUtil";
	public static List<String[]> mProcessList = null;
	private static Activity activity;

	public static void init(Activity act) {
		activity = act;
	}

	/**
	 * 获取版本号
	 * 
	 * @return 当前应用的版本号
	 */
	public static String getVersion(Context context) {
		String version = null;
		try {
			PackageManager manager = context.getPackageManager();
			PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
			version = info.versionName;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return version;
	}

	/**
	 * 用来判断服务是否运行.
	 * 
	 * @param context
	 *            the context
	 * @param className
	 *            判断的服务名字 "com.xxx.xx..XXXService"
	 * @return true 在运行 false 不在运行
	 */
	public static boolean isServiceRunning(Context context, String className) {
		boolean isRunning = false;
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningServiceInfo> servicesList = activityManager.getRunningServices(Integer.MAX_VALUE);
		Iterator<RunningServiceInfo> l = servicesList.iterator();
		while (l.hasNext()) {
			RunningServiceInfo si = (RunningServiceInfo) l.next();
			if (className.equals(si.service.getClassName())) {
				isRunning = true;
			}
		}
		return isRunning;
	}

	/**
	 * 停止服务.
	 * 
	 * @param context
	 *            the context
	 * @param className
	 *            the class name
	 * @return true, if successful
	 */
	public static boolean stopRunningService(Context context, String className) {
		Intent intent_service = null;
		boolean ret = false;
		try {
			intent_service = new Intent(context, Class.forName(className));
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (intent_service != null) {
			ret = context.stopService(intent_service);
		}
		return ret;
	}

	/**
	 * Gets the number of cores available in this device, across all processors.
	 * Requires: Ability to peruse the filesystem at "/sys/devices/system/cpu"
	 * 
	 * @return The number of cores, or 1 if failed to get result
	 */
	public static int getNumCores() {
		try {
			// Get directory containing CPU info
			File dir = new File("/sys/devices/system/cpu/");
			// Filter to only list the devices we care about
			File[] files = dir.listFiles(new FileFilter() {

				@Override
				public boolean accept(File pathname) {
					// Check if filename is "cpu", followed by a single digit
					// number
					if (Pattern.matches("cpu[0-9]", pathname.getName())) {
						return true;
					}
					return false;
				}

			});
			// Return the number of cores (virtual CPU devices)
			return files.length;
		} catch (Exception e) {
			e.printStackTrace();
			return 1;
		}
	}

	/**
	 * 描述：判断网络是否有效.
	 * 
	 * @param context
	 *            the context
	 * @return true, if is network available
	 */
	public static boolean isNetworkAvailable(Context context) {
		try {
			ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity != null) {
				NetworkInfo info = connectivity.getActiveNetworkInfo();
				if (info != null && info.isConnected()) {
					if (info.getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}

	/**
	 * Gps是否打开 需要<uses-permission
	 * android:name="android.permission.ACCESS_FINE_LOCATION" />权限
	 * 
	 * @param context
	 *            the context
	 * @return true, if is gps enabled
	 */
	public static boolean isGpsEnabled(Context context) {
		LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}

	/**
	 * 判断当前网络是否是移动数据网络.
	 * 
	 * @param context
	 *            the context
	 * @return boolean
	 */
	public static boolean isMobile(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
			return true;
		}
		return false;
	}

	/**
	 * 导入数据库.
	 * 
	 * @param context
	 *            the context
	 * @param dbName
	 *            the db name
	 * @param rawRes
	 *            the raw res
	 * @return true, if successful
	 */
	public static boolean importDatabase(Context context, String dbName, int rawRes) {
		int buffer_size = 1024;
		InputStream is = null;
		FileOutputStream fos = null;
		boolean flag = false;

		try {
			String dbPath = "/data/data/" + context.getPackageName() + "/databases/" + dbName;
			File dbfile = new File(dbPath);
			// 判断数据库文件是否存在，若不存在则执行导入，否则直接打开数据库
			if (!dbfile.exists()) {
				// 欲导入的数据库
				if (!dbfile.getParentFile().exists()) {
					dbfile.getParentFile().mkdirs();
				}
				dbfile.createNewFile();
				is = context.getResources().openRawResource(rawRes);
				fos = new FileOutputStream(dbfile);
				byte[] buffer = new byte[buffer_size];
				int count = 0;
				while ((count = is.read(buffer)) > 0) {
					fos.write(buffer, 0, count);
				}
				fos.flush();
			}
			flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (Exception e) {
				}
			}
			if (is != null) {
				try {
					is.close();
				} catch (Exception e) {
				}
			}
		}
		return flag;
	}

	/**
	 * 获取屏幕尺寸与密度.
	 * 
	 * @param context
	 *            the context
	 * @return mDisplayMetrics
	 */
	public static DisplayMetrics getDisplayMetrics(Context context) {
		Resources mResources;
		if (context == null) {
			mResources = Resources.getSystem();

		} else {
			mResources = context.getResources();
		}
		// DisplayMetrics{density=1.5, width=480, height=854, scaledDensity=1.5,
		// xdpi=160.421, ydpi=159.497}
		// DisplayMetrics{density=2.0, width=720, height=1280,
		// scaledDensity=2.0, xdpi=160.42105, ydpi=160.15764}
		DisplayMetrics mDisplayMetrics = mResources.getDisplayMetrics();
		return mDisplayMetrics;
	}

	/**
	 * 打开键盘.
	 * 
	 * @param context
	 *            the context
	 */
	public static void showSoftInput(Context context) {
		InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
	}

	/**
	 * 关闭键盘事件.
	 * 
	 * @param context
	 *            the context
	 */
	public static void closeSoftInput(Context context) {
		InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (inputMethodManager != null && ((Activity) context).getCurrentFocus() != null) {
			inputMethodManager.hideSoftInputFromWindow(((Activity) context).getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	/**
	 * 获取包信息.
	 * 
	 * @param context
	 *            the context
	 */
	public static PackageInfo getPackageInfo(Context context) {
		PackageInfo info = null;
		try {
			String packageName = context.getPackageName();
			info = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return info;
	}


	/**
	 * 
	 * 描述：获取可用内存.
	 * 
	 * @param context
	 * @return
	 */
	public static long getAvailMemory(Context context) {
		// 获取android当前可用内存大小
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		MemoryInfo memoryInfo = new MemoryInfo();
		activityManager.getMemoryInfo(memoryInfo);
		// 当前系统可用内存 ,将获得的内存大小规格化
		return memoryInfo.availMem;
	}

	/**
	 * 
	 * 描述：总内存.
	 * 
	 * @param context
	 * @return
	 */
	public static long getTotalMemory(Context context) {
		// 系统内存信息文件
		String file = "/proc/meminfo";
		String memInfo;
		String[] strs;
		long memory = 0;

		try {
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader, 8192);
			// 读取meminfo第一行，系统内存大小
			memInfo = bufferedReader.readLine();
			strs = memInfo.split("\\s+");
			// 获得系统总内存，单位KB
			memory = Integer.valueOf(strs[1]).intValue() * 1024;
			bufferedReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Byte转位KB或MB
		return memory;
	}

	/**
	 * 
	 * 描述：获取版本信息
	 * 
	 * @param context
	 * @return
	 */

	public static PackageInfo isAppInstalled(Context context, String packagename) {
		PackageInfo packageInfo = null;
		try {
			packageInfo = context.getPackageManager().getPackageInfo(packagename, 0);
		} catch (NameNotFoundException e) {

		}
		return packageInfo;
	}

	/**
	 * 描述：获取版本信息依据apk文件获取所属信息
	 * 
	 * @param context
	 * @return
	 */

	public static PackageInfo getAPKInfo(Context context, String filePath) {
		PackageInfo packageInfo = null;
		File file = new File(filePath);
		if (file.exists()) {
			PackageManager packageManager = context.getPackageManager();
			packageInfo = packageManager.getPackageArchiveInfo(filePath, PackageManager.GET_ACTIVITIES);
		}
		return packageInfo;

	}

	/**
	 * 描述：打开并安装文件.
	 *
	 * @param context
	 *            the context
	 * @param file
	 *            apk文件路径
	 */
	public static void installApk(Context context, File file) {
		Intent intent = new Intent();
		intent.setAction(android.content.Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		if(Build.VERSION.SDK_INT >= 24){
			try {
				Uri apkUri = FileProvider.getUriForFile(context, "com.wedrive.welink.appstore.fileprovider", file);
				intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//添加这一句表示对目标应用临时授权该Uri所代表的文件
				intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
				context.startActivity(intent);
			} catch (Exception e) {
				intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
				context.startActivity(intent);
			}
		}else{
			intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
			context.startActivity(intent);
		}

	}


	/**
	 * 描述：卸载程序.
	 *
	 * @param context
	 *            the context
	 * @param packageName
	 *            包名
	 */

	public static void unInstallApk(Context context, String packageName) {
		Intent intent = new Intent(Intent.ACTION_DELETE);
		intent.setData(Uri.parse("package:" + packageName));
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	/**
	 * 根据应用包名卸载应用（如果有root权限直接卸载，否则跳转到系统卸载界面）
	 *
	 * @param packageName 要卸载的app的包名
	 */
	public static void unInstallApp(final String packageName, final Context context) {
		if (!MainApplication.isRootAvailable) {
			unInstallApk(context, packageName);
			return;
		} else if (!MainApplication.appIsAccessGiven) {
			showSafeAlert(context, "进入管理程序授权本应用root权限，可开启静默卸载");
			unInstallApk(context, packageName);
			return;
		}

		new AsyncTask<Object, Object, Object>() {
			@Override
			protected Object doInBackground(Object... params) {
				try {
					showSafeAlert(context, "正在卸载...");
					final List<String> list = new ArrayList<String>();
					Command commond = new Command(1, 3000, "pm uninstall " + packageName) {
						@Override
						public void commandOutput(int id, String line) {
							if(!TextUtils.isEmpty(line)){
								if(line.toLowerCase().contains("success")){
									list.add(line);
								}
							}
						}
					};

					RootShell.getShell(true).add(commond);
					RootShell.commandWait(RootShell.getShell(true), commond);

					if (list.size() == 0) {
						unInstallApk(context, packageName);
					}
				} catch (Exception e) {
					unInstallApk(context, packageName);
				}
				return null;
			}
		}.execute(null, null);
	}

	/**
	 * 安装应用（如果有root权限直接安装，没有root权限跳到系统安装界面）
	 *
	 * @param context
	 * @param file
	 */
	public static void installApp(final Context context, final File file) {
		try {
			PackageInfo info = getAPKInfo(context, file.getAbsolutePath());
			if (info != null) {
				JSONObject extData = new JSONObject();
				extData.put("flag", 0);
				JSONObject obj = new JSONObject();
				obj.put("packagename", info.packageName);
				obj.put("version", info.versionCode);
				extData.put("app", obj);
				sendAppStoreAction(context, "onStartInstall", extData);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		//bug号：WELM-3201；bug链接：https://wdjira.mapbar.com/browse/WELM-3201
		Handler handler = new Handler(Looper.getMainLooper());
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (!MainApplication.isRootAvailable) {
					installApk(context, file);
					return;
				} else if (!MainApplication.appIsAccessGiven) {
					showSafeAlert(context, "进入管理程序授权本应用root权限，可开启静默安装");
					installApk(context, file);
					return;
				}

				new AsyncTask<Object, Object, Object>() {

					@Override
					protected Object doInBackground(Object... params) {
						try {
							showSafeAlert(context, "正在安装...");
							final List<String> list = new ArrayList<String>();
							Command command = new Command(1, 5000, "pm install -r " + file.getAbsolutePath()) {
								@Override
								public void commandOutput(int id, String line) {
									if (!TextUtils.isEmpty(line)) {
										if (line.toLowerCase().contains("success")) {
											list.add(line);
										}
									}
								}
							};

							RootShell.getShell(true).add(command);
							RootShell.commandWait(RootShell.getShell(true), command);
							if (list.size() == 0) {
								installApk(context, file);
							}
						} catch (Exception e) {
							installApk(context, file);
						}
						return null;
					}

				}.execute(null, null);
			}
		}, 300);
	}

	/**
	 *
	 * <p>功能描述</p>发送命令指令
	 * @param method
	 * @param extData
	 * @author wangzhichao
	 * @date 2015年11月5日
	 */

	private static void sendAppStoreAction(Context context, String method,JSONObject extData) {
		String commandData = null;
		try {
			JSONObject jObj = new JSONObject();
			jObj.put("moduleName", "WeDriveAppStore");
			jObj.put("version", 0);
			JSONObject jComd = new JSONObject();
			jComd.put("method", method);

			jComd.put("extData", extData);
			jObj.put("command", jComd);

			commandData = jObj.toString();
		} catch (Exception e) {
		}
		Intent intent = new Intent();
		intent.setAction(Configs.APPSTORE_COMMAND_SEND);
		intent.putExtra("com.wedrive.extra.COMMAND_DATA", commandData);
		context.sendBroadcast(intent);
	}

	public static void showAlert(Context context, String content) {
		// 将布局文件转换成相应的View对象
		View layout = View.inflate(context, R.layout.layout_toast, null);
		// 从layout中按照id查找TextView对象
		TextView textView = (TextView) layout.findViewById(R.id.tv_toast_text);
		// 设置TextView的text内容
		textView.setText(content);
		// 实例化一个Toast对象
		Toast toast = new Toast(context);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.TOP, 0, 0);
		toast.setView(layout);
		toast.show();

	}

	/**
	 * <p>
	 * 功能描述
	 * </p>
	 * 遍历已安装非系统的应用程序
	 * 
	 * @param context
	 * @return
	 * @author wangzhichao
	 * @date 2015年10月21日
	 */

	public static List<AppInfo> getInstalApks(Context context) {
		List<AppInfo> apks = new ArrayList<AppInfo>();
		PackageManager packageManager = context.getPackageManager();
		List<PackageInfo> packs = packageManager.getInstalledPackages(0);

		for (int i = 0; i < packs.size(); i++) {
			PackageInfo packageInfo = packs.get(i);
			// 判断是否系统应用
			if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 && !packageInfo.packageName.equals(context.getPackageName())) { // 非系统应用

				AppInfo info = new AppInfo();
				// 获得名称
				CharSequence appName = packageInfo.applicationInfo.loadLabel(packageManager);
				info.setAppName(appName.toString());
				
				info.setPackageName(packageInfo.packageName);
				info.setVersionCode(packageInfo.versionCode);
				info.setVersionName(packageInfo.versionName);

				long appSize = new File(packageInfo.applicationInfo.sourceDir).length();
				info.setAppSize(appSize);
				AppVersion version;
				try {
					version = MainApplication.dbUtils.findFirst(Selector.from(AppVersion.class).where("app_package_name", "=", packageInfo.packageName));
					if (version != null) {
						info.setApp_official_flag(""+version.getOfficial_flag());
					}
				} catch (DbException e) {
					e.printStackTrace();
				}
				apks.add(info);

			} else { // 系统应用　

			}
		}
		return apks;
	}

	public static HashMap<String,AppInfo> getInstalApkMaps(Context context) {
		HashMap<String,AppInfo> infoMaps=new HashMap<String,AppInfo>();
		PackageManager packageManager = context.getPackageManager();
		List<PackageInfo> packs = packageManager.getInstalledPackages(0);

		for (int i = 0; i < packs.size(); i++) {
			PackageInfo packageInfo = packs.get(i);
			// 判断是否系统应用
			if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 && !packageInfo.packageName.equals(context.getPackageName())) { // 非系统应用

				AppInfo info = new AppInfo();
				String appName=packageInfo.applicationInfo.loadLabel(packageManager).toString();
				info.setAppName(appName);
				info.setPackageName(packageInfo.packageName);
				info.setVersionCode(packageInfo.versionCode);
				info.setVersionName(packageInfo.versionName);

				long appSize = new File(packageInfo.applicationInfo.sourceDir).length();
				info.setAppSize(appSize);
				try {
					AppBean app = MainApplication.dbUtils.findFirst(Selector.from(AppBean.class).where("app_name", "=", appName));
					if (app != null) {
						info.setApp_official_flag(""+app.official_flag);
					}
				} catch (DbException e) {
					e.printStackTrace();
				}
				infoMaps.put(packageInfo.packageName,info);
			}
		}
		return infoMaps;
	}

	/**
	 * 根据包名获取appInfo(appInfo 中只包含了 应用名称 图标 appzize 包名)
	 * 
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static AppInfo getAppInfoByPackageName(Context context, String packageName) {

		PackageManager pm = context.getPackageManager();
		AppInfo appBean = new AppInfo();

		try {
			ApplicationInfo appInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);

			// 获得名称
			CharSequence appName = appInfo.loadLabel(pm);
			appBean.setAppName(appName.toString());

			// 设置图标
//			Drawable icon = appInfo.loadIcon(pm);
//			appBean.setIcon(icon);

			// 设置大小
			String appPath = appInfo.sourceDir;
			long appSize = new File(appPath).length();
			appBean.setAppSize(appSize);
			// 添加包名
			appBean.setPackageName(packageName);
			AppVersion version;
			try {
				version = MainApplication.dbUtils.findFirst(Selector.from(AppVersion.class).where("app_package_name", "=", appInfo.packageName));
				if (version != null) {
					appBean.setApp_official_flag(""+version.getOfficial_flag());
				}
			} catch (DbException e) {
				e.printStackTrace();
			}

		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		return appBean;

	}

	public static void showSafeAlert(final Context context, final String content) {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				showAlert(context, content);
			}
		});
	}
	


}
