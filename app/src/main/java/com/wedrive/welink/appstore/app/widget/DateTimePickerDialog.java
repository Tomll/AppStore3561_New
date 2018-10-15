package com.wedrive.welink.appstore.app.widget;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.LinearLayout;

import com.wedrive.welink.appstore.R;
import com.wedrive.welink.appstore.app.view.LoginPage.DatePickerCallBack;

@SuppressLint("SimpleDateFormat")
public class DateTimePickerDialog implements OnDateChangedListener {

	private DatePicker datePicker;
	private AlertDialog ad;
	private String dateTime;
	private String initDateTime;
	private Context activity;

	private Calendar upper, lower, select;
	private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

	/**
	 * 日期时间弹出选择框构
	 * 
	 * @param activity
	 *            ：调用的父activity
	 */

	public DateTimePickerDialog(Context activity) {
		this.activity = activity;
	}

	public void init(String upperLimit, String lowerLimit, String selected) {
		try {
			upper = new GregorianCalendar();
			upper.setTime(df.parse(upperLimit));
			
			lower = new GregorianCalendar();
			lower.setTime(df.parse(lowerLimit));

			select = new GregorianCalendar();
			select.setTime(df.parse(selected));
		} catch (ParseException e) {
			Log.e("message","exception:"+e.getMessage());
		}
	}
	
	public AlertDialog dateTimePicKDialog(final DatePickerCallBack callBack,final String method) {
		LinearLayout dateTimeLayout = (LinearLayout) LayoutInflater.from(
				activity).inflate(R.layout.date_dialog, null);
		datePicker = (DatePicker) dateTimeLayout.findViewById(R.id.datepicker);

		initDateTime = select.get(Calendar.YEAR) + "-"
				+ select.get(Calendar.MONTH) + "-"
				+ select.get(Calendar.DAY_OF_MONTH);
		datePicker.init(select.get(Calendar.YEAR), select.get(Calendar.MONTH),select.get(Calendar.DAY_OF_MONTH), this);
		dateTime = df.format(select.getTime());

//		datePicker.setCalendarViewShown(true);
//		datePicker.setMaxDate(upper.getTimeInMillis());
		
		ad = new AlertDialog.Builder(activity)
				.setTitle(initDateTime)
				.setView(dateTimeLayout)
				.setPositiveButton("设置", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String function = method + "(\""+ dateTime + "\")";
						callBack.callBack(function);
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

					}
				}).show();
		onDateChanged(datePicker,select.get(Calendar.YEAR), select.get(Calendar.MONTH),select.get(Calendar.DAY_OF_MONTH));
		return ad;
	}

	@SuppressLint("SimpleDateFormat")
	public void onDateChanged(DatePicker view, int year, int monthOfYear,int dayOfMonth) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(datePicker.getYear(), datePicker.getMonth(),datePicker.getDayOfMonth());	
//		dateTime = df.format(calendar.getTime());
//		ad.setTitle(dateTime);
		
		if (calendar.getTime().getTime()<lower.getTime().getTime()) {
			view.init(lower.get(Calendar.YEAR), lower.get(Calendar.MONTH), lower.get(Calendar.DAY_OF_MONTH), this);
			dateTime = df.format(lower.getTime());
			ad.setTitle(dateTime);
		}else if(calendar.getTime().getTime()>upper.getTime().getTime()){
			view.init(upper.get(Calendar.YEAR), upper.get(Calendar.MONTH), upper.get(Calendar.DAY_OF_MONTH), this);
			dateTime = df.format(upper.getTime());
			ad.setTitle(dateTime);
		}
		else{
			dateTime = df.format(calendar.getTime());
			ad.setTitle(dateTime);
		}
		
	}


}
