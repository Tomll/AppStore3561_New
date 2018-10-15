package com.wedrive.welink.appstore.app.model;

public class AppPreferce {

	public boolean netWorkAllow = false;
	public boolean clearLoadAPK = false;
	// 页面数据加载时间
	public long recomBanngerLoadTime; // 推荐bannger
	public long recomPageLoadTime; // 推荐页面
	public long searchPageLoadTime;// 搜索页面（热门应用列表）
	public long listPageLoadTime;// 榜单页面

	public boolean isNetWorkAllow() {
		return netWorkAllow;
	}

	public void setNetWorkAllow(boolean netWorkAllow) {
		this.netWorkAllow = netWorkAllow;
	}

	public boolean isClearLoadAPK() {
		return clearLoadAPK;
	}

	public void setClearLoadAPK(boolean clearLoadAPK) {
		this.clearLoadAPK = clearLoadAPK;
	}

	public long getRecomBanngerLoadTime() {
		return recomBanngerLoadTime;
	}

	public void setRecomBanngerLoadTime(long recomBanngerLoadTime) {
		this.recomBanngerLoadTime = recomBanngerLoadTime;
	}

	public long getRecomPageLoadTime() {
		return recomPageLoadTime;
	}

	public void setRecomPageLoadTime(long recomPageLoadTime) {
		this.recomPageLoadTime = recomPageLoadTime;
	}

	public long getSearchPageLoadTime() {
		return searchPageLoadTime;
	}

	public void setSearchPageLoadTime(long searchPageLoadTime) {
		this.searchPageLoadTime = searchPageLoadTime;
	}

	public long getListPageLoadTime() {
		return listPageLoadTime;
	}

	public void setListPageLoadTime(long listPageLoadTime) {
		this.listPageLoadTime = listPageLoadTime;
	}

}
