/**
 * Created by wangzhichao on 2015年11月10日.
 * Copyright (c) 2015 北京图为先科技有限公司. All rights reserved.
 */
package com.wedrive.welink.appstore.app.model;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginUserBean {

	private String userId;
	private String nickname;
	private String token;
	
	
	
	public String getUserId() {
		return userId;
	}



	public void setUserId(String userId) {
		this.userId = userId;
	}



	public String getNickname() {
		return nickname;
	}



	public void setNickname(String nickname) {
		this.nickname = nickname;
	}



	public String getToken() {
		return token;
	}



	public void setToken(String token) {
		this.token = token;
	}



	public void parse(JSONObject obj) throws JSONException{
		if (obj.has("userId")) {
			this.userId=obj.getString("userId");
		}
		if (obj.has("nickname")) {
			this.nickname=obj.getString("nickname");
		}
		if (obj.has("token")) {
			this.token=obj.getString("token");
		}
	}
}
