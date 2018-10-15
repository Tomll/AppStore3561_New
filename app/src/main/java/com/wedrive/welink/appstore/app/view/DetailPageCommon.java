package com.wedrive.welink.appstore.app.view;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.mapbar.android.model.ActivityInterface;
import com.mapbar.android.model.OnProviderListener;
import com.mapbar.android.model.ProviderResult;
import com.mapbar.android.net.MyHttpHandler;
import com.mapbar.scale.ScaleAdapter;
import com.wedrive.welink.appstore.Configs;
import com.wedrive.welink.appstore.MainApplication;
import com.wedrive.welink.appstore.R;
import com.wedrive.welink.appstore.app.model.AppBean;
import com.wedrive.welink.appstore.app.model.AppComment;
import com.wedrive.welink.appstore.app.model.AppDetails;
import com.wedrive.welink.appstore.app.provider.SearchProvider;
import com.wedrive.welink.appstore.app.util.AppUtil;
import com.wedrive.welink.appstore.app.util.CommonUtil;
import com.wedrive.welink.appstore.app.util.PropertiesUtil;
import com.wedrive.welink.appstore.app.util.RegExpUtil;
import com.wedrive.welink.appstore.app.widget.CircularImage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class DetailPageCommon implements OnClickListener {
	
	public static final String TAG = "DetailPageCommon";
	private ActivityInterface mAif;
	private Context mContext;	
	private EditText edittext;
	private RatingBar ratingbar;
	private TextView tv_grade, tv_grade_peoples, tv_load_common;
	private Dialog dialog;
	private BitmapUtils bitmapUtils;

	private AppComment comm;
	private AppDetails appDetails;
	private DetailPage detailPage;
	private String app_id;
	private CommonAdapter adapter;
	private List<AppComment> comms = new ArrayList<AppComment>();
	
	public Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch(msg.what){
			case 10:
				comm=(AppComment) msg.obj;
				initDialog(comm);
				break;
			}
		}
	};

	public DetailPageCommon(View view, ActivityInterface aif, Context context) {
		mAif = aif;
		mContext = context;
		bitmapUtils = new BitmapUtils(context);
		bitmapUtils.configDefaultBitmapMaxSize(100, 100);
		bitmapUtils.configDefaultLoadFailedImage(R.drawable.ico_people);
		bitmapUtils.clearCache();
		bitmapUtils.clearMemoryCache();
		bitmapUtils.clearDiskCache();
		initView(view);
	}

	private void initView(View view) {
		tv_grade = (TextView) view.findViewById(R.id.tv_grade);
		tv_grade_peoples = (TextView) view.findViewById(R.id.tv_grade_peoples);
		tv_load_common = (TextView) view.findViewById(R.id.tv_load_common);
		lVi_app_commons = (ListView) view.findViewById(R.id.lVi_app_commons);
		tv_load_common.setOnClickListener(this);
		initListViewFooter();
		adapter = new CommonAdapter();
		lVi_app_commons.setAdapter(adapter);
	}

	private boolean hasMoreData = false;
	private int pageIndex = 1;
	private boolean isFrist = true;// 是否是加载的第一页评论
	private TextView tvHintMore;
	private ProgressBar progressBar;

	/**
	 * 给listview添加脚
	 */
	private void initListViewFooter() {
		View view = View.inflate(mContext, R.layout.xlistview_footer, null);
		tvHintMore = (TextView) view.findViewById(R.id.xlistview_footer_hint_textview);
		progressBar = (ProgressBar) view.findViewById(R.id.xlistview_footer_progressbar);
		setListViewFooterHintText(NO_DATA);
		view.findViewById(R.id.xlistview_footer_content).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (hasMoreData) {
					setListViewProgressVisibile(true);// 有更多数据 显示progress
					getAPPCommons(appDetails, pageIndex, 10);
				}
			}
		});
		lVi_app_commons.addFooterView(view);
	}

	public static int LOAD_FAIL = 0;
	public static int HAS_MORE = 1;
	public static int NO_HAS_MORE = 2;
	public static int NET_UNUSABLE = 4;
	public static int NO_DATA = 5;

	/**
	 * 
	 * <p>功能描述</p>显示评论底部信息
	 * @param status
	 * @author wangzhichao
	 * @date 2015年12月25日
	 */
	
	private void setListViewFooterHintText(int status) {
		if (status == LOAD_FAIL) {
			tvHintMore.setText("加载失败，点击重试!");
		}
		if (status == HAS_MORE) {
			tvHintMore.setText("查看更多");
		}
		if (status == NO_HAS_MORE) {
			tvHintMore.setText("全部加载完毕");
		}
		if (status == NET_UNUSABLE) {
			tvHintMore.setText("网络不可用，请检查网络后重试");
		}
		if (status == NO_DATA) {
			tvHintMore.setText("暂无评论");
		}
	}

	/**
	 * 
	 * <p>功能描述</p>控制显示评论信息
	 * @param visible
	 * @author wangzhichao
	 * @date 2015年12月25日
	 */
	
	private void setListViewProgressVisibile(boolean visible) {
		progressBar.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
		tvHintMore.setVisibility(visible ? View.INVISIBLE : View.VISIBLE);
	}

	/**
	 * 此方法由DetailPage类查询app详情成功后调用 （用来设置分数和多少人评分，并获取第一页评论数据）
	 * 
	 * @param bean
	 * @param detailPage
	 */
	public void setAppDetailDetail(AppDetails bean, DetailPage detailPage, String app_id) {
		this.app_id = app_id;
		this.detailPage = detailPage;
		pageIndex = 1;
		isFrist = true;
		comms.clear();
		appDetails = bean;
		tv_grade.setText(""+bean.getApp_score_avg());
		tv_grade_peoples.setText(bean.getApp_comments_c() + "人评分");
		getAPPCommons(bean, pageIndex, 3);
	}

	/**
	 * 
	 * <p>
	 * 功能描述
	 * </p>
	 * 网络获取点评列表
	 * 
	 * @param bean
	 * @author wangzhichao
	 * @date 2015年9月2日
	 */
	private void getAPPCommons(AppDetails bean, int pageIndex, int pageNum) {
		if (CommonUtil.isNetworkAvailable(mContext)) {
			SearchProvider provider = new SearchProvider(mContext);
			provider.setOnProviderListener(mAppsListener);
			LinkedHashMap<String, String> paramMap = new LinkedHashMap<String, String>();
			paramMap.put("app_id", bean.getApp_id());
			paramMap.put("p_index", pageIndex + "");
			paramMap.put("p_p_num", pageNum + "");
			provider.loadAppCommons(paramMap);
		} else {
			if (isFrist) {
				try {
					comms = MainApplication.dbUtils.findAll(Selector.from(AppComment.class).where("app_id", "=", bean.getApp_id()).orderBy("data_comment_time", true).limit(3));
					isFrist = false;
					if(comms==null){
						hasMoreData = false;
						setListViewFooterHintText(NO_DATA);
						return;
					}
					if (comms.size()< 3) {
						hasMoreData = false;
						setListViewFooterHintText(NO_HAS_MORE);
					} else if (comms.size() == 0) {
						hasMoreData = false;
						setListViewFooterHintText(NO_DATA);
					} else {
						hasMoreData = true;
						setListViewFooterHintText(HAS_MORE);
					}
				} catch (DbException e) {
					e.printStackTrace();
				}
			} else {
				this.pageIndex++;
				getAppCommons4DB(bean, this.pageIndex, pageNum);
			}
		}
	}
	
	/**
	 * 
	 * <p>功能描述</p>获取当前用户对改软件的评论
	 * @author wangzhichao
	 * @date 2015年12月30日
	 */
	
	private void getAppUserCommon(AppDetails bean){
		String user_id=PropertiesUtil.getProperties(mContext, "WeDrive_Login_User_ID");
		if (CommonUtil.isNetworkAvailable(mContext)) {
			SearchProvider provider = new SearchProvider(mContext);
			provider.setOnProviderListener(mAppsListener);
			LinkedHashMap<String, String> paramMap = new LinkedHashMap<String, String>();
			paramMap.put("app_id", bean.getApp_id());
			paramMap.put("User_id", user_id);
			provider.loadUserAppCommons(paramMap);
		} else {
			AppComment comm=null;
			try {
				comm=MainApplication.dbUtils.findFirst(Selector.from(AppComment.class)
						.where("app_id", "=", bean.getApp_id()).where("data_user_id", "=", user_id));
			} catch (DbException e) {
				e.printStackTrace();
			}finally{
				Message message=new Message();
				message.what=10;
				message.obj=comm;
				handler.sendMessage(message);
			}
		}
	}

	/**
	 * 
	 * <p>功能描述</p>数据库获取评论数据
	 * @param bean
	 * @param pageIndex
	 * @param pageNum
	 * @author wangzhichao
	 * @date 2015年12月25日
	 */
	
	private void getAppCommons4DB(AppDetails bean, int pageIndex, int pageNum) {
		try {
			int count = (int) MainApplication.dbUtils.count(Selector.from(AppComment.class).where("app_id", "=", bean.getApp_id()));
			Cursor cs = MainApplication.dbUtils.execQuery("select * from AppComment where app_id = " + "'" + bean.getApp_id() + "'" 
			+ " order by data_comment_time desc limit " + ((pageIndex - 2) * 10 + 3)+ ",10;");
			if (count < ((pageIndex - 1) * 10 + 3)) {
				hasMoreData = false;
				setListViewFooterHintText(NO_HAS_MORE);
			} else {
				hasMoreData = true;
				setListViewFooterHintText(HAS_MORE);
			}
			
			setListViewProgressVisibile(false);// 隐藏progressbar

			while (cs.moveToNext()) {
				AppComment vo = new AppComment();
				vo.setApp_id(cs.getString(cs.getColumnIndex("app_id")));
				vo.setApp_version_id(cs.getString(cs.getColumnIndex("app_version_id")));
				vo.setData_comment_con(cs.getString(cs.getColumnIndex("data_comment_con")));
				vo.setData_comment_score(cs.getString(cs.getColumnIndex("data_comment_score")));
				vo.setData_comment_time(cs.getString(cs.getColumnIndex("data_comment_time")));
				vo.setData_id(cs.getString(cs.getColumnIndex("data_id")));
				vo.setData_user_id(cs.getString(cs.getColumnIndex("data_user_id")));
				vo.setData_user_name(cs.getString(cs.getColumnIndex("data_user_name")));
				comms.add(comms.size(), vo);
			}
		} catch (DbException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_load_common:
			if(appDetails!=null){
				PackageInfo info = CommonUtil.isAppInstalled(mContext, appDetails.app_package_name);
				if (info == null) {
					String path = appDetails.app_apk_path.substring(appDetails.app_apk_path.lastIndexOf("/") + 1);
					path = MainApplication.apkDownloadPath + File.separator + path;
					File apk = new File(path);
					if (!apk.exists() || AppUtil.getAPKInfo(mContext, apk.getAbsolutePath()) == null) {
						mAif.showAlert("请先下载应用");	
					}else{
						mAif.showAlert("请先安装应用");	
					}
				}
				else if(!Configs.APPSTORE_IS_LOGIN){
					mAif.showAlert("请先登录账号");
				}
				else{
					getAppUserCommon(appDetails);
				}
			}
			break;
		case R.id.tv_commit:// 提交按钮			
			commitComment();
			break;
		case R.id.tv_cancel:// 取消按钮
			comm = null;
			dialog.dismiss();
			break;
		default:
			break;
		}

	}

	/**
	 * 提交评论
	 */
	private void commitComment() {
		AppBean appBean = getAppBean(appDetails.getApp_id());
		if (appBean == null) return;
		
		int score=(int) ratingbar.getRating();
		if(score<=0.0){
			mAif.showAlert(R.string.tv_details_comment_content_score);
			return;
		}

		String commentContent = edittext.getText().toString();
		//验证字符长度
		if(commentContent !=null&&!"".equals(commentContent)){
			commentContent = commentContent.trim();
			int length = commentContent.length();
			if(length<10){
				//输入内容过短
				mAif.showAlert(R.string.tv_details_comment_content_short);
				return;
			}
			if(length>50){
				//输入内容过长
				mAif.showAlert(R.string.tv_details_comment_content_long);
				return;
			}
		}else{
			//为空
			mAif.showAlert(R.string.tv_details_comment_content);
			return;
		}

		if (CommonUtil.isNetworkAvailable(mContext)) {
			String user_id=PropertiesUtil.getProperties(mContext, "WeDrive_Login_User_ID");
			SearchProvider provider = new SearchProvider(mContext);
			provider.setOnProviderListener(mAppsListener);
			LinkedHashMap<String, String> paramMap = new LinkedHashMap<String, String>();
			paramMap.put("app_id", appBean.getApp_id());
			paramMap.put("app_v_id", appBean.getApp_version_id());

			LinkedHashMap<String, String> paramMap2 = new LinkedHashMap<String, String>();
			if(comm!=null) paramMap2.put("comment_id", comm.data_id);
			paramMap2.put("user_id", user_id);
			paramMap2.put("content", commentContent);
			paramMap2.put("score", ""+score);

			String token=PropertiesUtil.getProperties(mContext, "WeDrive_Login_Token");
			MyHttpHandler myHttpHandler = provider.upLoadAppCommon(paramMap,paramMap2,token);
			mAif.showProgressDialog(myHttpHandler, R.string.dialog_upload_app_common, true);
		}else{
			mAif.showAlert(R.string.dialog_loading_net_unconnect);
		}
	}

	/**
	 * 根据app_id从数据库中查询到相对应的Appbean
	 * 
	 * @param app_id
	 * @return
	 */
	private AppBean getAppBean(String app_id) {
		try {
			AppBean appBean = MainApplication.dbUtils.findFirst(Selector.from(AppBean.class).where("app_id", "=", app_id));
			return appBean;
		} catch (DbException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 显示评论输入框
	 */
	private void initDialog(AppComment comm) {
		if (dialog == null) {
			initDialogView();
		}
		if(comm!=null){
			ratingbar.setRating(Integer.parseInt(comm.data_comment_score));
			edittext.setText(comm.data_comment_con);	
		}else{
			ratingbar.setRating(0);
			edittext.setText("");
		}		
		dialog.show();
	}

	/**
	 * 
	 * <p>功能描述</p>初始化评论对话框
	 * @author wangzhichao
	 * @date 2015年12月28日
	 */
	
	private void initDialogView() {
		dialog = new Dialog(mContext, R.style.FullHeightDialog);
		dialog.setContentView(R.layout.layout_details_alrelt);
		Window dialogWindow = dialog.getWindow();
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		dialogWindow.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
		//lp.height = CommonUtil.dip2px(mContext, 252);
		lp.width = CommonUtil.dip2px(mContext, 392);
		dialogWindow.setAttributes(lp);
		dialogWindow.setBackgroundDrawableResource(R.color.transparent);
		edittext = (EditText) dialog.findViewById(R.id.edtTxt_details_content);
		ratingbar = (RatingBar) dialog.findViewById(R.id.ratBar_details_alrelt);
		
		ratingbar.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {			
			@Override
			public void onRatingChanged(RatingBar ratingBar, float rating,boolean fromUser) {
				switch ((int) rating) {
				case 1:
					mAif.showAlert("太逊了，给1分");
					break;
				case 2:
					mAif.showAlert("不给力，给2分");
					break;
				case 3:
					mAif.showAlert("还可以，给3分");
					break;
				case 4:
					mAif.showAlert("很不错，给4分");
					break;
				case 5:
					mAif.showAlert("好极了，给5分");
					break;
				}
			}
		});

		dialog.findViewById(R.id.tv_commit).setOnClickListener(this);
		dialog.findViewById(R.id.tv_cancel).setOnClickListener(this);
		edittext.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
					InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(edittext.getWindowToken(), 0);
					commitComment();
					return true;
				}
				return false;
			}
		});
	}

	private OnProviderListener mAppsListener = new OnProviderListener() {

		@Override
		public void onProviderResponse(int requestCode, int responseCode, ProviderResult result) {
			mAif.hideProgressDialog();	
			if (requestCode == Configs.REQUEST_CODE_LOAD_APP_COMMONS) {
				setListViewProgressVisibile(false);
			}
			
			if (responseCode == -1) {
				mAif.showAlert(R.string.dialog_loading_net_error);
				return;
			}			
			switch (requestCode) {
			case Configs.REQUEST_CODE_LOAD_APP_COMMONS:
				try {
					JSONObject obj = new JSONObject(result.getResponseStr());
					int status = obj.getInt("status");
					if (status != 200) {
						hasMoreData = false;
						setListViewFooterHintText(NO_DATA);
						return;
					}

					obj = obj.getJSONObject("data");
					if (obj == null) {
						return;
					}
					
					if(obj.has("p_data")){
						String app_id = obj.getString("app_id");
						String app_v_id = obj.getString("app_v_id");
						JSONArray jsonArr = obj.getJSONArray("p_data");
						if (jsonArr.length() > 0) {
							if (!isFrist) {
								if (jsonArr.length() < 10) {
									hasMoreData = false;
									setListViewFooterHintText(NO_HAS_MORE);
								} else {
									pageIndex++;
									hasMoreData = true;
									setListViewFooterHintText(HAS_MORE);
								}
							} else {
								isFrist = false;
								if (jsonArr.length() < 3) {
									hasMoreData = false;
									setListViewFooterHintText(NO_HAS_MORE);
								} else if (jsonArr.length() == 0) {
									hasMoreData = false;
									setListViewFooterHintText(NO_DATA);
								} else {
									pageIndex++;
									hasMoreData = true;
									setListViewFooterHintText(HAS_MORE);
								}
							}

							for (int i = 0; i < jsonArr.length(); i++) {
								JSONObject listobj = jsonArr.getJSONObject(i);

								AppComment comm = new AppComment();

								comm.setApp_id(app_id);
								comm.setApp_version_id(app_v_id);

								if (listobj.has("id"))
									comm.setData_id(listobj.getString("id"));
								if (listobj.has("comment_con"))
									comm.setData_comment_con(listobj
											.getString("comment_con"));
								if (listobj.has("comment_score"))
									comm.setData_comment_score(listobj
											.getString("comment_score"));
								if (listobj.has("user_id"))
									comm.setData_user_id(listobj
											.getString("user_id"));
								if (listobj.has("user_name"))
									comm.setData_user_name(listobj
											.getString("user_name"));
								if (listobj.has("comment_time"))
									comm.setData_comment_time(listobj
											.getString("comment_time"));

								comms.add(comms.size(), comm);
							}

							try {
								MainApplication.dbUtils.saveOrUpdateAll(comms);
							} catch (DbException e) {
								e.printStackTrace();
							}

							adapter.notifyDataSetChanged();

						} else {
							if (!isFrist) {
								hasMoreData = false;
								setListViewFooterHintText(NO_HAS_MORE);
							} else {
								hasMoreData = false;
								setListViewFooterHintText(NO_DATA);
							}
						}
					}
				} catch (JSONException e) {}
				break;
			case Configs.REQUEST_CODE_LOAD_APP_USER_COMMONS:
				Message message = new Message();
				message.what = 10;
				try {
					JSONObject obj = new JSONObject(result.getResponseStr());
					int status = obj.getInt("status");
					if (status == 200) {
						obj = obj.getJSONObject("data");
						if (obj != null) {
							AppComment comm = new AppComment();
							if (obj.has("id"))
								comm.setData_id(obj.getString("id"));
							if (obj.has("app_id"))
								comm.setApp_id(obj.getString("app_id"));
							if (obj.has("app_version_id"))
								comm.setApp_version_id(obj
										.getString("app_version_id"));
							if (obj.has("comment_con"))
								comm.setData_comment_con(obj
										.getString("comment_con"));
							if (obj.has("comment_score"))
								comm.setData_comment_score(obj
										.getString("comment_score"));
							if (obj.has("user_id"))
								comm.setData_user_id(obj.getString("user_id"));
							if (obj.has("user_name"))
								comm.setData_user_name(obj
										.getString("user_name"));
							if (obj.has("comment_time"))
								comm.setData_comment_time(obj
										.getString("comment_time"));
							message.obj = comm;
						}
					} else if (status == 206) {
						message.obj = null;
					} else {
						if (obj.has("msg")) {
							mAif.showAlert(obj.getString("msg"));
						}
					}
				} catch (JSONException e) {
					message.obj = null;
				} finally {
					handler.sendMessage(message);
				}
				break;
			case Configs.REQUEST_CODE_UPLOAD_APP_COMMON:
				JSONObject obj = null;
				try {
					obj = new JSONObject(result.getResponseStr());
					if (obj.has("msg")) {
						mAif.showAlert(obj.getString("msg"));
					}
					int status = obj.getInt("status");
					if (status == 200) {
						dialog.dismiss();
						comm = null;
						if (detailPage != null) {
							detailPage.getAppDetail4Net(app_id);// 走这个方法会重新走本类中的setAppDetailDetail
						}
					}
				} catch (Exception e) {
					mAif.showAlert("评分失败！");
				}
				break;
			}
		}

		@Override
		public void onReadResponse(int arg0, int arg1) {

		}

	};

	private ListView lVi_app_commons;

	public class CommonAdapter extends ScaleAdapter {

		class ViewHolder {
			CircularImage imgView_head;
			RatingBar rb_app;
			TextView tv_desc, tv_name, tv_date;
		}

		public int getCount() {
			return comms.size();
		}

		public Object getItem(int position) {
			return comms.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.item_detail_comment_view, parent, false);
				holder = new ViewHolder();
				holder.imgView_head = (CircularImage) convertView.findViewById(R.id.imgView_head);
				holder.rb_app = (RatingBar) convertView.findViewById(R.id.rb_app);
				holder.tv_desc = (TextView) convertView.findViewById(R.id.tv_desc);
				holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
				holder.tv_date = (TextView) convertView.findViewById(R.id.tv_date);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			AppComment comm = comms.get(position);			
			String headerImage=Configs.URL_USER_ICON_ID+"?userId="+comm.getData_user_id();
			bitmapUtils.display(holder.imgView_head,headerImage);			
			holder.tv_desc.setText(comm.getData_comment_con());
			String time = comm.getData_comment_time();
			holder.tv_date.setText(time.substring(0, time.lastIndexOf(" ")));
			holder.rb_app.setRating(Float.valueOf(comm.getData_comment_score()));
			String userName=comm.getData_user_name();
			if(RegExpUtil.regExpEmail(userName)){
				int index=userName.lastIndexOf("@");
				userName = userName.substring(0,index-4)+"****"+userName.substring(index,userName.length());
			}else if(RegExpUtil.regExpMobile(userName)){
				userName = userName.substring(0,3)+"****"+userName.substring(7,userName.length());
			}else if("null".equals(userName)){
				userName="匿名用户";
			}
			holder.tv_name.setText(userName);
			return convertView;
		}

		@Override
		public View getScaleView(int arg0, View arg1, ViewGroup arg2) {
			return getView(arg0, arg1, arg2);
		}

	}

}
