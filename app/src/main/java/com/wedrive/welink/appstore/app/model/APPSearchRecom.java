package com.wedrive.welink.appstore.app.model;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

import com.lidroid.xutils.db.annotation.Id;
import com.lidroid.xutils.db.annotation.Table;

@Table(name = "APPSearchRecommend")
// 建议加上注解， 混淆后表名不受影响
public class APPSearchRecom implements Serializable, Parcelable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9182839984969319086L;


	@Id(column = "hot_apps_app_id")
	public String hot_apps_app_id;
	public String hot_apps_app_name;
	public String hot_apps_app_v_id;
	public String hot_apps_official_flag;
	public String s_key;
	public String time;

	public APPSearchRecom() {
		super();
	}
	
	public APPSearchRecom(Parcel source) {
		hot_apps_app_id = source.readString();
		hot_apps_app_name = source.readString();
		hot_apps_app_v_id = source.readString();
		hot_apps_official_flag = source.readString();
		s_key = source.readString();
		time = source.readString();
	}

	@Override
	public boolean equals(Object o) {
		APPSearchRecom version=(APPSearchRecom)o;
		if(this.hot_apps_app_id.equals(version.hot_apps_app_id))
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
		dest.writeString(hot_apps_app_id);
		dest.writeString(hot_apps_app_name);
		dest.writeString(hot_apps_app_v_id);
		dest.writeString(hot_apps_official_flag);
		dest.writeString(s_key);
		dest.writeString(time);
	}
	public static final Parcelable.Creator<APPSearchRecom> CREATOR = new Creator<APPSearchRecom>() {

		@Override
		public APPSearchRecom createFromParcel(Parcel source) {
			return new APPSearchRecom(source);
		}

		@Override
		public APPSearchRecom[] newArray(int size) {
			return new APPSearchRecom[size];
		} 
	};
	

	public String getS_key() {
		return s_key;
	}

	public void setS_key(String s_key) {
		this.s_key = s_key;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getHot_apps_app_id() {
		return hot_apps_app_id;
	}

	public void setHot_apps_app_id(String hot_apps_app_id) {
		this.hot_apps_app_id = hot_apps_app_id;
	}

	public String getHot_apps_app_name() {
		return hot_apps_app_name;
	}

	public void setHot_apps_app_name(String hot_apps_app_name) {
		this.hot_apps_app_name = hot_apps_app_name;
	}

	public String getHot_apps_app_v_id() {
		return hot_apps_app_v_id;
	}

	public void setHot_apps_app_v_id(String hot_apps_app_v_id) {
		this.hot_apps_app_v_id = hot_apps_app_v_id;
	}

	public String getHot_apps_official_flag() {
		return hot_apps_official_flag;
	}

	public void setHot_apps_official_flag(String hot_apps_official_flag) {
		this.hot_apps_official_flag = hot_apps_official_flag;
	}

}
