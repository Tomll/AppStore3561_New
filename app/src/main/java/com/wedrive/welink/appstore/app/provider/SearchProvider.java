package com.wedrive.welink.appstore.app.provider;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.mapbar.android.model.ProviderResult;
import com.mapbar.android.net.HttpHandler.HttpRequestType;
import com.mapbar.android.net.MyHttpHandler;
import com.mapbar.android.provider.Provider;
import com.mapbar.android.provider.ResultParser;
import com.wedrive.welink.appstore.Configs;
import com.wedrive.welink.appstore.app.util.CommonUtil;
import com.wedrive.welink.appstore.app.util.DPApiTool;

public class SearchProvider extends Provider
{
	private final static String TAG = "SearchProvider";	
	private Context mContext;
	
	public SearchProvider(Context context) {
		super(context);
		mContext=context;
	}
	
	@Override
	public ResultParser createResultParser()
	{
		return new SearchResultParser();
	}
	
	/**
	 * 获取应用数据列表
	 * @param paramMap
	 * @return
	 */

	
	public MyHttpHandler loadAppsList(LinkedHashMap<String, String> paramMap){
		String apiUrl = Configs.URL_LOAD_APPS_LIST;		
		StringBuffer sbUrl = new StringBuffer(apiUrl);
		String queryString = DPApiTool.getQueryString(paramMap);
		if (!TextUtils.isEmpty(queryString)){
			try {
				queryString = URIUtil.encodeQuery(queryString, "UTF-8");
			} catch (URIException e) {
				e.printStackTrace();
			}
		}
		sbUrl.append(queryString);
		int pageIndex = Integer.parseInt(paramMap.get("p_index"));
		if(Configs.isDebug) Log.e(TAG,"loadAppsList:"+sbUrl);
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("ck",Configs.HEADER_CORPORATE_KEY);
		setHeaders(headers);
		return getDataFromNet(HttpRequestType.GET, Configs.REQUEST_CODE_LOAD_APPS_LIST, pageIndex,sbUrl.toString());
	}
	
	/**
	 * 获取应用数据列表
	 * @param paramMap
	 * @return
	 */

	
	public MyHttpHandler loadBanngerList(LinkedHashMap<String, String> paramMap){
		String apiUrl = Configs.URL_RECORD_BANNGER_LIST;		
		StringBuffer sbUrl = new StringBuffer(apiUrl);
		String queryString = DPApiTool.getQueryString(paramMap);
		if (!TextUtils.isEmpty(queryString)){
			try {
				queryString = URIUtil.encodeQuery(queryString, "UTF-8");
			} catch (URIException e) {
				e.printStackTrace();
			}
		}
		sbUrl.append(queryString);
		if(Configs.isDebug) Log.e(TAG,"loadBanngerList:"+sbUrl);
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("ck",Configs.HEADER_CORPORATE_KEY);
		setHeaders(headers);
		return getDataFromNet(HttpRequestType.GET, Configs.REQUEST_CODE_RECORD_BANNGER_LIST, -1,sbUrl.toString());
	}
	
	
	/**
	 * 获取应用详情数据
	 * @param paramMap
	 * @return
	 */
	
	public MyHttpHandler loadAppDetail(LinkedHashMap<String, String> paramMap){
		String apiUrl = Configs.URL_LOAD_APP_DETAIL;
		StringBuffer sbUrl = new StringBuffer(apiUrl);
		String queryString = DPApiTool.getQueryString(paramMap);
		if (!TextUtils.isEmpty(queryString)) {
			try {
				queryString = URIUtil.encodeQuery(queryString, "UTF-8");
			} catch (URIException e) {
				e.printStackTrace();
			}
		}
		sbUrl.append(queryString);
		if(Configs.isDebug) Log.e(TAG,"loadAppDetail:"+sbUrl);
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("ck",Configs.HEADER_CORPORATE_KEY);
		setHeaders(headers);
		return getDataFromNet(HttpRequestType.GET, Configs.REQUEST_CODE_LOAD_APP_DETAIL, -1,sbUrl.toString());
	}
	
