package com.wedrive.welink.appstore.app.model;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

import com.lidroid.xutils.db.annotation.Id;
import com.lidroid.xutils.db.annotation.Table;

@Table(name = "AppInstall")
// 建议加上注解， 混淆后表名不受影响
public class AppInstall implements Serializable,Parcelable {

	/**
	 * 
	 */

	private static final long serialVersionUID = -3228030127995557449L;
	@Id(column = "app_id")
	public String app_id;
	public String app_name;
	public String app_size;
	public String app_version_id;
	public String app_version_no;
	public int official_flag;
	public String description;
	public String apk_path;
	public String icon_path;
	public String app_package_name;
	public String app_md5;
	
	public AppInstall() {
		super();
	}
	
	public AppInstall(Parcel source) {
		app_id = source.readString();
		app_name = source.readString();
		app_size = source.readString();
		app_version_id = source.readString();
		app_version_no = source.readString();
		official_flag = source.readInt();
		description = source.readString();
		apk_path = source.readString();
		icon_path = source.readString();
		app_package_name = source.readString();
		app_md5 = source.readString();
	}

	@Override
	public boolean equals(Object o) {
		AppInstall version=(AppInstall)o;
		if(this.app_id.equals(version.app_id))
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
		dest.writeString(app_size);
		dest.writeString(app_version_id);
		dest.writeString(app_version_no);
		dest.writeInt(official_flag);
		dest.writeString(description);	
		dest.writeString(apk_path);
		dest.writeString(icon_path);
		dest.writeString(app_package_name);
		dest.writeString(app_md5);
	}
	public static final Parcelable.Creator<AppInstall> CREATOR = new Creator<AppInstall>() {

		@Override
		public AppInstall createFromParcel(Parcel source) {
			return new AppInstall(source);
		}

		@Override
		public AppInstall[] newArray(int size) {
			return new AppInstall[size];
		} 
	};

	
	public String getApp_package_name() {
		return app_package_name;
	}

	public void setApp_package_name(String app_package_name) {
		this.app_package_name = app_package_name;
	}

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

	public String getApp_version_no() {
		return app_version_no;
	}

	public void setApp_version_no(String app_version_no) {
		this.app_version_no = app_version_no;
	}

	public String getApp_md5() {
		return app_md5;
	}

	public void setApp_md5(String app_md5) {
		this.app_md5 = app_md5;
	}

	public int getOfficial_flag() {
		return official_flag;
	}

	public void setOfficial_flag(int official_flag) {
		this.official_flag = official_flag;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getApk_path() {
		return apk_path;
	}

	public void setApk_path(String apk_path) {
		this.apk_path = apk_path;
	}

	public String getIcon_path() {
		return icon_path;
	}

	public void setIcon_path(String icon_path) {
		this.icon_path = icon_path;
	}

	public String getApp_size() {
		return app_size;
	}

	public void setApp_size(String app_size) {
		this.app_size = app_size;
	}


}
