package com.wedrive.welink.appstore.app.view;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.task.TaskHandler;
import com.lidroid.xutils.util.LogUtils;
import com.mapbar.android.model.ActivityInterface;
import com.mapbar.android.model.FilterObj;
import com.mapbar.android.model.Log;
import com.mapbar.android.model.OnDialogListener;
import com.mapbar.android.model.OnProviderListener;
import com.mapbar.android.model.ProviderResult;
import com.mapbar.android.net.MyHttpHandler;
import com.mapbar.android.statistics.api.MapbarMobStat;
import com.wedrive.welink.appstore.Configs;
import com.wedrive.welink.appstore.MainActivity;
import com.wedrive.welink.appstore.MainApplication;
import com.wedrive.welink.appstore.R;
import com.wedrive.welink.appstore.app.download.DownloadInfo;
import com.wedrive.welink.appstore.app.download.DownloadManager;
import com.wedrive.welink.appstore.app.model.AppInfo;
import com.wedrive.welink.appstore.app.model.HisbillboardBean;
import com.wedrive.welink.appstore.app.model.WeekbillboardBean;
import com.wedrive.welink.appstore.app.provider.SearchProvider;
import com.wedrive.welink.appstore.app.util.AppUtil;
import com.wedrive.welink.appstore.app.util.CommonUtil;

public class HomePageList implements OnProviderListener {
    public static String TAG = "HomePageList";
    private ActivityInterface mAif;
    private Context mContext;

    private RecyclerAdapter weekAdapter;
    private RecyclerAdapter hisAdapter;

    private List<WeekbillboardBean> surgeApps = new ArrayList<WeekbillboardBean>();// 飙升
    private List<HisbillboardBean> sutraApps = new ArrayList<HisbillboardBean>();// 经典

    private static final int WEEK_PAGE = 0;//飙升榜
    private static final int HIS_PAGE = 1;//经典榜

    private LayoutInflater inflater;

    public HomePageList(View view, ActivityInterface aif, Context context) {
        mAif = aif;
        mContext = context;
        inflater = LayoutInflater.from(context);
        initView(view);
    }

    private void initView(View mView) {
        RecyclerView rvSurge = (RecyclerView) mView.findViewById(R.id.lVi_surge);//飙升
        RecyclerView rvSutra = (RecyclerView) mView.findViewById(R.id.lVi_sutra);//经典

        rvSurge.setLayoutManager(new LinearLayoutManager(mContext));
        rvSutra.setLayoutManager(new LinearLayoutManager(mContext));

        weekAdapter = new RecyclerAdapter(0);
        hisAdapter = new RecyclerAdapter(1);

        rvSurge.setAdapter(weekAdapter);
        rvSutra.setAdapter(hisAdapter);

//        rvSurge.addOnScrollListener(new PauseOnScrollListener(MainApplication.imageLoader.bitmapUtils, true, true));
//        rvSutra.addOnScrollListener(new PauseOnScrollListener(MainApplication.imageLoader.bitmapUtils, true, true));
    }

    @Override
    public void onProviderResponse(int requestCode, int responseCode, ProviderResult result) {
        if (responseCode == -1) {
            mAif.showAlert(R.string.dialog_loading_net_error);
        } else {
            switch (requestCode) {
                case Configs.REQUEST_CODE_BILLBOARD_APPS_LIST:
                    try {
                        JSONObject obj = new JSONObject(result.getResponseStr());
                        if (obj.has("status")) {
                            int status = obj.getInt("status");
                            if (status == 200) {
                                obj = obj.getJSONObject("data");
                                surgeApps.clear();
                                sutraApps.clear();

                                JSONArray WeekbillboardJsonArr = obj.getJSONArray("weekbillboard");
                                preserWeekbillboardJsonArr(WeekbillboardJsonArr);

                                JSONArray hisbillboardJsonArr = obj.getJSONArray("hisbillboard");
                                parserHisbillbaordJsonArr(hisbillboardJsonArr);

                                weekAdapter.notifyDataSetChanged();
                                hisAdapter.notifyDataSetChanged();

                                mAif.hideProgressDialog();
                                MainApplication.isListFirst = false;
                            } else {
                                mAif.hideProgressDialog();
                                if (obj.has("msg")) {
                                    mAif.showAlert(obj.getString("msg"));
                                }
                            }
                        } else {
                            mAif.hideProgressDialog();
                            mAif.showAlert("返回数据错误！");
                        }
                    } catch (Exception e) {
                        mAif.hideProgressDialog();
                        mAif.showAlert("解析数据出错！");
                    }
                    break;
            }
        }
    }

