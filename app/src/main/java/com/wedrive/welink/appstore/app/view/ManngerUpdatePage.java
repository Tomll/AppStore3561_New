package com.wedrive.welink.appstore.app.view;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lidroid.xutils.db.sqlite.WhereBuilder;
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
import com.mapbar.android.statistics.api.MapbarMobStat;
import com.wedrive.welink.appstore.Configs;
import com.wedrive.welink.appstore.MainActivity;
import com.wedrive.welink.appstore.MainApplication;
import com.wedrive.welink.appstore.R;
import com.wedrive.welink.appstore.app.download.DownloadInfo;
import com.wedrive.welink.appstore.app.download.DownloadManager;
import com.wedrive.welink.appstore.app.model.AppInfo;
import com.wedrive.welink.appstore.app.model.AppVersion;
import com.wedrive.welink.appstore.app.provider.SearchProvider;
import com.wedrive.welink.appstore.app.util.AppUtil;
import com.wedrive.welink.appstore.app.util.CommonUtil;

public class ManngerUpdatePage extends BasePage {
	
    private final static String TAG = "ManngerUpdatePage";
    private Context mContext;
    private ActivityInterface mAif;

    private TextView tv_update_descript;
    private List<AppVersion> appVersions = new ArrayList<AppVersion>();

    private RecyclerView rvUpdate;
    private UpdateAdapter updateAdapter;

    public ManngerUpdatePage(Context context, View view, ActivityInterface aif) {
        super(context, view, aif);

        mContext = context;
        mAif = aif;
        initView(view);
    }

    private void initView(View view) {
        tv_update_descript = (TextView) view.findViewById(R.id.tv_update_descript);
        rvUpdate = (RecyclerView) view.findViewById(R.id.lVi_update_apps);

        LinearLayoutManager manager = new LinearLayoutManager(mContext);
        manager.setOrientation(LinearLayoutManager.VERTICAL);

        rvUpdate.setLayoutManager(manager);
        updateAdapter = new UpdateAdapter();
        rvUpdate.setAdapter(updateAdapter);
    }

    @Override
    public void setFilterObj(int flag, FilterObj filter) {
        super.setFilterObj(flag, filter);
        Object tag = filter.getTag();
        if (List.class.isInstance(tag)) {
            rvUpdate.setVisibility(View.VISIBLE);
            tv_update_descript.setVisibility(View.INVISIBLE);
            List<AppVersion> versions = (List<AppVersion>) tag;
            if (versions != null && versions.size() > 0) {
                appVersions = versions;
                updateAdapter.notifyDataSetChanged();
            }
        }

        if (String.class.isInstance(tag)) {
            rvUpdate.setVisibility(View.INVISIBLE);
            tv_update_descript.setVisibility(View.VISIBLE);
            tv_update_descript.setText((String) tag);
        }

    }

    @Override
    public void onReceiveData(int arg0, int code, Object arg2) {
        if (arg0 == getMyViewPosition()) {
            switch (code) {
                case 10:
                    rvUpdate.setVisibility(View.VISIBLE);
                    tv_update_descript.setVisibility(View.GONE);
                    appVersions = (List<AppVersion>) arg2;
                    if (appVersions != null && appVersions.size() > 0) {
                        updateAdapter.notifyDataSetChanged();
                    }
                    break;
                case 11:
                    String message = arg2.toString();
                    rvUpdate.setVisibility(View.INVISIBLE);
                    tv_update_descript.setVisibility(View.VISIBLE);
                    tv_update_descript.setText(message);
                    break;
            }
        }
    }


    @Override
    public void viewWillAppear(int flag) {
        super.viewWillAppear(flag);
        MainActivity.mMainActivity.setFirstAndSecondTitle("更新应用", "管理");
        MapbarMobStat.onPageStart(mContext, Configs.AppStore_Interface_UpdateAppPage);
    }

    @Override
    public void viewWillDisappear(int flag) {
        super.viewWillDisappear(flag);
        MapbarMobStat.onPageEnd(mContext, Configs.AppStore_Interface_UpdateAppPage);
    }

    @Override
    public void onResume() {
        super.onResume();
        //MapbarMobStat.onPageStart(mContext, Configs.AppStore_Interface_UpdateAppPage);
    }

    @Override
    public void onPause() {
        super.onPause();
        //MapbarMobStat.onPageEnd(mContext, Configs.AppStore_Interface_UpdateAppPage);
    }

