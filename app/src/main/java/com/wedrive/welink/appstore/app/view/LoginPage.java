/**
 * Created by wangzhichao on 2015年11月6日.
 * Copyright (c) 2015 北京图为先科技有限公司. All rights reserved.
 */
package com.wedrive.welink.appstore.app.view;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.Instrumentation;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.GeolocationPermissions;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mapbar.android.model.ActivityInterface;
import com.mapbar.android.model.BasePage;
import com.mapbar.android.model.FilterObj;
import com.mapbar.android.model.OnProviderListener;
import com.mapbar.android.model.ProviderResult;
import com.mapbar.android.provider.Provider;
import com.mapbar.android.statistics.api.MapbarMobStat;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.wedrive.welink.appstore.Configs;
import com.wedrive.welink.appstore.MainActivity;
import com.wedrive.welink.appstore.MainApplication;
import com.wedrive.welink.appstore.R;
import com.wedrive.welink.appstore.app.model.LoginUserBean;
import com.wedrive.welink.appstore.app.provider.SearchProvider;
import com.wedrive.welink.appstore.app.util.CommonUtil;
import com.wedrive.welink.appstore.app.util.PropertiesUtil;
import com.wedrive.welink.appstore.app.widget.DateTimePickerDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class LoginPage extends BasePage implements OnClickListener{

	private String pageName="appstore_"+getClass().getSimpleName();
	private Context mContext;
	private ActivityInterface mAif;
	private MainActivity mMainActivity;
	private LinearLayout mLinearLayout;
	private WebView web_view;
	private TextView tv_no_net;
	private Dialog dialog;
	private ProgressDialog mProgressDialog; 
	private DateTimePickerDialog mDateDialog;

	private static String mLoginUser;
	private static String mAccessToken;
	private static String mUnionid;
	private SearchProvider mProvider;
	private WeLinkJSInterface mWeLinkJSInterface;
	private static ThirdLoginCallBack mThirdLoginCallBack;
	private static AvatarCallBack mAvatarCallBack;
	private static ThirdBindCallBack mThirdBindCallBack;
	private static CloseKBCallBack mCloseKBCallBack;
	private Bitmap mBitmap;
	
	private static int loginState=-1;//0登录1绑定
	public static boolean loginCheck=false;
	public static LoginPage mLoginPage;
	
	public static Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Bundle bundle=null;
			switch (msg.what) {
			case 10:
				bundle=msg.getData();
				if(bundle!=null){
					mAccessToken=bundle.getString("wx_access_token");
					mLoginUser=bundle.getString("wx_openid");
					mUnionid=bundle.getString("wx_unionid");
					
					if(loginState==0 && mThirdLoginCallBack!=null){
						mThirdLoginCallBack.onComplete();
					}
					if(loginState==1 && mThirdBindCallBack!=null){
						mThirdBindCallBack.onComplete();
					}					
				}
				break;
			case 11:
				if(loginState==0 && mThirdLoginCallBack!=null){
					mThirdLoginCallBack.faile();
				}
				if(loginState==1 && mThirdBindCallBack!=null){
					mThirdBindCallBack.faile();
				}
				break;
			case 12:
				if(loginState==0 && mThirdLoginCallBack!=null){
					mThirdLoginCallBack.onComplete();
				}
				if(loginState==1 && mThirdBindCallBack!=null){
					mThirdBindCallBack.onComplete();
				}	
				break;
			case 13:
				if(mThirdLoginCallBack!=null) mThirdLoginCallBack.success();
				break;
			case 14:
				if(mThirdLoginCallBack!=null) mThirdLoginCallBack.faile();
				break;
			case 15:
				if(mAvatarCallBack!=null) mAvatarCallBack.call();
				break;
			case 16:
				if(mThirdBindCallBack!=null) mThirdBindCallBack.success();
				break;
			case 17:
				if(mThirdBindCallBack!=null) mThirdBindCallBack.faile();
				break;
			}
		}
	};	

	public LoginPage(Context context, View view, ActivityInterface aif) {
		super(context, view, aif);
		mContext = context;
		mAif = aif;
		mMainActivity=(MainActivity) aif;
		mLoginPage=this;
		initView(view);
		init();
	}
	
	private void initView(View view){
		mLinearLayout=(LinearLayout) view.findViewById(R.id.base_view);
		web_view=(WebView) view.findViewById(R.id.web_view);
		web_view.setBackgroundColor(0); // 设置背景色
		tv_no_net=(TextView) view.findViewById(R.id.tv_no_net);
		mDateDialog=new DateTimePickerDialog(mContext);
		mProgressDialog = new ProgressDialog(mContext);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgressDialog.setMessage("页面加载中，请稍后...");
		mProgressDialog.setCancelable(true);
	}
	
	
	@Override
	public void viewWillAppear(int flag) {
		super.viewWillAppear(flag);
		//9MapbarMobStat.onPageStart(mContext,pageName);
	}
	
	@Override
	public void viewWillDisappear(int flag) {
		super.viewWillDisappear(flag);
		//9MapbarMobStat.onPageEnd(mContext,pageName);
	}

	@Override
	public void viewDidAppear(int flag) {
		super.viewWillAppear(flag);
		MainActivity.mMainActivity.setAllTitle("","返回","");
		closeSoftBoard();
		if(!CommonUtil.isNetworkAvailable(mContext)){;
			web_view.setVisibility(View.GONE);			
			tv_no_net.setVisibility(View.VISIBLE);
		}else{
			web_view.setVisibility(View.VISIBLE);			
			tv_no_net.setVisibility(View.GONE);
			web_view.loadUrl(Configs.URL_WELINKE_HTML_LOGIN);
		}
	}

	@Override
	public void viewDidDisappear(int arg0) {
		super.viewDidDisappear(arg0);
		if(mProgressDialog!=null) mProgressDialog.dismiss();
	}

	/**
	 * 
	 * <p>功能描述</p>初始化数据
	 * @author wangzhichao
	 * @date 2015年11月10日
	 */
	@SuppressLint("SetJavaScriptEnabled")
	private void init(){
		mProvider = new SearchProvider(mContext);
		mWeLinkJSInterface =new WeLinkJSInterface();			
		web_view.getSettings().setJavaScriptEnabled(true);	
		web_view.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);  
		web_view.getSettings().setDomStorageEnabled(true);
		String appCacheDir = mContext.getApplicationContext().getDir("cache", Context.MODE_PRIVATE).getPath();
		web_view.getSettings().setAppCachePath(appCacheDir);
		web_view.getSettings().setAllowFileAccess(true);
		web_view.getSettings().setDatabaseEnabled(true);       
		web_view.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		web_view.addJavascriptInterface(mWeLinkJSInterface, "WeLink");
		web_view.setWebViewClient(mWebViewClient);
		web_view.setWebChromeClient(mChromeClient);
	}
	
	
	@Override
	public void onResume() {
		super.onResume();
		if (loginCheck && loginState != -1) {
			handler.postDelayed(new Runnable(){
			    public void run() {
			    	Message message = handler.obtainMessage();
					message.what = 11;
					handler.sendMessage(message);
			    }
			}, 3 * 1000);
		}
	}

	/**
	 * 
	 * <p>功能描述</p>编辑选择照片
	 * @param uri
	 * @author wangzhichao
	 * @date 2015年11月10日
	 */
	
	private void startPhotoZoom(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", true);
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", 100);
		intent.putExtra("outputY", 100);
		intent.putExtra("scale", true);
		intent.putExtra("return-data", true);
		intent.putExtra("noFaceDetection", true);
		intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG);
		mAif.startActivityForResult(intent, REQUESTCODE_CUTTING);
	}
	
	/**
	 * 
	 * <p>功能描述</p>保存编辑后的数据
	 * @param picdata
	 * @author wangzhichao
	 * @date 2015年11月10日
	 */
	
	private void saveUserPhoto(Intent picdata) {		
		try {
			Bundle extras = picdata.getExtras();
			mBitmap = extras.getParcelable("data");
			Message message = handler.obtainMessage();
			message.what=15;
			handler.sendMessage(message);			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.tv_camera:
			Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			// 下面这句指定调用相机拍照后的照片存储的路径
			takeIntent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(new File(Environment
							.getExternalStorageDirectory(), IMAGE_FILE_NAME)));
			mAif.startActivityForResult(takeIntent, REQUESTCODE_TAKE);

//			FilterObj filter = new FilterObj();
//			mAif.showPage(Configs.VIEW_POSITION_MANNGER_LOGIN, Configs.VIEW_POSITION_MANNGER_CAMERA, filter,true,null,null);	
//			dialog.dismiss();

//			Uri uri=null;
//			File file=new File(Environment.getExternalStorageDirectory(),IMAGE_FILE_NAME);
//			Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//			// 下面这句指定调用相机拍照后的照片存储的路径
//			if (Build.VERSION.SDK_INT >= 24){
//				uri=Uri.fromFile(file);
//			}else{
//				uri = FileProvider.getUriForFile(mContext, "com.wedrive.welink.appstore.fileprovider", file);
//			}
//			takeIntent.putExtra(MediaStore.EXTRA_OUTPUT,uri);
//			mAif.startActivityForResult(takeIntent, REQUESTCODE_TAKE);
			break;
		case R.id.tv_photo:
			Intent pickIntent = new Intent(Intent.ACTION_PICK, null);
			// 如果朋友们要限制上传到服务器的图片类型时可以直接写如：image/jpeg 、 image/png等的类型
			pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
			mAif.startActivityForResult(pickIntent, REQUESTCODE_PICK);
			dialog.dismiss();
			break;
		}
	}

	private class WeLinkJSInterface{
		
		private WeLinkLSInterface mWeLinkLSInterface;	
		
		public WeLinkLSInterface getLocalStorage(){
			if(mWeLinkLSInterface==null){
				mWeLinkLSInterface=new WeLinkLSInterface();
			}
			return mWeLinkLSInterface;
		}
		
		/**
		 * 
		 * <p>功能描述</p>设置标题
		 * @param titleJson
		 * @author wangzhichao
		 * @date 2015年11月9日
		 */
		
		public void setTitle(final String json){
			handler.post(new Runnable() {			
				@Override
				public void run() {
					try {
						String firstTitle="";
						String secondTitle="";
						String thirdTitle="";
						
						OnClickListener leftOnClickListener=null;
						OnClickListener rightOnClickListener=null;

						JSONObject jObj = new JSONObject(json);
						if(jObj.has("title")){
							firstTitle=jObj.getString("title");
						}
						
						if(jObj.has("back")){
							JSONObject bObj = jObj.getJSONObject("back");
							secondTitle=bObj.getString("text");
							if(bObj.has("method")){
								final String rMethod=bObj.getString("method");
								if(bObj.has("params")){
									final String[] rParam=bObj.getString("params").split(",");								
									final StringBuffer sb=new StringBuffer();
									for(int i=0;i<rParam.length;i++){
										if(i==rParam.length-1) sb.append(rParam[i]);
										else sb.append(rParam[i]).append(",");
									}
									
									leftOnClickListener=new OnClickListener() {								
										@Override
										public void onClick(View v) {
											web_view.loadUrl("javascript:"+rMethod+"(" + sb.toString() + ")");
										}
									};
								}else{
									leftOnClickListener=new OnClickListener() {								
										@Override
										public void onClick(View v) {
											web_view.loadUrl("javascript:"+rMethod+"()");
										}
									};
								}	
							}else{
								leftOnClickListener=new OnClickListener() {								
									@Override
									public void onClick(View v) {
										onBack();
									}
								};
							}
						}
						
						if(jObj.has("button")){
							JSONObject bObj = jObj.getJSONObject("button");
							thirdTitle=bObj.getString("text");
							if(bObj.has("method")){
								final String rMethod=bObj.getString("method");
								if(bObj.has("params")){								
									final String[] rParam=bObj.getString("params").split(",");								
									final StringBuffer sb=new StringBuffer();
									for(int i=0;i<rParam.length;i++){
										if(i==rParam.length-1)
										sb.append(rParam[i]);
										else sb.append(rParam[i]).append(",");
									}
									
									rightOnClickListener=new OnClickListener() {								
										@Override
										public void onClick(View v) {
											web_view.loadUrl("javascript:"+rMethod+"(" + sb.toString() + ")");
										}
									};
								}else{
									rightOnClickListener=new OnClickListener() {								
										@Override
										public void onClick(View v) {
											web_view.loadUrl("javascript:"+rMethod+"()");
										}
									};
								}	
							}
						}

						if(mAif.getCurrentPageObj().getPosition()==Configs.VIEW_POSITION_MANNGER_LOGIN){
							MainActivity.mMainActivity.setAllTitle(firstTitle, secondTitle,thirdTitle);						
							MainActivity.mMainActivity.setTitleOnClickListener(leftOnClickListener, rightOnClickListener);
						}
					} catch (JSONException e) {
						Log.e("message","exception:"+e.getMessage());
					}
				}
			});
			
		}

		/**
		 * 
		 * <p>功能描述</p>第三方登陆的方式，1：微信；2：QQ
		 * @param type
		 * @author wangzhichao
		 * @date 2015年11月9日
		 */
		
		public void loginWith(final int type,final String method){
			setThirdLoginCallBack(new ThirdLoginCallBack(){
				@Override
				public void callBack(String method,boolean flag) {
					if(flag) web_view.loadUrl("javascript:"+method+"(true)");
					else web_view.loadUrl("javascript:"+method+"(false)");
					mThirdLoginCallBack=null;
					mCloseKBCallBack=null;
					loginState=-1;
					loginCheck=false;
				}
				
				@Override
				public void onComplete() {
					mProvider.setOnProviderListener(mProviderListener);
					mProvider.authWXLogin(mLoginUser, mAccessToken, "appstore",mUnionid);			
				}

				@Override
				public void success() {
					callBack(method,true);
				}

				@Override
				public void faile() {
					callBack(method,false);
				}
							
			});
			
			loginState=0;
			loginCheck=true;
			
			switch(type){
			case 1:
				handler.post(new Runnable() {			
					@Override
					public void run() {							
						if (!MainApplication.api.isWXAppInstalled()) {
							mAif.showAlert("您没有安装微信客户端，请先安装");
							Message message = handler.obtainMessage();
							message.what=11;
							handler.sendMessage(message);
							return;
						}
						if (!MainApplication.api.isWXAppSupportAPI()) {
							mAif.showAlert("您当前微信版本不支持微信开放平台，请升级版本");
							Message message = handler.obtainMessage();
							message.what=11;
							handler.sendMessage(message);
							return;
						}
						
						setCloseKBCallBack(new CloseKBCallBack() {
							@Override
							public void callBack() {
								if (loginState != -1) {
									Message message = handler.obtainMessage();
									message.what = 11;
									handler.sendMessage(message);
								}
								InputMethodManager imm = (InputMethodManager) mMainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
								if (imm.isActive() && mMainActivity.getCurrentFocus() != null) {
									if (mMainActivity.getCurrentFocus().getWindowToken() != null) {
										web_view.loadUrl("javascript:clearInputOffset()");
										imm.hideSoftInputFromWindow(mMainActivity.getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
									}
								}
							}
						});	
						
						SendAuth.Req req = new SendAuth.Req();
						req.scope = Configs.THIRED_LOGIN_WEIXIN_SCOPE;
						req.state = Configs.THIRED_LOGIN_WEIXIN_STATE;
						if(!MainApplication.api.sendReq(req)){
							Message message=handler.obtainMessage();
							message.what = 11;
							handler.sendMessage(message);
							mAif.showAlert("调起授权界面失败，请稍后再试!");
						}

					}
				});
				break;
			case 2:
//				handler.post(new Runnable() {			
//					@Override
//					public void run() {					
//						mTencent = Tencent.createInstance(Configs.THIRED_LOGIN_AppId, mContext);
//						mTencent.login((Activity) mContext, "all", new BaseUiListener());
//					}
//				});				
				break;
			}
		}
		
		/**
		 * 
		 * <p>功能描述</p>调用客户端拍摄或选择头像
		 * @param callback
		 * @author wangzhichao
		 * @date 2015年11月12日
		 */
		
		public void getAvatar(final String method){
			File file=new File(Environment.getExternalStorageDirectory(), IMAGE_FILE_NAME);
			if(file.exists()) file.delete();		
			setAvatarCallBack(new AvatarCallBack(){
				@Override
				public void callBack(String method) {
					if(dialog!=null) dialog.dismiss();
					web_view.loadUrl("javascript:"+method+"(true)");
					MainActivity.mMainActivity.recycleLoginUserIcon();
				}

				@Override
				public void call() {
					callBack(method);				
				}		
			});
			handler.post(new Runnable() {
				@Override
				public void run() {	
					if(dialog==null){
						dialog = new Dialog(mContext, R.style.customDialog);
						dialog.setContentView(R.layout.photo_dialog);
						dialog.findViewById(R.id.tv_camera).setOnClickListener(LoginPage.this);
						dialog.findViewById(R.id.tv_photo).setOnClickListener(LoginPage.this);
					}
					if(dialog.isShowing()) dialog.dismiss();
					else dialog.show();
				}
			});		
		}
		
		/**
		 * 
		 * <p>功能描述</p>返回用户选择头像图片的base64，如果用户未选择返回空字符串
		 * @return
		 * @author wangzhichao
		 * @date 2015年11月13日
		 */
		
		public String getAvatarData(){
			String result="";	
			if(mBitmap!=null){
				try {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
					baos.flush();
					baos.close();
					mBitmap.recycle();
					mBitmap=null;
					byte[] bitmapBytes = baos.toByteArray();
					result = "data:image/jpeg;base64,"+Base64.encodeToString(bitmapBytes, Base64.NO_WRAP);
				} catch (IOException e) {
					Log.e("message","exception:"+e.getMessage());
				}
			}				
			return result;
		}
		
		/**
		 * 
		 * <p>功能描述</p>选择日期格式
		 * @param options
		 * @param callback
		 * @return
		 * @author wangzhichao
		 * @date 2015年11月16日
		 */
		
		public void selectDate(final String options, final String method){				
			handler.post(new Runnable() {			
				@Override
				public void run() {					
					try {
						JSONObject jObj = new JSONObject(options);
						String upperLimit=jObj.getString("upperLimit");
						String lowerLimit=jObj.getString("lowerLimit");
						String selected=jObj.getString("selected");

						DatePickerCallBack callBack=new DatePickerCallBack(){							
							@Override
							public void callBack(String method) {
								web_view.loadUrl("javascript:"+method);								
							}					
						};
						mDateDialog.init(upperLimit, lowerLimit, selected);
						mDateDialog.dateTimePicKDialog(callBack,method);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			});		
			
		}
		
		/**
		 * 
		 * <p>功能描述</p>绑定第三方账号
		 * @param type
		 * @param callback
		 * @author wangzhichao
		 * @date 2015年11月23日
		 */
		
		public void bindAccount(final int type, final String method){		
			setThirdBindCallBack(new ThirdBindCallBack(){
				@Override
				public void callBack(String method,boolean flag) {
					if(flag) web_view.loadUrl("javascript:"+method+"(true)");
					else web_view.loadUrl("javascript:"+method+"(false)");
					mThirdBindCallBack=null;
					mCloseKBCallBack=null;
					loginState=-1;
					loginCheck=false;
				}

				@Override
				public void success() {
					callBack(method,true);
				}

				@Override
				public void faile() {
					callBack(method,false);
				}

				@Override
				public void onComplete() {
					switch(type){
						case 1:
						{
							String openId=PropertiesUtil.getProperties(mContext, "WeDrive_Third_Open_WX");	
							String token=PropertiesUtil.getProperties(mContext, "WeDrive_Login_Token");			
							String access=PropertiesUtil.getProperties(mContext, "WeDrive_Third_Access_WX");
							String refresh=PropertiesUtil.getProperties(mContext, "WeDrive_Third_Refersh_WX");
							String unionid=PropertiesUtil.getProperties(mContext, "WeDrive_Third_Unionid_WX");
						
							mProvider.setOnProviderListener(mProviderListener);
							mProvider.bindWXThrid(openId,token,"weixin","appstore",access,refresh,unionid);							
						}
						break;
						case 2:
						{
							String openId=PropertiesUtil.getProperties(mContext, "WeDrive_Third_Open_QQ");	
							String token=PropertiesUtil.getProperties(mContext, "WeDrive_Login_Token");			
							String access=PropertiesUtil.getProperties(mContext, "WeDrive_Third_Access_QQ");
							
							mProvider.setOnProviderListener(mProviderListener);
							mProvider.bindQQThrid(openId,token,"qq","appstore",access);
						}
						break;
					}
				}				
			});			

			loginState=1;
			loginCheck=true;
			
			switch(type){
				case 1:
					if (!MainApplication.api.isWXAppInstalled()) {
						mAif.showAlert("您没有安装微信客户端，请先安装");
						Message message = handler.obtainMessage();
						message.what=11;
						handler.sendMessage(message);
						return;
					}
					if (!MainApplication.api.isWXAppSupportAPI()) {
						mAif.showAlert("您当前微信版本不支持微信开放平台，请升级版本");
						Message message = handler.obtainMessage();
						message.what=11;
						handler.sendMessage(message);
						return;
					}
					
					setCloseKBCallBack(new CloseKBCallBack() {
						@Override
						public void callBack() {
							if (loginState != -1) {
								Message message = handler.obtainMessage();
								message.what = 11;
								handler.sendMessage(message);
							}
							InputMethodManager imm = (InputMethodManager) mMainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
							if (imm.isActive() && mMainActivity.getCurrentFocus() != null) {
								if (mMainActivity.getCurrentFocus().getWindowToken() != null) {
									web_view.loadUrl("javascript:clearInputOffset()");
									imm.hideSoftInputFromWindow(mMainActivity.getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
								}
							}
						}
					});	
					handler.post(new Runnable() {
						@Override
						public void run() {	
							SendAuth.Req req = new SendAuth.Req();
							req.scope = Configs.THIRED_LOGIN_WEIXIN_SCOPE;
							req.state = Configs.THIRED_LOGIN_WEIXIN_STATE;
							if(!MainApplication.api.sendReq(req)){
								Message message=handler.obtainMessage();
								message.what = 11;
								handler.sendMessage(message);
								mAif.showAlert("调起授权界面失败，请稍后再试!");
							}
						}
					});
				break;
			case 2:
				handler.post(new Runnable() {
					@Override
					public void run() {	
//						mTencent = Tencent.createInstance(Configs.THIRED_LOGIN_AppId, mContext);
//						mTencent.login((Activity) mContext, "all", new BaseUiListener());
					}
				});
				break;
			}
		}
		
		/**
		 * 
		 * <p>功能描述</p>清除历史记录
		 * @author wangzhichao
		 * @date 2015年11月25日
		 */
		
		public void clearHistory(){ 
			web_view.clearHistory();
		}
		
		/**
		 * 
		 * 
		 * <p>功能描述</p>登录成功
		 * @author wangzhichao
		 * @date 2015年11月25日
		 */
		
		public void loginSuccessful(boolean logged){
			Configs.APPSTORE_IS_LOGIN=logged;
			if(!logged){
				loginState=-1;
				loginCheck=false;
				onResume();
			}
		}
		
		/**
		 * 
		 * <p>功能描述</p>获取软键盘高度
		 * @return
		 * @author wangzhichao
		 * @date 2015年12月14日
		 */
		
		public int getKeyboardHeight(){
			int height=(int) (mContext.getResources().getDisplayMetrics().heightPixels * 0.625);
			return height;
		}
		
		/**
		 * 
		 * <p>功能描述</p>返回应用类型
		 * @return
		 * @author wangzhichao
		 * @date 2015年12月21日
		 */
		
		public String getProduct(){
			return "appstore";
		}

		/**
		 *
		 * <p>功能描述</p>加载完成返回进入界面
		 * @return
		 * @author wangzhichao
		 * @date 2015年11月14日
		 */

//		public void onBack(){
//			mAif.showPrevious(null);
//		}
		
	}
	
	private class WeLinkLSInterface{
		/**
		 * 
		 * <p>功能描述</p>保存token值
		 * @param key
		 * @param value
		 * @author wangzhichao
		 * @date 2015年11月9日
		 */
		
		public void setItem(String key,String value){
			PropertiesUtil.addProperties(mContext, key, value);
		}
		
		/**
		 * 
		 * <p>功能描述</p>h获取token值
		 * @return
		 * @author wangzhichao
		 * @date 2015年11月9日
		 */
		
		public String getItem(String key){
			return PropertiesUtil.getProperties(mContext, key);
		}
		
		/**
		 * 
		 * <p>功能描述</p>上传token值
		 * @author wangzhichao
		 * @date 2015年11月9日
		 */
		
		public void removeItem(String key){
			PropertiesUtil.removeProperties(mContext, key);
		}
		
		/**
		 * 
		 * <p>功能描述</p>清空缓存数据
		 * @author wangzhichao
		 * @date 2015年11月12日
		 */
		
		public void clear(){
			PropertiesUtil.clearProperties(mContext);
		}

		/**
		 *
		 * <p>功能描述</p>h5界面统计点击事件
		 * @author wangzhichao
		 * @date 2015年11月12日
		 */

		public void onClick_Event(int index){
			String onClick_Event="";
			switch(index){
				case 1:
					onClick_Event=Configs.AppStore_OnClick_Account;
					break;
				case 2:
					onClick_Event=Configs.AppStore_OnClick_PhoneQuick;
					break;
				case 3:
					onClick_Event=Configs.AppStore_OnClick_Weixin;
					break;
				case 4:
					onClick_Event=Configs.AppStore_OnClick_AccountSettings;
					break;
				case 41:
					onClick_Event=Configs.AppStore_OnClick_BindPhone;
					break;
				case 42:
					onClick_Event=Configs.AppStore_OnClick_BindMail;
					break;
				case 43:
					onClick_Event=Configs.AppStore_OnClick_BindThirdAccount;
					break;
				case 44:
					onClick_Event=Configs.AppStore_OnClick_MyProfile;
					break;
				case 45:
					onClick_Event=Configs.AppStore_OnClick_MyCar;
					break;
				case 46:
					onClick_Event=Configs.AppStore_OnClick_ChangePassword;
					break;
				case 47:
					onClick_Event=Configs.AppStore_OnClick_ExitAccount;
					break;
				case 5:
					onClick_Event=Configs.AppStore_OnClick_Account;
					break;
				case 51:
					onClick_Event=Configs.AppStore_OnClick_VerifyPhone;
					break;
				case 52:
					onClick_Event=Configs.AppStore_OnClick_PrivacyPolicy;
					break;
				case 6:
					onClick_Event=Configs.AppStore_OnClick_ForgetPassword;
					break;
			}
			((MainActivity)mAif).onClick_View_OnClick_Event(mContext,onClick_Event);
		}

	}
	
	private final int REQUESTCODE_TAKE = 10;
	private final int REQUESTCODE_PICK = 11;
	private final int REQUESTCODE_CUTTING = 12;
	private final String IMAGE_FILE_NAME = "tempIamge.png";
	
	@Override
	public void setFilterObj(int flag, FilterObj filter) {
		super.setFilterObj(flag, filter);
		if(filter!=null){
			mBitmap = (Bitmap) filter.getTag();
			if(mBitmap!=null){
				Message message = handler.obtainMessage();
				message.what=15;
				handler.sendMessage(message);
			}
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUESTCODE_PICK:// 直接从相册获取
			if(data!=null && data.getData()!=null){
				startPhotoZoom(data.getData());
			}
			
//			try {
//				ContentResolver resolver = mContext.getContentResolver();  
//				Uri originalUri = data.getData();  
//				Bitmap bitmap = MediaStore.Images.Media.getBitmap(resolver, originalUri);
//				if (bitmap != null) {  
//					BitmapDrawable bd = new BitmapDrawable(bitmap);  
//			        Drawable drawable = (Drawable) bd;  
//					FilterObj filter = new FilterObj();
//					filter.setTag(drawable);
//					mAif.showPage(Configs.VIEW_POSITION_MANNGER_LOGIN, Configs.VIEW_POSITION_MANNGER_CROP, filter,true,null,null);
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
			break;
		case REQUESTCODE_TAKE:// 调用相机拍照
			File temp = new File(Environment.getExternalStorageDirectory() + File.separator + IMAGE_FILE_NAME);
			startPhotoZoom(Uri.fromFile(temp));
			break;
		case REQUESTCODE_CUTTING:// 取得裁剪后的图片
			if (data != null && data.getExtras()!=null) {
				saveUserPhoto(data);
			}
			break;
		}
	}
	
	private OnProviderListener mProviderListener = new OnProviderListener() {

		@Override
		public void onProviderResponse(int requestCode, int responseCode,ProviderResult result) {
			mAif.hideProgressDialog();
			if (responseCode == Provider.RESULT_OK) {

				Message message = handler.obtainMessage();
				switch (requestCode) {
				case Configs.REQUEST_CODE_WXAUTH_LOGIN://qq登录		
				case Configs.REQUEST_CODE_QQAUTH_LOGIN://微信登录
					try {
						Configs.APPSTORE_IS_LOGIN=false;
						JSONObject obj = new JSONObject(result.getResponseStr());						
						int status = obj.getInt("code");
						if (status == 200) {
							obj = obj.getJSONObject("data");
							LoginUserBean userBean=new LoginUserBean();
							userBean.parse(obj);

							mAccessToken = userBean.getToken();
							Configs.X_Auth_Token = mAccessToken;
							Configs.APPSTORE_IS_LOGIN=true;

							HashMap<String,String> properties=new HashMap<String,String>();
							properties.put("WeDrive_Login_Name", mLoginUser);
							properties.put("WeDrive_Login_Token", mAccessToken);	
							properties.put("WeDrive_Login_User_ID)", userBean.getUserId());
							properties.put("WeDrive_Login_Nice_Name", userBean.getNickname());	
							PropertiesUtil.addAllProperties(mContext, properties);
							
							MainActivity.mMainActivity.recycleLoginUserIcon();
							message.what=13;							
							mAif.showAlert("登录成功");
						}else{
							message.what=14;
							String msg=obj.getString("message");
							mAif.showAlert(msg);
						}
					} catch (JSONException e) {
						message.what=14;
						mAif.showAlert("登录失败");
					}finally{
						handler.sendMessage(message);
					}
					break;
				case Configs.REQUEST_CODE_BIND_THIRD://绑定第三方
					try {						
						JSONObject obj = new JSONObject(result.getResponseStr());						
						int status = obj.getInt("code");
						if(status==200){
							mAif.showAlert("绑定成功");
							message.what=16;		
						}else if(status==1036){
							mAif.showAlert("该微信已绑定其它账号，请先解绑");
							message.what=17;
						}
						else{
							String msg=obj.getString("message");
							mAif.showAlert(msg);
							message.what=17;
						}
					} catch (JSONException e) {
						mAif.showAlert("绑定失败");
						message.what=17;	
					}finally{
						handler.sendMessage(message);
					}
					break;
				default:
					break;
				}
			} else {
				Message message = handler.obtainMessage();
				switch (requestCode) {
				case Configs.REQUEST_CODE_WXAUTH_LOGIN://qq登录		
				case Configs.REQUEST_CODE_QQAUTH_LOGIN://微信登录	
					mAif.showAlert("登录失败");
					message.what=18;					
					break;
				case Configs.REQUEST_CODE_BIND_THIRD://绑定第三方
					mAif.showAlert("绑定失败");
					message.what=17;
					break;
				}				
				handler.sendMessage(message);
			}
		}

		@Override
		public void onReadResponse(int arg0, int arg1) {

		}
	};
	
	
	public interface ThirdLoginCallBack {
		public void callBack(String callback,boolean flag);
		public void onComplete();
		public void success();
		public void faile();
	}
	
	public interface ThirdBindCallBack {
		public void callBack(String callback,boolean flag);
		public void onComplete();
		public void success();
		public void faile();
	}
	
	public interface AvatarCallBack {
		public void callBack(String callback);
		public void call();
	}
	
	public interface DatePickerCallBack {
		public void callBack(String callback);
	}
	
	public interface CloseKBCallBack {
		public void callBack();
	}
	
	
	public void setThirdLoginCallBack(ThirdLoginCallBack mThirdLoginCallBack) {
		this.mThirdLoginCallBack = mThirdLoginCallBack;
	}
	
	public void setThirdBindCallBack(ThirdBindCallBack mThirdBindCallBack) {
		this.mThirdBindCallBack = mThirdBindCallBack;
	}

	public void setAvatarCallBack(AvatarCallBack mAvatarCallBack) {
		this.mAvatarCallBack = mAvatarCallBack;
	}

	public void setCloseKBCallBack(CloseKBCallBack mCloseKBCallBack) {
		this.mCloseKBCallBack = mCloseKBCallBack;
	}
	
	private WebChromeClient mChromeClient = new WebChromeClient() {

        private View myView = null;
        private CustomViewCallback myCallback = null;

        // 配置权限 （在WebChromeClinet中实现）
        @Override
        public void onGeolocationPermissionsShowPrompt(String origin,
                GeolocationPermissions.Callback callback) {
            callback.invoke(origin, true, false);
            super.onGeolocationPermissionsShowPrompt(origin, callback);
        }

        // 扩充数据库的容量（在WebChromeClinet中实现）
        @Override
        public void onExceededDatabaseQuota(String url,
                String databaseIdentifier, long currentQuota,
                long estimatedSize, long totalUsedQuota,
                WebStorage.QuotaUpdater quotaUpdater) {

            quotaUpdater.updateQuota(estimatedSize * 2);
        }

        // 扩充缓存的容量
        @Override
        public void onReachedMaxAppCacheSize(long spaceNeeded,
                long totalUsedQuota, WebStorage.QuotaUpdater quotaUpdater) {

            quotaUpdater.updateQuota(spaceNeeded * 2);
        }

        // Android 使WebView支持HTML5 Video（全屏）播放的方法
        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            if (myCallback != null) {
                myCallback.onCustomViewHidden();
                myCallback = null;
                return;
            }

            ViewGroup parent = (ViewGroup) web_view.getParent();
            parent.removeView(web_view);
            parent.addView(view);
            myView = view;
            myCallback = callback;
            mChromeClient = this;
        }

        @Override
        public void onHideCustomView() {
            if (myView != null) {
                if (myCallback != null) {
                    myCallback.onCustomViewHidden();
                    myCallback = null;
                }

                ViewGroup parent = (ViewGroup) myView.getParent();
                parent.removeView(myView);
                parent.addView(web_view);
                myView = null;
            }
        }
    };
    
    private WebViewClient mWebViewClient = new WebViewClient() {
    	
    	public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error){  
            handler.proceed();  
    	}  
    	
    	@Override 
        public void onPageFinished(WebView view,String url){		
            if(mProgressDialog!=null) mProgressDialog.dismiss();
        }

 		@Override
 		public void onReceivedError(WebView view, int errorCode,String description, String failingUrl) {
 			if(mProgressDialog!=null) mProgressDialog.dismiss();
 			mAif.showAlert("网页加载失败");
 		}

 		@Override
 		public void onPageStarted(WebView view, String url, Bitmap favicon) {
 			if(mProgressDialog!=null) mProgressDialog.show();
 		}
 		
    };


    /**
     * 
    * @Title: closeSoftBoard 
    * @Description: 关闭软键盘
    * @return void :
    * @author : wangzc
    * @date 2016年4月26日
     */
    private void closeSoftBoard(){
		View view = mMainActivity.getWindow().peekDecorView();
        if (view != null) {
            InputMethodManager inputmanger = (InputMethodManager) mMainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputmanger.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
	}

	/**
	 * 
	 * <p>功能描述</p>模拟返回按键
	 * @author wangzhichao
	 * @date 2015年11月9日
	 */

	public void onBack() {
    	if(web_view.canGoBack()){
    		web_view.goBack();
    	}else{
			MainActivity.mMainActivity.setTitleOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					new Thread() {
						public void run() {
							try {
								Instrumentation inst = new Instrumentation();
								inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
							} catch (Exception e) {
								Log.e("Exception when onBack", e.toString());
							}
						}
					}.start();
				}
			}, null);
    		web_view.stopLoading();
    		web_view.clearHistory();
    		mLinearLayout.removeView(web_view); 
    		web_view.removeAllViews();
    		web_view.destroy();
    		mProgressDialog.dismiss();
			setCloseKBCallBack(null);
    		closeSoftBoard();
    		mAif.showPrevious(null);
    	} 
	}
    
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_BACK){
        	onBack();
        }     
		return true;
    }

	@Override
	public boolean dispatchKeyEvent(KeyEvent keyEvent) {
		if(keyEvent.getAction()==KeyEvent.ACTION_DOWN){
			if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK && mCloseKBCallBack != null) {
				mCloseKBCallBack.callBack();
				mCloseKBCallBack=null;
			}
		}
		return super.dispatchKeyEvent(keyEvent);
	}
    
    @Override
	public int getMyViewPosition() {
		return Configs.VIEW_POSITION_MANNGER_LOGIN;
	}

}