	/**
	 * 应用商店自更新
	 * @param paramMap
	 * @return
	 */
	
	public MyHttpHandler checkVersion(LinkedHashMap<String, String> paramMap){
		String apiUrl = Configs.URL_APPSTOR_CHECK_VERSION;
		StringBuffer sbUrl = new StringBuffer(apiUrl);
		String queryString = DPApiTool.getQueryString(paramMap);
		if (!TextUtils.isEmpty(queryString)) {
			try {
				queryString = URIUtil.encodeQuery(queryString, "UTF-8");
			} catch (URIException e) {
				e.printStackTrace();
			}
		}
		sbUrl.append(queryString).append("/");
		if(Configs.isDebug) Log.e(TAG,"checkVersion:"+sbUrl);
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("ck",Configs.HEADER_CORPORATE_KEY);
		setHeaders(headers);
		return getDataFromNet(HttpRequestType.GET, Configs.REQUEST_CODE_APPSTOR_CHECK_VERSION, -1,sbUrl.toString());
	}
	
	/**
	 * 获取更新应用信息
	 * @param paramMap
	 * @return
	 */
	
	public MyHttpHandler checkVersionApps(LinkedHashMap<String, String> paramMap,LinkedHashMap<String, String> paramMap2) {
		String apiUrl = Configs.URL_CHECK_VERSION_APPS;
		StringBuffer sbUrl = new StringBuffer(apiUrl);
		String queryString = DPApiTool.getQueryString(paramMap);
		if (!TextUtils.isEmpty(queryString)) {
			try {
				queryString = URIUtil.encodeQuery(queryString, "UTF-8");
			} catch (URIException e) {
				e.printStackTrace();
			}
		}
		sbUrl.append(queryString);
		
		String queryString2 = DPApiTool.getQueryString2(paramMap2);
		if (!TextUtils.isEmpty(queryString2)) {
			try {
				queryString2 = URIUtil.encodeQuery(queryString2, "UTF-8");
			} catch (URIException e) {
				e.printStackTrace();
			}
		}
		sbUrl.append("/?").append(queryString2);
		if(Configs.isDebug) Log.e(TAG,"checkVersionApps:"+sbUrl.toString());
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("ck",Configs.HEADER_CORPORATE_KEY);
		setHeaders(headers);
		return getDataFromNet(HttpRequestType.POST, Configs.REQUEST_CODE_CHECK_VERSION_APPS, 1,sbUrl.toString());
	}
	
	
	/**
	 * 获取应用评论数据列表
	 * @param paramMap
	 * @return
	 */
	
	public MyHttpHandler loadAppCommons(LinkedHashMap<String, String> paramMap){
		String apiUrl = Configs.URL_LOAD_APP_COMMONS;
		StringBuffer sbUrl = new StringBuffer(apiUrl);
		String queryString = DPApiTool.getQueryString(paramMap);
		if (!TextUtils.isEmpty(queryString)){
			try {
				queryString = URIUtil.encodeQuery(queryString, "UTF-8");
			} catch (URIException e) {
				e.printStackTrace();
			}
		}
		sbUrl.append(queryString);
		int pageIndex = Integer.parseInt(paramMap.get("p_index"));
		if(Configs.isDebug) Log.e(TAG,"loadAppCommons:"+sbUrl);
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("ck",Configs.HEADER_CORPORATE_KEY);
		setHeaders(headers);
		return getDataFromNet(HttpRequestType.GET, Configs.REQUEST_CODE_LOAD_APP_COMMONS, pageIndex,sbUrl.toString());
	}
	
	/**
	 * 
	 * <p>功能描述</p>获取用户应用评论数据
	 * @param paramMap
	 * @return
	 * @author wangzhichao
	 * @date 2015年12月30日
	 */
	
