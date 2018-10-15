package com.wedrive.welink.appstore.app.receive;

import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mapbar.android.model.CommandInfo;
import com.wedrive.welink.appstore.MainApplication;

public class MessageReceive extends BroadcastReceiver
{

	@Override
	public void onReceive(Context context, Intent intent) {
		String extra = intent.getStringExtra(com.mapbar.wedrive.Extra.COMMAND_DATA);
		System.out.println("广播数据：：："+extra);
		try
		{
			JSONObject obj;
			obj = new JSONObject(extra);
			if(obj.has("command"))
			{
				obj = obj.getJSONObject("command");
				if(obj.has("method"))
				{
					String method = obj.getString("method");
					if(obj.has("extData")) obj = obj.getJSONObject("extData");
					CommandInfo ci = new CommandInfo();
					ci.setExtData(obj);
					ci.setMethod(method);
					MainApplication.getInstance().onCommandReceive(context,ci);
				}
			}
		}
		catch(Exception e)
		{
			
		}

	}
	

}
