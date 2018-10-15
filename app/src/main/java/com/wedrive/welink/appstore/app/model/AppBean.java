package com.wedrive.welink.appstore.app.model;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

import com.lidroid.xutils.db.annotation.Id;
import com.lidroid.xutils.db.annotation.Table;

@Table(name = "AppBean")  // 建议加上注解， 混淆后表名不受影响
public class AppBean implements Serializable,Parcelable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3228030127995557449L;
	@Id(column = "app_id")
	public String app_id;	
	public String app_name;
	public String app_version_id;
	public String description;
	public int official_flag;
	public String icon_path;
	
	
	public AppBean() {
		super();
	}
	
	public AppBean(Parcel source) {
		app_id = source.readString();
		app_name = source.readString();
		app_version_id = source.readString();
		description = source.readString();
		official_flag = source.readInt();
		icon_path = source.readString();
	}

	@Override
	public boolean equals(Object o) {
		AppBean app=(AppBean)o;
		if(this.app_id.equals(app.app_id))
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
		dest.writeString(app_id);
		dest.writeString(app_name);
		dest.writeString(app_version_id);
		dest.writeString(description);
		dest.writeInt(official_flag);
		dest.writeString(icon_path);
	}
	public static final Parcelable.Creator<AppBean> CREATOR = new Creator<AppBean>() {

		@Override
		public AppBean createFromParcel(Parcel source) {
			return new AppBean(source);
		}

		@Override
		public AppBean[] newArray(int size) {
			return new AppBean[size];
		} 
	};
	
	
	public String getApp_id() {
		return app_id;
	}
	public void setApp_id(String app_id) {
		this.app_id = app_id;
	}
	public String getApp_name() {
		return app_name;
	}
	public void setApp_name(String app_name) {
		this.app_name = app_name;
	}
	public String getApp_version_id() {
		return app_version_id;
	}
	public void setApp_version_id(String app_version_id) {
		this.app_version_id = app_version_id;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public int getOfficial_flag() {
		return official_flag;
	}

	public void setOfficial_flag(int official_flag) {
		this.official_flag = official_flag;
	}

	public String getIcon_path() {
		return icon_path;
	}
	public void setIcon_path(String icon_path) {
		this.icon_path = icon_path;
	}
	
}
