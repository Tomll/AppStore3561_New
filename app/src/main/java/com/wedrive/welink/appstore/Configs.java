package com.wedrive.welink.appstore;


public class Configs
{
	public final static boolean isDebug = false;

	public final static int DB_VERSION= 2;//数据库版本

	public final static int VIEW_POSITION_NONE = -1;
	public final static int VIEW_POSITION_HOME = 1;
	public final static int VIEW_POSITION_DETAIL = 2;
	public final static int VIEW_POSITION_MANNGER_LOAD = 3;
	public final static int VIEW_POSITION_MANNGER_UPDATE = 4;
	public final static int VIEW_POSITION_MANNGER_UNINSTAL = 5;
	public final static int VIEW_POSITION_MANNGER_SETING = 6;
	public final static int VIEW_POSITION_MANNGER_HELP = 7;
	public final static int VIEW_POSITION_MANNGER_CONTACT = 8;
	public final static int VIEW_POSITION_MANNGER_LOGIN = 9;
	public final static int VIEW_POSITION_MANNGER_CAMERA = 10;
	public final static int VIEW_POSITION_MANNGER_CROP = 11;

	//应用商店外网地址
	public final static String URL_LOAD_APPS_LIST="http://wdservice.mapbar.com/appstorewsapi/applist";
	public final static String URL_LOAD_APP_DETAIL="http://wdservice.mapbar.com/appstorewsapi/appdetail";
	public final static String URL_LOAD_APP_COMMONS="http://wdservice.mapbar.com/appstorewsapi/getcomments";
	public final static String URL_LOAD_APP_USER_COMMONS="http://wdservice.mapbar.com/appstorewsapi/getcomment";
//	public final static String URL_UPLOAD_APP_COMMON="http://wdservice.mapbar.com/appstorewsapi/docomment";
	public final static String URL_UPLOAD_APP_COMMON="http://wdservice.mapbar.com/appstorewsapi/doCommentByWecloud";//兼容3561个人中心账号的评论接口
	public final static String URL_APPS_RECOM_LIST="http://wdservice.mapbar.com/appstorewsapi/searchinit";
	public final static String URL_SEARCH_APPS_LIST="http://wdservice.mapbar.com/appstorewsapi/search";
	public final static String URL_CHECK_VERSION_APPS="http://wdservice.mapbar.com/appstorewsapi/checkversions";
	public final static String URL_BILLBOARD_APPS_LIST="http://wdservice.mapbar.com/appstorewsapi/billboard";
	public final static String URL_RECORD_LOG_LIST="http://wdservice.mapbar.com/appstorewsapi/recorddown";
	public final static String URL_APPSTOR_CHECK_VERSION="http://wdservice.mapbar.com/appstorewsapi/checksys";
	public final static String URL_RECORD_BANNGER_LIST="http://wdservice.mapbar.com/appstorewsapi/bannerlist";
	public final static String URL_RECORD_INSTAL_LIST="http://wdservice.mapbar.com/appstorewsapi/checkexistlist";
	//账号登录外网地址
	public final static String URL_THIRD_lOGIN = "https://wdservice.mapbar.com/ssoapi/user/thirdLogin";
	public final static String URL_REFERSH_TOKEN = "https://wdservice.mapbar.com/ssoapi/user/refreshToken";
	public final static String URL_USER_ICON = "https://wdservice.mapbar.com/ssoapi/user/queryPic";
	public final static String URL_USER_ICON_ID = "https://wdservice.mapbar.com/ssoapi/user/queryPicById";
	public final static String URL_BIND_THIRD = "https://wdservice.mapbar.com/ssoapi/user/bindThridPlatform";
	public final static String URL_WELINKE_HTML_LOGIN = "http://wdcdn.mapbar.com/embed/sso/";

//	public final static String URL_LOAD_APPS_LIST="http://wdservice.mapbar.com/appstorewsapiv3test/applist";
//	public final static String URL_LOAD_APP_DETAIL="http://wdservice.mapbar.com/appstorewsapiv3test/appdetail";
//	public final static String URL_LOAD_APP_COMMONS="http://wdservice.mapbar.com/appstorewsapiv3test/getcomments";
//	public final static String URL_LOAD_APP_USER_COMMONS="http://wdservice.mapbar.com/appstorewsapiv3test/getcomment";
//	public final static String URL_UPLOAD_APP_COMMON="http://wdservice.mapbar.com/appstorewsapiv3test/docomment";
//	public final static String URL_APPS_RECOM_LIST="http://wdservice.mapbar.com/appstorewsapiv3test/searchinit";
//	public final static String URL_SEARCH_APPS_LIST="http://wdservice.mapbar.com/appstorewsapiv3test/search";
//	public final static String URL_CHECK_VERSION_APPS="http://wdservice.mapbar.com/appstorewsapiv3test/checkversions";
//	public final static String URL_BILLBOARD_APPS_LIST="http://wdservice.mapbar.com/appstorewsapiv3test/billboard";
//	public final static String URL_RECORD_LOG_LIST="http://wdservice.mapbar.com/appstorewsapiv3test/recorddown";
//	public final static String URL_APPSTOR_CHECK_VERSION="http://wdservice.mapbar.com/appstorewsapiv3test/checksys";
//	public final static String URL_RECORD_BANNGER_LIST="http://wdservice.mapbar.com/appstorewsapiv3test/bannerlist";
//	public final static String URL_RECORD_INSTAL_LIST="http://wdservice.mapbar.com/appstorewsapiv3test/checkexistlist";
	
//	public final static String URL_THIRD_lOGIN = "http://192.168.85.49/user/thirdLogin";
//	public final static String URL_REFERSH_TOKEN = "http://192.168.85.49/user/refreshToken";
//	public final static String URL_USER_ICON = "http://192.168.85.49/user/queryPic";
//	public final static String URL_USER_ICON_ID = "http://192.168.85.49/user/queryPicById";
//	public final static String URL_BIND_THIRD = "http://192.168.85.49/user/bindThridPlatform";
//	public final static String URL_WELINKE_HTML_LOGIN = "http://192.168.85.33/embed/sso/";

