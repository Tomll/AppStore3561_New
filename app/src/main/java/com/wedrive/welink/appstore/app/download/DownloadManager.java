package com.wedrive.welink.appstore.app.download;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.db.converter.ColumnConverter;
import com.lidroid.xutils.db.converter.ColumnConverterFactory;
import com.lidroid.xutils.db.sqlite.ColumnDbType;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.util.LogUtils;
import com.wedrive.welink.appstore.MainApplication;

public class DownloadManager {

	private int maxDownloadThread = 10;
    private List<DownloadInfo> downloadInfoList = new ArrayList<DownloadInfo>();  
    private Map<String,DownloadInfo> downloadInfoMap = new HashMap<String,DownloadInfo>(); 

    public DownloadManager(Context appContext) {
    	ColumnConverterFactory.registerColumnConverter(HttpHandler.State.class, new HttpHandlerStateConverter());
        try {
            downloadInfoList = MainApplication.dbUtils.findAll(Selector.from(DownloadInfo.class));
            for(int i=0;i<downloadInfoList.size();i++){
                DownloadInfo info=downloadInfoList.get(i);
                downloadInfoMap.put(info.getAppId(),info);
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    public DownloadInfo isAppLoading(String appID){
    	return downloadInfoMap.get(appID);
    }

    public int getDownloadInfoListCount() {
        return downloadInfoList.size();
    }

    public DownloadInfo getDownloadInfo(int index) {
        return downloadInfoList.get(index);
    }

    /**
     *
     * <p>功能描述</p>添加一个新的下载文件
     * @param downloadInfo
     * @param callback
     * @throws DbException
     * @author wangzhichao
     * @date 2016年2月19日
     */
    
    public void addNewDownload(DownloadInfo downloadInfo , RequestCallBack<File> callback) throws DbException {			
			if(downloadInfoList.contains(downloadInfo) || downloadInfoMap.containsKey(downloadInfo.getAppId())){
				return;
			}else{
				HttpUtils http = new HttpUtils();
				http.configRequestThreadPoolSize(maxDownloadThread);
				HttpHandler<File> handler = http.download(downloadInfo.getDownloadUrl(), downloadInfo.getFileSavePath(),downloadInfo.isAutoResume(), 
						downloadInfo.isAutoRename(),new ManagerCallBack(downloadInfo, callback));
				downloadInfo.setHandler(handler);
				downloadInfo.setState(handler.getState());
				downloadInfoList.add(downloadInfo);
				downloadInfoMap.put(downloadInfo.getAppId(), downloadInfo);
				MainApplication.dbUtils.saveBindingId(downloadInfo);
			}
	}

    /**
     * 
     * <p>功能描述</p>重新下载索引指定文件
     * @param index
     * @param callback
     * @throws DbException
     * @author wangzhichao
     * @date 2016年2月19日
     */
    
    public void resumeDownload(int index, final RequestCallBack<File> callback) throws DbException {
        DownloadInfo downloadInfo = downloadInfoList.get(index);
        resumeDownload(downloadInfo, callback);
    }

    /**
     * 
     * <p>功能描述</p>重新下载指定文件
     * @param downloadInfo
     * @param callback
     * @throws DbException
     * @author wangzhichao
     * @date 2016年2月19日
     */


    public void resumeDownload(DownloadInfo downloadInfo, final RequestCallBack<File> callback) throws DbException {
        HttpUtils http = new HttpUtils();
        http.configRequestThreadPoolSize(maxDownloadThread);
        HttpHandler<File> handler = http.download(downloadInfo.getDownloadUrl(),downloadInfo.getFileSavePath(),downloadInfo.isAutoResume(),
                downloadInfo.isAutoRename(),new ManagerCallBack(downloadInfo, callback));
        downloadInfo.setHandler(handler);
        downloadInfo.setState(handler.getState());
        MainApplication.dbUtils.saveOrUpdate(downloadInfo);
    }
    
    /**
     * 
     * <p>功能描述</p>重新下载所有文件的
     * @throws DbException
     * @author wangzhichao
     * @date 2016年2月19日
     */
    
    
    public void resumeAllDownload() throws DbException {
        for (final DownloadInfo downloadInfo : downloadInfoList) {
            try {
                HttpHandler<File> handler = downloadInfo.getHandler();
                if(handler!=null){
                    RequestCallBack<File> callback=handler.getRequestCallBack();
                    resumeDownload(downloadInfo, callback);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 
     * <p>功能描述</p>删除指定索引下载的文件
     * @param index
     * @throws DbException
     * @author wangzhichao
     * @date 2016年2月19日
     */
    
    public void removeDownload(int index) throws DbException {
        DownloadInfo downloadInfo = downloadInfoList.get(index);
        downloadInfoMap.remove(downloadInfo.getFileName());
        removeDownload(downloadInfo);
    }

    /**
     * 
     * <p>功能描述</p>删除下载指定文件
     * @param downloadInfo
     * @throws DbException
     * @author wangzhichao
     * @date 2016年2月19日
     */
    
    public void removeDownload(DownloadInfo downloadInfo) throws DbException {
        HttpHandler<File> handler = downloadInfo.getHandler();
        if (handler != null && !handler.isCancelled()) {
            handler.cancel();
        }
        downloadInfoList.remove(downloadInfo);
        downloadInfoMap.remove(downloadInfo.getAppId());
        MainApplication.dbUtils.delete(downloadInfo);
    }

    /**
     * 
     * <p>功能描述</p>停止下载指定索引的下载文件
     * @param index
     * @throws DbException
     * @author wangzhichao
     * @date 2016年2月19日
     */
    
    public void stopDownload(int index) throws DbException {
        DownloadInfo downloadInfo = downloadInfoList.get(index);
        stopDownload(downloadInfo);
    }

    /**
     * 
     * <p>功能描述</p>停止下载指定的下载文件
     * @param downloadInfo
     * @throws DbException
     * @author wangzhichao
     * @date 2016年2月19日
     */
    public void stopDownload(DownloadInfo downloadInfo) throws DbException {
        HttpHandler<File> handler = downloadInfo.getHandler();
        if (handler != null && !handler.isCancelled()) {
            handler.cancel();
        } else {
            downloadInfo.setState(HttpHandler.State.CANCELLED);
        }
        MainApplication.dbUtils.saveOrUpdate(downloadInfo);
    }

    /**
     * 
     * <p>功能描述</p>停止所有文件的下载
     * @throws DbException
     * @author wangzhichao
     * @date 2016年2月19日
     */
    
    
    public void stopAllDownload() throws DbException {
        for (DownloadInfo downloadInfo : downloadInfoList) {
            HttpHandler<File> handler = downloadInfo.getHandler();
            if (handler != null && !handler.isCancelled()) {
                handler.cancel();
            } else {
                downloadInfo.setState(HttpHandler.State.CANCELLED);
            }
        }
        MainApplication.dbUtils.saveOrUpdateAll(downloadInfoList);
    }

    /**
     * 
     * <p>功能描述</p>后台继续下载所有文件
     * @throws DbException
     * @author wangzhichao
     * @date 2016年2月19日
     */
    
    public void backupDownloadInfoList() throws DbException {
        for (DownloadInfo downloadInfo : downloadInfoList) {
            HttpHandler<File> handler = downloadInfo.getHandler();
            if (handler != null) {
                downloadInfo.setState(handler.getState());
            }
        }
        MainApplication.dbUtils.saveOrUpdateAll(downloadInfoList);
    }

    public int getMaxDownloadThread() {
        return maxDownloadThread;
    }

    public void setMaxDownloadThread(int maxDownloadThread) {
        this.maxDownloadThread = maxDownloadThread;
    }

    public class ManagerCallBack extends RequestCallBack<File> {
        private DownloadInfo downloadInfo;
        private RequestCallBack<File> baseCallBack;

        public RequestCallBack<File> getBaseCallBack() {
            return baseCallBack;
        }

        public void setBaseCallBack(RequestCallBack<File> baseCallBack) {
            this.baseCallBack = baseCallBack;
        }

        private ManagerCallBack(DownloadInfo downloadInfo, RequestCallBack<File> baseCallBack) {
            this.baseCallBack = baseCallBack;
            this.downloadInfo = downloadInfo;
        }

        @Override
        public Object getUserTag() {
            if (baseCallBack == null) return null;
            return baseCallBack.getUserTag();
        }

        @Override
        public void setUserTag(Object userTag) {
            if (baseCallBack == null) return;
            baseCallBack.setUserTag(userTag);
        }

        @Override
        public void onStart() {
            HttpHandler<File> handler = downloadInfo.getHandler();
            if (handler != null) {
                downloadInfo.setState(handler.getState());
            }
            try {
            	MainApplication.dbUtils.saveOrUpdate(downloadInfo);
            } catch (DbException e) {
                LogUtils.e(e.getMessage(), e);
            }
            if (baseCallBack != null) {
                baseCallBack.onStart();
            }
        }

        @Override
        public void onCancelled() {
            HttpHandler<File> handler = downloadInfo.getHandler();
            if (handler != null) {
                downloadInfo.setState(handler.getState());
            }
            try {
            	MainApplication.dbUtils.saveOrUpdate(downloadInfo);
            } catch (DbException e) {
                LogUtils.e(e.getMessage(), e);
            }
            if (baseCallBack != null) {
                baseCallBack.onCancelled();
            }
        }

        @Override
        public void onLoading(long total, long current, boolean isUploading) {
            HttpHandler<File> handler = downloadInfo.getHandler();
            if (handler != null) {
                downloadInfo.setState(handler.getState());
            }
            downloadInfo.setFileLength(total);
            downloadInfo.setProgress(current);
            try {
            	MainApplication.dbUtils.saveOrUpdate(downloadInfo);
            } catch (DbException e) {
                LogUtils.e(e.getMessage(), e);
            }
            if (baseCallBack != null) {
                baseCallBack.onLoading(total, current, isUploading);
            }
        }

        @Override
        public void onSuccess(ResponseInfo<File> responseInfo) {
            HttpHandler<File> handler = downloadInfo.getHandler();
            if (handler != null) {
                downloadInfo.setState(handler.getState());
            }
            if (baseCallBack != null) {
                baseCallBack.onSuccess(responseInfo);
            }
        }

        @Override
        public void onFailure(HttpException error, String msg) {
            HttpHandler<File> handler = downloadInfo.getHandler();
            if (handler != null) {
                downloadInfo.setState(handler.getState());
            }
            try {
            	MainApplication.dbUtils.saveOrUpdate(downloadInfo);
            } catch (DbException e) {
                LogUtils.e(e.getMessage(), e);
            }
            if (baseCallBack != null) {
                baseCallBack.onFailure(error, msg);
            }
        }
    }

    private class HttpHandlerStateConverter implements ColumnConverter<HttpHandler.State> {

        @Override
        public HttpHandler.State getFieldValue(Cursor cursor, int index) {
            return HttpHandler.State.valueOf(cursor.getInt(index));
        }

        @Override
        public HttpHandler.State getFieldValue(String fieldStringValue) {
            if (fieldStringValue == null) return null;
            return HttpHandler.State.valueOf(fieldStringValue);
        }

        @Override
        public Object fieldValue2ColumnValue(HttpHandler.State fieldValue) {
            return fieldValue.value();
        }

        @Override
        public ColumnDbType getColumnDbType() {
            return ColumnDbType.INTEGER;
        }
    }
}
