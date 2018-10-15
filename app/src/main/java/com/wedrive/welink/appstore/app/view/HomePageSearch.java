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
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
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

import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.util.LogUtils;
import com.mapbar.android.model.ActivityInterface;
import com.mapbar.android.model.FilterObj;
import com.mapbar.android.model.OnDialogListener;
import com.mapbar.android.model.OnProviderListener;
import com.mapbar.android.model.ProviderResult;
import com.mapbar.android.net.MyHttpHandler;
import com.wedrive.welink.appstore.Configs;
import com.wedrive.welink.appstore.MainActivity;
import com.wedrive.welink.appstore.MainApplication;
import com.wedrive.welink.appstore.R;
import com.wedrive.welink.appstore.app.download.DownloadInfo;
import com.wedrive.welink.appstore.app.download.DownloadManager;
import com.wedrive.welink.appstore.app.model.APPSearch;
import com.wedrive.welink.appstore.app.model.APPSearchHistory;
import com.wedrive.welink.appstore.app.model.APPSearchRecom;
import com.wedrive.welink.appstore.app.model.AppInfo;
import com.wedrive.welink.appstore.app.provider.SearchProvider;
import com.wedrive.welink.appstore.app.util.AppUtil;
import com.wedrive.welink.appstore.app.util.CommonUtil;
import com.wedrive.welink.appstore.app.widget.MyViewGroup;

public class HomePageSearch implements OnClickListener {

    private static final String TAG = "HomePageSearch";
    private View mView;
    private ActivityInterface mAif;
    private Context mContext;
    private HomePage homePage;

    private View lv_search_recom;
    private View lv_search_search;
    private TextView tvClearHistory;
    private RecyclerView gv_search_apps;
    private SearchResultAdapter searchAdapter;
    private MyViewGroup myVG_recom_apps;
    private MyViewGroup myVG_history_apps;

    private List<APPSearch> searchResultList = new ArrayList<APPSearch>();
    private List<APPSearchRecom> recoms = new ArrayList<APPSearchRecom>();
    private List<APPSearchHistory> listHistory = new ArrayList<APPSearchHistory>();

    private DecimalFormat df = new DecimalFormat("0.00");

    public HomePageSearch(View view, ActivityInterface aif, Context context, HomePage homePage) {
        mView = view;
        mAif = aif;
        mContext = context;
        this.homePage = homePage;
        initView(view);
    }

    private void initView(View view) {
        lv_search_recom = view.findViewById(R.id.lv_search_recom);
        lv_search_search = view.findViewById(R.id.lv_search_search);
        tvClearHistory = (TextView) view.findViewById(R.id.tv_cleanHistory);
        myVG_recom_apps = (MyViewGroup) view.findViewById(R.id.myviewGroup_recom_apps);
        myVG_history_apps = (MyViewGroup) view.findViewById(R.id.myviewGroup_history_apps);
        gv_search_apps = (RecyclerView) view.findViewById(R.id.gv_search_list);
        tvClearHistory.setOnClickListener(this);

        GridLayoutManager manager = new GridLayoutManager(mContext, 2);
        gv_search_apps.setLayoutManager(manager);

        searchAdapter = new SearchResultAdapter();
        gv_search_apps.setAdapter(searchAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_cleanHistory:
                clearHisData();
                break;
            default:
                break;
        }
    }

    /**
     * <p>
     * 功能描述
     * </p>
     * 刷新数据
     *
     * @author wangzhichao
     * @date 2015年9月24日
     */