	public final static int REQUEST_CODE_LOGIN = 11;
	public final static int REQUEST_CODE_REGISTER = 12;
	public final static int REQUEST_CODE_LOAD_APPS_LIST = 13;
	public final static int REQUEST_CODE_LOAD_APP_DETAIL = 14;
	public final static int REQUEST_CODE_LOAD_APP_COMMONS = 15;
	public final static int REQUEST_CODE_LOAD_APP_USER_COMMONS = 16;
	public final static int REQUEST_CODE_UPDATE_APP_USER_COMMONS = 17;
	public final static int REQUEST_CODE_UPLOAD_APP_COMMON = 18;	
	public final static int REQUEST_CODE_APPS_RECOM_LIST = 19;
	public final static int REQUEST_CODE_SEARCH_APPS_LIST = 20;
	public final static int REQUEST_CODE_CHECK_VERSION_APPS = 21;		
	public final static int REQUEST_CODE_BILLBOARD_APPS_LIST = 22;
	public final static int REQUEST_CODE_RECORD_LOG_LIST = 23;
	public final static int REQUEST_CODE_APPSTOR_CHECK_VERSION = 24;
	public final static int REQUEST_CODE_RECORD_BANNGER_LIST = 25;
	public final static int REQUEST_CODE_INSTAL_APPS_LIST = 26;
	public static final int REQUEST_CODE_WXAUTH_LOGIN = 27; 
	public static final int REQUEST_CODE_QQAUTH_LOGIN = 28; 
	public static final int REQUEST_CODE_REDERSH_TOKEN = 29; 
	public static final int REQUEST_CODE_BIND_THIRD = 30; 
	
	public final static int VIEW_FLAG_NONE = -1;	
	public final static int DATA_TYPE_NONE = -1;
	
	public final static String DOWNLOAD_ROOT_DIR ="mapbar/wedriver/appstore";
	public final static String DOWNLOAD_IMAGE_DIR = "mapbar/wedriver/appstore/images";
	public final static String DOWNLOAD_APK_DIR = "mapbar/wedriver/appstore/apks";
	public final static String DOWNLOAD_DB_DIR = "mapbar/wedriver/appstore/db";
	public final static String DRIVE_ACCOUTN_PROPERTIES = "mapbar/wedriver/wedrive_account_config.properties";
	public final static String DB_NAME = "DB_AS.db";