    @Override
    public void onReadResponse(int i, int i1) {

    }

    /**
     * 解析飙升排行数据
     */
    private void preserWeekbillboardJsonArr(JSONArray weekbillboardJsonArr) throws JSONException {
        if (weekbillboardJsonArr.length() > 0) {

            for (int i = 0; i < weekbillboardJsonArr.length(); i++) {
                JSONObject listobj = weekbillboardJsonArr.getJSONObject(i);
                WeekbillboardBean bean = new WeekbillboardBean();

                if (listobj.has("app_id"))
                    bean.setApp_id(listobj.getString("app_id"));
                if (listobj.has("name"))
                    bean.setName(listobj.getString("name"));
                if (listobj.has("app_v_id"))
                    bean.setApp_v_id(listobj.getString("app_v_id"));
                if (listobj.has("description"))
                    bean.setDescription(listobj.getString("description"));
                if (listobj.has("official_flag"))
                    bean.setOfficial_flag(listobj.getInt("official_flag"));
                if (listobj.has("icon_path"))
                    bean.setIcon_path(listobj.getString("icon_path"));
                if (listobj.has("size"))
                    bean.setSize(listobj.getString("size"));
                if (listobj.has("apk_path"))
                    bean.setApk_path(listobj.getString("apk_path"));
                if (listobj.has("package_name"))
                    bean.setPackage_name(listobj.getString("package_name"));
                if (listobj.has("version_no"))
                    bean.setVersion_no(listobj.getString("version_no"));
                if (listobj.has("scheme"))
                    bean.setApp_uri(listobj.getString("scheme"));
                if (listobj.has("md5"))
                    bean.setApp_md5(listobj.getString("md5"));
                if (listobj.has("score_avg")) {
                    String string = listobj.getString("score_avg");
                    if (string == null || string.equals("")) {
                        bean.setScore_avg(0);
                    }
                    bean.setScore_avg(Float.valueOf(string));
                }
                surgeApps.add(bean);
            }
            try {
                MainApplication.dbUtils.deleteAll(WeekbillboardBean.class);
                MainApplication.dbUtils.saveOrUpdateAll(surgeApps);
            } catch (DbException e) {
                Log.e("message", "exception:" + e.getMessage());
            }
        } else {
            mAif.showAlert("飙升榜返回数据为空！");
        }

    }