    /**
     * <p>
     * 功能描述
     * </p>
     * 下载apk文件
     *
     * @param bean
     * @author wangzhichao
     * @date 2015年9月6日
     */

    private DownloadInfo loadApk(AppVersion bean) {
        DownloadInfo downloadInfo = new DownloadInfo();
        try {
            String fileName = bean.apk_path.substring(bean.apk_path.lastIndexOf("/") + 1);
            String path = MainApplication.apkDownloadPath + File.separator + fileName;
            File apk = new File(path);
            if (apk.exists()) apk.delete();
            downloadInfo.setSize(Double.parseDouble(bean.app_size));
            downloadInfo.setAppId(bean.app_id);
            downloadInfo.setOfficial_flag(bean.official_flag);
            downloadInfo.setLogoUrl(bean.icon_path);
            downloadInfo.setDownloadUrl(bean.apk_path);
            downloadInfo.setAutoRename(false);
            downloadInfo.setAutoResume(true);
            downloadInfo.setFileName(bean.app_name);
            downloadInfo.setFileSavePath(path);
            downloadInfo.setApp_v_id(bean.getApp_version_id());
            downloadInfo.setMd5(bean.getApp_md5());
            MainApplication.downloadManager.addNewDownload(downloadInfo, new DownloadRequestCallBack());
            MainApplication.dbUtils.saveOrUpdate(new AppInfo(bean.getApp_id(), bean.getApp_package_name(), bean.getApk_path()));
        } catch (DbException e) {
            Log.e("message", "exception:" + e.getMessage());
        }
        return downloadInfo;
    }

    /**
     * RecyclerView适配器的ViewHolder
     */
    private class UpdateViewHolder extends RecyclerView.ViewHolder {
        private DecimalFormat df = new DecimalFormat("0.00");
        private View convertView;
        private ImageView imageView_app_icon;
        private ImageView imageView_official;
        private TextView tv_app_name;
        private TextView tv_app_size;
        private ProgressBar lcb_download_progress;
        private ImageView imageView_update, imageView_start, imageView_pause, imageView_install, imageView_reset;

        private AppVersion mAppVersion;
        private DownloadInfo downloadInfo;
        private RequestCallBack callBack;

        public UpdateViewHolder(View itemView) {
            super(itemView);
            convertView = itemView;
        }

        public void setCallBack(RequestCallBack callBack) {
            this.callBack = callBack;
        }

        public AppVersion getmAppVersion() {
            return mAppVersion;
        }

        public void setAppVersion(AppVersion mAppVersion) {
            this.mAppVersion = mAppVersion;
        }

        public DownloadInfo getDownloadInfo() {
            return downloadInfo;
        }

        public void setDownloadInfo(DownloadInfo downloadInfo) {
            this.downloadInfo = downloadInfo;
        }

        private void initView() {
            imageView_app_icon = (ImageView) convertView.findViewById(R.id.imageView_app_icon);
            imageView_official = (ImageView) convertView.findViewById(R.id.imageView_official);
            tv_app_name = (TextView) convertView.findViewById(R.id.tv_app_name);
            tv_app_size = (TextView) convertView.findViewById(R.id.tv_app_size);
            lcb_download_progress = (ProgressBar) convertView.findViewById(R.id.lcb_download_progress);
            imageView_update = (ImageView) convertView.findViewById(R.id.imageView_update);
            imageView_install = (ImageView) convertView.findViewById(R.id.imageView_install);
            imageView_start = (ImageView) convertView.findViewById(R.id.imageView_start);
            imageView_pause = (ImageView) convertView.findViewById(R.id.imageView_pause);
            imageView_reset = (ImageView) convertView.findViewById(R.id.imageView_reset);

            imageView_update.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkLoadApk();
                    // TODO: 2018/9/26 更新页面 更新---
                    MapbarMobStat.onEvent(mContext,"F0140","更新");
                }
            });

            imageView_install.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    try {
                        File file = new File(downloadInfo.getFileSavePath());
                        if (file.exists()) {
                            AppUtil.installApp(mContext, file);
                        } else {
                            mAif.showDialog("温馨提示", "该安装包已删除，是否继续更新?", "确定", "取消", new OnDialogListener() {
                                @Override
                                public void onOk() {
                                    try {
                                        setButtonState(Configs.APP_BUTTON_STATUS_PAUSE, UpdateViewHolder.this);
                                        lcb_download_progress.setProgress(0);
                                        downloadInfo.setLoadSuccess(false);
                                        downloadInfo.setProgress(0);
                                        DownloadRequestCallBack callBack = new DownloadRequestCallBack();
                                        MainApplication.downloadManager.resumeDownload(downloadInfo, callBack);
                                        callBack.setUserTag(new WeakReference<UpdateViewHolder>(UpdateViewHolder.this));
                                        UpdateViewHolder.this.setCallBack(callBack);
                                    } catch (DbException e) {
                                        LogUtils.e(e.getMessage(), e);
                                    }
                                }

                                @Override
                                public void onCancel() {
                                    try {
//                                        tv_app_size.setText(df.format(Double.parseDouble(mAppVersion.app_size)) + "MB");
                                        tv_app_size.setText(String.format("%1$sMB", df.format(Double.parseDouble(mAppVersion.app_size))));
                                        MainApplication.downloadManager.removeDownload(downloadInfo);
                                        setButtonState(Configs.APP_BUTTON_STATUS_UPDATE, UpdateViewHolder.this);
                                    } catch (DbException e) {
                                        LogUtils.e(e.getMessage(), e);
                                    }
                                }
                            });
                        }
                    } catch (Exception e) {
                        LogUtils.e(e.getMessage(), e);
                    }
                }
            });

            imageView_start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkStartApk();
                }
            });

            imageView_pause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        MainApplication.downloadManager.stopDownload(downloadInfo);
                        setButtonState(Configs.APP_BUTTON_STATUS_START, UpdateViewHolder.this);
                    } catch (DbException e) {
                        e.printStackTrace();
                    }
                }
            });

            imageView_reset.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        MainApplication.downloadManager.removeDownload(downloadInfo);
                        File file = new File(downloadInfo.getFileSavePath());
                        if (file.exists()) file.delete();
