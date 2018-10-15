package com.wedrive.welink.appstore.app.model;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

import com.lidroid.xutils.db.annotation.Id;
import com.lidroid.xutils.db.annotation.Table;

@Table(name = "AppComment")
// 建议加上注解， 混淆后表名不受影响
public class AppComment implements Serializable, Parcelable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4924910992818067424L;

	public String app_id;
	public String app_version_id;
	@Id(column = "data_id")
	public String data_id;
	public String data_comment_con;
	public String data_comment_score;
	public String data_user_id;
	public String data_user_name;
	public String data_comment_time;
	
	public AppComment() {
		super();
	}
	
	public AppComment(Parcel source) {
		app_id = source.readString();
		app_version_id = source.readString();
		data_id = source.readString();
		data_comment_con = source.readString();
		data_comment_score = source.readString();
		data_user_id = source.readString();
		data_user_name = source.readString();
		data_comment_time = source.readString();
	}

	@Override
	public boolean equals(Object o) {
		AppComment version=(AppComment)o;
		if(this.data_id.equals(version.data_id))
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
		dest.writeString(app_version_id);
		dest.writeString(data_id);
		dest.writeString(data_comment_con);
		dest.writeString(data_comment_score);
		dest.writeString(data_user_id);
		dest.writeString(data_user_name);	
		dest.writeString(data_comment_time);
	}
	public static final Parcelable.Creator<AppComment> CREATOR = new Creator<AppComment>() {

		@Override
		public AppComment createFromParcel(Parcel source) {
			return new AppComment(source);
		}

		@Override
		public AppComment[] newArray(int size) {
			return new AppComment[size];
		} 
	};
	

	public String getApp_id() {
		return app_id;
	}

	public void setApp_id(String app_id) {
		this.app_id = app_id;
	}

	public String getApp_version_id() {
		return app_version_id;
	}

	public void setApp_version_id(String app_version_id) {
		this.app_version_id = app_version_id;
	}

	public String getData_id() {
		return data_id;
	}

	public void setData_id(String data_id) {
		this.data_id = data_id;
	}

	public String getData_comment_con() {
		return data_comment_con;
	}

	public void setData_comment_con(String data_comment_con) {
		this.data_comment_con = data_comment_con;
	}

	public String getData_comment_score() {
		return data_comment_score;
	}

	public void setData_comment_score(String data_comment_score) {
		this.data_comment_score = data_comment_score;
	}

	public String getData_user_id() {
		return data_user_id;
	}

	public void setData_user_id(String data_user_id) {
		this.data_user_id = data_user_id;
	}

	public String getData_user_name() {
		return data_user_name;
	}

	public void setData_user_name(String data_user_name) {
		this.data_user_name = data_user_name;
	}

	public String getData_comment_time() {
		return data_comment_time;
	}

	public void setData_comment_time(String data_comment_time) {
		this.data_comment_time = data_comment_time;
	}

}
