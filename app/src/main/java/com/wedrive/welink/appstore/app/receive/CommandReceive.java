package com.wedrive.welink.appstore.app.receive;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import com.mapbar.android.model.CommandInfo;
import com.wedrive.welink.appstore.MainApplication;

public class CommandReceive extends BroadcastReceiver{
	
	private final static String TAG = "CommandReceive";
	
	@Override
	public void onReceive(Context context, Intent intent){
		String action = intent.getAction();
		if(action.equals(ConnectivityManager.CONNECTIVITY_ACTION)){	//接收网络连接状态的广播
			String method="";
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = (NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);
			NetworkInfo activeNetInfo = mConnectivityManager.getActiveNetworkInfo();			
			if(activeNetInfo != null){
				if(activeNetInfo.getType() != netInfo.getType()){
					return;
				}
			}

			if(activeNetInfo != null && activeNetInfo.isConnected()) { //网络连接 
				if(activeNetInfo.getType()==ConnectivityManager.TYPE_WIFI){  //WiFi网络 
	                method="WiFiNetReceive";
	            }else if(activeNetInfo.getType()==ConnectivityManager.TYPE_MOBILE){ //移动网络  
	                method="MobileNetReceive";
	            }
            }else { //网络断开  
            	method="NoNetReceive";
            }
            if(!TextUtils.isEmpty(method)){
    			CommandInfo ci = new CommandInfo();
    			ci.setMethod(method);
    			MainApplication.getInstance().onCommandReceive(context, ci);
    		}
        } 
	}
}
