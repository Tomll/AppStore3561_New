package com.wedrive.welink.appstore.app.model;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

import com.lidroid.xutils.db.annotation.Id;
import com.lidroid.xutils.db.annotation.Table;

@Table(name = "AppBannger")
// 建议加上注解， 混淆后表名不受影响
public class AppBannger implements Serializable, Parcelable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3228030127995557449L;
	@Id(column = "bannger_name")
	public String bannger_name;
	public String bannger_type;
	public String click_url;
	public String order_position;
	public String image_path;
	
	public AppBannger() {
		super();
	}
	
	public AppBannger(Parcel source) {
		bannger_name = source.readString();
		bannger_type = source.readString();
		click_url = source.readString();
		order_position = source.readString();
		image_path = source.readString();
	}

	@Override
	public boolean equals(Object o) {
		AppBannger bannger=(AppBannger)o;
		if(this.bannger_name.equals(bannger.bannger_name))
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
		dest.writeString(bannger_name);
		dest.writeString(bannger_type);
		dest.writeString(click_url);
		dest.writeString(order_position);
		dest.writeString(image_path);	
	}
	
	public static final Parcelable.Creator<AppBannger> CREATOR = new Creator<AppBannger>() {

		@Override
		public AppBannger createFromParcel(Parcel source) {
			return new AppBannger(source);
		}

		@Override
		public AppBannger[] newArray(int size) {
			return new AppBannger[size];
		} 
	};

	public String getBannger_name() {
		return bannger_name;
	}

	public void setBannger_name(String bannger_name) {
		this.bannger_name = bannger_name;
	}

	public String getBannger_type() {
		return bannger_type;
	}

	public void setBannger_type(String bannger_type) {
		this.bannger_type = bannger_type;
	}

	public String getClick_url() {
		return click_url;
	}

	public void setClick_url(String click_url) {
		this.click_url = click_url;
	}

	public String getOrder_position() {
		return order_position;
	}

	public void setOrder_position(String order_position) {
		this.order_position = order_position;
	}

	public String getImage_path() {
		return image_path;
	}

	public void setImage_path(String image_path) {
		this.image_path = image_path;
	}

	
}
