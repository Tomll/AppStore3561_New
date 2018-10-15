package com.wedrive.welink.appstore.app.download;

import java.util.List;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.lidroid.xutils.exception.DbException;


public class DownloadService extends Service {

    private static DownloadManager DOWNLOAD_MANAGER;

    public static DownloadManager getDownloadManager(Context appContext) {
        if (!DownloadService.isServiceRunning(appContext)) {
            //Intent downloadSvr = new Intent("com.wedrive.welink.appstore.download.service.action");
            Intent downloadSvr = new Intent(appContext,DownloadService.class);
            downloadSvr.setAction("com.wedrive.welink.appstore.download.service.action");
            appContext.startService(downloadSvr);
        }
        if (DownloadService.DOWNLOAD_MANAGER == null) {
            DownloadService.DOWNLOAD_MANAGER = new DownloadManager(appContext);
        }
        return DOWNLOAD_MANAGER;
    }

    public DownloadService() {
        super();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
        if (DOWNLOAD_MANAGER != null) {
            try {
                DOWNLOAD_MANAGER.stopAllDownload();              
            } catch (DbException e) {
                Log.e("message","DownloadService exception:"+e.getMessage());
            }
        }
        super.onDestroy();
    }

    /**
     * 
     * <p>功能描述</p>判断某个服务是否正在运行中
     * @param context
     * @return
     * @author wangzhichao
     * @date 2016年2月19日
     */
    
    public static boolean isServiceRunning(Context context) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(Integer.MAX_VALUE);

        if (serviceList == null || serviceList.size() == 0) {
            return false;
        }

        for (int i = 0; i < serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(DownloadService.class.getName())) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }
}
