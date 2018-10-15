package com.wedrive.welink.appstore.app.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.core.BitmapSize;
import com.mapbar.android.model.ActivityInterface;
import com.mapbar.android.statistics.api.MapbarMobStat;
import com.wedrive.welink.appstore.Configs;
import com.wedrive.welink.appstore.MainActivity;
import com.wedrive.welink.appstore.MainApplication;
import com.wedrive.welink.appstore.R;
import com.wedrive.welink.appstore.app.model.AppDetails;

import java.text.DecimalFormat;

public class DetailPageDetail implements OnClickListener {

	private View mView;
	private ActivityInterface mAif;
	private Context mContext;

	public DetailPageDetail(View view, ActivityInterface aif, Context context) {
		mView = view;
		mAif = aif;
		mContext = context;
		initView(view);
	}

	private LinearLayout lv_photos;
	private LinearLayout rl_zk_introduce;
	private LinearLayout rl_zk_app_update;

	private TextView tv_details_app_introduce_content;
	private TextView tv_details_app_update_content;
	private TextView tv_zk_introduce;
	private TextView tv_zk_app_update;

	private ImageView img_app_update;
	private ImageView imgIntroduce;
	
	private TextView tv_details_app_size;
	private TextView tv_details_app_version;
	private TextView tv_details_app_developer;
	
	private DetailPhotoDetail page;

