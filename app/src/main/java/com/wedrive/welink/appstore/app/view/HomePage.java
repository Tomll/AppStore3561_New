package com.wedrive.welink.appstore.app.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.lidroid.xutils.exception.DbException;
import com.mapbar.android.model.ActivityInterface;
import com.mapbar.android.model.BasePage;
import com.mapbar.android.model.CommandInfo;
import com.mapbar.android.model.OnProviderListener;
import com.mapbar.android.model.ProviderResult;
import com.mapbar.android.provider.Provider;
import com.mapbar.android.statistics.api.MapbarMobStat;
import com.wedrive.welink.appstore.Configs;
import com.wedrive.welink.appstore.MainActivity;
import com.wedrive.welink.appstore.MainApplication;
import com.wedrive.welink.appstore.R;
import com.wedrive.welink.appstore.app.model.APPSearchHistory;
import com.wedrive.welink.appstore.app.model.LoginUserBean;
import com.wedrive.welink.appstore.app.provider.SearchProvider;
import com.wedrive.welink.appstore.app.util.CommonUtil;
import com.wedrive.welink.appstore.app.util.PropertiesUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;

public class HomePage extends BasePage implements OnClickListener {

    private final static String TAG = "HomePage";
    private Context mContext;
    private boolean isBack = false;
    private ActivityInterface mAif;
    private static int mSelectViewId = 1;

    public HomePage(Context context, View view, ActivityInterface aif) {
        super(context, view, aif);
        mContext = context;
        mAif = aif;
        if (!CommonUtil.isNetworkAvailable(context))
            mAif.showAlert(R.string.dialog_loading_net_unconnect);
        initView(view);
    }

    public View lv_search_area;
    public View lv_title_area;
    private View lv_recom;
    private View lv_search;
    private View lv_list;
    private View lv_mannger;

    private View currentMenu;
    private View currentContent;

    private Drawable iv_user_login;
    private Drawable iv_user_logout;

    private HomePageRecom mHomePageRecom;
    private HomePageSearch mHomePageSearch;
    private HomePageList mHomePageList;
    private HomePageMannger mHomePageMannger;