//                        tv_app_size.setText(df.format(Double.parseDouble(mAppVersion.app_size)) + "MB");
                        tv_app_size.setText(String.format("%1$sMB", df.format(Double.parseDouble(mAppVersion.app_size))));
                        setButtonState(Configs.APP_BUTTON_STATUS_UPDATE, UpdateViewHolder.this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        public void initData() {
            MainApplication.imageLoader.display(imageView_app_icon, mAppVersion.icon_path);
            tv_app_name.setText(mAppVersion.getApp_name());
//            tv_app_size.setText(df.format(Double.parseDouble(mAppVersion.app_size)) + "MB");
            tv_app_size.setText(String.format("%1$sMB", df.format(Double.parseDouble(mAppVersion.app_size))));
            if (mAppVersion.official_flag == 1) {
                imageView_official.setVisibility(View.VISIBLE);
            } else {
                imageView_official.setVisibility(View.INVISIBLE);
            }
            if (downloadInfo != null) {
                refresh();
            } else {
                setButtonState(Configs.APP_BUTTON_STATUS_UPDATE, this);
            }
        }

        /**
         * @return void :
         * @Title: checkLoadApk
         * @Description: 依据网络状态对话框提示下载apk
         * @author : wangzc
         * @date 2016年8月31日
         */

        private void checkLoadApk() {
            if (!CommonUtil.isNetworkAvailable(mContext)) {
                mAif.showAlert(R.string.net_unconnect_imp_label);
                return;
            }

            if (CommonUtil.isWifiAvailable(mContext)) { //wifi下载
                setButtonState(Configs.APP_BUTTON_STATUS_PAUSE, UpdateViewHolder.this);
                DownloadInfo downloadInfo = loadApk(mAppVersion);
                setDownloadInfo(downloadInfo);
                HttpHandler<File> handler = downloadInfo.getHandler();
                if (handler != null) {
                    RequestCallBack callBack = handler.getRequestCallBack();
                    if (callBack instanceof DownloadManager.ManagerCallBack) {
                        DownloadManager.ManagerCallBack managerCallBack = (DownloadManager.ManagerCallBack) callBack;
                        managerCallBack.setBaseCallBack(new DownloadRequestCallBack());
                    }
                    callBack.setUserTag(new WeakReference<UpdateViewHolder>(UpdateViewHolder.this));
                    setCallBack(callBack);
                }
            } else { //当用户使用2G/3G/4G网络下载
                Resources resources = mContext.getResources();
                String content = MainApplication.mAppPreferce.netWorkAllow ? resources.getString(R.string.net_is_workAllow_open)
                        : resources.getString(R.string.net_is_workAllow_close);
                mAif.showDialog("温馨提示", content, "确定", "取消", new OnDialogListener() {
                    @Override
                    public void onOk() {
                        setButtonState(Configs.APP_BUTTON_STATUS_START, UpdateViewHolder.this);
                        DownloadInfo downloadInfo = loadApk(mAppVersion);
                        setDownloadInfo(downloadInfo);
                        HttpHandler<File> handler = downloadInfo.getHandler();
                        if (handler != null) {
                            RequestCallBack callBack = handler.getRequestCallBack();
                            if (callBack instanceof DownloadManager.ManagerCallBack) {
                                DownloadManager.ManagerCallBack managerCallBack = (DownloadManager.ManagerCallBack) callBack;
                                managerCallBack.setBaseCallBack(new DownloadRequestCallBack());
                            }
                            callBack.setUserTag(new WeakReference<UpdateViewHolder>(UpdateViewHolder.this));
                            setCallBack(callBack);
                        }
                    }

                    @Override
                    public void onCancel() {

                    }
                });
            }
        }

        /**
         * @return void :
         * @Title: checkstartApk
         * @Description: 依据网络状态对话框提示开始继续下载apk
         * @author : wangzc
         * @date 2016年8月31日
         */

        private void checkStartApk() {
            if (!CommonUtil.isNetworkAvailable(mContext)) {
                mAif.showAlert(R.string.net_unconnect_imp_label);
                return;
            }

            if (CommonUtil.isWifiAvailable(mContext)) { //wifi下载
                try {
                    setButtonState(Configs.APP_BUTTON_STATUS_PAUSE, UpdateViewHolder.this);
                    MainApplication.downloadManager.resumeDownload(downloadInfo, UpdateViewHolder.this.callBack);
                } catch (DbException e) {
                    e.printStackTrace();
                }
            } else { //当用户使用2G/3G/4G网络下载
                Resources resources = mContext.getResources();
                String content = MainApplication.mAppPreferce.netWorkAllow ? resources.getString(R.string.net_is_workAllow_open)
                        : resources.getString(R.string.net_is_workAllow_close);
                mAif.showDialog("温馨提示", content, "确定", "取消", new OnDialogListener() {
                    @Override
                    public void onOk() {
                        try {
                            setButtonState(Configs.APP_BUTTON_STATUS_PAUSE, UpdateViewHolder.this);
                            MainApplication.downloadManager.resumeDownload(downloadInfo, UpdateViewHolder.this.callBack);
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
         * @return void :
         * @Title: checkstartApk
         * @Description: 检测现在的apk包是否完整可用
         * @author : wangzc
         * @date 2016年12月20日
         */

        private void checkApkMd5() {
            ((MainActivity) mAif).showDialog("温馨提示", downloadInfo.getFileName() + "安装包已损坏，是否重新下载?", "重新下载", "取消", new OnDialogListener() {
                @Override
                public void onOk() {
                    try {
                        setButtonState(Configs.APP_BUTTON_STATUS_PROGRESS, UpdateViewHolder.this);
                        lcb_download_progress.setProgress(0);
                        downloadInfo.setLoadSuccess(false);
                        downloadInfo.setProgress(0);
                        MainApplication.downloadManager.resumeDownload(downloadInfo, UpdateViewHolder.this.callBack);
                    } catch (Exception e) {
                    }
                }

                @Override
                public void onCancel() {
                    try {
                        MainApplication.downloadManager.removeDownload(downloadInfo);
//                        tv_app_size.setText(df.format(Double.parseDouble(mAppVersion.app_size)) + "MB");
                        tv_app_size.setText(String.format("%1$sMB", df.format(Double.parseDouble(mAppVersion.app_size))));
                        setButtonState(Configs.APP_BUTTON_STATUS_UPDATE, UpdateViewHolder.this);
                    } catch (Exception e) {
                    }
                }
            }, new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        dialog.dismiss();
                        try {
                            MainApplication.downloadManager.removeDownload(downloadInfo);
//                            tv_app_size.setText(df.format(Double.parseDouble(mAppVersion.app_size)) + "MB");
                            tv_app_size.setText(String.format("%1$sMB", df.format(Double.parseDouble(mAppVersion.app_size))));
                            setButtonState(Configs.APP_BUTTON_STATUS_UPDATE, UpdateViewHolder.this);
                        } catch (Exception e) {
                        }
                        return true;
                    }
                    return false;
                }
            });
        }

        public void refresh() {
            if (downloadInfo == null) return;
            String currentSize = df.format(downloadInfo.getSize() * downloadInfo.getProgress() / downloadInfo.getFileLength()) + "MB";
            if ("NaNMB".equals(currentSize)) currentSize = "0MB";
            HttpHandler.State state = downloadInfo.getState();
            if (state != null) {
                switch (state) {
                    case WAITING:
                    case STARTED:
                    case LOADING:
                        setButtonState(Configs.APP_BUTTON_STATUS_PAUSE, UpdateViewHolder.this);
                        try {
                            tv_app_size.setText(currentSize + "/" + df.format(downloadInfo.getSize()) + "MB");
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
                        setButtonState(Configs.APP_BUTTON_STATUS_START, UpdateViewHolder.this);
                        try {
                            tv_app_size.setText(currentSize + "/" + df.format(downloadInfo.getSize()) + "MB");
                            if (downloadInfo.getFileLength() > 0) {
                                lcb_download_progress.setProgress((int) (downloadInfo.getProgress() * 100 / downloadInfo.getFileLength()));
                            } else {
                                lcb_download_progress.setProgress(0);
                            }
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                        break;
                    case SUCCESS:
                        setButtonState(Configs.APP_BUTTON_STATUS_INSTALL, UpdateViewHolder.this);
                        try {
                            tv_app_size.setText(df.format(downloadInfo.getSize()) + "MB/" + df.format(downloadInfo.getSize()) + "MB");
                            if (!downloadInfo.isLoadSuccess) {
                                downloadInfo.setLoadSuccess(true);
                                MainApplication.dbUtils.saveOrUpdate(downloadInfo);
                                sendLoadSuccessLog(downloadInfo.getAppId(), downloadInfo.getApp_v_id());
                                File file = new File(downloadInfo.getFileSavePath());
                                if (file.exists()) {
                                    if (CommonUtil.verifyFileMD5(file, downloadInfo.getMd5())) {//校验通过
                                        AppUtil.installApp(mContext, file);
                                    } else {//校验失败，删除下载的损坏文件
                                        file.delete();
                                        checkApkMd5();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            LogUtils.e(e.getMessage(), e);
                        }
                        break;
                    case FAILURE:
                        try {
                            setButtonState(Configs.APP_BUTTON_STATUS_START, UpdateViewHolder.this);
                            tv_app_size.setText(currentSize + "/" + df.format(downloadInfo.getSize()) + "MB");
                            if (downloadInfo.getFileLength() > 0) {
                                lcb_download_progress.setProgress((int) (downloadInfo.getProgress() * 100 / downloadInfo.getFileLength()));
                            } else {
                                lcb_download_progress.setProgress(0);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                }
            } else {
                if (downloadInfo.getProgress() == 100) {
                    setButtonState(Configs.APP_BUTTON_STATUS_INSTALL, UpdateViewHolder.this);
                } else {
                    setButtonState(Configs.APP_BUTTON_STATUS_START, UpdateViewHolder.this);
                    try {
                        tv_app_size.setText(currentSize + "/" + df.format(downloadInfo.getSize()) + "MB");
                        if (downloadInfo.getFileLength() > 0) {
                            lcb_download_progress.setProgress((int) (downloadInfo.getProgress() * 100 / downloadInfo.getFileLength()));
                        } else {
                            lcb_download_progress.setProgress(0);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 需要更新的app列表的适配器
     */
    private class UpdateAdapter extends RecyclerView.Adapter<UpdateViewHolder> {
        DecimalFormat df = new DecimalFormat("0.00");

        @Override
        public UpdateViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new UpdateViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_manage_version_view, parent, false));
        }

        @Override
        public void onBindViewHolder(UpdateViewHolder holder, int position) {
            holder.initView();
            AppVersion version = appVersions.get(position);
            DownloadInfo downloadInfo = MainApplication.downloadManager.isAppLoading(version.app_id);
            holder.setAppVersion(version);

            if (downloadInfo != null) {
                holder.setDownloadInfo(downloadInfo);
            } else {
                holder.setDownloadInfo(null);
            }

            holder.initData();

            if (downloadInfo != null) {
                if (downloadInfo.isLoadSuccess() || downloadInfo.getProgress() == 100) {
                    downloadInfo.setLoadSuccess(true);
                    setButtonState(Configs.APP_BUTTON_STATUS_INSTALL, holder);
                    holder.tv_app_size.setText(df.format(downloadInfo.getSize()) + "MB/" + df.format(downloadInfo.getSize()) + "MB");
                } else {
                    try {
                        HttpHandler<File> handler = downloadInfo.getHandler();
                        if (handler == null) {
                            MainApplication.downloadManager.resumeDownload(downloadInfo, new DownloadRequestCallBack());
                            handler = downloadInfo.getHandler();
                        }
                        if (handler != null) {
                            RequestCallBack callBack = handler.getRequestCallBack();
                            if (callBack instanceof DownloadManager.ManagerCallBack) {
                                DownloadManager.ManagerCallBack managerCallBack = (DownloadManager.ManagerCallBack) callBack;
                                managerCallBack.setBaseCallBack(new DownloadRequestCallBack());
                            }
                            callBack.setUserTag(new WeakReference<UpdateViewHolder>(holder));
                            holder.setCallBack(callBack);
                        }
                    } catch (Exception e) {
                        Log.e("message", "exception:" + e.getMessage());
                    }
                }
            }
        }

        @Override
        public int getItemCount() {
            return appVersions.size();
        }
    }

    /**
     * <p>
     * 功能描述
     * </p>
     * 判断更新安装显示状态
     *
     * @param state
     * @param holder
     * @author wangzhichao
     * @date 2015年9月15日
     */
    private void setButtonState(int state, UpdateViewHolder holder) {
        holder.imageView_update.setVisibility(View.GONE);
        holder.imageView_install.setVisibility(View.GONE);
        holder.imageView_start.setVisibility(View.GONE);
        holder.imageView_pause.setVisibility(View.GONE);
        holder.imageView_reset.setVisibility(View.GONE);
        holder.lcb_download_progress.setVisibility(View.INVISIBLE);
        switch (state) {
            case Configs.APP_BUTTON_STATUS_UPDATE:
                holder.imageView_update.setVisibility(View.VISIBLE);
                break;
            case Configs.APP_BUTTON_STATUS_INSTALL:
                holder.imageView_install.setVisibility(View.VISIBLE);
                break;
            case Configs.APP_BUTTON_STATUS_START:
                holder.imageView_start.setVisibility(View.VISIBLE);
                holder.imageView_reset.setVisibility(View.VISIBLE);
                holder.lcb_download_progress.setVisibility(View.VISIBLE);
                break;
            case Configs.APP_BUTTON_STATUS_PAUSE:
                holder.imageView_pause.setVisibility(View.VISIBLE);
                holder.imageView_reset.setVisibility(View.VISIBLE);
                holder.lcb_download_progress.setVisibility(View.VISIBLE);
                break;
        }
    }

    private class DownloadRequestCallBack extends RequestCallBack<File> {

        private void refreshListItem(int code) {
            if (userTag == null)
                return;
            WeakReference<UpdateViewHolder> tag = (WeakReference<UpdateViewHolder>) userTag;
            UpdateViewHolder holder = tag.get();
            if (holder != null) {
                if (code == 416 && holder.downloadInfo != null) {//下载完成，416异常编号实际已经下载完成
                    holder.downloadInfo.setState(HttpHandler.State.SUCCESS);
                }
                holder.refresh();
            }
        }

        @Override
        public void onStart() {
            refreshListItem(0);
        }

        @Override
        public void onLoading(long total, long current, boolean isUploading) {
            refreshListItem(0);
        }

        @Override
        public void onSuccess(ResponseInfo<File> responseInfo) {
            refreshListItem(0);
        }

        @Override
        public void onFailure(HttpException error, String msg) {
            refreshListItem(error.getExceptionCode());
        }

        @Override
        public void onCancelled() {
            refreshListItem(0);
        }
    }

    @Override
    public void onCommandReceive(CommandInfo ci) {
        super.onCommandReceive(ci);
        try {
            if ("removedPackage".equals(ci.getMethod()) && ci.getExtData() != null) {
                Intent intent = (Intent) ci.getExtData();
                String packageName = intent.getData().getSchemeSpecificPart();
                Iterator<AppVersion> iterator = appVersions.iterator();
                do {
                    String pkgName = iterator.next().getApp_package_name();
                    if (packageName.equals(pkgName)) {
                        iterator.remove();
                        updateAdapter.notifyDataSetChanged();
                        break;
                    }
                } while (iterator.hasNext());
                MainApplication.dbUtils.delete(AppVersion.class, WhereBuilder.b("app_package_name", "=", packageName));
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
            LinkedHashMap<String, String> paramMap = new LinkedHashMap<String, String>();
            paramMap.put("app_id", app_id);
            paramMap.put("app_v_id", app_v_id);
            provider.loadRecordLogs(paramMap);
        }
    }

    @Override
    public int getMyViewPosition() {
        return Configs.VIEW_POSITION_MANNGER_UPDATE;
    }
}
