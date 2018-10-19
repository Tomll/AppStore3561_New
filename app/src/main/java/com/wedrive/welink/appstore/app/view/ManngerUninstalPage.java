package com.wedrive.welink.appstore.app.view;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mapbar.android.model.ActivityInterface;
import com.mapbar.android.model.BasePage;
import com.mapbar.android.model.CommandInfo;
import com.mapbar.android.model.FilterObj;
import com.mapbar.android.statistics.api.MapbarMobStat;
import com.wedrive.welink.appstore.Configs;
import com.wedrive.welink.appstore.MainActivity;
import com.wedrive.welink.appstore.R;
import com.wedrive.welink.appstore.app.model.AppInfo;
import com.wedrive.welink.appstore.app.util.AppUtil;
import com.wedrive.welink.appstore.app.util.CommonUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ManngerUninstalPage extends BasePage {
    private final static String TAG = "ManngerUninstalPage";
    private Context mContext;
    private ActivityInterface mAif;

    private TextView tv_update_descript;
    private PackageManager manager;
    private List<AppInfo> appInstallList = new ArrayList<AppInfo>();

    private RecyclerView rvLoadedApps;
    private LoadedAppsAdapter loadedAppsAdapter;

    public ManngerUninstalPage(Context context, View view, ActivityInterface aif) {
        super(context, view, aif);

        mContext = context;
        mAif = aif;
        manager = mContext.getPackageManager();
        initView(view);
    }


    private void initView(View view) {
        rvLoadedApps = (RecyclerView) view.findViewById(R.id.lVi_uninstall_apps);
        tv_update_descript = (TextView) view.findViewById(R.id.tv_update_descript);

        LinearLayoutManager manager = new LinearLayoutManager(mContext);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        rvLoadedApps.setLayoutManager(manager);

        loadedAppsAdapter = new LoadedAppsAdapter();
        rvLoadedApps.setAdapter(loadedAppsAdapter);
    }

    @Override
    public void setFilterObj(int flag, FilterObj filter) {
        super.setFilterObj(flag, filter);
        Object tag = filter.getTag();
        if (List.class.isInstance(tag)) {
            rvLoadedApps.setVisibility(View.VISIBLE);
            tv_update_descript.setVisibility(View.INVISIBLE);
            List<AppInfo> infos = (List<AppInfo>) tag;
            if (infos != null && infos.size() >= 0) {
                appInstallList = infos;
                loadedAppsAdapter.notifyDataSetChanged();
            }
        }
        if (String.class.isInstance(tag)) {
            rvLoadedApps.setVisibility(View.INVISIBLE);
            tv_update_descript.setVisibility(View.VISIBLE);
            tv_update_descript.setText((String) tag);
        }
    }

    @Override
    public void onReceiveData(int arg0, int code, Object arg2) {
        if (arg0 == getMyViewPosition()) {
            switch (code) {
                case 10:
                    List<AppInfo> infos = (List<AppInfo>) arg2;
                    if (infos != null && infos.size() >= 0) {
                        appInstallList = infos;
                        loadedAppsAdapter.notifyDataSetChanged();
                    }
                    break;
                case 11:
                    String result = arg2.toString();
                    mAif.showAlert(result);
                    break;
            }
        }
    }

    private class LoadedViewHolder extends RecyclerView.ViewHolder {
        LinearLayout lny_item;
        ImageView imageView_app_icon;
        ImageView imageView_official;
        TextView tv_app_name;
        TextView tv_app_size;
        ImageView imageView_uninstall;

        public LoadedViewHolder(View itemView) {
            super(itemView);
            lny_item = (LinearLayout) itemView.findViewById(R.id.lny_item);
            imageView_app_icon = (ImageView) itemView.findViewById(R.id.imageView_app_icon);
            imageView_official = (ImageView) itemView.findViewById(R.id.imageView_official);
            tv_app_name = (TextView) itemView.findViewById(R.id.tv_app_name);
            tv_app_size = (TextView) itemView.findViewById(R.id.tv_app_size);
            imageView_uninstall = (ImageView) itemView.findViewById(R.id.imageView_uninstall);
            imageView_official = (ImageView) itemView.findViewById(R.id.imageView_official);
        }
    }

    private class LoadedAppsAdapter extends RecyclerView.Adapter<LoadedViewHolder> {

        @Override
        public LoadedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new LoadedViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_manage_uninstall_view, parent, false));
        }

        @Override
        public void onBindViewHolder(LoadedViewHolder holder, int position) {
            final AppInfo appInstall = appInstallList.get(position);
            holder.tv_app_name.setText(appInstall.getAppName());
            holder.tv_app_size.setText(CommonUtil.FormetFileSize(appInstall.getAppSize()));
            if ("1".equals(appInstall.getApp_official_flag())) {
                holder.imageView_official.setVisibility(View.VISIBLE);
            } else {
                holder.imageView_official.setVisibility(View.INVISIBLE);
            }

            try {
                PackageInfo info = manager.getPackageInfo(appInstall.getPackageName(), 0);
                holder.imageView_app_icon.setImageDrawable(info.applicationInfo.loadIcon(manager));
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }

            holder.imageView_uninstall.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    // TODO: 2018/9/26 卸载界面 卸载---
                    MapbarMobStat.onEvent(mContext,"F0141","卸载");
                    AppUtil.unInstallApp(appInstall.getPackageName(), mContext);
                }
            });
        }

        @Override
        public int getItemCount() {
            return appInstallList.size();
        }
    }

    @Override
    public void onCommandReceive(CommandInfo ci) {
        super.onCommandReceive(ci);
        if ("removedPackage".equals(ci.getMethod()) && ci.getExtData() != null) {
            Intent intent = (Intent) ci.getExtData();
            String packageName = intent.getData().getSchemeSpecificPart();
            Iterator<AppInfo> iterator = appInstallList.iterator();
            while (iterator.hasNext()){
                String infoName = iterator.next().getPackageName();
                if (packageName.equals(infoName)) {
                    iterator.remove();
                    loadedAppsAdapter.notifyDataSetChanged();
                    break;
                }
            }
        }

    }


    @Override
    public void viewWillAppear(int flag) {
        super.viewWillAppear(flag);
        MainActivity.mMainActivity.setFirstAndSecondTitle("卸载应用", "管理");
        //9MapbarMobStat.onPageStart(mContext, Configs.AppStore_Interface_UninstalAppPage);
    }

    @Override
    public void viewWillDisappear(int flag) {
        super.viewWillDisappear(flag);
        //9MapbarMobStat.onPageEnd(mContext, Configs.AppStore_Interface_UninstalAppPage);
    }

    @Override
    public void onResume() {
        super.onResume();
        //8MapbarMobStat.onPageStart(mContext, Configs.AppStore_Interface_UninstalAppPage);
    }

    @Override
    public void onPause() {
        super.onPause();
        //8MapbarMobStat.onPageEnd(mContext, Configs.AppStore_Interface_UninstalAppPage);
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

    @Override
    public int getMyViewPosition() {
        return Configs.VIEW_POSITION_MANNGER_UNINSTAL;
    }
}
