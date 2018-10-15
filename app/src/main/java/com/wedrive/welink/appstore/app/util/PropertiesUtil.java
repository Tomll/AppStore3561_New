/**
 * Created by wangzhichao on 2016年2月18日.
 * Copyright (c) 2015 北京图为先科技有限公司. All rights reserved.
 */
package com.wedrive.welink.appstore.app.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import com.wedrive.welink.appstore.Configs;

import android.content.Context;
import android.os.Environment;

public class PropertiesUtil {
	
	private static String rootPath=Environment.getExternalStorageDirectory().getAbsolutePath();
	
	static {
		try {
			File file = new File(rootPath + File.separator+ Configs.DRIVE_ACCOUTN_PROPERTIES);
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * <p>功能描述</p>加载设置的Properties参数文件
	 * @param context
	 * @param file
	 * @return
	 * @author wangzhichao
	 * @date 2016年2月18日
	 */

	public static Properties loadConfig(Context context) {
		Properties properties = new Properties();
		try {
			FileInputStream s = new FileInputStream(rootPath + File.separator + Configs.DRIVE_ACCOUTN_PROPERTIES);
			properties.load(s);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return properties;
	}
	
	/**
	 * 
	 * <p>功能描述</p>添加新的配置参数
	 * @param key
	 * @param value
	 * @author wangzhichao
	 * @date 2016年2月19日
	 */
	
	public static void addProperties(Context context,String key,String value){
		Properties properties=loadConfig(context);
		properties.put(key, value);
		saveConfig(context,properties);
	}
	
	/**
	 * 
	 * <p>功能描述</p>添加新的配置参数
	 * @param key
	 * @param value
	 * @author wangzhichao
	 * @date 2016年2月19日
	 */
	
	public static void addAllProperties(Context context,HashMap<String,String> ps){
		Properties properties=loadConfig(context);
		properties.putAll(ps);
		saveConfig(context,properties);
	}
	
	/**
	 * 
	 * <p>功能描述</p>删除配置参数
	 * @param key
	 * @param value
	 * @author wangzhichao
	 * @date 2016年2月19日
	 */
	
	public static void removeProperties(Context context,String key){
		Properties properties=loadConfig(context);
		properties.remove(key);
		saveConfig(context,properties);
	}
	
	/**
	 * 
	 * <p>功能描述</p>获取配置参数
	 * @param key
	 * @param value
	 * @author wangzhichao
	 * @date 2016年2月19日
	 */
	
	public static String getProperties(Context context,String key){
		Properties properties=loadConfig(context);
		return properties.getProperty(key,null);
	}
	
	/**
	 * 
	 * <p>功能描述</p>获取配置参数
	 * @param key
	 * @param value
	 * @author wangzhichao
	 * @date 2016年2月19日
	 */
	
	public static void clearProperties(Context context){
		Properties properties=loadConfig(context);
		properties.clear();
		saveConfig(context,properties);
	}

	/**
	 * 
	 * <p>功能描述</p>保存设置的Properties参数文件
	 * @param context
	 * @param file
	 * @param properties
	 * @author wangzhichao
	 * @date 2016年2月18日
	 */
	public static void saveConfig(Context context,Properties properties) {
		try {
			FileOutputStream s = new FileOutputStream(rootPath + File.separator + Configs.DRIVE_ACCOUTN_PROPERTIES, false);
			properties.store(s, "");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
}