	private void initView(View view) {
		lv_photos = (LinearLayout) view.findViewById(R.id.lv_photos);
		tv_details_app_introduce_content = (TextView) view.findViewById(R.id.tv_details_app_introduce_content);
		tv_details_app_update_content = (TextView) view.findViewById(R.id.tv_details_app_update_content);
		// 介绍
		rl_zk_introduce = (LinearLayout) view.findViewById(R.id.rl_zk_introduce);// 介绍展开按钮
		tv_zk_introduce = (TextView) view.findViewById(R.id.tv_zk_introduce);// 介绍展开按钮
		imgIntroduce = (ImageView) view.findViewById(R.id.img_app_introduce);
		rl_zk_introduce.setOnClickListener(this);
		rl_zk_introduce.setTag(true);

		// 更新
		rl_zk_app_update = (LinearLayout) view.findViewById(R.id.rl_zk_app_update);// 介绍展开按钮
		tv_zk_app_update = (TextView) view.findViewById(R.id.tv_zk_app_update);// 介绍展开按钮
		img_app_update = (ImageView) view.findViewById(R.id.img_app_update);
		rl_zk_app_update.setOnClickListener(this);
		rl_zk_app_update.setTag(true);
		
		//版本信息
		tv_details_app_size=(TextView) view.findViewById(R.id.tv_details_app_size);
		tv_details_app_version=(TextView) view.findViewById(R.id.tv_details_app_version);
		tv_details_app_developer=(TextView) view.findViewById(R.id.tv_details_app_developer);

	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 11:
				tv_details_app_introduce_content.setMaxLines(3);
				rl_zk_introduce.setVisibility(View.VISIBLE);
				break;
			case 12:
				tv_details_app_update_content.setMaxLines(3);
				rl_zk_app_update.setVisibility(View.VISIBLE);
				break;
			}
		}
	};

	/**
	 * 方法名称：
	 * 方法描述：设置app详情信息
	 * 方法参数：
	 * 返回类型：
	 * 创建人：wangzc
	 * 创建时间：2016/11/11 10:29
	*/

	public void setAppDetailDetail(AppDetails bean) {
		//((MainActivity)mAif).onClick_View_OnClick_Event(mContext, Configs.AppStore_OnClick_Details);
		DecimalFormat df = new DecimalFormat("0.00");
		tv_details_app_size.setText("大小："+df.format(Double.parseDouble(bean.getApp_size()))+"MB");
		tv_details_app_version.setText("版本："+bean.getApp_version_name());
		tv_details_app_developer.setText("开发商："+bean.getApp_developer());

		String brief_desc = bean.getApp_brief_desc();
		String update_desc = bean.getApp_update_desc();
		lv_photos.removeAllViews();

		if (brief_desc != null && !"".equals(brief_desc) && !"null".equals(brief_desc)) {
			tv_details_app_introduce_content.setText(brief_desc.replace("\\n", "\n"));
			handler.post(new Runnable() {
				@Override
				public void run() {
					if (tv_details_app_introduce_content.getLineCount() > 3) {
						handler.sendEmptyMessage(11);
					}else{
						rl_zk_introduce.setVisibility(View.GONE);
					}
				}
			});
		}

		if (update_desc != null && !"".equals(update_desc) && !"null".equals(update_desc)) {
			tv_details_app_update_content.setText(update_desc.replace("\\n", "\n"));
			handler.post(new Runnable() {
				@Override
				public void run() {
					if (tv_details_app_update_content.getLineCount() > 3) {
						handler.sendEmptyMessage(12);
					}else{
						rl_zk_app_update.setVisibility(View.GONE);
					}
				}
			});

		}

		String images = bean.getApp_image_path();
		if (images != null && !"".equals(images)) {
			final String[] ims = images.split(",");
			if (ims != null && ims.length > 0) {
				int width = (int) mContext.getResources().getDimension(R.dimen.home_detail_image_width);
				int height = (int) mContext.getResources().getDimension(R.dimen.home_detail_image_height);
				
				BitmapDisplayConfig config=new BitmapDisplayConfig();
				BitmapSize size=new BitmapSize(600,300);
				config.setBitmapMaxSize(size);
				config.setLoadFailedDrawable(mContext.getResources().getDrawable(R.drawable.img_df_pic));
				
				for (int i = 0; i < ims.length; i++) {
					View view=View.inflate(mContext, R.layout.item_detail_image_view, null);
					ImageView imageView = (ImageView) view.findViewById(R.id.lny_item);
					imageView.setLayoutParams(new LinearLayout.LayoutParams(width,height));
					MainApplication.imageLoader.display(imageView, ims[i], config);
					final int index=i;
					OnClickListener myOnClickListener=new OnClickListener() {
						@Override
						public void onClick(View v) {
							if (page == null) {
								page = new DetailPhotoDetail(mContext, ims, index);
							} else {
								page.setCurrSelectPosition(index);
							}
							page.show();
						}
					};
					view.setOnClickListener(myOnClickListener);
					lv_photos.addView(view,i);
				}
			} else {
				mView.findViewById(R.id.rv_photos_area).setVisibility(View.GONE);
			}
		} else {
			mView.findViewById(R.id.rv_photos_area).setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rl_zk_introduce:
			update(tv_details_app_introduce_content, rl_zk_introduce, imgIntroduce, tv_zk_introduce);
			break;
		case R.id.rl_zk_app_update:
			update(tv_details_app_update_content, rl_zk_app_update, img_app_update, tv_zk_app_update);
			break;
		}
	}

	/**
	 * 方法名称：
	 * 方法描述：设置显示app应用介绍“展开”和“收起”功能
	 * 方法参数：
	 * 返回类型：
	 * 创建人：wangzc
	 * 创建时间：2016/11/11 10:31
	*/
	
	
	private void update(TextView tvContent, LinearLayout button, ImageView buttonImg, TextView buttonText) {
		boolean flag = (Boolean) button.getTag();
		if (flag) {
			button.setTag(false);
			tvContent.setMaxLines(100);
			// TODO: 2018/9/27 详情页面 展开---
            MapbarMobStat.onEvent(mContext,"F0125","展开");
        } else {
			button.setTag(true);
			tvContent.setMaxLines(3);
			// TODO: 2018/9/27 详情页面 收起---
            MapbarMobStat.onEvent(mContext,"F0124","收起");
        }

		buttonText.setText(flag ? "收起" : "展开");
		buttonImg.setBackgroundResource(flag ? R.drawable.icon_detail_up : R.drawable.icon_detail_down);
	}

}
