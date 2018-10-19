package com.wedrive.welink.appstore.app.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.core.BitmapSize;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.mapbar.android.model.ActivityInterface;
import com.mapbar.android.model.FilterObj;
import com.mapbar.android.model.OnProviderListener;
import com.mapbar.android.model.ProviderResult;
import com.mapbar.android.statistics.api.MapbarMobStat;
import com.wedrive.welink.appstore.Configs;
import com.wedrive.welink.appstore.MainActivity;
import com.wedrive.welink.appstore.MainApplication;
import com.wedrive.welink.appstore.R;
import com.wedrive.welink.appstore.app.model.AppBannger;
import com.wedrive.welink.appstore.app.model.AppBean;
import com.wedrive.welink.appstore.app.provider.SearchProvider;
import com.wedrive.welink.appstore.app.util.CommonUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class HomePageRecom {

    public static final String TAG = "HomePageRecom";
    private ActivityInterface mAif;
    private Context mContext;
    private LinearLayout lv_banners;

    private List<AppBannger> bans = new ArrayList<AppBannger>();
    private List<AppBean> apps = new ArrayList<AppBean>();

    private boolean loadBannger, load_Recom;
    private int pageIndex = 1;
    private int pageSize = 50;

    private RecyclerView rvApps;
    private RecyclerViewAdapter mAdapter;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 21:
                    setBanngerImages(bans); // 刷新bannger应用列表
                    break;
                case 22:
                    mAdapter.notifyDataSetChanged(); // 刷新汽车热门应用列表
                    break;
                case 23:
                    setBanngerImages(bans); // 刷新bannger应用列表
                    mAdapter.notifyDataSetChanged(); // 刷新汽车热门应用列表
                    break;
            }
        }
    };

    public HomePageRecom(View view, ActivityInterface aif, Context context) {
        mAif = aif;
        mContext = context;
        initView(view);
        initRecomData();
    }

    private void initView(View view) {
        lv_banners = (LinearLayout) view.findViewById(R.id.lv_banners);
        rvApps = (RecyclerView) view.findViewById(R.id.gv_apps_list);

        LinearLayoutManager manager = new LinearLayoutManager(mContext);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        rvApps.setLayoutManager(manager);

        rvApps.setItemAnimator(null);

        mAdapter = new RecyclerViewAdapter();
        //设置item右边间距30dp
        rvApps.addItemDecoration(new SpaceItemDecoration(15));
        rvApps.setAdapter(mAdapter);
    }

    /**
     * @return void :
     * @Title: initRecomData
     * @Description: 加载推荐数据
     * @author : wangzc
     * @date 2016年5月13日
     */

    public void initRecomData() {
        if (((MainActivity) mAif).isFinishing()) return;

        if (CommonUtil.isNetworkAvailable(mContext) && (MainApplication.isBanngerFirst || MainApplication.isAppFirst)) {
            mAif.showProgressDialog(R.string.dialog_loading_data);
            SearchProvider provider = new SearchProvider(mContext);
            if (MainApplication.isBanngerFirst) {
                provider.setOnProviderListener(mAppsListener);
                LinkedHashMap<String, String> paramMap = new LinkedHashMap<String, String>();
                paramMap.put("os_type", Configs.HEADER_OS_VERSION);
                provider.loadBanngerList(paramMap);
            }

            if (MainApplication.isAppFirst) {
                provider.setOnProviderListener(mAppsListener);
                LinkedHashMap<String, String> paramMap2 = new LinkedHashMap<String, String>();
                paramMap2.put("os_version", "" + android.os.Build.VERSION.SDK_INT);
                paramMap2.put("p_index", "" + pageIndex);
                paramMap2.put("p_p_num", "" + pageSize);
                provider.loadAppsList(paramMap2);
            }
        } else {
            try {
                bans = MainApplication.dbUtils.findAll(Selector.from(AppBannger.class).orderBy("order_position", false));
                apps = MainApplication.dbUtils.findAll(AppBean.class);
                Message message = handler.obtainMessage();
                message.what = 23;
                handler.sendMessage(message);
            } catch (DbException e) {
                Log.e("message", "exception:" + e.getMessage());
            }
        }
    }

    /**
     * @return void :
     * @Title: refershRecomData
     * @Description: 加载推荐数据
     * @author : wangzc
     * @date 2016年5月13日
     */
    public void refershRecomData() {
        if (CommonUtil.isNetworkAvailable(mContext) && (MainApplication.isBanngerFirst || MainApplication.isAppFirst)) {
            mAif.showProgressDialog(R.string.dialog_loading_data);
            SearchProvider provider = new SearchProvider(mContext);
            if (MainApplication.isBanngerFirst) {
                provider.setOnProviderListener(mAppsListener);
                LinkedHashMap<String, String> paramMap = new LinkedHashMap<String, String>();
                paramMap.put("os_type", Configs.HEADER_OS_VERSION);
                provider.loadBanngerList(paramMap);
            }

            if (MainApplication.isAppFirst) {
                provider.setOnProviderListener(mAppsListener);
                LinkedHashMap<String, String> paramMap2 = new LinkedHashMap<String, String>();
                paramMap2.put("os_version", "" + android.os.Build.VERSION.SDK_INT);
                paramMap2.put("p_index", "" + pageIndex);
                paramMap2.put("p_p_num", "" + pageSize);
                provider.loadAppsList(paramMap2);
            }
        }
    }

    /**
     * <p>功能描述</p>设置bannger图片
     *
     * @param bans
     * @author wangzhichao
     * @date 2015年9月11日
     */
    private void setBanngerImages(List<AppBannger> bans) {
        int width = (int) mContext.getResources().getDimension(R.dimen.home_bannger_image_width);
        int height = (int) mContext.getResources().getDimension(R.dimen.home_bannger_image_height);

        BitmapDisplayConfig config = new BitmapDisplayConfig();
        BitmapSize size = new BitmapSize(600, 300);
        config.setBitmapMaxSize(size);
        config.setLoadFailedDrawable(mContext.getResources().getDrawable(R.drawable.img_df_pic));

        for (int i = 0; i < bans.size(); i++) {
            final AppBannger bean = bans.get(i);
            View view = View.inflate(mContext, R.layout.item_recom_bannger_view, null);
            ImageView imageView = (ImageView) view.findViewById(R.id.img_bannger_item);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(width, height));
            MainApplication.imageLoader.display(imageView, bean.image_path, config);
            OnClickListener myOnClickListener = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ("1".equals(bean.bannger_type)) {//打开app
                        FilterObj detail = new FilterObj();
                        detail.setFlag(10);
                        List<String> objs = new ArrayList<String>();
                        objs.add(bean.click_url);
                        detail.setTag(objs);
                        mAif.showPage(Configs.VIEW_POSITION_HOME, Configs.VIEW_POSITION_DETAIL, detail, true, null, null);
                        // TODO: 2018/9/26 推荐界面 广告应用---
                        MapbarMobStat.onEvent(mContext,"F0118","广告应用");
                    } else if ("2".equals(bean.bannger_type)) {//功能
                        String index = bean.click_url;
                        if ("0001".equals(index)) {//帮助(关于我们)
                            mAif.showPage(Configs.VIEW_POSITION_HOME, Configs.VIEW_POSITION_MANNGER_HELP, null, true, null, null);
                        }
                    } else if ("3".equals(bean.bannger_type)) {//专题

                    } else if ("4".equals(bean.bannger_type)) {//网页
                        String url = bean.click_url;
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.getApplicationContext().startActivity(intent);
                    }
                }
            };
            view.setOnClickListener(myOnClickListener);
            lv_banners.addView(view, i);
        }
    }

    private OnProviderListener mAppsListener = new OnProviderListener() {

        @Override
        public void onProviderResponse(int requestCode, int responseCode, ProviderResult result) {
            if (responseCode == -1) {
                if (!((MainActivity) mAif).isFinishing()) mAif.hideProgressDialog();
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
                    case Configs.REQUEST_CODE_LOAD_APPS_LIST:
                        load_Recom = true;
                        if (loadBannger && load_Recom) {
                            if (!((MainActivity) mAif).isFinishing()) mAif.hideProgressDialog();
                        }
                        try {
                            obj = obj.getJSONObject("data");
                            JSONArray jsonArr = obj.getJSONArray("p_data");
                            if (jsonArr.length() > 0) {
                                List<AppBean> as = new ArrayList<AppBean>();
                                for (int i = 0; i < jsonArr.length(); i++) {
                                    JSONObject listobj = jsonArr.getJSONObject(i);
                                    AppBean bean = new AppBean();

                                    if (listobj.has("app_id"))
                                        bean.setApp_id(listobj.getString("app_id"));
                                    if (listobj.has("app_name"))
                                        bean.setApp_name(listobj.getString("app_name"));
                                    if (listobj.has("app_v_id"))
                                        bean.setApp_version_id(listobj.getString("app_v_id"));
                                    if (listobj.has("description"))
                                        bean.setDescription(listobj.getString("description"));
                                    if (listobj.has("official_flag"))
                                        bean.setOfficial_flag(listobj.getInt("official_flag"));
                                    if (listobj.has("icon_path"))
                                        bean.setIcon_path(listobj.getString("icon_path"));

                                    as.add(bean);
                                }

                                apps = as;
                                Message message = handler.obtainMessage();
                                message.what = 22;
                                handler.sendMessage(message);

                                MainApplication.dbUtils.deleteAll(AppBean.class);
                                MainApplication.dbUtils.saveOrUpdateAll(apps);
                                MainApplication.isAppFirst = false;
                            } else {
                                mAif.showAlert("返回数据为空！");
                            }
                        } catch (Exception e) {
                            mAif.showAlert("解析数据出错！");
                        }
                        break;
                    case Configs.REQUEST_CODE_RECORD_BANNGER_LIST:
                        loadBannger = true;
                        if (loadBannger && load_Recom) {
                            if (!((MainActivity) mAif).isFinishing()) mAif.hideProgressDialog();
                        }
                        try {
                            JSONArray jsonArr = obj.getJSONArray("data");
                            if (jsonArr.length() > 0) {
                                List<AppBannger> as = new ArrayList<AppBannger>();
                                for (int i = 0; i < jsonArr.length(); i++) {
                                    JSONObject listobj = jsonArr.getJSONObject(i);
                                    AppBannger bean = new AppBannger();

                                    if (listobj.has("name"))
                                        bean.setBannger_name(listobj.getString("name"));
                                    if (listobj.has("type"))
                                        bean.setBannger_type(listobj.getString("type"));
                                    if (listobj.has("click_url"))
                                        bean.setClick_url(listobj.getString("click_url"));
                                    if (listobj.has("order_position"))
                                        bean.setOrder_position(listobj.getString("order_position"));
                                    if (listobj.has("image_path"))
                                        bean.setImage_path(listobj.getString("image_path"));
                                    as.add(bean);
                                }

                                bans = as;
                                Message message = handler.obtainMessage();
                                message.what = 21;
                                handler.sendMessage(message);

                                MainApplication.dbUtils.deleteAll(AppBannger.class);
                                MainApplication.dbUtils.saveOrUpdateAll(bans);
                                MainApplication.isBanngerFirst = false;
                            } else {
                                mAif.showAlert("返回数据为空！");
                            }
                        } catch (Exception e) {
                            mAif.showAlert("解析数据出错！");
                        }
                        break;
                }
            }
        }

        @Override
        public void onReadResponse(int arg0, int arg1) {

        }

    };

    /**
     * RecyclerView的Holder
     */
    private class RecyclerViewHolder extends RecyclerView.ViewHolder {
        LinearLayout linearLayout;
        ImageView imageView_icon;
        ImageView imageView_official;
        TextView txt_app_name;
        TextView txt_app_description;

        public RecyclerViewHolder(View itemView) {
            super(itemView);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.lny_item);
            imageView_icon = (ImageView) itemView.findViewById(R.id.imageView_icon);
            imageView_official = (ImageView) itemView.findViewById(R.id.imageView_official);
            txt_app_name = (TextView) itemView.findViewById(R.id.txt_app_name);
            txt_app_description = (TextView) itemView.findViewById(R.id.txt_app_description);
        }
    }

    /**
     * 热门汽车应用的列表RecyclerView的适配器
     */
    private class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewHolder> {
        private LayoutInflater inflater;

        public RecyclerViewAdapter() {
            inflater = LayoutInflater.from(mContext);
        }

        @Override
        public RecyclerViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new RecyclerViewHolder(inflater.inflate(R.layout.item_recom_apps_view, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(RecyclerViewHolder holder, int position) {
            final int pos = position;
            AppBean app = apps.get(position);

            holder.txt_app_name.setText(app.app_name);
            holder.txt_app_description.setText(app.description);
            holder.txt_app_description.setSelected(true);//list中textView跑马灯必须
            MainApplication.imageLoader.display(holder.imageView_icon, app.icon_path);
            if (app.getOfficial_flag() == 1) {
                holder.imageView_official.setVisibility(View.VISIBLE);
            } else {
                holder.imageView_official.setVisibility(View.INVISIBLE);
            }

            holder.linearLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    FilterObj detail = new FilterObj();
                    detail.setFlag(10);
                    List<String> objs = new ArrayList<String>();
                    objs.add(apps.get(pos).getApp_id());
                    objs.add(apps.get(pos).getApp_version_id());
                    detail.setTag(objs);
                    mAif.showPage(Configs.VIEW_POSITION_HOME, Configs.VIEW_POSITION_DETAIL, detail, true, null, null);
                    // TODO: 2018/9/26 推荐界面 热门汽车应用---
                    MapbarMobStat.onEvent(mContext,"F0119","热门汽车应用");
                }
            });
        }

        @Override
        public int getItemCount() {
            return apps.size();
        }
    }


    //RecyclerView的item间距类
    class SpaceItemDecoration extends RecyclerView.ItemDecoration {
        int mSpace;

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);

            outRect.left = mSpace;
            outRect.right = mSpace;

            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.left = 0;
            }

            /*outRect.bottom = mSpace;
            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.top = mSpace;
            }*/
        }

        public SpaceItemDecoration(int space) {
            this.mSpace = space;
        }
    }


}