	public static final String PACKAGENAME_NAVI = "com.mapbar.android.carnavi";
	public static final String SCHEME_NAVI = "wedrive.navigation:";
	
	
	public final static int APP_BUTTON_STATUS_DOWNLOAD = 1;
	public final static int APP_BUTTON_STATUS_INSTALL = 2;
	public final static int APP_BUTTON_STATUS_OPEN = 3;
	public final static int APP_BUTTON_STATUS_UPDATE = 4;
	public final static int APP_BUTTON_STATUS_RESET = 5;
	public final static int APP_BUTTON_STATUS_PAUSE = 6;
	public final static int APP_BUTTON_STATUS_PROGRESS = 7;
	public final static int APP_BUTTON_STATUS_START = 8;
	
	public final static long RECOM_DATA_EXPIRE_TIME=24*60*60*1000L;
	public final static long SEARCH_DATA_EXPIRE_TIME=24*60*60*1000L;
	public final static long LIST_DATA_EXPIRE_TIME=24*60*60*1000L;
	public final static long BANNGER_DATA_EXPIRE_TIME=12*60*60*1000L;
	
	public static final String HEADER_OS_VERSION = "1";//系统类型(1:手机  2车机  3平板)
	public static final String HEADER_DEVICE_TYPE = "android";//设备系统
	//public static final String HEADER_CORPORATE_KEY = "d82a9f81c85c4eb597b6fc8c1b86715a";//企业标识
	public static final String HEADER_CORPORATE_KEY = "ea6cc1a771254b5b89a8b71a4f12903b";//3561项目：企业标识

	public final static String APPSTORE_COMMAND_SEND="com.wedrive.action.APPSTORE_COMMAND_SEND";
	public final static String APPSTORE_COMMAND_RESULT="com.wedrive.action.APPSTORE_COMMAND_RESULT";
	
	public static final String THIRED_LOGIN_AppId = "1104874807";
	public static final String THIRED_LOGIN_AppKey = "p3rC3MO2NUqtZHjS";
	public static final String THIRED_LOGIN_WEIXIN_APP_ID = "wxd0ba3905a2b422e4";
	public static final String THIRED_LOGIN_AppSecret = "d4624c36b6795d1d99dcf0547af5443d";
	public static final String THIRED_LOGIN_WEIXIN_SCOPE = "snsapi_userinfo";
	public static final String THIRED_LOGIN_WEIXIN_STATE = "carjob_wx_login";

	public static String X_Auth_Token = null;
	public static String X_Auth_Nice_Name = null;
	public static boolean APPSTORE_IS_LOGIN=false;

	/*
	 联系我们
	 */

	public static String CONTACT_NET="http://wedrive.navinfo.com/welink";
	public static String CONTACT_QQ="301264644";
	public static String CONTACT_WX="趣驾WeLink";

	/*
	  回控按键
	*/

	public static final int KeyEvent_KeyCode_Confirm = 23;//确认按键
	public static final int KeyEvent_KeyCode_Up = 19;//向上按键
	public static final int KeyEvent_KeyCode_Down = 20;//向下按键
	public static final int KeyEvent_KeyCode_Left = 21;//向左按键
	public static final int KeyEvent_KeyCode_Right = 22;//向右按键


	/*
	  统计
	*/

	public static final String Event_ID_StartApp = "A_Event_StartApp";//启动app事件id编号
	public static final String Event_ID_OnClick = "A_Event_OnClick";//点击事件id编号

	public static final String Event_AppStore_StartApp = "AppStore_Start";//应用商店启动app事件key值
	public static final String Event_AppStore_OnClick = "AppStore_OnClick";//应用商店点击事件key值

	public static final String AppStore_Interface_HomePage = "AppStore_HomePage";//首页界面统计值
	public static final String AppStore_Interface_DetailPage = "AppStore_DetailPage";//应用详情界面统计值
	public static final String AppStore_Interface_DownloadCenterPage = "AppStore_DownloadCenterPage";//下载中心界面统计值
	public static final String AppStore_Interface_UpdateAppPage = "AppStore_UpdateAppPage";//更新应用界面统计值
	public static final String AppStore_Interface_UninstalAppPage = "AppStore_UninstalAppPage";//卸载应用界面统计值
	public static final String AppStore_Interface_SettingsPage = "AppStore_SettingsPage";//设置界面统计值
	public static final String AppStore_Interface_HelpPage = "AppStore_HelpPage";//帮助界面统计值
	public static final String AppStore_Interface_ContactUsPage = "AppStore_ContactUsPage";//联系我们界面统计值

