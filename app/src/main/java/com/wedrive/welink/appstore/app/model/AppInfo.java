package com.wedrive.welink.appstore.app.model;

/**
 * Created by wangzhichao on 2015年10月21日.
 * Copyright (c) 2015 北京图为先科技有限公司. All rights reserved.
 */

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

import com.lidroid.xutils.db.annotation.Id;
import com.lidroid.xutils.db.annotation.Table;
@Table(name = "AppInfo")
public class AppInfo  implements Serializable ,Parcelable{

	/** 
	 */
	private static final long serialVersionUID = -1211253440556651129L;

	@Id(column = "appId")
	private String appId = "";
	private String appName = "";
	private String packageName = "";
	private String versionName = "";
	private String savePath = "";
	private long appSize;
	private String app_official_flag = "";
	private int versionCode = 0;

	public AppInfo() {
		super();
	}

	public AppInfo(String appId, String packageName,String savePath) {
		super();
		this.appId = appId;
		this.packageName = packageName;
		this.savePath = savePath;
	}
	
	public AppInfo(Parcel source) {
		appId = source.readString();
		appName = source.readString();
		packageName = source.readString();
		versionName = source.readString();
		savePath = source.readString();
		appSize = source.readLong();
		app_official_flag = source.readString();
		versionCode = source.readInt();
	}

	@Override
	public boolean equals(Object o) {
		AppInfo version=(AppInfo)o;
		if(this.appId.equals(version.appId))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(appId);
		dest.writeString(appName);
		dest.writeString(packageName);
		dest.writeString(versionName);
		dest.writeString(savePath);
		dest.writeLong(appSize);
		dest.writeString(app_official_flag);	
		dest.writeInt(versionCode);
	}
	
	public static final Parcelable.Creator<AppInfo> CREATOR = new Creator<AppInfo>() {

		@Override
		public AppInfo createFromParcel(Parcel source) {
			return new AppInfo(source);
		}

		@Override
		public AppInfo[] newArray(int size) {
			return new AppInfo[size];
		} 
	};

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public String getSavePath() {
		return savePath;
	}

	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}

	public int getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}

	public long getAppSize() {
		return appSize;
	}

	public void setAppSize(long appSize) {
		this.appSize = appSize;
	}

	public String getApp_official_flag() {
		return app_official_flag;
	}

	public void setApp_official_flag(String app_official_flag) {
		this.app_official_flag = app_official_flag;
	}


}