	public MyHttpHandler loadUserAppCommons(LinkedHashMap<String, String> paramMap){
		String apiUrl = Configs.URL_LOAD_APP_USER_COMMONS;
		StringBuffer sbUrl = new StringBuffer(apiUrl);
		String queryString = DPApiTool.getQueryString(paramMap);
		if (!TextUtils.isEmpty(queryString)){
			try {
				queryString = URIUtil.encodeQuery(queryString, "UTF-8");
			} catch (URIException e) {
				e.printStackTrace();
			}
		}
		sbUrl.append(queryString);
		if(Configs.isDebug) Log.e(TAG,"loadAppCommons:"+sbUrl);
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("ck",Configs.HEADER_CORPORATE_KEY);
		setHeaders(headers);
		return getDataFromNet(HttpRequestType.GET, Configs.REQUEST_CODE_LOAD_APP_USER_COMMONS, 1,sbUrl.toString());
	}
	
	/**
	 * 
	 * <p>功能描述</p>更新用户应用评论数据
	 * @param paramMap
	 * @return
	 * @author wangzhichao
	 * @date 2016年1月4日
	 */
	
//	public MyHttpHandler updateUserAppCommons(LinkedHashMap<String, String> paramMap){
//		String apiUrl = Configs.URL_UPDATE_APP_USER_COMMONS;
//		StringBuffer sbUrl = new StringBuffer(apiUrl);
//		String queryString = DPApiTool.getQueryString(paramMap);
//		if (!TextUtils.isEmpty(queryString)){
//			try {
//				queryString = URIUtil.encodeQuery(queryString, "UTF-8");
//			} catch (URIException e) {
//				e.printStackTrace();
//			}
//		}
//		sbUrl.append(queryString);
//		if(Configs.isDebug) Log.e(TAG,"loadAppCommons:"+sbUrl);
//		HashMap<String, String> headers = new HashMap<String, String>();
//		headers.put("ck",Configs.HEADER_CORPORATE_KEY);
//		setHeaders(headers);
//		return getDataFromNet(HttpRequestType.GET, Configs.REQUEST_CODE_UPDATE_APP_USER_COMMONS, 1,sbUrl.toString());
//	}
	
	/**
	 * 提交应用评论数据
	 * @param paramMap
	 * @return
	 */
	
	public MyHttpHandler upLoadAppCommon(LinkedHashMap<String, String> paramMap,LinkedHashMap<String, String> paramMap2,String token) {
		String apiUrl = Configs.URL_UPLOAD_APP_COMMON;
		StringBuffer sbUrl = new StringBuffer(apiUrl);
		String queryString = DPApiTool.getQueryString(paramMap);
		if (!TextUtils.isEmpty(queryString)) {
			try {
				queryString = URIUtil.encodeQuery(queryString, "UTF-8");
			} catch (URIException e) {
				e.printStackTrace();
			}
		}
		sbUrl.append(queryString);
		
		String queryString2 = DPApiTool.getQueryString2(paramMap2);
		if (!TextUtils.isEmpty(queryString2)) {
			try {
				queryString2 = URIUtil.encodeQuery(queryString2, "UTF-8");
			} catch (URIException e) {
				e.printStackTrace();
			}
		}
		sbUrl.append("/?").append(queryString2);
		
		if(Configs.isDebug) Log.e(TAG,"upLoadAppCommon:"+sbUrl.toString());
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("token",token);
		headers.put("ck",Configs.HEADER_CORPORATE_KEY);
		setHeaders(headers);
		return getDataFromNet(HttpRequestType.GET, Configs.REQUEST_CODE_UPLOAD_APP_COMMON, 1,sbUrl.toString());
	}
	
	/**
	 * 获取收索界面推荐应用数据
	 * @param paramMap
	 * @return
	 */
	
