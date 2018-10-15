package com.wedrive.welink.appstore.app.view;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;

import android.content.Context;
import android.content.DialogInterface;
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

import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.util.LogUtils;
import com.mapbar.android.model.ActivityInterface;
import com.mapbar.android.model.BasePage;
import com.mapbar.android.model.CommandInfo;
import com.mapbar.android.model.OnDialogListener;
import com.mapbar.android.statistics.api.MapbarMobStat;
import com.wedrive.welink.appstore.Configs;
import com.wedrive.welink.appstore.MainActivity;
import com.wedrive.welink.appstore.MainApplication;
import com.wedrive.welink.appstore.R;
import com.wedrive.welink.appstore.app.download.DownloadInfo;
import com.wedrive.welink.appstore.app.download.DownloadManager;
import com.wedrive.welink.appstore.app.provider.SearchProvider;
import com.wedrive.welink.appstore.app.util.AppUtil;
import com.wedrive.welink.appstore.app.util.CommonUtil;

public class ManngerLoadPage extends BasePage {
    private final static String TAG = "ManngerLoadPage";
    private Context mContext;
    private ActivityInterface mAif;
    private DecimalFormat df = new DecimalFormat("0.00");

    private ManagerLoadAdapter loadAdapter;

    public ManngerLoadPage(Context context, View view, ActivityInterface aif) {
        super(context, view, aif);

        mContext = context;
        mAif = aif;
        initView(view);
    }

    private void initView(View view) {
        RecyclerView rvLoad = (RecyclerView) view.findViewById(R.id.lVi_load_apps);
        LinearLayoutManager manager = new LinearLayoutManager(mContext);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        rvLoad.setLayoutManager(manager);
        loadAdapter = new ManagerLoadAdapter();
        rvLoad.setAdapter(loadAdapter);
    }

    @Override
    public void viewWillAppear(int flag) {
        super.viewWillAppear(flag);
        MainActivity.mMainActivity.setFirstAndSecondTitle("下载中心", "管理");
        MapbarMobStat.onPageStart(mContext, Configs.AppStore_Interface_DownloadCenterPage);
        if (loadAdapter != null) loadAdapter.notifyDataSetChanged();
    }

    @Override
    public void viewWillDisappear(int flag) {
        super.viewWillDisappear(flag);
        MapbarMobStat.onPageEnd(mContext, Configs.AppStore_Interface_DownloadCenterPage);
    }

    @Override
    public void onResume() {
        super.onResume();
        //MapbarMobStat.onPageStart(mContext, Configs.AppStore_Interface_DownloadCenterPage);
        if (loadAdapter != null) loadAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        //MapbarMobStat.onPageEnd(mContext, Configs.AppStore_Interface_DownloadCenterPage);
    }

    /**
     * 适配器
     */
    public class ManagerLoadAdapter extends RecyclerView.Adapter<LoadViewHolder> {

