package com.wedrive.welink.appstore.app.model;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

import com.lidroid.xutils.db.annotation.Id;
import com.lidroid.xutils.db.annotation.Table;

@Table(name = "SearchHistoryBean")
// 建议加上注解， 混淆后表名不受影响
public class APPSearchHistory implements Serializable,Parcelable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2274984421300487754L;
	@Id(column = "search_id")
	public String search_id;
	public String search_content;
	public long search_time;


	public APPSearchHistory() {
		super();
	}
	
	public APPSearchHistory(Parcel source) {
		search_id = source.readString();
		search_content = source.readString();
		search_time = source.readLong();
	}

	@Override
	public boolean equals(Object o) {
		APPSearchHistory version=(APPSearchHistory)o;
		if(this.search_id.equals(version.search_id))
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
		dest.writeString(search_id);
		dest.writeString(search_content);
		dest.writeLong(search_time);
	}
	
	public static final Parcelable.Creator<APPSearchHistory> CREATOR = new Creator<APPSearchHistory>() {

		@Override
		public APPSearchHistory createFromParcel(Parcel source) {
			return new APPSearchHistory(source);
		}

		@Override
		public APPSearchHistory[] newArray(int size) {
			return new APPSearchHistory[size];
		} 
	};

	public String getSearch_id() {
		return search_id;
	}

	public void setSearch_id(String search_id) {
		this.search_id = search_id;
	}

	public String getSearch_content() {
		return search_content;
	}

	public void setSearch_content(String search_content) {
		this.search_content = search_content;
	}

	public long getSearch_time() {
		return search_time;
	}

	public void setSearch_time(long search_time) {
		this.search_time = search_time;
	}

}