    /**
     * 解析经典排行数据
     */
    private void parserHisbillbaordJsonArr(JSONArray hisbillboardJsonArr) throws JSONException {
        if (hisbillboardJsonArr.length() > 0) {
            for (int i = 0; i < hisbillboardJsonArr.length(); i++) {
                JSONObject listobj = hisbillboardJsonArr.getJSONObject(i);
                HisbillboardBean bean = new HisbillboardBean();

                if (listobj.has("app_id"))
                    bean.setApp_id(listobj.getString("app_id"));
                if (listobj.has("name"))
                    bean.setName(listobj.getString("name"));
                if (listobj.has("app_v_id"))
                    bean.setApp_v_id(listobj.getString("app_v_id"));
                if (listobj.has("description"))
                    bean.setDescription(listobj.getString("description"));
                if (listobj.has("official_flag"))
                    bean.setOfficial_flag(listobj.getInt("official_flag"));
                if (listobj.has("icon_path"))
                    bean.setIcon_path(listobj.getString("icon_path"));
                if (listobj.has("size"))
                    bean.setSize(listobj.getString("size"));
                if (listobj.has("apk_path"))
                    bean.setApk_path(listobj.getString("apk_path"));
                if (listobj.has("package_name"))
                    bean.setPackage_name(listobj.getString("package_name"));
                if (listobj.has("version_no"))
                    bean.setVersion_no(listobj.getString("version_no"));
                if (listobj.has("scheme"))
                    bean.setApp_uri(listobj.getString("scheme"));
                if (listobj.has("md5"))
                    bean.setApp_md5(listobj.getString("md5"));
                if (listobj.has("score_avg")) {
                    String string = listobj.getString("score_avg");
                    if (string == null || string.equals("")) {
                        bean.setScore_avg(0);
                    }
                    bean.setScore_avg(Float.valueOf(string));
                }
                sutraApps.add(bean);
            }
            try {
                MainApplication.dbUtils.deleteAll(HisbillboardBean.class);
                MainApplication.dbUtils.saveOrUpdateAll(sutraApps);
            } catch (DbException e) {
                Log.e("message", "exception:" + e.getMessage());
            }
        } else {
            mAif.showAlert("经典榜返回数据为空！");
        }

    }

    /**
     * 减缓recyclerview滑动过快的监听器
     */
    private class PauseOnScrollListener extends RecyclerView.OnScrollListener {
        private TaskHandler taskHandler;
        private final boolean pauseOnScroll;
        private final boolean pauseOnFling;

        PauseOnScrollListener(TaskHandler taskHandler, boolean pauseOnScroll, boolean pauseOnFling) {
            this.taskHandler = taskHandler;
            this.pauseOnScroll = pauseOnScroll;
            this.pauseOnFling = pauseOnFling;
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            switch (newState) {
                case RecyclerView.SCROLL_STATE_IDLE:
                    taskHandler.resume();
                    break;
                case RecyclerView.SCROLL_STATE_DRAGGING:
                    if (pauseOnScroll)
                        taskHandler.pause();
                    break;
                case RecyclerView.SCROLL_STATE_SETTLING:
                    if (pauseOnFling)
                        taskHandler.pause();
                    break;
            }
        }
    }