	public MyHttpHandler loadRecomApps(LinkedHashMap<String, String> paramMap) {
		String apiUrl = Configs.URL_APPS_RECOM_LIST;
		StringBuffer sbUrl = new StringBuffer(apiUrl);
		String queryString = DPApiTool.getQueryString(paramMap);
		if (!TextUtils.isEmpty(queryString)) {
			try {
				queryString = URIUtil.encodeQuery(queryString, "UTF-8");
			} catch (URIException e) {
				e.printStackTrace();
			}
		}
		sbUrl.append(queryString);
		if(Configs.isDebug) Log.e(TAG,"loadRecomApps:"+sbUrl.toString());
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("ck",Configs.HEADER_CORPORATE_KEY);
		setHeaders(headers);
		return getDataFromNet(HttpRequestType.GET, Configs.REQUEST_CODE_APPS_RECOM_LIST, 1,sbUrl.toString());
	}
	
	/**
	 * 获取收索应用数据
	 * @param paramMap
	 * @return
	 */
	
	public MyHttpHandler loadSearchApps(LinkedHashMap<String, String> paramMap) {
		String apiUrl = Configs.URL_SEARCH_APPS_LIST;
		StringBuffer sbUrl = new StringBuffer(apiUrl);
		String queryString = DPApiTool.getQueryString(paramMap);
		if (!TextUtils.isEmpty(queryString)) {
			try {
				queryString = URIUtil.encodeQuery(queryString, "UTF-8");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		sbUrl.append(queryString);
		if(Configs.isDebug) Log.e(TAG,"loadSearchApps:"+sbUrl.toString());
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("ck",Configs.HEADER_CORPORATE_KEY);
		setHeaders(headers);
		return getDataFromNet(HttpRequestType.GET, Configs.REQUEST_CODE_SEARCH_APPS_LIST, 1,sbUrl.toString());
	}
	
	/**
	 * 获取下载日志记录数据
	 * @param paramMap
	 * @return
	 */
	
	public MyHttpHandler loadRecordLogs(LinkedHashMap<String, String> paramMap) {
		String apiUrl = Configs.URL_RECORD_LOG_LIST;
		StringBuffer sbUrl = new StringBuffer(apiUrl);
		String queryString = DPApiTool.getQueryString(paramMap);
		if (!TextUtils.isEmpty(queryString)) {
			try {
				queryString = URIUtil.encodeQuery(queryString, "UTF-8");
			} catch (URIException e) {
				e.printStackTrace();
			}
		}
		sbUrl.append(queryString);
		if(Configs.isDebug) Log.e(TAG,"loadRecordLogs:"+sbUrl.toString());
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("ck",Configs.HEADER_CORPORATE_KEY);
		setHeaders(headers);
		return getDataFromNet(HttpRequestType.GET, Configs.REQUEST_CODE_RECORD_LOG_LIST, 1,sbUrl.toString());
	}
	
	/**
	 * 获取榜单应用数据
	 * @param paramMap
	 * @return
	 */
	
	public MyHttpHandler loadBillBoardApps(LinkedHashMap<String, String> paramMap) {
		String apiUrl = Configs.URL_BILLBOARD_APPS_LIST;
		StringBuffer sbUrl = new StringBuffer(apiUrl);
		String queryString = DPApiTool.getQueryString(paramMap);
		if (!TextUtils.isEmpty(queryString)) {
			try {
				queryString = URIUtil.encodeQuery(queryString, "UTF-8");
			} catch (URIException e) {
				e.printStackTrace();
			}
		}
		sbUrl.append(queryString);
		if(Configs.isDebug) Log.e(TAG,"loadBillBoardApps:"+sbUrl.toString());
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("ck",Configs.HEADER_CORPORATE_KEY);
		setHeaders(headers);
		return getDataFromNet(HttpRequestType.GET, Configs.REQUEST_CODE_BILLBOARD_APPS_LIST, 1,sbUrl.toString());
	}
	
	/**
	 * 
	 * <p>功能描述</p>获取已经安装的应用列表
	 * @param paramMap
	 * @return
	 * @author wangzhichao
	 * @date 2015年10月22日
	 */
	
	public MyHttpHandler loadInstalApps(LinkedHashMap<String, String> paramMap,LinkedHashMap<String, String> paramMap2) {
		String apiUrl = Configs.URL_RECORD_INSTAL_LIST;
		StringBuffer sbUrl = new StringBuffer(apiUrl);
		String queryString = DPApiTool.getQueryString(paramMap);
		if (!TextUtils.isEmpty(queryString)) {
			try {
				queryString = URIUtil.encodeQuery(queryString, "UTF-8");
			} catch (URIException e) {
				e.printStackTrace();
			}
		}
		sbUrl.append(queryString);
		String queryString2 = DPApiTool.getQueryString2(paramMap2);
		if (!TextUtils.isEmpty(queryString2)) {
			try {
				queryString2 = URIUtil.encodeQuery(queryString2, "UTF-8");
			} catch (URIException e) {
				e.printStackTrace();
			}
		}
		sbUrl.append("/?").append(queryString2);
		if(Configs.isDebug) Log.e(TAG,"loadInstalApps:"+sbUrl.toString());
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("ck",Configs.HEADER_CORPORATE_KEY);
		setHeaders(headers);
		return getDataFromNet(HttpRequestType.GET, Configs.REQUEST_CODE_INSTAL_APPS_LIST, 1,sbUrl.toString());
	}
	
	/**
	 * @功能描述 微信第三方登录
	 * @param String
	 *            openId String accessToken String type
	 * 
	 * @return MyHttpHandler http
	 * @author sulw
	 * @date 2015/1/22
	 */
	public MyHttpHandler authWXLogin(String openId, String accessToken,String product,String unionid) {

		byte[] bytes = null;
		JSONObject jObj = new JSONObject();
		try {
			jObj.put("openId", openId);
			jObj.put("accessToken", accessToken);
			jObj.put("type", "weixin");
			jObj.put("product", product);
			jObj.put("unionid", unionid);
			bytes = jObj.toString().getBytes();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		HashMap<String, String> headers = new HashMap<String, String>();
		String ContentType = "application/json";
		headers.put("Accept", "application/json");
		headers.put("device-id", CommonUtil.getDeviceID(mContext));
		headers.put("device-type", Configs.HEADER_DEVICE_TYPE);
		headers.put("app-version", CommonUtil.getVersion(mContext));
		setHeaders(headers);
		return getDataFromNet(HttpRequestType.POST, Configs.REQUEST_CODE_WXAUTH_LOGIN, -1,Configs.URL_THIRD_lOGIN, null, bytes, null, ContentType);
	}
	
	/**
	 * @功能描述 QQ第三方登录
	 * @param String
	 *            openId String accessToken String type
	 * 
	 * @return MyHttpHandler http
	 * @author sulw
	 * @date 2015/1/22
	 */
	public MyHttpHandler authQQLogin(String openId, String accessToken,String product) {

		byte[] bytes = null;
		JSONObject jObj = new JSONObject();
		try {
			jObj.put("openId", openId);
			jObj.put("accessToken", accessToken);
			jObj.put("type", "qq");
			jObj.put("product", product);
			bytes = jObj.toString().getBytes();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		HashMap<String, String> headers = new HashMap<String, String>();
		String ContentType = "application/json";
		headers.put("Accept", "application/json");
		headers.put("device-id", CommonUtil.getDeviceID(mContext));
		headers.put("device-type", Configs.HEADER_DEVICE_TYPE);
		headers.put("app-version", CommonUtil.getVersion(mContext));
		setHeaders(headers);
		
		return getDataFromNet(HttpRequestType.POST, Configs.REQUEST_CODE_QQAUTH_LOGIN, -1,Configs.URL_THIRD_lOGIN, null, bytes, null, ContentType);
	}
	
	/**
	 * 
	 * <p>功能描述</p>刷新token
	 * @param loginName
	 * @param token
	 * @return
	 * @author wangzhichao
	 * @date 2015年11月11日
	 */
	
	public MyHttpHandler refershToken(String loginName, String token) {
		byte[] bytes = null;
		JSONObject jObj = new JSONObject();
		try {
			jObj.put("loginName", loginName);
			jObj.put("token", token);
			bytes = jObj.toString().getBytes();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		HashMap<String, String> headers = new HashMap<String, String>();
		String ContentType = "application/json";
		headers.put("Accept", "application/json");
		headers.put("device-id", CommonUtil.getDeviceID(mContext));
		headers.put("device-type", Configs.HEADER_DEVICE_TYPE);
		headers.put("app-version", CommonUtil.getVersion(mContext));
		setHeaders(headers);
		if(Configs.isDebug) Log.e(TAG,"refershToken:"+jObj.toString());
		return getDataFromNet(HttpRequestType.POST, Configs.REQUEST_CODE_REDERSH_TOKEN, -1,Configs.URL_REFERSH_TOKEN, null, bytes, null, ContentType);
	}

	/**
	 * 
	 * <p>功能描述</p>微信第三方绑定
	 * @param token
	 * @param product
	 * @return
	 * @author wangzhichao
	 * @date 2015年11月23日
	 */
	
	public MyHttpHandler bindWXThrid(String openId,String token, String type,String product,String accessToken,String refreshToken,String unionid) {
		byte[] bytes = null;
		JSONObject jObj = new JSONObject();
		try {
			jObj.put("openId", openId);
			jObj.put("token", token);
			jObj.put("type", type);
			jObj.put("product", product);
			jObj.put("accessToken", accessToken);
			jObj.put("refreshToken", refreshToken);
			jObj.put("unionid", unionid);
			bytes = jObj.toString().getBytes();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		HashMap<String, String> headers = new HashMap<String, String>();
		String ContentType = "application/json";
		headers.put("Accept", "application/json");
		headers.put("device-id", CommonUtil.getDeviceID(mContext));
		headers.put("device-type", Configs.HEADER_DEVICE_TYPE);
		headers.put("app-version", CommonUtil.getVersion(mContext));
		setHeaders(headers);
		
		return getDataFromNet(HttpRequestType.POST, Configs.REQUEST_CODE_BIND_THIRD, -1,Configs.URL_BIND_THIRD, null, bytes, null, ContentType);
	}
	
	/**
	 * 
	 * <p>功能描述</p>QQ第三方绑定
	 * @param openId
	 * @param token
	 * @param type
	 * @param product
	 * @param accessToken
	 * @return
	 * @author wangzhichao
	 * @date 2015年11月26日
	 */
	
	public MyHttpHandler bindQQThrid(String openId,String token, String type,String product,String accessToken) {
		byte[] bytes = null;
		JSONObject jObj = new JSONObject();
		try {
			jObj.put("openId", openId);
			jObj.put("token", token);
			jObj.put("type", type);
			jObj.put("product", product);
			jObj.put("accessToken", accessToken);
			bytes = jObj.toString().getBytes();
		} catch (Exception e) {
			e.printStackTrace();
		}

		HashMap<String, String> headers = new HashMap<String, String>();
		String ContentType = "application/json";
		headers.put("Accept", "application/json");
		headers.put("device-id", CommonUtil.getDeviceID(mContext));
		headers.put("device-type", Configs.HEADER_DEVICE_TYPE);
		headers.put("app-version", CommonUtil.getVersion(mContext));
		setHeaders(headers);

		return getDataFromNet(HttpRequestType.POST, Configs.REQUEST_CODE_BIND_THIRD, -1,Configs.URL_BIND_THIRD, null, bytes, null, ContentType);
	}
	
	
	
	private class SearchResultParser extends ResultParser
	{
		@Override
		public ProviderResult parseResult(int requestCode, int flag, String json)
		{
			ProviderResult mPr = new ProviderResult();
			try
			{
    			if(json != null)
    			{
    				String responseStr = (String)json;
    				mPr = new ProviderResult();
    				mPr.setResponseStr(responseStr);
    				mPr.setResponseCode(Provider.RESPONSE_OK);
    				if(isDebug) Log.e(TAG, responseStr);
    				return mPr;
    			}
			}
			catch(Exception e)
			{
				if(Configs.isDebug) Log.e("message","exception:"+e.getMessage());
			}
			mPr.setResponseCode(Provider.RESPONSE_ERROR);
			return mPr;
		}
	}

}
