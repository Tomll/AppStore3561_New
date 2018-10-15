package com.wedrive.welink.appstore.app.model;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

import com.lidroid.xutils.db.annotation.Id;
import com.lidroid.xutils.db.annotation.Table;

@Table(name = "AppDetails")
public class AppDetails implements Serializable,Parcelable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id(column = "app_id")
	
	public String app_id;
	public String app_name;
	public String app_developer;
	public int app_official_flag;
	public String app_update_time;
	
	public String app_comments_c;
	public String app_download_c;
	public float app_score_avg;
	public String app_md5;
	public String app_size;
	
	public String app_version_no;
	public String app_version_name;
	public String app_package_name;	
	public String app_apk_path;
	public String app_icon_path;
	
	public String app_image_path;
	public String app_language_type;
	public String app_description;
	public String app_brief_desc;
	public String app_update_desc;
	public String app_uri;

	public AppDetails() {
		super();
	}
	
	public AppDetails(Parcel source) {
		app_id = source.readString();
		app_name = source.readString();
		app_developer = source.readString();
		app_official_flag = source.readInt();
		app_update_time = source.readString();
		
		app_comments_c = source.readString();
		app_download_c = source.readString();
		app_score_avg = source.readFloat();
		app_md5 = source.readString();	
		app_size = source.readString();
		
		app_version_no = source.readString();
		app_version_name = source.readString();
		app_package_name = source.readString();
		app_apk_path = source.readString();
		app_icon_path = source.readString();
		
		app_image_path = source.readString();
		app_language_type = source.readString();
		app_description = source.readString();
		app_brief_desc = source.readString();
		app_update_desc = source.readString();
		app_uri = source.readString();
	}

	@Override
	public boolean equals(Object o) {
		AppDetails version=(AppDetails)o;
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
		dest.writeString(app_developer);
		dest.writeInt(app_official_flag);
		dest.writeString(app_update_time);
		
		dest.writeString(app_comments_c);
		dest.writeString(app_download_c);	
		dest.writeFloat(app_score_avg);
		dest.writeString(app_md5);
		dest.writeString(app_size);

		dest.writeString(app_version_no);
		dest.writeString(app_version_name);
		dest.writeString(app_package_name);
		dest.writeString(app_apk_path);
		dest.writeString(app_icon_path);
		
		dest.writeString(app_image_path);
		dest.writeString(app_language_type);	
		dest.writeString(app_description);
		dest.writeString(app_brief_desc);
		dest.writeString(app_update_desc);
		
		dest.writeString(app_uri);
		
	}
	public static final Parcelable.Creator<AppDetails> CREATOR = new Creator<AppDetails>() {

		@Override
		public AppDetails createFromParcel(Parcel source) {
			return new AppDetails(source);
		}

		@Override
		public AppDetails[] newArray(int size) {
			return new AppDetails[size];
		} 
	};
	
	public String getApp_id() {
		return app_id;
	}

	public String getApp_package_name() {
		return app_package_name;
	}

	public void setApp_package_name(String app_package_name) {
		this.app_package_name = app_package_name;
	}

	public String getApp_comments_c() {
		return app_comments_c;
	}

	public void setApp_comments_c(String app_comments_c) {
		this.app_comments_c = app_comments_c;
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

	public String getApp_developer() {
		return app_developer;
	}

	public void setApp_developer(String app_developer) {
		this.app_developer = app_developer;
	}

	public String getApp_update_time() {
		return app_update_time;
	}

	public void setApp_update_time(String app_update_time) {
		this.app_update_time = app_update_time;
	}

	public String getApp_download_c() {
		return app_download_c;
	}

	public void setApp_download_c(String app_download_c) {
		this.app_download_c = app_download_c;
	}

	public String getApp_md5() {
		return app_md5;
	}

	public void setApp_md5(String app_md5) {
		this.app_md5 = app_md5;
	}

	public String getApp_size() {
		return app_size;
	}

	public void setApp_size(String app_size) {
		this.app_size = app_size;
	}

	public String getApp_version_no() {
		return app_version_no;
	}

	public void setApp_version_no(String app_version_no) {
		this.app_version_no = app_version_no;
	}

	public String getApp_version_name() {
		return app_version_name;
	}

	public void setApp_version_name(String app_version_name) {
		this.app_version_name = app_version_name;
	}

	public String getApp_apk_path() {
		return app_apk_path;
	}

	public void setApp_apk_path(String app_apk_path) {
		this.app_apk_path = app_apk_path;
	}

	public String getApp_icon_path() {
		return app_icon_path;
	}

	public void setApp_icon_path(String app_icon_path) {
		this.app_icon_path = app_icon_path;
	}

	public String getApp_image_path() {
		return app_image_path;
	}

	public void setApp_image_path(String app_image_path) {
		this.app_image_path = app_image_path;
	}

	public String getApp_language_type() {
		return app_language_type;
	}

	public void setApp_language_type(String app_language_type) {
		this.app_language_type = app_language_type;
	}

	public String getApp_description() {
		return app_description;
	}

	public void setApp_description(String app_description) {
		this.app_description = app_description;
	}

	public String getApp_brief_desc() {
		return app_brief_desc;
	}

	public void setApp_brief_desc(String app_brief_desc) {
		this.app_brief_desc = app_brief_desc;
	}

	public String getApp_update_desc() {
		return app_update_desc;
	}

	public void setApp_update_desc(String app_update_desc) {
		this.app_update_desc = app_update_desc;
	}

	public String getApp_uri() {
		return app_uri;
	}

	public void setApp_uri(String app_uri) {
		this.app_uri = app_uri;
	}

	public int getApp_official_flag() {
		return app_official_flag;
	}

	public void setApp_official_flag(int app_official_flag) {
		this.app_official_flag = app_official_flag;
	}

	public float getApp_score_avg() {
		return app_score_avg;
	}

	public void setApp_score_avg(float app_score_avg) {
		this.app_score_avg = app_score_avg;
	}
	
	

}