    /**
     * 飙升榜适配器
     */
    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerHolder> {
        private int type;

        private RecyclerAdapter(int type) {
            this.type = type;
        }

        @Override
        public RecyclerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new RecyclerHolder(inflater.inflate(R.layout.item_home_list_view, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerHolder holder, int position) {
            DownloadInfo downloadInfo;
            switch (type) {
                case 0:
                    WeekbillboardBean weekData = surgeApps.get(position);
                    downloadInfo = MainApplication.downloadManager.isAppLoading(weekData.getApp_id());
                    holder.setData(weekData);
                    break;
                default:
                    HisbillboardBean hisData = sutraApps.get(position);
                    downloadInfo = MainApplication.downloadManager.isAppLoading(hisData.getApp_id());
                    holder.setData(hisData);
                    break;
            }
            holder.setDownloadInfo(downloadInfo);
            holder.initData(position);

            if (downloadInfo != null) {
                if (downloadInfo.isLoadSuccess() || downloadInfo.getProgress() == 100) {
                    downloadInfo.setLoadSuccess(true);
                    holder.setButtonState(Configs.APP_BUTTON_STATUS_INSTALL, holder);
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
                                DownloadRequestCallBack downloadRequestCallBack = new DownloadRequestCallBack();
                                downloadRequestCallBack.setUserTags(callBack.getUserTags());
                                managerCallBack.setBaseCallBack(downloadRequestCallBack);
                            }
                            callBack.addUserTag(new WeakReference<RecyclerHolder>(holder));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public int getItemCount() {
            return surgeApps.size();
        }
    }

    /**
     * recyclerView的ViewHolder
     */
    private class RecyclerHolder extends RecyclerView.ViewHolder implements OnClickListener {
        private int type;
        private Object mData;
        private TextView tv_order;
        private ImageView img_icon;
        private TextView tv_app_size;
        private TextView tv_app_desc;
        private RatingBar rb_app;
        private TextView tv_app_name;
        private ImageView img_icon_rz;
        private ImageView img_btn_download;
        private ProgressBar lcb_download_progress;
        private LinearLayout ll_base;

        private DownloadInfo downloadInfo;

        public RecyclerHolder(View itemView) {
            super(itemView);
            initView(itemView);
        }

        public DownloadInfo getDownloadInfo() {
            return downloadInfo;
        }

        public void setDownloadInfo(DownloadInfo downloadInfo) {
            this.downloadInfo = downloadInfo;
        }

        public void setData(Object data) {
            this.mData = data;
            if (data instanceof WeekbillboardBean)
                type = WEEK_PAGE;
            if (data instanceof HisbillboardBean)
                type = HIS_PAGE;
        }

        public Object getData() {
            return mData;
        }

        /**
         * 初始化view
         */
        protected void initView(View view) {
            ll_base = (LinearLayout) view.findViewById(R.id.ll_base);
            tv_order = (TextView) view.findViewById(R.id.tv_order);
            img_icon = (ImageView) view.findViewById(R.id.img_icon);
            tv_app_size = (TextView) view.findViewById(R.id.tv_app_size);
            tv_app_desc = (TextView) view.findViewById(R.id.tv_app_desc);
            rb_app = (RatingBar) view.findViewById(R.id.rb_app);
            tv_app_name = (TextView) view.findViewById(R.id.tv_app_name);
            img_icon_rz = (ImageView) view.findViewById(R.id.img_icon_rz);
            img_btn_download = (ImageView) view.findViewById(R.id.imgView_details_download);
            lcb_download_progress = (ProgressBar) view.findViewById(R.id.lcb_download_progress);
            ll_base.setOnClickListener(this);
            img_btn_download.setOnClickListener(this);
        }

        /**
         * 给view设置数据
         */
        private void initData(int position) {
            MainApplication.imageLoader.display(img_icon, getIcon_path());
            if (getOfficial_flag() == 0) {
                img_icon_rz.setVisibility(View.INVISIBLE);
            } else if (getOfficial_flag() == 1) {
                img_icon_rz.setVisibility(View.VISIBLE);
            }
            DecimalFormat df = new DecimalFormat("0.00");
            tv_app_size.setText(String.format("%1$sMB", df.format(Double.parseDouble(getSize()))));
            rb_app.setRating(getAppGrade());
            tv_order.setText(String.format("%1$s", position + 1));
            tv_app_name.setText(getName());
            tv_app_desc.setText(getAppDesc());

            if (downloadInfo != null) {
                refresh();
            } else {
                setButtonState(checkButtonState(getPackageName(), Integer.parseInt(getVersionNo()), getApk_path()), this);
            }
        }

        /**
         * @param packageName 应用包名
         * @param nCode       服务器获取的版本号
         * @param apkPath     服务器获取的apkPath
         * @return
         */
        private int checkButtonState(String packageName, int nCode, String apkPath) {
            PackageInfo info = CommonUtil.isAppInstalled(mContext, packageName);

            if (info != null) {// 已安装，显示打开
                int oCode = info.versionCode;// 已安装的版本号
                if (nCode > oCode) {// 比较版本号
                    return Configs.APP_BUTTON_STATUS_UPDATE;
                } else {
                    return Configs.APP_BUTTON_STATUS_OPEN;
                }
            } else {// 没有安装
                String path = apkPath.substring(apkPath.lastIndexOf("/") + 1);
                path = MainApplication.apkDownloadPath + File.separator + path;
                File apk = new File(path);
                if (apk.exists()) {// 没有安装,有apk文件，显示安装
                    if (AppUtil.getAPKInfo(mContext, apk.getAbsolutePath()) != null) {
                        return Configs.APP_BUTTON_STATUS_INSTALL;
                    } else {
                        return Configs.APP_BUTTON_STATUS_DOWNLOAD;
                    }
                } else {// 没有安装,没有apk文件，显示下载
                    return Configs.APP_BUTTON_STATUS_DOWNLOAD;
                }
            }
        }

        private void setButtonState(int state, RecyclerHolder holder) {
            switch (state) {
                case Configs.APP_BUTTON_STATUS_DOWNLOAD:
                    holder.img_btn_download.setImageResource(R.drawable.ico_load);
                    holder.img_btn_download.setVisibility(View.VISIBLE);
                    holder.lcb_download_progress.setVisibility(View.GONE);
                    break;
                case Configs.APP_BUTTON_STATUS_INSTALL:
                    holder.img_btn_download.setImageResource(R.drawable.ico_install);
                    holder.img_btn_download.setVisibility(View.VISIBLE);
                    holder.lcb_download_progress.setVisibility(View.GONE);
                    break;
                case Configs.APP_BUTTON_STATUS_OPEN:
                    holder.img_btn_download.setImageResource(R.drawable.ico_open);
                    holder.img_btn_download.setVisibility(View.VISIBLE);
                    holder.lcb_download_progress.setVisibility(View.GONE);
                    break;
                case Configs.APP_BUTTON_STATUS_UPDATE:
                    holder.img_btn_download.setImageResource(R.drawable.ico_update);
                    holder.img_btn_download.setVisibility(View.VISIBLE);
                    holder.lcb_download_progress.setVisibility(View.GONE);
                    break;
                case Configs.APP_BUTTON_STATUS_PROGRESS:
                    holder.img_btn_download.setVisibility(View.GONE);
                    holder.lcb_download_progress.setVisibility(View.VISIBLE);
                    break;
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
                setButtonState(Configs.APP_BUTTON_STATUS_PROGRESS, this);
                loadApk();
                refreshItem(type, getAdapterPosition(), getApp_id());
            } else { //当用户使用2G/3G/4G网络下载
                Resources resources = mContext.getResources();
                String content = MainApplication.mAppPreferce.netWorkAllow ? resources.getString(R.string.net_is_workAllow_open)
                        : resources.getString(R.string.net_is_workAllow_close);
                mAif.showDialog("温馨提示", content, "确定", "取消", new OnDialogListener() {
                    @Override
                    public void onOk() {
                        setButtonState(Configs.APP_BUTTON_STATUS_PROGRESS, RecyclerHolder.this);
                        loadApk();
                        refreshItem(type, getAdapterPosition(), getApp_id());
                    }

                    @Override
                    public void onCancel() {

                    }
                });
            }
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.ll_base:
                    FilterObj detailObj = new FilterObj();
                    detailObj.setFlag(12);
                    List<String> objs = new ArrayList<String>();
                    switch (type) {
                        case WEEK_PAGE://飙升
                            objs.add(surgeApps.get(getAdapterPosition()).getApp_id());
                            objs.add(surgeApps.get(getAdapterPosition()).getApp_v_id());
                            break;
                        case HIS_PAGE://经典
                            objs.add(sutraApps.get(getAdapterPosition()).getApp_id());
                            objs.add(sutraApps.get(getAdapterPosition()).getApp_v_id());
                            break;
                    }
                    detailObj.setTag(objs);
                    mAif.showPage(Configs.VIEW_POSITION_HOME, Configs.VIEW_POSITION_DETAIL, detailObj, true, null, null);
                    // TODO: 2018/9/26 榜单界面 选择应用---
                    MapbarMobStat.onEvent(mContext,"F0130","选择应用");
                    break;
                case R.id.imgView_details_download:
                    switch (checkButtonState(getPackageName(), Integer.parseInt(getVersionNo()), getApk_path())) {
                        case Configs.APP_BUTTON_STATUS_DOWNLOAD://下载
                            checkLoadApk();
                            break;
                        case Configs.APP_BUTTON_STATUS_INSTALL://安装
                            String path = getApk_path().substring(getApk_path().lastIndexOf("/") + 1);
                            path = MainApplication.apkDownloadPath + File.separator + path;
                            File apk_install = new File(path);
                            if (apk_install.exists()) {
                                AppUtil.installApp(mContext, apk_install);
                            } else {
                                mAif.showDialog("温馨提示", "该安装包已删除，是否继续下载?", "确定", "取消", new OnDialogListener() {
                                    @Override
                                    public void onOk() {
                                        try {
                                            lcb_download_progress.setVisibility(View.VISIBLE);
                                            setButtonState(Configs.APP_BUTTON_STATUS_PROGRESS, RecyclerHolder.this);
                                            lcb_download_progress.setProgress(0);
                                            downloadInfo.setLoadSuccess(false);
                                            downloadInfo.setProgress(0);
                                            MainApplication.downloadManager.resumeDownload(downloadInfo, new DownloadRequestCallBack());
                                            refreshItem(type, getAdapterPosition(), getApp_id());
                                        } catch (DbException e) {
                                            LogUtils.e(e.getMessage(), e);
                                        }
                                    }

                                    @Override
                                    public void onCancel() {
                                        try {
                                            lcb_download_progress.setProgress(0);
                                            MainApplication.downloadManager.removeDownload(downloadInfo);
                                            refreshItem(type, getAdapterPosition(), getApp_id());
                                        } catch (DbException e) {
                                            LogUtils.e(e.getMessage(), e);
                                        }
                                    }
                                });
                            }
                            break;
                        case Configs.APP_BUTTON_STATUS_OPEN://打开
                            String scheme = getApp_Uri();
                            String packageName = getPackageName();
                            String appName = getName();
                            if (!TextUtils.isEmpty(scheme)) scheme += ":";
                            else scheme = CommonUtil.chooseUriToPackageName(packageName);
                            ((MainActivity) mAif).startApp_AppStoreStart_Event(mContext, packageName, scheme, appName);
                            break;
                        case Configs.APP_BUTTON_STATUS_UPDATE://更新
                            checkLoadApk();
                            break;
                    }
                    break;
            }
        }

        /**
         * 下载
         */
        private void loadApk() {
            try {
                DownloadInfo downloadInfo = new DownloadInfo();
                String fileName = getApk_path().substring(getApk_path().lastIndexOf("/") + 1);
                String path = MainApplication.apkDownloadPath + File.separator + fileName;
                File apk = new File(path);
                if (apk.exists()) apk.delete();
                downloadInfo.setSize(Double.valueOf(getSize()));
                downloadInfo.setAppId(getApp_id());
                downloadInfo.setLogoUrl(getIcon_path());
                downloadInfo.setDownloadUrl(getApk_path());
                downloadInfo.setAutoRename(false);
                downloadInfo.setAutoResume(true);
                downloadInfo.setFileName(getName());
                downloadInfo.setFileSavePath(path);
                downloadInfo.setApp_v_id(getApp_Version_id());
                downloadInfo.setMd5(getApp_Md5());
                MainApplication.downloadManager.addNewDownload(downloadInfo, new DownloadRequestCallBack());
                MainApplication.dbUtils.saveOrUpdate(new AppInfo(getApp_id(), getPackageName(), getApk_path()));
            } catch (DbException e) {
                e.printStackTrace();
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
                        setButtonState(Configs.APP_BUTTON_STATUS_PROGRESS, RecyclerHolder.this);
                        lcb_download_progress.setProgress(0);
                        downloadInfo.setLoadSuccess(false);
                        downloadInfo.setProgress(0);
                        MainApplication.downloadManager.resumeDownload(downloadInfo, new DownloadRequestCallBack());
                        refreshItem(type, getAdapterPosition(), getApp_id());
                    } catch (Exception e) {
                        LogUtils.e(e.getMessage(), e);
                    }

                }

                @Override
                public void onCancel() {
                    try {
                        MainApplication.downloadManager.removeDownload(downloadInfo);
                        refreshItem(type, getAdapterPosition(), getApp_id());
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
                            refreshItem(type, getAdapterPosition(), getApp_id());
                        } catch (Exception e) {
                            LogUtils.e(e.getMessage(), e);
                        }
                        return true;
                    }
                    return false;
                }
            });
        }

        /**
         * 回调调用
         */
        private void refresh() {
            if (downloadInfo == null) return;
            HttpHandler.State state = downloadInfo.getState();
            if (state != null) {
                switch (state) {
                    case WAITING:
                    case STARTED:
                    case CANCELLED:
                    case LOADING:
                        setButtonState(Configs.APP_BUTTON_STATUS_PROGRESS, this);
                        if (downloadInfo.getFileLength() > 0) {
                            int size = (int) ((downloadInfo.getProgress() * 100 / downloadInfo.getFileLength()));
                            lcb_download_progress.setProgress(size);
                        } else {
                            lcb_download_progress.setProgress(0);
                        }
                        break;
                    case SUCCESS:
                        lcb_download_progress.setProgress(100);
                        setButtonState(Configs.APP_BUTTON_STATUS_INSTALL, this);
                        try {
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
                        setButtonState(Configs.APP_BUTTON_STATUS_PROGRESS, this);
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
                }
            } else {
                if (downloadInfo.getProgress() == 100) {
                    setButtonState(Configs.APP_BUTTON_STATUS_INSTALL, this);
                } else {
                    setButtonState(Configs.APP_BUTTON_STATUS_PROGRESS, this);
                    try {
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

        private String getPackageName() {
            if (mData instanceof WeekbillboardBean) {
                return ((WeekbillboardBean) mData).package_name;
            } else {
                return ((HisbillboardBean) mData).package_name;
            }
        }

        private String getVersionNo() {
            String versionNo = "";
            if (mData instanceof WeekbillboardBean) {
                versionNo = ((WeekbillboardBean) mData).version_no;
            } else {
                versionNo = ((HisbillboardBean) mData).version_no;
            }
            return versionNo.isEmpty() ? "0" : versionNo;
        }

        private String getAppDesc() {
            if (mData instanceof WeekbillboardBean) {
                return ((WeekbillboardBean) mData).description;
            } else {
                return ((HisbillboardBean) mData).description;
            }
        }

        private float getAppGrade() {
            if (mData instanceof WeekbillboardBean) {
                return ((WeekbillboardBean) mData).score_avg;
            } else {
                return ((HisbillboardBean) mData).score_avg;
            }
        }

        private String getSize() {
            if (mData instanceof WeekbillboardBean) {
                return ((WeekbillboardBean) mData).size;
            } else {
                return ((HisbillboardBean) mData).size;
            }
        }

        private String getIcon_path() {
            if (mData instanceof WeekbillboardBean) {
                return ((WeekbillboardBean) mData).icon_path;
            } else {
                return ((HisbillboardBean) mData).icon_path;
            }
        }

        private int getOfficial_flag() {
            if (mData instanceof WeekbillboardBean) {
                return ((WeekbillboardBean) mData).official_flag;
            } else {
                return ((HisbillboardBean) mData).official_flag;
            }
        }

        private String getName() {
            if (mData instanceof WeekbillboardBean) {
                return ((WeekbillboardBean) mData).name;
            } else {
                return ((HisbillboardBean) mData).name;
            }
        }

        private String getApk_path() {
            if (mData instanceof WeekbillboardBean) {
                return ((WeekbillboardBean) mData).apk_path;
            } else {
                return ((HisbillboardBean) mData).apk_path;
            }
        }

        private String getApp_id() {
            if (mData instanceof WeekbillboardBean) {
                return ((WeekbillboardBean) mData).app_id;
            } else {
                return ((HisbillboardBean) mData).app_id;
            }
        }

        private String getApp_Version_id() {
            if (mData instanceof WeekbillboardBean) {
                return ((WeekbillboardBean) mData).app_v_id;
            } else {
                return ((HisbillboardBean) mData).app_v_id;
            }
        }

        private String getApp_Uri() {
            if (mData instanceof WeekbillboardBean) {
                return ((WeekbillboardBean) mData).app_uri;
            } else {
                return ((HisbillboardBean) mData).app_uri;
            }
        }

        private String getApp_Md5() {
            if (mData instanceof WeekbillboardBean) {
                return ((WeekbillboardBean) mData).app_md5;
            } else {
                return ((HisbillboardBean) mData).app_md5;
            }
        }
    }

    /**
     * 获取飙升榜需要刷新的位置
     *
     * @param appId  点击的经典榜的位置的appId
     * @param search 需要查找的榜单的数据列表
     * @return 要刷新的位置的id
     */
    private int getWeekRefreshIndex(String appId, List<WeekbillboardBean> search) {
        if (appId.isEmpty() || search == null || search.isEmpty()) {
            return -1;
        }
        for (int i = 0, length = search.size(); i < length; i++) {
            if (appId.equals(search.get(i).app_id))
                return i;
        }
        return -1;
    }

    /**
     * 获取经典榜需要刷新的位置
     *
     * @param appId  点击的飙升榜榜的位置的appId
     * @param search 需要查找的榜单的数据列表
     * @return 要刷新的位置的id
     */
    private int getHisRefreshIndex(String appId, List<HisbillboardBean> search) {
        if (appId.isEmpty() || search == null || search.isEmpty()) {
            return -1;
        }
        for (int i = 0, length = search.size(); i < length; i++) {
            if (appId.equals(search.get(i).app_id))
                return i;
        }
        return -1;
    }

    /**
     * 刷新经典帮或者飙升榜中指定位置的item，并且刷新另一个列表中的相同的元素
     *
     * @param type     要刷新的榜单
     * @param position 刷新的位置
     * @param appId    另一个帮当中如果有相同的元素，也进行刷新
     */
    private void refreshItem(int type, int position, String appId) {
        switch (type) {
            case WEEK_PAGE://飙升
                weekAdapter.notifyItemChanged(position);
                hisAdapter.notifyItemChanged(getHisRefreshIndex(appId, sutraApps));
                break;
            case HIS_PAGE://经典
                hisAdapter.notifyItemChanged(position);
                weekAdapter.notifyItemChanged(getWeekRefreshIndex(appId, surgeApps));
                break;
        }
    }

    /**
     * <p>功能描述</p>加载榜单数据
     *
     * @author wangzhichao
     * @date 2015年10月23日
     */
    void refershAPPList() {
        if (CommonUtil.isNetworkAvailable(mContext) && MainApplication.isListFirst) {
            SearchProvider provider = new SearchProvider(mContext);
            provider.setOnProviderListener(this);
            LinkedHashMap<String, String> paramMap = new LinkedHashMap<String, String>();
            paramMap.put("os_version", "" + android.os.Build.VERSION.SDK_INT);
            MyHttpHandler myHttpHandler = provider.loadBillBoardApps(paramMap);
            mAif.showProgressDialog(myHttpHandler, R.string.dialog_loading_data, true);
        } else {
            try {
                surgeApps = MainApplication.dbUtils.findAll(WeekbillboardBean.class);
                sutraApps = MainApplication.dbUtils.findAll(HisbillboardBean.class);

                weekAdapter.notifyDataSetChanged();
                hisAdapter.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class DownloadRequestCallBack extends RequestCallBack<File> {

        private void refreshListItem(int code) {
            if (userTags == null)
                return;

            List<Object> tags = userTags;

            for (int i = 0, length = tags.size(); i < length; i++) {
                WeakReference<RecyclerHolder> tag = (WeakReference<RecyclerHolder>) tags.get(i);
                RecyclerHolder holder = tag.get();
                if (holder != null) {
                    if (code == 416 && holder.downloadInfo != null)
                        holder.downloadInfo.setState(HttpHandler.State.SUCCESS);
                    holder.refresh();
                }
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
}