	public static final String AppStore_OnClick_Recommend = "AppStore_HomePage_Recommend";//点击app应用进入应用详情界面统计值
	public static final String AppStore_OnClick_Details = "AppStore_HomePage_Details";//应用详情界面点击详情统计值
	public static final String AppStore_OnClick_Comments = "AppStore_HomePage_Comments";//应用详情界面点击评论统计值
	public static final String AppStore_OnClick_Search = "AppStore_HomePage_Search";//首页点击搜索统计值
	public static final String AppStore_OnClick_List = "AppStore_HomePage_List";//首页点击榜单统计值
	public static final String AppStore_OnClick_Management = "AppStore_HomePage_Management";//首页点击管理统计值
	public static final String AppStore_OnClick_Login= "AppStore_HomePage_Management_Login";//首页管理模块点击账号登录统计值
	public static final String AppStore_OnClick_DownloadCenter = "AppStore_HomePage_Management_DownloadCenter";//首页管理模块点击下载中心统计值
	public static final String AppStore_OnClick_UpdateApp = "AppStore_HomePage_Management_UpdateApp";//首页管理模块点击更新应用统计值
	public static final String AppStore_OnClick_UninstallApp = "AppStore_HomePage_Management_UninstallApp";//首页管理模块点击卸载应用统计值
	public static final String AppStore_OnClick_Settings = "AppStore_HomePage_Management_Settings";//首页管理模块点击设置统计值
	public static final String AppStore_OnClick_Help = "AppStore_HomePage_Management_Help";//首页管理模块点击帮助统计值
	public static final String AppStore_OnClick_ContactUs = "AppStore_HomePage_Management_ContactUs";//首页管理模块点击联系我们统计值

	public static final String AppStore_OnClick_Account = "AppStore_Login_Account";//首页界面点击账号登录进入账号登录界面统计值
	public static final String AppStore_OnClick_PhoneQuick = "AppStore_Login_PhoneQuick";//账号登录界面点击登录统计值
	public static final String AppStore_OnClick_Weixin = "AppStore_Login_Weixin";//账号登录界面点击微信登录统计值
	public static final String AppStore_OnClick_AccountSettings = "AppStore_Login_AccountSettings";//管理界面点击账号登录进入账号设置界面统计值
	public static final String AppStore_OnClick_BindPhone = "AppStore_Login_AccountSettings_BindPhone";//账号登录界面点击绑定手机统计值
	public static final String AppStore_OnClick_BindMail = "AppStore_Login_AccountSettings_BindMail";//账号登录界面点击绑定邮箱统计值
	public static final String AppStore_OnClick_BindThirdAccount = "AppStore_Login_AccountSettings_BindThirdAccount";//账号登录界面点击绑定第三方账号统计值
	public static final String AppStore_OnClick_MyProfile = "AppStore_Login_AccountSettings_MyProfile";//账号登录界面点击我的资料统计值
	public static final String AppStore_OnClick_MyCar = "AppStore_Login_AccountSettings_MyCar";//账号登录界面点击我的爱车统计值
	public static final String AppStore_OnClick_ChangePassword = "AppStore_Login_AccountSettings_ChangePassword";//账号登录界面点击修改密码统计值
	public static final String AppStore_OnClick_ExitAccount = "AppStore_Login_AccountSettings_ExitAccount";//账号登录界面点击退出账号统计值
	public static final String AppStore_OnClick_Register = "AppStore_Login_Register";//账号登录界面点击注册统计值
	public static final String AppStore_OnClick_VerifyPhone = "AppStore_Login_Register_VerifyPhone";//账号登录界面点击验证手机统计值
	public static final String AppStore_OnClick_PrivacyPolicy = "AppStore_Login_Register_PrivacyPolicy";//账号登录界面点击隐私协议统计值
	public static final String AppStore_OnClick_ForgetPassword = "AppStore_Login_ForgetPassword";//账号登录界面点击忘记密码统计值
}