    public void refershData() {
        if (lv_search_recom.getVisibility() == View.VISIBLE) {
            getRecomApps();
            getSearchHis();
        } else {
            searchAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 方法名称：refershSearchData
     * 方法描述：更新收索的数据状态
     * 方法参数：
     * 返回类型：
     * 创建人：wangzc
     * 创建时间：2017/5/9 18:39
    */

    public void refershSearchData() {
        if (lv_search_recom.getVisibility() == View.VISIBLE) {
            getRecomApps();
            getSearchHis();
        } else {
            searchAdapter.notifyDataSetChanged();
        }
    }

    /**
     * <p>
     * 功能描述
     * </p>
     * 清空本地搜索历史存储的数据
     *
     * @author wangzhichao
     * @date 2015年9月6日
     */
    private void clearHisData() {
        mAif.showDialog("温馨提示", "确定清除历史数据", "确定", "取消", new OnDialogListener() {
            @Override
            public void onOk() {
                try {
                    listHistory.clear();
                    tvClearHistory.setVisibility(View.GONE);
                    myVG_history_apps.removeAllViews();
                    MainApplication.dbUtils.deleteAll(APPSearchHistory.class);
                } catch (DbException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancel() {

            }
        });
    }

    /**
     * <p>功能描述</p>显示热门应用
     *
     * @author wangzhichao
     * @date 2015年12月2日
     */

    private void initHot(List<APPSearchRecom> recoms) {
        myVG_recom_apps.removeAllViews();
        if (recoms == null) return;
        for (final APPSearchRecom appSearchRecom : recoms) {
            View convertView = LayoutInflater.from(mContext).inflate(R.layout.item_search_recom_view, null);
            TextView txt_app_name = (TextView) convertView.findViewById(R.id.txt_app_name);
            txt_app_name.setText(appSearchRecom.getHot_apps_app_name());
            txt_app_name.setPadding(3, 5, 3, 5);
            convertView.setFocusable(true);
            convertView.setBackgroundResource(R.drawable.shade_focus_state);
            convertView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    homePage.lv_search_area.setVisibility(View.GONE);
                    FilterObj detail = new FilterObj();
                    detail.setFlag(11);
                    List<String> objs = new ArrayList<String>();
                    objs.add(appSearchRecom.hot_apps_app_id);
                    objs.add(appSearchRecom.hot_apps_app_v_id);
                    detail.setTag(objs);
                    mAif.showPage(Configs.VIEW_POSITION_HOME, Configs.VIEW_POSITION_DETAIL, detail, true, null, null);
                }
            });
            myVG_recom_apps.addView(convertView);
        }
    }

    /**
     * <p>功能描述</p>显示收索数据
     *
     * @param listHistory
     * @author wangzhichao
     * @date 2015年12月2日
     */

    private void initHistory(List<APPSearchHistory> listHistory) {
        myVG_history_apps.removeAllViews();
        if (listHistory == null) return;
        for (APPSearchHistory appSearchHistory : listHistory) {
            View convertView = LayoutInflater.from(mContext).inflate(R.layout.item_search_recom_view, null);
            TextView txt_app_name = (TextView) convertView.findViewById(R.id.txt_app_name);
            final String content = appSearchHistory.getSearch_content();
            txt_app_name.setText(content);
            txt_app_name.setGravity(Gravity.CENTER_VERTICAL);
            txt_app_name.setPadding(0, 5, 10, 5);
            convertView.setFocusable(true);
            convertView.setBackgroundResource(R.drawable.shade_focus_state);
            convertView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    homePage.historySearchApp(content);
                }
            });
            myVG_history_apps.addView(convertView);
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

    /**
     * <p>
     * 功能描述
     * </p>
     * 获取搜索历史数据
     *
     * @author wangzhichao
     * @date 2015年9月6日
     */

    private void getSearchHis() {
        try {
            listHistory = MainApplication.dbUtils.findAll(Selector.from(APPSearchHistory.class).orderBy("search_time", true).limit(4));
            if (listHistory == null) {
                tvClearHistory.setVisibility(View.GONE);
            } else {
                tvClearHistory.setVisibility(listHistory.size() == 0 ? View.GONE : View.VISIBLE);
                initHistory(listHistory);
            }
        } catch (DbException e) {
            e.printStackTrace();
        }

    }

    /**
     * <p>
     * 功能描述
     * </p>
     * 执行搜索
     *
     * @param content
     * @author wangzhichao
     * @date 2015年9月6日
     */

    public void searchApp(String content) {
        lv_search_recom.setVisibility(View.GONE);
        lv_search_search.setVisibility(View.VISIBLE);
        searchResultList.clear();
//		content="图吧导航（车机/pad版）";
        getSearchResult(content);
    }

    /**
     * <p>
     * 功能描述
     * </p>
     * 获取搜索数据
     *
     * @param content
     * @author wangzhichao
     * @date 2015年9月6日
     */

    private void getSearchResult(String content) {
        if (CommonUtil.isNetworkAvailable(mContext)) {
            SearchProvider provider = new SearchProvider(mContext);
            provider.setOnProviderListener(mAppsListener);
            LinkedHashMap<String, String> paramMap = new LinkedHashMap<String, String>();
            paramMap.put("os_version", "" + android.os.Build.VERSION.SDK_INT);
            paramMap.put("s_key", content);
            MyHttpHandler myHttpHandler = provider.loadSearchApps(paramMap);
            mAif.showProgressDialog(myHttpHandler, R.string.dialog_loading_data, true);
        } else {
            searchResultList.clear();
            searchAdapter.notifyDataSetChanged();
            mAif.showAlert(R.string.dialog_loading_net_unconnect);
        }

    }