        @Override
        public LoadViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new LoadViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_manage_load_view, parent, false));
        }

        @Override
        public void onBindViewHolder(LoadViewHolder holder, int position) {
            DownloadInfo downloadInfo = MainApplication.downloadManager.getDownloadInfo(position);
            holder.setDownloadInfo(downloadInfo);
            holder.initView();
            holder.refresh();
            MainApplication.imageLoader.display(holder.imageView_app_icon, downloadInfo.getLogoUrl());
            holder.tv_app_name.setText(downloadInfo.getFileName());
            if (downloadInfo.getOfficial_flag() == 1) {
                holder.imageView_official.setVisibility(View.VISIBLE);
            } else {
                holder.imageView_official.setVisibility(View.INVISIBLE);
            }

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
                        callBack.setUserTag(new WeakReference<LoadViewHolder>(holder));
                        holder.setCallBack(callBack);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public int getItemCount() {
            return MainApplication.downloadManager.getDownloadInfoListCount();
        }
    }

    /**
     * 适配器的ViewHolder
     */
    public class LoadViewHolder extends RecyclerView.ViewHolder {
        private View convertView;
        private ImageView imageView_app_icon;
        private ImageView imageView_official;
        private TextView tv_app_name, tv_app_size;
        private ProgressBar lcb_download_progress;
        private ImageView imageView_start, imageView_pause, imageView_instal, imageView_delete;

        private DownloadInfo downloadInfo;
        private RequestCallBack callBack;

        public LoadViewHolder(View itemView) {
            super(itemView);
            convertView = itemView;
        }

        public void setCallBack(RequestCallBack callBack) {
            this.callBack = callBack;
        }

        public void setDownloadInfo(DownloadInfo downloadInfo) {
            this.downloadInfo = downloadInfo;
//            refresh();
        }

        private void initView() {
            imageView_app_icon = (ImageView) convertView.findViewById(R.id.imageView_app_icon);
            imageView_official = (ImageView) convertView.findViewById(R.id.imageView_official);
            tv_app_name = (TextView) convertView.findViewById(R.id.tv_app_name);
            tv_app_size = (TextView) convertView.findViewById(R.id.tv_app_size);
            lcb_download_progress = (ProgressBar) convertView.findViewById(R.id.lcb_download_progress);
            imageView_start = (ImageView) convertView.findViewById(R.id.imageView_start);
            imageView_pause = (ImageView) convertView.findViewById(R.id.imageView_pause);
            imageView_instal = (ImageView) convertView.findViewById(R.id.imageView_instal);
            imageView_delete = (ImageView) convertView.findViewById(R.id.imageView_delete);
            tv_app_size.setText("0MB/" + df.format(downloadInfo.getSize()) + "MB");

            imageView_start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    checkStartApk();
                }
            });

            imageView_pause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    try {
                        MainApplication.downloadManager.stopDownload(downloadInfo);
                        setButtonState(Configs.APP_BUTTON_STATUS_START, LoadViewHolder.this);
                    } catch (DbException e) {
                        LogUtils.e(e.getMessage(), e);
                    }
                }
            });

            imageView_instal.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    // TODO: 2018/9/26 下载页面 安装---
                    MapbarMobStat.onEvent(mContext,"F0138","安装");
                    try {
                        File file = new File(downloadInfo.getFileSavePath());
                        if (file.exists()) {
                            AppUtil.installApp(mContext, file);
                        } else {
                            mAif.showDialog("温馨提示", "该安装包已删除，是否继续下载?", "确定", "取消", new OnDialogListener() {
                                @Override
                                public void onOk() {
                                    setButtonState(Configs.APP_BUTTON_STATUS_PAUSE, LoadViewHolder.this);
                                    lcb_download_progress.setProgress(0);
                                    try {
                                        downloadInfo.setLoadSuccess(false);
                                        downloadInfo.setProgress(0);
                                        DownloadRequestCallBack callBack = new DownloadRequestCallBack();
                                        MainApplication.downloadManager.resumeDownload(downloadInfo, callBack);
                                        callBack.setUserTag(new WeakReference<LoadViewHolder>(LoadViewHolder.this));
                                        LoadViewHolder.this.setCallBack(callBack);
                                    } catch (DbException e) {
                                        LogUtils.e(e.getMessage(), e);
                                    }
                                }

                                @Override
                                public void onCancel() {
                                    try {
                                        MainApplication.downloadManager.removeDownload(downloadInfo);
                                        //bug号：WELM-3139；bug链接：https://wdjira.mapbar.com/browse/WELM-3139
                                        loadAdapter.notifyDataSetChanged();
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

            imageView_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    // TODO: 2018/9/26 下载页面 删除---
                    MapbarMobStat.onEvent(mContext,"F0137","删除");
                    mAif.showDialog("温馨提示", "应用正在下载确定删除", "确定", "取消", new OnDialogListener() {
                        @Override
                        public void onOk() {
                            try {
                                MainApplication.downloadManager.removeDownload(downloadInfo);
                                File file = new File(downloadInfo.getFileSavePath());
                                if (file.exists()) file.delete();
                                loadAdapter.notifyDataSetChanged();
                            } catch (Exception e) {
                                Log.e("message", "exception:" + e.getMessage());
                            }
                        }

                        @Override
                        public void onCancel() {

                        }
                    });
                }
            });
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
                    setButtonState(Configs.APP_BUTTON_STATUS_PAUSE, LoadViewHolder.this);
                    MainApplication.downloadManager.resumeDownload(downloadInfo, LoadViewHolder.this.callBack);
                } catch (DbException e) {
                    LogUtils.e(e.getMessage(), e);
                }
            } else { //当用户使用2G/3G/4G网络下载
                Resources resources = mContext.getResources();
                String content = MainApplication.mAppPreferce.netWorkAllow ? resources.getString(R.string.net_is_workAllow_open)
                        : resources.getString(R.string.net_is_workAllow_close);
                mAif.showDialog("温馨提示", content, "确定", "取消", new OnDialogListener() {
                    @Override
                    public void onOk() {
                        try {
                            setButtonState(Configs.APP_BUTTON_STATUS_PAUSE, LoadViewHolder.this);
                            MainApplication.downloadManager.resumeDownload(downloadInfo, LoadViewHolder.this.callBack);
                        } catch (DbException e) {
                            LogUtils.e(e.getMessage(), e);
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
                        downloadInfo.setProgress(0);
                        downloadInfo.setLoadSuccess(false);
                        setButtonState(Configs.APP_BUTTON_STATUS_PAUSE, LoadViewHolder.this);
                        MainApplication.downloadManager.resumeDownload(downloadInfo, LoadViewHolder.this.callBack);
                    } catch (Exception e) {
                        LogUtils.e(e.getMessage(), e);
                    }
                }

                @Override
                public void onCancel() {
                    try {
                        MainApplication.downloadManager.removeDownload(downloadInfo);
                        loadAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        LogUtils.e(e.getMessage(), e);
                    }
                }
            }, new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        dialog.dismiss();
                        try {
                            MainApplication.downloadManager.removeDownload(downloadInfo);
                            loadAdapter.notifyItemChanged(getAdapterPosition());
                        } catch (Exception e) {
                            LogUtils.e(e.getMessage(), e);
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
                        setButtonState(Configs.APP_BUTTON_STATUS_PAUSE, LoadViewHolder.this);
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
                        break;
                    case CANCELLED:
                        setButtonState(Configs.APP_BUTTON_STATUS_START, LoadViewHolder.this);
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
                        break;
                    case SUCCESS:
                        setButtonState(Configs.APP_BUTTON_STATUS_INSTALL, LoadViewHolder.this);
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
                                    } else {//校验失败
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
                        setButtonState(Configs.APP_BUTTON_STATUS_START, LoadViewHolder.this);
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
                        break;
                }
            } else {
                if (downloadInfo.getProgress() == 100) {
                    setButtonState(Configs.APP_BUTTON_STATUS_INSTALL, LoadViewHolder.this);
                } else {
                    setButtonState(Configs.APP_BUTTON_STATUS_START, LoadViewHolder.this);
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

    private class DownloadRequestCallBack extends RequestCallBack<File> {

        private void refreshListItem(int code) {
            if (userTag == null)
                return;
            WeakReference<LoadViewHolder> tag = (WeakReference<LoadViewHolder>) userTag;
            LoadViewHolder holder = tag.get();
            if (holder != null) {
                if (code == 416 && holder.downloadInfo != null) {
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
    private void setButtonState(int state, LoadViewHolder holder) {
        holder.imageView_start.setVisibility(View.GONE);
        holder.imageView_pause.setVisibility(View.GONE);
        holder.imageView_instal.setVisibility(View.GONE);
        holder.imageView_delete.setVisibility(View.GONE);
        holder.lcb_download_progress.setVisibility(View.INVISIBLE);

        switch (state) {
            case Configs.APP_BUTTON_STATUS_PAUSE:
                holder.imageView_pause.setVisibility(View.VISIBLE);
                holder.imageView_delete.setVisibility(View.VISIBLE);
                holder.lcb_download_progress.setVisibility(View.VISIBLE);
                break;
            case Configs.APP_BUTTON_STATUS_START:
                holder.imageView_start.setVisibility(View.VISIBLE);
                holder.imageView_delete.setVisibility(View.VISIBLE);
                holder.lcb_download_progress.setVisibility(View.VISIBLE);
                break;
            case Configs.APP_BUTTON_STATUS_INSTALL:
                holder.imageView_instal.setVisibility(View.VISIBLE);
                holder.imageView_delete.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onCommandReceive(CommandInfo ci) {
        super.onCommandReceive(ci);
        String Method = ci.getMethod();
        if ("addedPackage".equals(Method)) {
            if (loadAdapter != null) loadAdapter.notifyDataSetChanged();
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
        return Configs.VIEW_POSITION_MANNGER_LOAD;
    }
}
