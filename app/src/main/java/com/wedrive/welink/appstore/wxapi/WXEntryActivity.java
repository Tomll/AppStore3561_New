package com.wedrive.welink.appstore.wxapi;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.wedrive.welink.appstore.Configs;
import com.wedrive.welink.appstore.MainApplication;
import com.wedrive.welink.appstore.app.util.PropertiesUtil;
import com.wedrive.welink.appstore.app.view.LoginPage;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

	private String GetCodeRequest = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code";

	public Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 10:
				String message=(String) msg.obj;
				if (!TextUtils.isEmpty(message)){
					Toast.makeText(WXEntryActivity.this, message, Toast.LENGTH_LONG).show();
				}
				break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		LoginPage.loginCheck=false;
		MainApplication.api.handleIntent(getIntent(), this);		
	}
	

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		MainApplication.api.handleIntent(intent, this);
		finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onReq(BaseReq arg0) {
		finish();
	}



	@Override
	public void onResp(BaseResp resp) {
		Message msg=handler.obtainMessage();
		msg.what=10;
		Message message=LoginPage.handler.obtainMessage();
		message.what=11;		
		switch (resp.errCode) {
		case BaseResp.ErrCode.ERR_OK:// 用户同意授权
			String code = ((SendAuth.Resp) resp).code;
			if (ConstantsAPI.COMMAND_SENDAUTH == resp.getType()) {//分享成功
				getUserInfo(code);
			}else{
				msg.obj="分享失败";
				msg.sendToTarget();
				message.sendToTarget();
			}
			finish();
			break;
		case BaseResp.ErrCode.ERR_AUTH_DENIED:// 用户拒绝授权
			msg.obj="发送拒绝";
			msg.sendToTarget();
			message.sendToTarget();
			finish();
			break;
		case BaseResp.ErrCode.ERR_USER_CANCEL:// 用户取消授权
			msg.obj="发送取消";
			msg.sendToTarget();
			message.sendToTarget();
			finish();
			break;
		default:
			msg.obj="发送返回";
			msg.sendToTarget();
			message.sendToTarget();
			finish();
			break;
		}
	}
	
	/**
	 * 
	 * <p>功能描述</p>获取access_token
	 * @param code
	 * @author wangzhichao
	 * @date 2015年11月6日
	 */
	
	private void getUserInfo(String code) {
		final String access_token = getCodeRequest(code);		
		new Thread(){  
			 public void run(){ 
				 Looper.prepare(); 
				 WXGetAccessToken(access_token);
				 Looper.loop(); 		
			 }			 
		}.start();
	}

	/**
	 * 获取access_token的URL（微信）
	 * 
	 * @param code
	 *            授权时，微信回调给的
	 * @return URL
	 */
	public String getCodeRequest(String code) {
		String result = null;
		GetCodeRequest = GetCodeRequest.replace("APPID",urlEnodeUTF8(Configs.THIRED_LOGIN_WEIXIN_APP_ID));
		GetCodeRequest = GetCodeRequest.replace("SECRET",urlEnodeUTF8(Configs.THIRED_LOGIN_AppSecret));
		GetCodeRequest = GetCodeRequest.replace("CODE", urlEnodeUTF8(code));
		result = GetCodeRequest;
		return result;
	}

	public String urlEnodeUTF8(String str) {
		String result = str;
		try {
			result = URLEncoder.encode(str, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * 获取access_token等等的信息(微信)
	 */
	private void WXGetAccessToken(String access_token) {
		HttpClient get_access_token_httpClient = new DefaultHttpClient();
		String wx_access_token = "";
		String wx_openid = "";
		String wx_refresh_token="";
		String wx_unionid="";
		try {
			HttpPost postMethod = new HttpPost(access_token);
			HttpResponse response = get_access_token_httpClient.execute(postMethod); // 执行POST方法
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				InputStream is = response.getEntity().getContent();
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				String str = "";
				StringBuffer sb = new StringBuffer();
				while ((str = br.readLine()) != null) {
					sb.append(str);
				}
				is.close();
				JSONObject json = new JSONObject(sb.toString());
				wx_access_token = (String) json.get("access_token");
				wx_openid = (String) json.get("openid");
				wx_refresh_token = (String) json.get("refresh_token");
				wx_unionid = (String) json.get("unionid");
				
				HashMap<String,String> properties=new HashMap<String,String>();
				properties.put("WeDrive_Third_Open_WX", wx_openid);
				properties.put("WeDrive_Third_Access_WX", wx_access_token);	
				properties.put("WeDrive_Third_Refersh_WX", wx_refresh_token);
				properties.put("WeDrive_Third_Unionid_WX", wx_unionid);
				PropertiesUtil.addAllProperties(this, properties);
			
				Message message=LoginPage.handler.obtainMessage();
				Bundle bundle=new Bundle();
				message.what=10;
				bundle.putString("wx_access_token", wx_access_token);
				bundle.putString("wx_openid", wx_openid);
				bundle.putString("wx_unionid", wx_unionid);
				message.setData(bundle);
				message.sendToTarget();
			} else {
				Message msg=handler.obtainMessage();
				msg.what=10;
				msg.obj="授权失败";
				
				Message message=LoginPage.handler.obtainMessage();
				message.what=11;
				
				msg.sendToTarget();
				message.sendToTarget();
			}
		} catch (Exception e) {
			Message msg=handler.obtainMessage();
			msg.what=10;
			msg.obj="授权失败";
			
			Message message=LoginPage.handler.obtainMessage();
			message.what=11;
			
			msg.sendToTarget();
			message.sendToTarget();
		}
	}
}