    /**
     * <p>
     * 功能描述
     * </p>
     * 返回刷新所搜历史数据
     *
     * @author wangzhichao
     * @date 2015年9月24日
     */

    public void replay() {
        homePage.setSearchResultCount(0, false);
        lv_search_recom.setVisibility(View.VISIBLE);
        lv_search_search.setVisibility(View.GONE);
        getSearchHis();
    }

    /**
     * <p>
     * 功能描述
     * </p>
     * 获取推荐应用数据
     *
     * @author wangzhichao
     * @date 2015年9月6日
     */

    public void getRecomApps() {
        recoms.clear();
        if (CommonUtil.isNetworkAvailable(mContext) && MainApplication.isSearchFirst) {
            SearchProvider provider = new SearchProvider(mContext);
            provider.setOnProviderListener(mAppsListener);
            LinkedHashMap<String, String> paramMap = new LinkedHashMap<String, String>();
            paramMap.put("os_version", "" + android.os.Build.VERSION.SDK_INT);
            provider.loadRecomApps(paramMap);
        } else {
            try {
                recoms = MainApplication.dbUtils.findAll(APPSearchRecom.class);
                initHot(recoms);
            } catch (DbException e) {
                e.printStackTrace();
            }
        }
        mView.invalidate();
    }

    private class DownloadRequestCallBack extends RequestCallBack<File> {

