package com.wedrive.welink.appstore;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.exception.DbException;
import com.mapbar.android.control.AppActivity;
import com.mapbar.android.model.CommandInfo;
import com.mapbar.android.model.OnDialogListener;
import com.mapbar.android.model.PageObject;
import com.mapbar.android.model.VersionInfo;
import com.mapbar.android.net.MyHttpHandler;
import com.mapbar.android.statistics.api.MapbarMobStat;
import com.wedrive.android.welink.appapi.WLAppManager;
import com.wedrive.welink.appstore.MainApplication.OnActivityListener;
import com.wedrive.welink.appstore.app.RootShell.RootShell;
import com.wedrive.welink.appstore.app.control.MainController;
import com.wedrive.welink.appstore.app.download.DownloadInfo;
import com.wedrive.welink.appstore.app.receive.InstalReceive;
import com.wedrive.welink.appstore.app.util.AppUtil;
import com.wedrive.welink.appstore.app.util.CommonUtil;
import com.wedrive.welink.appstore.app.widget.LoadingDialog;
import com.wedrive.welink.appstore.debug.LogManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class MainActivity extends AppActivity implements OnActivityListener {
	private final static String TAG = "MainActivity";

	public static MainActivity mMainActivity;
	private MainController mMainController;
	private Dialog popDialog;
	private LoadingDialog dialog;
	private boolean locationMobStat=true;
	private boolean showPage=true;
	private InstalReceive mInstalReceive;
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 10:
				String mobile=getResources().getString(R.string.net_wifi_to_mobile);
				showDialog("温馨提示", mobile, "确定",  "取消",new OnDialogListener(){
					@Override
					public void onOk() {
						try {
							MainApplication.downloadManager.resumeAllDownload();
						} catch (DbException e) {
							e.printStackTrace();
						}	
					}

					@Override
					public void onCancel() {
						
					}
				});
				break;
			case 11:
				showPage=true;
				String stop=getResources().getString(R.string.net_no_stop_download);
				PageObject page = getCurrentPageObj();
				if(page != null && Configs.VIEW_POSITION_MANNGER_LOAD == page.getPage().getMyViewPosition()){
					showPage=false;
				}
				showDialog("温馨提示", String.format(stop,msg.arg1), showPage ? "查看" : "确定",new OnDialogListener(){
					@Override
					public void onOk() {
						if(showPage) showPage(Configs.VIEW_POSITION_HOME, Configs.VIEW_POSITION_MANNGER_LOAD,null,true,null,null);
					}
					
					@Override
					public void onCancel() {
						
					}
				});
				break;
			case 12: 
				if(popDialog!=null && popDialog.isShowing()){
					popDialog.dismiss();
				}
				break;
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogManager.registerUncaughtExceptionHandler();
		WLAppManager.getInstance(getApplicationContext()).start();
		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.layout_welcome);
		mMainActivity = this;
		mMainController = new MainController(this);
		MainApplication.getInstance().setOnActivityListener(this);
		AppUtil.init(this);
		initRoot();
		mMainController.onNewIntent(getIntent());

		//注册应用安装卸载的监听
		mInstalReceive = new InstalReceive();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
		intentFilter.addDataScheme("package");
		registerReceiver(mInstalReceive, intentFilter);
        MapbarMobStat.readyToStatistic(this);//调用此方法才会将统计数据持久化存储，比较耗时，所以不放在Application初始化的逻辑中
    }

	@Override
	public void onNewIntent(Intent intent){
		super.onNewIntent(intent);
		mMainController.onNewIntent(intent);
	}
	
	public void onResume() {
		super.onResume();
		sendAitalkModuleName(false);
	}

	/**
	 * 初始化完成，可以进行界面切换
	 */

	@Override
	public void onFinishedInit(int flag) {
		mMainController.onResume(flag);
	}

	/**
	 * 正常的Activity onPause
	 */
	
	@Override
	protected void onPause() {
		super.onPause();
		mMainController.onPause();
	}

	/**
	 * 正常的Activity onDestroy
	 */
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		dialog=null;
		WLAppManager.getInstance(getApplicationContext()).stop();
		try {
			MainApplication.downloadManager.backupDownloadInfoList();
		} catch (Exception e) {
			Log.e("message","onDestroy exception:"+e.getMessage());
		}

		//取消注册应用安装卸载的监听
		unregisterReceiver(mInstalReceive);
	}
	
	/**
	 * 是否可以正常退出
	 */
	
	@Override
	public boolean canExit() {
		if (!isCanExit) {
			this.isCanExit = true;
			this.showAlert(R.string.toast_againto_exit);
			return false;
		}
		return true;
	}

	/**
	 * 正常的Activity onKeyDown
	 */
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (mMainController.onKeyDown(keyCode, event)){
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_BACK){
			mMainActivity.dismissTitle();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onCommandReceive(Context context,CommandInfo ci) {
		String method=ci.getMethod();
		if("onExitWelink".equals(method)){
			finish();
			MapbarMobStat.onKill(context);
			System.exit(0);
		}else if("onExitCurrentApp".equals(method)){
			try {
				JSONObject json = (JSONObject) ci.getExtData();
				String packageName = json.getString("packagename");
				if (getPackageName().equals(packageName)) {
					finish();
					MapbarMobStat.onKill(context);
					System.exit(0);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}else if("onDiscernResult".equals(method)){
			try {
				JSONObject json = (JSONObject) ci.getExtData();
				int index = json.getInt("index");
				if (index==6) {
					finish();
					MapbarMobStat.onKill(context);
					System.exit(0);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}else if("MobileNetReceive".equals(method)){
			sendToPage(Configs.VIEW_POSITION_HOME,10, null);
			if(checkLoadCount() > 0){
				try {
					MainApplication.downloadManager.stopAllDownload();//先暂停在提示继续下载对话框
					Message message=mHandler.obtainMessage();
					message.what=10;
					mHandler.sendMessage(message);
				} catch (DbException e) {
					e.printStackTrace();
				}
			}
		}else if("WiFiNetReceive".equals(method)){
			sendToPage(Configs.VIEW_POSITION_HOME,10, null);
			if(checkLoadCount() > 0){
				try {
					MainApplication.downloadManager.resumeAllDownload();
					Message message=mHandler.obtainMessage();
					message.what=12;
					mHandler.sendMessage(message);
				} catch (DbException e) {
					e.printStackTrace();
				}
			}
		}else if("NoNetReceive".equals(method)){
			sendToPage(Configs.VIEW_POSITION_HOME,10, null);
			int count=checkLoadCount();
			if(count > 0){
				try {
					MainApplication.downloadManager.stopAllDownload();
					Message message=mHandler.obtainMessage();
					message.what=11;
					message.arg1=count;
					mHandler.sendMessage(message);
				} catch (DbException e) {
					e.printStackTrace();
				}
			}
		}
		else{
			PageObject page = this.getCurrentPageObj();
			if (page != null){
				page.getPage().onCommandReceive(ci);
				if(Configs.VIEW_POSITION_HOME != page.getPosition()){
					page=getPageObjByPos(Configs.VIEW_POSITION_HOME);
					page.getPage().onCommandReceive(ci);
				}
			}
		}		
	}
	
	/**
	 * 
	* @Title: checkLoadCount 
	* @Description: 检查当前正在现在的数量
	* @return int :
	* @author : wangzc
	* @date 2016年10月9日
	 */
	
	private int checkLoadCount(){
		int loadCount=0;
		int count=MainApplication.downloadManager.getDownloadInfoListCount();			
		for(int i=0;i<count;i++){
			DownloadInfo info=MainApplication.downloadManager.getDownloadInfo(i);
			if(!info.isLoadSuccess){
				loadCount++;
			}
		}		
		return loadCount;
	}
	
	
	/**
	 * 
	* @Title: sendAitalkModuleName 
	* @Description: 正常的Activity onResume， 不在此处做任何初始化工作 一般是应用前台后台来回切换时 按须做处理即可
	* @return void :
	* @author : 
	* @date 2016年9月1日
	 */	
	
	private void sendAitalkModuleName(boolean isClean){
		String commandData = null;
		try
		{
			JSONObject jObj = new JSONObject();
			jObj.put("moduleName", "WeDriveAitalk");
			jObj.put("version",0);
			JSONObject jComd = new JSONObject();
			jComd.put("method", "setModuleName");

			JSONObject extData = new JSONObject();
			extData.put("name", "WeDriveAppStore");
			extData.put("cleanFlag", isClean);
			extData.put("flag", 0);
			
			jComd.put("extData", extData);
			jObj.put("command", jComd);
			
			commandData = jObj.toString();
		}
		catch(Exception e)
		{
		}
		Intent intent = new Intent();
		intent.setAction(com.mapbar.wedrive.Action.AITALK_COMMAND_SEND);
		intent.putExtra(com.mapbar.wedrive.Extra.COMMAND_DATA, commandData);
		intent.setFlags(32);
		this.sendBroadcast(intent);
	}

	/**
	 * 方法名称：
	 * 方法描述：
	 * 方法参数：
	 * 返回类型：
	 * 创建人：wangzc
	 * 创建时间：2016/11/2 15:52
	*/
	
	public void startApp_AppStoreStart_Event(Context mContext, String packageName, String scheme, String appName){
		boolean flag=false;
		if(!TextUtils.isEmpty(scheme)){
			try {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(scheme));
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				if(Configs.SCHEME_NAVI.equals(scheme)){
					intent.setComponent(new ComponentName(Configs.PACKAGENAME_NAVI, "com.mapbar.android.carnavi.activity.OutCallActivity"));
				}
				mContext.startActivity(intent);
				flag=true;
			} catch (Exception e) {
				showAlert("无法通过Uri启动应用");
			}
		}else{
			try {
				Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(packageName);
				if (intent != null) {
					mContext.startActivity(intent);
					flag=true;
				}
				else {
					showAlert("无法打开系统底层应用");
					flag=false;
				}
			} catch (Exception e) {
				showAlert("无法通过包名启动应用");
			}
		}

		if(flag){
			HashMap<String, String> extra=new HashMap<String, String>();
			extra.put(Configs.Event_AppStore_StartApp, appName);
			MapbarMobStat.onEventKv(mContext, Configs.Event_ID_StartApp, extra);
		}

	}

	/**
	 *
	 * @Title: onClick_View_OnClick_Event
	 * @Description: view点击并统计view点击次数事件
	 * @return void :
	 * @author : wangzc
	 * @date 2016年10月18日
	 */

	public void onClick_View_OnClick_Event(Context mContext,String value){
		HashMap<String, String> extra=new HashMap<String, String>();
		extra.put(Configs.Event_AppStore_OnClick, value);
		MapbarMobStat.onEventKv(mContext, Configs.Event_ID_OnClick, extra);
	}

	@Override
	public boolean isUseLocation() {
		return true;
	}


	@Override
	public void onLocationChanged(Location location) {
		super.onLocationChanged(location);
		if (location == null || location.getExtras() == null) {
			return;
		}
		if(locationMobStat){
			if(!TextUtils.isEmpty(location.getExtras().getString("city"))){
				double lat=location.getLatitude();
				double lon=location.getLongitude();
				double accuracy=location.getAccuracy();
				double altitude=location.getAltitude();
				long location_time=location.getTime();
				String city = location.getExtras().getString("city");
				if(!TextUtils.isEmpty(city)){
					MapbarMobStat.setLocationInfo(lat,lon,accuracy,altitude,location_time,city);
					locationMobStat=false;
				}
			}
		}
	}

	@Override
	public void showProgressDialog(int msgId) {
		try {
			if(dialog==null) dialog = new LoadingDialog(this);
			String msg = this.getResources().getString(msgId);
			dialog.setTip(msg);
			dialog.setIndeterminate(true);
			dialog.setCancelable(true);
			dialog.setOnKeyListener(new OnKeyListener()
            {
                @Override
                public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent evnet)
                {
                    if (keyCode == KeyEvent.KEYCODE_BACK)
                    {
                        dialog.dismiss();
                        return false;
                    }
                    else if (keyCode == KeyEvent.KEYCODE_SEARCH)
                    {
                        return true;
                    }
                    return false;
                }
            });
			dialog.show();
		} catch (Resources.NotFoundException e) {
			e.printStackTrace();
		}
	}

	public void showProgressDialog(int msgId,OnKeyListener onKeyListener,boolean flag) {
		try {
			if(dialog==null) dialog = new LoadingDialog(this);
			String msg = this.getResources().getString(msgId);
			dialog.setTip(msg);
			dialog.setIndeterminate(true);
			dialog.setCancelable(flag);
			dialog.setOnKeyListener(onKeyListener);
			dialog.show();
		} catch (Resources.NotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void showProgressDialog(final MyHttpHandler myHttpHandler, int msgId, boolean b) {
		try {
			if(dialog==null) dialog = new LoadingDialog(this);
			String msg = this.getResources().getString(msgId);
			dialog.setTip(msg);
			dialog.setIndeterminate(true);
			dialog.setCancelable(true);
			dialog.setOnKeyListener(new OnKeyListener()
            {
                @Override
                public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent evnet)
                {
                    if (keyCode == KeyEvent.KEYCODE_BACK)
                    {
                        if(myHttpHandler != null)
                        {
                            myHttpHandler.cancel(true);
                        }
                        return false;
                    }
                    else if (keyCode == KeyEvent.KEYCODE_SEARCH)
                    {
                        return true;
                    }
                    return false;
                }
            });
			dialog.show();
		} catch (Resources.NotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void hideProgressDialog() {
		try {
			if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 用于统一标题栏layout
	 */
	
	@Override
	public void setTitle(View view, int titleId) {
		mMainController.setTitle(view, titleId);
	}
	
	@Override
	public void setTitleCustomView(View view, View customView) {
		mMainController.setTitleCustomView(view, customView);
	}

	public void setFirstTitle(String title) {
		mMainController.setFirstTitle(title);
	}

	public void setFirstAndSecondTitle(String firstTitle, String secondTitle) {
		mMainController.setFirstAndSecondTitle(firstTitle, secondTitle);
	}
	
	public void setAllTitle(String firstTitle, String secondTitle, String thirdTitle) {
		mMainController.setAllTitle(firstTitle, secondTitle,thirdTitle);
	}
	
	public void setTitleOnClickListener(OnClickListener leftOnClickListener,OnClickListener rightOnClickListener) {
		mMainController.setTitleOnClickListener(leftOnClickListener,rightOnClickListener);
	}

	public void setTitleLoginVisibile(boolean isVisibile,Drawable drawable) {
		mMainController.setTitleLoginVisibile(isVisibile,drawable);
	}

	public void setTitleLoginUserIcon(boolean isVisibile,String iconUrl) {
		mMainController.setTitleLoginUserIcon(isVisibile, iconUrl);
	}
	
	public void setTitleDividerVisibile(boolean b) {
		mMainController.setTitleDividerVisibile(b);
	}
	
	public void dismissTitle() {
		mMainController.dismissTitle();
	}
	
	public void recycleLoginUserIcon(){
		mMainController.recycleLoginUserIcon();
	}

	@Override
	public PageObject createPage(int index) {
		return mMainController.createPage(index);
	}

	@Override
	public int getAnimatorResId() {
		return R.id.animator;
	}
	

	@Override
	public int getMainPosition() {
		return Configs.VIEW_POSITION_HOME;
	}

	@Override
	public int getOutPosition() {
		return Configs.VIEW_POSITION_NONE;
	}

	@Override
	public int getNonePositioin() {
		return Configs.VIEW_POSITION_NONE;
	}

	@Override
	public int getViewNoneFlag() {
		return Configs.VIEW_FLAG_NONE;
	}

	/**
	 * 启动语音识别，由各个界面调用，需要在此实现语音识别相关功能
	 */
	@Override
	public void startAitalk() {
	}

	/**
	 * 取消语音识别，由各个界面调用，需要在此处理语音识别取消
	 */
	@Override
	public void cancelAitalk() {
		
	}

	private boolean isCanExit = true;

	/**
	 * 界面有切换动作时，会调用到
	 */
	@Override
	public void onPageActivity() {
		isCanExit = true;
	}

	
	@Override
	public void showAlert(int resId) {
		showAlert(getResources().getString(resId));
	}

	@Override
	public void showAlert(String content) {
		View layout = View.inflate(this, R.layout.layout_toast, null);
		TextView textView = (TextView) layout.findViewById(R.id.tv_toast_text);
		textView.setText(content);
		Toast toast = new Toast(getApplicationContext());
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.TOP, 0, 20);
		toast.setView(layout);
		toast.show();
	}
	
	public void showDialog(String title, String content, String okText,final OnDialogListener listener) {
		showDialog(R.style.customDialog, R.layout.layout_dialog_one, title, content,okText,listener);
	}

	@Override
	public void showDialog(String title, String content, String okText, String cancelText, final OnDialogListener listener) {
		showDialog(R.style.customDialog, R.layout.layout_dialog_two, title, content,okText , cancelText, listener);
	}
	
	public void showDialog(String title, String content, String okText, String cancelText, final OnDialogListener listener,final OnKeyListener onKeyListener) {
		showDialog(R.style.customDialog, R.layout.layout_dialog_two, title, content, okText,cancelText , listener, onKeyListener,Gravity.CENTER);
	}

	/**
	 * 统一Dialog样式，可在此做处理
	 */
	
	public void showDialog(int style, int resId, String title, String content, String okText,final OnDialogListener listener) {
		showDialog(style, resId, title, content, okText, listener,Gravity.CENTER);
	}
	
	@Override
	public void showDialog(int style, int resId, String title, String content, String okText, String cancelText, OnDialogListener listener) {
		showDialog(style, resId, title, content, okText,cancelText , listener, Gravity.CENTER);
	}


	/**
	 * 统一Dialog样式，可在此做处理
	 */
	
	public void showDialog(int style, int resId, String title, String content, String okText,final OnDialogListener listener, int gravity) {
		if(isFinishing()){
			return;
		}
		final Dialog popDialog = new Dialog(this, style);

		LayoutInflater inflater = LayoutInflater.from(this);
		View view = inflater.inflate(resId, null);// 得到加载view
		popDialog.setContentView(view);// 设置布局

		Window dialogWindow = popDialog.getWindow();
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		dialogWindow.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
//		lp.height = CommonUtil.dip2px(this, 252);
		lp.width = CommonUtil.dip2px(this, 392);
		dialogWindow.setAttributes(lp);

		TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
		if (TextUtils.isEmpty(title))
			tv_title.setVisibility(View.GONE);
		else
			tv_title.setText(title);

		TextView tv_desc = (TextView) view.findViewById(R.id.tv_desc);
		tv_desc.setText(content);
		tv_desc.setGravity(gravity);

		Button btn_ok = (Button) view.findViewById(R.id.btn_ok);
		btn_ok.setText(okText);
		btn_ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				popDialog.dismiss();
				if (listener != null)
					listener.onOk();
			}
		});
		popDialog.setCancelable(true);
		popDialog.show();
	}
	
	@Override
	public void showDialog(int style, int resId, String title, String content, String okText, String cancelText, final OnDialogListener listener, int gravity) {
		if(isFinishing()){
			return;
		}
		if(popDialog ==null ) popDialog = new Dialog(this, style);

		LayoutInflater inflater = LayoutInflater.from(this);
		View view = inflater.inflate(resId, null);// 得到加载view
		popDialog.setContentView(view);// 设置布局

		Window dialogWindow = popDialog.getWindow();
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		dialogWindow.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
//		lp.height = CommonUtil.dip2px(this, 252);
		lp.width = CommonUtil.dip2px(this, 392);
		dialogWindow.setAttributes(lp);

		TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
		if (TextUtils.isEmpty(title))
			tv_title.setVisibility(View.GONE);
		else
			tv_title.setText(title);

		TextView tv_desc = (TextView) view.findViewById(R.id.tv_desc);
		tv_desc.setText(content);
		tv_desc.setGravity(gravity);

		Button btn_ok = (Button) view.findViewById(R.id.btn_ok);
		btn_ok.setText(okText);
		btn_ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				popDialog.dismiss();
				if (listener != null)
					listener.onOk();
			}
		});

		Button btn_cancel = (Button) view.findViewById(R.id.btn_cancel);
		btn_cancel.setText(cancelText);
		btn_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				popDialog.dismiss();
				if (listener != null)
					listener.onCancel();
			}

		});

		popDialog.setCancelable(true);
		popDialog.show();

	}
	
	public void showDialog(int style, int resId, String title, String content, String okText, String cancelText, final OnDialogListener listener,
			final OnKeyListener onKeyListener,int gravity) {
		if(isFinishing()){
			return;
		}
		if(popDialog ==null ) popDialog = new Dialog(this, style);

		LayoutInflater inflater = LayoutInflater.from(this);
		View view = inflater.inflate(resId, null);// 得到加载view
		popDialog.setContentView(view);// 设置布局

		Window dialogWindow = popDialog.getWindow();
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		dialogWindow.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
//		lp.height = CommonUtil.dip2px(this, 252);
		lp.width = CommonUtil.dip2px(this, 392);
		dialogWindow.setAttributes(lp);

		TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
		if (TextUtils.isEmpty(title))
			tv_title.setVisibility(View.GONE);
		else
			tv_title.setText(title);

		TextView tv_desc = (TextView) view.findViewById(R.id.tv_desc);
		tv_desc.setText(content);
		tv_desc.setGravity(gravity);

		Button btn_ok = (Button) view.findViewById(R.id.btn_ok);
		btn_ok.setText(okText);
		btn_ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				popDialog.dismiss();
				if (listener != null)
					listener.onOk();
			}
		});

		Button btn_cancel = (Button) view.findViewById(R.id.btn_cancel);
		btn_cancel.setText(cancelText);
		btn_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				popDialog.dismiss();
				if (listener != null)
					listener.onCancel();
			}

		});
		
		popDialog.setOnKeyListener(onKeyListener);	
		popDialog.setCancelable(true);
		popDialog.setCanceledOnTouchOutside(false);
		popDialog.show();

	}

	/**
	 * 由框架获取的上下文
	 */
	@Override
	public Context getContext() {
		return this;
	}

	/**
	 * 有新版本更新时，会被调用
	 */
	@Override
	public void onNewVersionUpdate(final VersionInfo vi) {
	}

	/**
	 * 应用将要退出时，会被调用
	 */
	@Override
	public void onRelease() {
		super.onRelease();
	}

	/**
	 * 当Activity界面初始化完毕后 系统进入初始化前被调用
	 */
	@Override
	public void appWillEnterBackgroundInit(int flag) {
		Log.e(TAG, "appWillEnterBackgroundInit");
	}

	/**
	 * 设置是否需要等待进入后台初始化 如果存在一些比较耗时的初始化时 将此方法打开
	 * 并在appDidEnterBackgroundInit中进行初始化工作 等待进入后台初始化时
	 * 需要在等待结束后调用doEnterBackgroundInit进行初始化的完成工作
	 */
	@Override
	public boolean waitEnterBackgroundInit(int flag) {
		Log.e(TAG, "waitEnterBackgroundInit");
		return false;
	}

	/**
	 * 当waitEnterBackgroundInit返回false时 该方法将会被调用 一些比较耗时的初始化工作将在此进行
	 */
	@Override
	public void appDidEnterBackgroundInit(int flag) {
		Log.e(TAG, "appDidEnterBackgroundInit");
	}

	
	/**
	 * 
	 * <p>功能描述</p>添加检测应用root权限
	 * @author jyb
	 * @date 2015年10月26日
	 */
	
	private void initRoot() {
		new AsyncTask<Object, Object, Object>() {
			@Override
			protected Object doInBackground(Object... params) {				
				try {
					MainApplication.isRootAvailable = RootShell.isRootAvailable();
					MainApplication.appIsAccessGiven = RootShell.isAccessGiven();
				} catch (Exception e) {
					if (Configs.isDebug) Log.e(TAG, "initRoot exception:"+e.getMessage());
				}
				return null;
			}
		}.execute(null, null);
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		return super.dispatchKeyEvent(event);
	}
}