    private EditText edtTxt_search_content;
    private TextView txt_search_count;
    private TextView tv_first_title;


    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 10:
                    if (mSelectViewId == 4 && mAif.getCurrentPageObj().getPosition() == Configs.VIEW_POSITION_HOME) {
                        setUserIcon();
                    }
                    break;
                case 11:
                    if (mSelectViewId == 4 && mAif.getCurrentPageObj().getPosition() == Configs.VIEW_POSITION_HOME) {
                        MainActivity.mMainActivity.setTitleLoginVisibile(true, iv_user_login);
                    }
                    break;
            }
        }
    };

    @Override
    public void viewWillAppear(int flag) {
        super.viewWillAppear(flag);
        //9MapbarMobStat.onPageStart(mContext, Configs.AppStore_Interface_HomePage);
        //refershToken();
        switch (mSelectViewId) {
            case 1:
                lv_search_area.setVisibility(View.GONE);
//                MainActivity.mMainActivity.setFirstTitle("推荐");
                setTitle("推荐");
                break;
            case 2:
                lv_search_area.setVisibility(View.VISIBLE);
//                MainActivity.mMainActivity.dismissTitle();
                lv_title_area.setVisibility(View.GONE);
                mHomePageSearch.refershSearchData();
                break;
            case 3:
                lv_search_area.setVisibility(View.GONE);
//                MainActivity.mMainActivity.setFirstTitle("榜单");
                setTitle("榜单");
                mHomePageList.refershAPPList();
                break;
            case 4:
                lv_search_area.setVisibility(View.GONE);
//                MainActivity.mMainActivity.setFirstTitle("管理");
//                setUserIcon();
                setTitle("管理");
                mHomePageMannger.refershUpdateApp();
                break;
        }
    }

    @Override
    public void viewWillDisappear(int flag) {
        super.viewWillDisappear(flag);
        //9MapbarMobStat.onPageEnd(mContext, Configs.AppStore_Interface_HomePage);
    }

    @Override
    public void onResume() {
        super.onPause();
        //8MapbarMobStat.onPageStart(mContext, Configs.AppStore_Interface_HomePage);
        //refershToken();
    }

    @Override
    public void onPause() {
        super.onPause();
        //8MapbarMobStat.onPageEnd(mContext, Configs.AppStore_Interface_HomePage);
    }

    @Override
    public void onReceiveData(int arg0, int code, Object obj) {
        super.onReceiveData(arg0, code, obj);
        if (arg0 == getMyViewPosition()) {
            if (code == 10) {
                //refershToken();
                mHomePageMannger.loadAppInstal();
            }
        }
    }

    private void initView(View view) {
        view.findViewById(R.id.lny_search).setOnClickListener(this);
        view.findViewById(R.id.lny_clear).setOnClickListener(this);
        view.findViewById(R.id.imageView_search).setOnClickListener(this);
        view.findViewById(R.id.imageView_clear).setOnClickListener(this);
        txt_search_count = (TextView) view.findViewById(R.id.txt_search_count);
        edtTxt_search_content = (EditText) view.findViewById(R.id.edtTxt_search_content);
        //标题TextView
        tv_first_title = (TextView) view.findViewById(R.id.tv_first_title);

        //纵向的四个tab
        TextView txt_recom = (TextView) view.findViewById(R.id.txt_recom);
        TextView txt_search = (TextView) view.findViewById(R.id.txt_search);
        TextView txt_list = (TextView) view.findViewById(R.id.txt_list);
        TextView txt_mannger = (TextView) view.findViewById(R.id.txt_mannger);

        txt_recom.setOnClickListener(this);
        txt_search.setOnClickListener(this);
        txt_list.setOnClickListener(this);
        txt_mannger.setOnClickListener(this);
        iv_user_login = mContext.getResources().getDrawable(R.drawable.iv_user_login);
        iv_user_logout = mContext.getResources().getDrawable(R.drawable.iv_user_logout);
        edtTxt_search_content.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) { // 隐藏软键盘
                    InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(edtTxt_search_content.getWindowToken(), 0);
                    searchAppByName();
                    return true;
                }
                return false;
            }
        });
        //搜索栏、标题栏
        lv_search_area = view.findViewById(R.id.lv_search_area);
        lv_title_area = view.findViewById(R.id.lv_title_area);
        //内容区view控件
        lv_recom = view.findViewById(R.id.lv_recom);
        lv_search = view.findViewById(R.id.lv_search);
        lv_list = view.findViewById(R.id.lv_list);
        lv_mannger = view.findViewById(R.id.lv_mannger);
        //内容区page对象
        mHomePageRecom = new HomePageRecom(lv_recom, mAif, mContext);
        mHomePageSearch = new HomePageSearch(lv_search, mAif, mContext, this);
        mHomePageList = new HomePageList(lv_list, mAif, mContext);
        mHomePageMannger = new HomePageMannger(lv_mannger, mAif, mContext);

        currentMenu = txt_recom;
        currentContent = lv_recom;
        currentMenu.setSelected(true);
        mSelectViewId = 1;
        ((MainActivity) mAif).onClick_View_OnClick_Event(mContext, Configs.AppStore_OnClick_Recommend);
    }

    //设置标题
    public void setTitle(String title){
        lv_title_area.setVisibility(View.VISIBLE);
        tv_first_title.setText(title);
    }

    /**
     * @param count
     * @param isVisibie
     */

    public void setSearchResultCount(int count, boolean isVisibie) {
        if (isVisibie) {
            txt_search_count.setVisibility(View.VISIBLE);
            txt_search_count.setText(count + "个结果");
        } else {
            txt_search_count.setVisibility(View.GONE);
        }
    }

    /**
     * <p>功能描述</p>刷新用户头像
     *
     * @author wangzhichao
     * @date 2015年11月11日
     */

    private void setUserIcon() {
        if (Configs.APPSTORE_IS_LOGIN) {
            try {
                String token = PropertiesUtil.getProperties(mContext, "WeDrive_Login_Token");
                if (!TextUtils.isEmpty(token)) {
                    String headerImage = Configs.URL_USER_ICON + "?token=" + URLEncoder.encode(token, "UTF-8");
                    MainActivity.mMainActivity.setTitleLoginUserIcon(true, headerImage);
                } else {
                    MainActivity.mMainActivity.setTitleLoginVisibile(true, iv_user_logout);
                }
            } catch (UnsupportedEncodingException e) {
                MainActivity.mMainActivity.setTitleLoginVisibile(true, iv_user_logout);
            }
        } else {
            MainActivity.mMainActivity.setTitleLoginVisibile(true, iv_user_login);
        }
    }

    /**
     * <p>功能描述</p>刷新token
     *
     * @author wangzhichao
     * @date 2015年11月11日
     */

    private void refershToken() {
        String user = PropertiesUtil.getProperties(mContext, "WeDrive_Login_User_ID");
        String token = PropertiesUtil.getProperties(mContext, "WeDrive_Login_Token");

        if (!TextUtils.isEmpty(user) && !TextUtils.isEmpty(token)) {
            if (CommonUtil.isNetworkAvailable(mContext)) {
                SearchProvider mProvider = new SearchProvider(mContext);
                mProvider.setOnProviderListener(mProviderListener);
                mProvider.refershToken(user, token);
            } else {
                Configs.APPSTORE_IS_LOGIN = false;
                Message message = new Message();
                message.what = 11;
                handler.sendMessage(message);
            }
        } else {
            Configs.APPSTORE_IS_LOGIN = false;
            Message message = new Message();
            message.what = 11;
            handler.sendMessage(message);
        }
    }

    @Override
    public void onClick(View v) {
        //MainActivity.mMainActivity.setTitleDividerVisibile(false);
        switch (v.getId()) {
            case R.id.txt_recom:
                if (currentMenu != v) {
                    mSelectViewId = 1;
                    //MainActivity.mMainActivity.setFirstTitle("推荐");
                    setTitle("推荐");
                    lv_search_area.setVisibility(View.GONE);
                    currentMenu.setSelected(false);
                    currentContent.setVisibility(View.GONE);
                    v.setSelected(true);
                    lv_recom.setVisibility(View.VISIBLE);
                    currentMenu = v;
                    currentContent = lv_recom;
                    mHomePageRecom.refershRecomData();
                    ((MainActivity) mAif).onClick_View_OnClick_Event(mContext, Configs.AppStore_OnClick_Recommend);
                }
                break;
            case R.id.txt_search:
                if (currentMenu != v) {
                    mSelectViewId = 2;
                    lv_search_area.setVisibility(View.VISIBLE);

                    //MainActivity.mMainActivity.dismissTitle();
                    lv_title_area.setVisibility(View.GONE);

                    currentMenu.setSelected(false);
                    currentContent.setVisibility(View.GONE);
                    v.setSelected(true);
                    lv_search.setVisibility(View.VISIBLE);
                    currentMenu = v;
                    currentContent = lv_search;
                    mHomePageSearch.refershData();
                    ((MainActivity) mAif).onClick_View_OnClick_Event(mContext, Configs.AppStore_OnClick_Search);
                }
                break;
            case R.id.txt_list:
                if (currentMenu != v) {
                    mSelectViewId = 3;
                    //MainActivity.mMainActivity.setFirstTitle("榜单");
                    //MainActivity.mMainActivity.setTitleDividerVisibile(true);
                    setTitle("榜单");
                    lv_search_area.setVisibility(View.GONE);
                    currentMenu.setSelected(false);
                    currentContent.setVisibility(View.GONE);
                    v.setSelected(true);
                    lv_list.setVisibility(View.VISIBLE);
                    currentMenu = v;
                    currentContent = lv_list;
                    mHomePageList.refershAPPList();
                    ((MainActivity) mAif).onClick_View_OnClick_Event(mContext, Configs.AppStore_OnClick_List);
                }
                break;
            case R.id.txt_mannger:
                if (currentMenu != v) {
                    mSelectViewId = 4;
//                    MainActivity.mMainActivity.setFirstTitle("管理");
//                    setUserIcon();
                    setTitle("管理");
                    lv_search_area.setVisibility(View.GONE);
                    currentMenu.setSelected(false);
                    currentContent.setVisibility(View.GONE);
                    v.setSelected(true);
                    lv_mannger.setVisibility(View.VISIBLE);
                    currentMenu = v;
                    currentContent = lv_mannger;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            mHomePageMannger.loadAppInstal();
                        }
                    });
                    ((MainActivity) mAif).onClick_View_OnClick_Event(mContext, Configs.AppStore_OnClick_Management);
                }
                break;
            case R.id.imageView_search:
            case R.id.lny_search:
                searchAppByName();
                // TODO: 2018/9/26 搜索页面 搜索---
                MapbarMobStat.onEvent(mContext,"F0128","搜索");
                break;
            case R.id.imageView_clear:
            case R.id.lny_clear:
                edtTxt_search_content.setText("");
                if (isBack) {
                    mHomePageSearch.replay();
                    isBack = false;
                }
                // TODO: 2018/9/26 搜索页面 取消搜索---
                MapbarMobStat.onEvent(mContext,"F0129","取消搜索");
                break;
        }

    }

    /**
     * <p>功能描述</p>
     *
     * @author wangzhichao
     * @date 2016年2月18日
     */

    public void historySearchApp(String content) {
        edtTxt_search_content.setText(content);
        searchAppByName();
    }

   
	/**
	 * 
	 * <p>功能描述</p>模糊收索 app应用
	 * @author wangzhichao
	 * @date 2015年9月24日
	 */
	
	private void searchAppByName() {
		InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(edtTxt_search_content.getWindowToken(), 0);
		String content = edtTxt_search_content.getText().toString();
		edtTxt_search_content.setCursorVisible(false);
		if (content != null && !"".equals(content)) {
			try {
				APPSearchHistory bean = new APPSearchHistory();
				bean.search_id = UUID.randomUUID().toString().toUpperCase();
				bean.search_content = content;
				bean.search_time = System.currentTimeMillis();
				mHomePageSearch.searchApp(content);
				MainApplication.dbUtils.execNonQuery("delete from SearchHistoryBean where search_content=" + "'" + content + "'" + ";");
				MainApplication.dbUtils.save(bean);
				isBack = true;
			} catch (DbException e) {
				Log.e("message", "exception:" + e.getMessage());
			}
		} else {
			mAif.showAlert(R.string.title_search_Content);
		}
	}

	@Override
	public void onCommandReceive(CommandInfo ci) {
		super.onCommandReceive(ci);
		if ("addedPackage".equals(ci.getMethod()) || "removedPackage".equals(ci.getMethod())) {
			if("removedPackage".equals(ci.getMethod())) {
				Intent intent = (Intent) ci.getExtData();
				String packageName = intent.getData().getSchemeSpecificPart();
				mHomePageMannger.updateCacheData(packageName);
			}
			switch (mSelectViewId) {
			case 1:
				
				break;
			case 2:
				mHomePageSearch.refershData();
				break;
			case 3:
				mHomePageList.refershAPPList();
				break;
			case 4:
				mHomePageMannger.loadAppInstal();
				break;
			}
		}
	}

	@Override
	public int getMyViewPosition() {
		return Configs.VIEW_POSITION_HOME;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return false;
	}
	
	private OnProviderListener mProviderListener = new OnProviderListener() {
		@Override
		public void onProviderResponse(int requestCode, int responseCode,ProviderResult result) {
			mAif.hideProgressDialog();
			if (responseCode == Provider.RESULT_OK) {
				switch (requestCode) {
				case Configs.REQUEST_CODE_REDERSH_TOKEN:
					Message message=handler.obtainMessage();
					try {
						JSONObject obj = new JSONObject(result.getResponseStr());						
						int status = obj.getInt("code");
						if (status == 200) {
							obj = obj.getJSONObject("data");
							LoginUserBean userBean=new LoginUserBean();
							userBean.parse(obj);

							String X_Auth_Token = userBean.getToken();
							String X_Auth_Nice_Name = userBean.getNickname();
							Configs.X_Auth_Token = X_Auth_Token;
							Configs.X_Auth_Nice_Name = X_Auth_Token;

							Configs.APPSTORE_IS_LOGIN=true;
							message.what=10;
							PropertiesUtil.addProperties(mContext, "WeDrive_Login_Token",X_Auth_Token);
							PropertiesUtil.addProperties(mContext, "WeDrive_Login_Nice_Name",X_Auth_Nice_Name);
						}else{						
							Configs.APPSTORE_IS_LOGIN=false;
							message.what = 11;
						}
					} catch (JSONException e) {
						Configs.APPSTORE_IS_LOGIN=false;
						message.what = 11;
					}finally{
						handler.sendMessage(message);
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
