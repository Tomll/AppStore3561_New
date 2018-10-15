/**
 * Created by wangzhichao on 2015年9月7日.
 * Copyright (c) 2015 北京图为先科技有限公司. All rights reserved.
 */
package com.wedrive.welink.appstore.app.receive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.mapbar.android.model.CommandInfo;
import com.wedrive.welink.appstore.MainApplication;
import com.wedrive.welink.appstore.R;
import com.wedrive.welink.appstore.app.download.DownloadInfo;
import com.wedrive.welink.appstore.app.model.AppInfo;
import com.wedrive.welink.appstore.app.model.AppInstall;
import com.wedrive.welink.appstore.app.model.AppVersion;
import com.wedrive.welink.appstore.app.util.CommonUtil;

import java.io.File;

public class InstalReceive extends BroadcastReceiver {
	
	private Context mContext;

	@Override
	public void onReceive(Context context, Intent intent) {
		mContext=context;
		if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
			String packageName = intent.getData().getSchemeSpecificPart();
			PackageInfo info = CommonUtil.isAppInstalled(mContext, packageName);
			try {
				AppInfo app = MainApplication.dbUtils.findFirst(Selector.from(AppInfo.class).where("packageName", "=", packageName));
				if (app != null) {
					String appName = (String) info.applicationInfo.loadLabel(context.getPackageManager());										
					DownloadInfo downloadInfo =  MainApplication.downloadManager.isAppLoading(app.getAppId());
					if(downloadInfo!=null) MainApplication.downloadManager.removeDownload(downloadInfo);
					if (MainApplication.mAppPreferce.clearLoadAPK) {
						String path = app.getSavePath().substring(app.getSavePath().lastIndexOf("/") + 1);
						path = MainApplication.apkDownloadPath + File.separator + path;
						File apk = new File(path);
						if(apk.exists() && apk.delete()){
							showAlert(context,appName+"安装成功，已帮您清除安装包");
						}else {
							showAlert(context,appName+"安装成功");
						}
					}else{
						showAlert(context,appName+"安装成功");
					}
				}
			} catch (Exception e) {
				Log.e("message","exception:"+e.getMessage());
			}

			CommandInfo ci = new CommandInfo();
			ci.setMethod("addedPackage");
			ci.setExtData(intent);
			MainApplication.getInstance().onCommandReceive(context, ci);
		}
		// 移除
		if(intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
			String packageName = intent.getData().getSchemeSpecificPart();
			try {
				MainApplication.dbUtils.delete(AppVersion.class, WhereBuilder.b("app_package_name", "=", packageName));
				MainApplication.dbUtils.delete(AppInstall.class, WhereBuilder.b("app_package_name", "=", packageName));

				AppInfo app = MainApplication.dbUtils.findFirst(Selector.from(AppInfo.class).where("packageName", "=", packageName));
				if (app != null) showAlert(context,"卸载成功");
			}catch (Exception e) {
				Log.e("message","exception:"+e.getMessage());
			}

			CommandInfo ci = new CommandInfo();
			ci.setMethod("removedPackage");
			ci.setExtData(intent);
			MainApplication.getInstance().onCommandReceive(context, ci);
		}
	}
	
	/**
	 * 
	 * <p>功能描述</p>显示吐司
	 * @param context
	 * @param content
	 * @author wangzhichao
	 * @date 2015年11月5日
	 */
	
	
	public void showAlert(Context context,String content) {
        //将布局文件转换成相应的View对象
        View layout=View.inflate(context,R.layout.layout_toast,null);
        //从layout中按照id查找TextView对象
        TextView textView=(TextView)layout.findViewById(R.id.tv_toast_text);
        //设置TextView的text内容
        textView.setText(content);
        //实例化一个Toast对象
        Toast toast=new Toast(context.getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0, 20);
        toast.setView(layout);
        toast.show();
	}

}
