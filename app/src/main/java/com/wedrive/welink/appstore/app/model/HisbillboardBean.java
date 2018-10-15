package com.wedrive.welink.appstore.app.model;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

import com.lidroid.xutils.db.annotation.Id;
import com.lidroid.xutils.db.annotation.Table;

/**
 * @author jiaoyb 类描述：经典榜 javaBean
 */
@Table(name = "HisbillboardBean")
// 建议加上注解， 混淆后表名不受影响
public class HisbillboardBean implements Serializable, Parcelable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8606733806765183607L;
	@Id(column = "app_id")
	public String app_id;
	public String name;
	public String app_v_id;
	public int official_flag;
	public String description;
	public String size;
	public float score_avg;
	public String package_name;
	public String version_no;
	public String icon_path;
	public String apk_path;
	public String app_uri;
	public String app_md5;

	public HisbillboardBean() {
		super();
	}
	
	public HisbillboardBean(Parcel source) {
		app_id = source.readString();
		name = source.readString();
		app_v_id = source.readString();
		official_flag = source.readInt();
		description = source.readString();
		size = source.readString();
		score_avg = source.readFloat();
		package_name = source.readString();
		version_no = source.readString();
		icon_path = source.readString();
		apk_path = source.readString();
		app_uri = source.readString();
		app_md5 = source.readString();
	}

	@Override
	public boolean equals(Object o) {
		HisbillboardBean version=(HisbillboardBean)o;
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
		dest.writeString(name);
		dest.writeString(app_v_id);
		dest.writeInt(official_flag);
		dest.writeString(description);
		dest.writeString(size);
		dest.writeFloat(score_avg);	
		dest.writeString(package_name);
		dest.writeString(version_no);
		dest.writeString(icon_path);
		dest.writeString(apk_path);
		dest.writeString(app_uri);
		dest.writeString(app_md5);
	}
	public static final Parcelable.Creator<HisbillboardBean> CREATOR = new Creator<HisbillboardBean>() {

		@Override
		public HisbillboardBean createFromParcel(Parcel source) {
			return new HisbillboardBean(source);
		}

		@Override
		public HisbillboardBean[] newArray(int size) {
			return new HisbillboardBean[size];
		} 
	};
	

	public String getVersion_no() {
		return version_no;
	}

	public void setVersion_no(String version_no) {
		this.version_no = version_no;
	}

	public String getPackage_name() {
		return package_name;
	}

	public void setPackage_name(String package_name) {
		this.package_name = package_name;
	}


	public float getScore_avg() {
		return score_avg;
	}

	public void setScore_avg(float score_avg) {
		this.score_avg = score_avg;
	}

	public String getApp_id() {
		return app_id;
	}

	public void setApp_id(String app_id) {
		this.app_id = app_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getApp_v_id() {
		return app_v_id;
	}

	public void setApp_v_id(String app_v_id) {
		this.app_v_id = app_v_id;
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

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getIcon_path() {
		return icon_path;
	}

	public void setIcon_path(String icon_path) {
		this.icon_path = icon_path;
	}

	public String getApk_path() {
		return apk_path;
	}

	public void setApk_path(String apk_path) {
		this.apk_path = apk_path;
	}

	public String getApp_uri() {
		return app_uri;
	}

	public void setApp_uri(String app_uri) {
		this.app_uri = app_uri;
	}

	public String getApp_md5() {
		return app_md5;
	}

	public void setApp_md5(String app_md5) {
		this.app_md5 = app_md5;
	}
}