        private void refreshListItem(int code) {
            if (userTag == null)
                return;
            WeakReference<RecyclerViewHolder> tag = (WeakReference<RecyclerViewHolder>) userTag;
            RecyclerViewHolder holder = tag.get();
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
     * 搜索结果适配器
     */
    private class SearchResultAdapter extends RecyclerView.Adapter<RecyclerViewHolder> {

        @Override
        public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new RecyclerViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_app_search_view, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerViewHolder holder, int position) {
            APPSearch appSearch = searchResultList.get(position);
            DownloadInfo downloadInfo = MainApplication.downloadManager.isAppLoading(appSearch.app_id);
            holder.setAppSearch(appSearch);
            holder.setDownloadInfo(downloadInfo);
            holder.initData();

            if (downloadInfo != null) {
                if (downloadInfo.isLoadSuccess() || downloadInfo.getProgress() == 100) {
                    downloadInfo.setLoadSuccess(true);
                    setButtonState(Configs.APP_BUTTON_STATUS_INSTALL, holder);
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
                            callBack.setUserTag(new WeakReference<RecyclerViewHolder>(holder));
                            holder.setCallBack(callBack);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        }

        @Override
        public int getItemCount() {
            return searchResultList.size();
        }
    }

    private class RecyclerViewHolder extends RecyclerView.ViewHolder {
        LinearLayout ll_base;
        ImageView img_icon;
        TextView tv_app_size;
        TextView tv_app_desc;
        RatingBar rb_app;
        TextView tv_app_name;
        ImageView img_icon_rz;
        ImageView img_btn_download;
        ImageView img_btn_install;
        ImageView img_btn_open;
        ImageView img_btn_update;
        ProgressBar lcb_download_progress;

        private APPSearch appSearch;
        private DownloadInfo downloadInfo;
        private RequestCallBack callBack;

        public void setCallBack(RequestCallBack callBack) {
            this.callBack = callBack;
        }

        public void setAppSearch(APPSearch appSearch) {
            this.appSearch = appSearch;
        }

        public void setDownloadInfo(DownloadInfo downloadInfo) {
            this.downloadInfo = downloadInfo;
        }

        public RecyclerViewHolder(View itemView) {
            super(itemView);
            initView(itemView);
        }

        private void initView(View convertView) {
            ll_base = (LinearLayout) convertView.findViewById(R.id.ll_base);
            img_icon = (ImageView) convertView.findViewById(R.id.img_icon);
            tv_app_size = (TextView) convertView.findViewById(R.id.tv_app_size);
            tv_app_desc = (TextView) convertView.findViewById(R.id.tv_app_desc);
            rb_app = (RatingBar) convertView.findViewById(R.id.rb_app);
            tv_app_name = (TextView) convertView.findViewById(R.id.tv_app_name);
            img_icon_rz = (ImageView) convertView.findViewById(R.id.img_icon_rz);
            img_btn_download = (ImageView) convertView.findViewById(R.id.imgView_details_download);
            img_btn_install = (ImageView) convertView.findViewById(R.id.imgView_details_instal);
            img_btn_open = (ImageView) convertView.findViewById(R.id.imgView_details_open);
            img_btn_update = (ImageView) convertView.findViewById(R.id.imgView_details_update);
            lcb_download_progress = (ProgressBar) convertView.findViewById(R.id.lcb_download_progress);

            ll_base.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    homePage.lv_search_area.setVisibility(View.GONE);
                    FilterObj detail = new FilterObj();
                    detail.setFlag(11);
                    List<String> objs = new ArrayList<String>();
                    objs.add(searchResultList.get(getAdapterPosition()).getApp_id());
                    objs.add(searchResultList.get(getAdapterPosition()).getApp_v_id());
                    detail.setTag(objs);
                    mAif.showPage(Configs.VIEW_POSITION_HOME, Configs.VIEW_POSITION_DETAIL, detail, true, null, null);
                }
            });
            /**
             * 下载
             */
            img_btn_download.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkLoadApk();
                }
            });

            /**
             * 安装
             */
            img_btn_install.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    String path = appSearch.getApk_path();
                    path = path.substring(path.lastIndexOf("/") + 1);
                    path = MainApplication.apkDownloadPath + File.separator + path;
                    File apk = new File(path);
                    if (apk.exists()) {
                        AppUtil.installApp(mContext, apk);
                    } else {
                        mAif.showDialog("温馨提示", "该安装包已删除，是否继续下载?", "确定", "取消", new OnDialogListener() {
                            @Override
                            public void onOk() {
                                try {
                                    setButtonState(Configs.APP_BUTTON_STATUS_PROGRESS, RecyclerViewHolder.this);
                                    lcb_download_progress.setProgress(0);
                                    downloadInfo.setLoadSuccess(false);
                                    downloadInfo.setProgress(0);
                                    MainApplication.downloadManager.resumeDownload(downloadInfo, new DownloadRequestCallBack());
                                    searchAdapter.notifyItemChanged(getAdapterPosition());
                                } catch (DbException e) {
                                    LogUtils.e(e.getMessage(), e);
                                }
                            }

                            @Override
                            public void onCancel() {
                                try {
                                    lcb_download_progress.setProgress(0);
                                    MainApplication.downloadManager.removeDownload(downloadInfo);
                                    setButtonState(checkButtonState(appSearch.package_name, Integer.parseInt(appSearch.version_no), appSearch.apk_path), RecyclerViewHolder.this);
                                } catch (DbException e) {
                                    LogUtils.e(e.getMessage(), e);
                                }
                            }
                        });
                    }
                }
            });
            /**
             * 更新
             */
            img_btn_update.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkLoadApk();
                }
            });
            /**
             * 打开
             */
            img_btn_open.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    String scheme = appSearch.app_uri;
                    String packageName = appSearch.package_name;
                    String appName = appSearch.name;
                    if (!TextUtils.isEmpty(scheme)) scheme += ":";
                    else scheme = CommonUtil.chooseUriToPackageName(packageName);
                    ((MainActivity) mAif).startApp_AppStoreStart_Event(mContext, packageName, scheme, appName);
                }
            });
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
                setButtonState(Configs.APP_BUTTON_STATUS_PROGRESS, RecyclerViewHolder.this);
                loadApk();
                searchAdapter.notifyItemChanged(getAdapterPosition());
            } else { //当用户使用2G/3G/4G网络下载
                Resources resources = mContext.getResources();
                String content = MainApplication.mAppPreferce.netWorkAllow ? resources.getString(R.string.net_is_workAllow_open)
                        : resources.getString(R.string.net_is_workAllow_close);
                mAif.showDialog("温馨提示", content, "确定", "取消", new OnDialogListener() {
                    @Override
                    public void onOk() {
                        setButtonState(Configs.APP_BUTTON_STATUS_PROGRESS, RecyclerViewHolder.this);
                        loadApk();
                        searchAdapter.notifyItemChanged(getAdapterPosition());
                    }

                    @Override
                    public void onCancel() {

                    }
                });
            }
        }

        /**
         * <p>
         * 功能描述
         * </p>
         * 下载apk文件
         *
         * @author wangzhichao
         * @date 2015年9月6日
         */

        private DownloadInfo loadApk() {
            DownloadInfo downloadInfo = new DownloadInfo();
            try {
                String fileName = appSearch.apk_path.substring(appSearch.apk_path.lastIndexOf("/") + 1);
                String path = MainApplication.apkDownloadPath + File.separator + fileName;
                File apk = new File(path);
                if (apk.exists()) apk.delete();
                downloadInfo.setSize(Double.valueOf(appSearch.size));
                downloadInfo.setAppId(appSearch.app_id);
                downloadInfo.setOfficial_flag(appSearch.official_flag);
                downloadInfo.setLogoUrl(appSearch.icon_path);
                downloadInfo.setDownloadUrl(appSearch.apk_path);
                downloadInfo.setAutoRename(false);
                downloadInfo.setAutoResume(true);
                downloadInfo.setFileName(appSearch.name);
                downloadInfo.setFileSavePath(MainApplication.apkDownloadPath + File.separator + fileName);
                downloadInfo.setApp_v_id(appSearch.getApp_v_id());
                downloadInfo.setMd5(appSearch.getApp_md5());
                MainApplication.downloadManager.addNewDownload(downloadInfo, new DownloadRequestCallBack());
                MainApplication.dbUtils.saveOrUpdate(new AppInfo(appSearch.getApp_id(), appSearch.getPackage_name(), appSearch.getApk_path()));
            } catch (DbException e) {
                e.printStackTrace();
            }
            return downloadInfo;
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
                        setButtonState(Configs.APP_BUTTON_STATUS_PROGRESS, RecyclerViewHolder.this);
                        lcb_download_progress.setProgress(0);
                        downloadInfo.setLoadSuccess(false);
                        downloadInfo.setProgress(0);
                        MainApplication.downloadManager.resumeDownload(downloadInfo, new DownloadRequestCallBack());
                        searchAdapter.notifyItemChanged(getAdapterPosition());
                    } catch (DbException e) {
                        LogUtils.e(e.getMessage(), e);
                    }
                }

                @Override
                public void onCancel() {
                    try {
                        MainApplication.downloadManager.removeDownload(downloadInfo);
                        setButtonState(checkButtonState(appSearch.package_name, Integer.parseInt(appSearch.version_no), appSearch.apk_path), RecyclerViewHolder.this);
                    } catch (DbException e) {
                    }
                }
            }, new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        dialog.dismiss();
                        try {
                            MainApplication.downloadManager.removeDownload(downloadInfo);
                            setButtonState(checkButtonState(appSearch.package_name, Integer.parseInt(appSearch.version_no), appSearch.apk_path), RecyclerViewHolder.this);
                        } catch (DbException e) {
                        }
                        return true;
                    }
                    return false;
                }
            });
        }

        /**
         * @return void :
         * @Title: initData
         * @Description:
         * @author : wangzc
         * @date 2016年7月4日
         */

        public void initData() {
            tv_app_size.setText(df.format(Double.parseDouble(appSearch.size)) + "MB");
            rb_app.setRating(appSearch.score_avg);
            tv_app_name.setText(appSearch.name);
            tv_app_desc.setText(appSearch.description);
            MainApplication.imageLoader.display(img_icon, appSearch.icon_path);
            if (appSearch.official_flag == 1) {
                img_icon_rz.setVisibility(View.VISIBLE);
            } else {
                img_icon_rz.setVisibility(View.INVISIBLE);
            }
            if (downloadInfo != null) {
                refresh();
            } else {
                setButtonState(checkButtonState(appSearch.package_name, Integer.parseInt(appSearch.version_no), appSearch.apk_path), this);
            }
        }

        public void refresh() {
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
                        lcb_download_progress.setVisibility(View.GONE);
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

    /**
     * <p>
     * 功能描述
     * </p>
     * 设置按钮显示状态
     *
     * @param state
     * @author jiaoyb
     * @date 2015年9月8日
     */
    private void setButtonState(int state, RecyclerViewHolder holder) {
        holder.img_btn_download.setVisibility(View.GONE);
        holder.img_btn_install.setVisibility(View.GONE);
        holder.img_btn_open.setVisibility(View.GONE);
        holder.img_btn_update.setVisibility(View.GONE);
        holder.lcb_download_progress.setVisibility(View.GONE);
        switch (state) {
            case Configs.APP_BUTTON_STATUS_DOWNLOAD:
                holder.img_btn_download.setVisibility(View.VISIBLE);
                break;
            case Configs.APP_BUTTON_STATUS_INSTALL:
                holder.img_btn_install.setVisibility(View.VISIBLE);
                break;
            case Configs.APP_BUTTON_STATUS_OPEN:
                holder.img_btn_open.setVisibility(View.VISIBLE);
                break;
            case Configs.APP_BUTTON_STATUS_UPDATE:
                holder.img_btn_update.setVisibility(View.VISIBLE);
                break;
            case Configs.APP_BUTTON_STATUS_PROGRESS:
                holder.lcb_download_progress.setVisibility(View.VISIBLE);
                break;
        }
    }

    int width = 0;

    private OnProviderListener mAppsListener = new OnProviderListener() {

        @Override
        public void onProviderResponse(int requestCode, int responseCode, ProviderResult result) {
            mAif.hideProgressDialog();
            if (responseCode == -1) {
                mAif.showAlert(R.string.dialog_loading_net_error);
            } else {
                JSONObject obj = null;
                try {
                    obj = new JSONObject(result.getResponseStr());
                    if (obj.has("status")) {
                        int status = obj.getInt("status");
                        if (status != 200) {
                            if (obj.has("msg")) {
                                mAif.showAlert(obj.getString("msg"));
                                homePage.setSearchResultCount(0, false);
                                return;
                            }
                        }
                    } else {
                        mAif.showAlert("返回数据错误！");
                        return;
                    }
                } catch (JSONException e) {
                    mAif.showAlert("解析数据出错！");
                    return;
                }

                switch (requestCode) {
                    case Configs.REQUEST_CODE_APPS_RECOM_LIST:
                        try {
                            obj = obj.getJSONObject("data");
                            if (obj == null) {
                                return;
                            }

                            String s_key = obj.getString("s_key");
                            String time = obj.getString("time");

                            Object hot_apps = obj.get("hot_apps");
                            if (hot_apps != null && !"null".equals(hot_apps.toString())) {
                                JSONArray jsonArr = obj.getJSONArray("hot_apps");
                                if (jsonArr.length() > 0) {
                                    List<APPSearchRecom> rs = new ArrayList<APPSearchRecom>();

                                    for (int i = 0; i < jsonArr.length(); i++) {
                                        JSONObject listobj = jsonArr.getJSONObject(i);

                                        APPSearchRecom recom = new APPSearchRecom();

                                        recom.setS_key(s_key);
                                        recom.setTime(time);

                                        if (listobj.has("app_id"))
                                            recom.setHot_apps_app_id(listobj.getString("app_id"));
                                        if (listobj.has("app_name"))
                                            recom.setHot_apps_app_name(listobj.getString("app_name"));
                                        if (listobj.has("app_v_id"))
                                            recom.setHot_apps_app_v_id(listobj.getString("app_v_id"));
                                        if (listobj.has("official_flag"))
                                            recom.setHot_apps_official_flag(listobj.getString("official_flag"));

                                        rs.add(recom);
                                    }
                                    recoms = rs;
                                    initHot(recoms);
                                    MainApplication.isSearchFirst = false;
                                    try {
                                        MainApplication.dbUtils.deleteAll(APPSearchRecom.class);
                                        MainApplication.dbUtils.saveOrUpdateAll(recoms);
                                    } catch (DbException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            Log.e("message", "exception:" + e.getMessage());
                        }
                        break;
                    case Configs.REQUEST_CODE_SEARCH_APPS_LIST:
                        try {
                            JSONArray jsonArr = obj.getJSONArray("data");
                            if (jsonArr == null) {
                                homePage.setSearchResultCount(0, true);
                                return;
                            }
                            if (jsonArr.length() > 0) {
                                homePage.setSearchResultCount(jsonArr.length(), true);
                                ArrayList<APPSearch> resultList = new ArrayList<APPSearch>();
                                for (int i = 0; i < jsonArr.length(); i++) {
                                    JSONObject listobj = jsonArr.getJSONObject(i);
                                    APPSearch bean = new APPSearch();

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
                                    resultList.add(bean);
                                }
                                searchResultList = resultList;
                                searchAdapter.notifyDataSetChanged();
                            }
                        } catch (Exception e) {
                            Log.e("message", "exception:" + e.getMessage());
                        }
                        break;

                }
            }
        }

        @Override
        public void onReadResponse(int arg0, int arg1) {

        }

    };
}
