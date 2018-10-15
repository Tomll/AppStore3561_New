package com.wedrive.welink.appstore.app.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import android.content.Context;
import android.os.Environment;

public class FileUtil {
	

	/**
	 * 把字符串数据写入文件
	 * @param content 需要写入的字符串
	 * @param path    文件路径名称
	 * @param append  是否以添加的模式写入
	 * @return 是否写入成功
	 */
	public static boolean writeFile(byte[] content, String path, boolean append) {
		boolean res = false;
		File f = new File(path);
		RandomAccessFile raf = null;
		try {
			if (f.exists()) {
				if (!append) {
					f.delete();
					f.createNewFile();
				}
			} else {
				f.createNewFile();
			}
			if (f.canWrite()) {
				raf = new RandomAccessFile(f, "rw");
				raf.seek(raf.length());
				raf.write(content);
				res = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			IOUtils.close(raf);
		}
		return res;
	}

	/**
	 * 把字符串数据写入文件
	 * @param content 需要写入的字符串
	 * @param path    文件路径名称
	 * @param append  是否以添加的模式写入
	 * @return 是否写入成功
	 */
	public static boolean writeFile(String content, String name, boolean append) {
		String path=Environment.getExternalStorageDirectory().getAbsolutePath();
		File file = new File(path+"/Test");
		if(!file.exists()){
			file.mkdir();
		}
		return writeFile(content.getBytes(), file.getAbsolutePath()+"/"+name, append);
	}

	
	/**
	 * 把数据写入文件
	 * @param is       数据流
	 * @param path     文件路径
	 * @param recreate 如果文件存在，是否需要删除重建
	 * @return 是否写入成功
	 */
	public static boolean writeFile(InputStream is, String path, boolean recreate) {
		boolean res = false;
		File f = new File(path);
		FileOutputStream fos = null;
		try {
			if (recreate && f.exists()) {
				f.delete();
			}
			if (!f.exists() && null != is) {
				File parentFile = new File(f.getParent());
				parentFile.mkdirs();
				int count = -1;
				byte[] buffer = new byte[1024];
				fos = new FileOutputStream(f);
				while ((count = is.read(buffer)) != -1) {
					fos.write(buffer, 0, count);
				}
				res = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			IOUtils.close(fos);
			IOUtils.close(is);
		}
		return res;
	}
	
	public static void write2File(String content,File path, String filename){
		try {
			File file = new File(path, filename);
			FileWriter fw = new FileWriter(file);
			fw.write(content);
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void write2SDCardFile(String content, String filename) {
		write2File(content, Environment.getExternalStorageDirectory(),filename);
	}
	
	public final static String readFile(File file){
		
		StringBuilder sb = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			 sb = new StringBuilder();
			String line = null;
			while((line = br.readLine())!= null){
				sb.append(line);
			}
			br.close();
			return sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String readAssetFile(Context context,String filePath){
		String jsonStr = null;
		try {
			InputStream is = context.getResources().getAssets().open(filePath);
			byte[] buffer = new byte[is.available()];
			is.read(buffer);
			is.close();
			jsonStr = new String(buffer, "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jsonStr;
	}

	public static boolean delAllFile(String path) {
		boolean flag = false;
		File file = new File(path);
		if (!file.exists()) {
			  return flag;
		}
		if (!file.isDirectory()) {
			return flag;
		}
		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++) {
			 if (path.endsWith(File.separator)) {
				  temp = new File(path + tempList[i]);
			 } else {
				   temp = new File(path + File.separator + tempList[i]);
			 }
			 if (temp.isFile()) {
				   temp.delete();
			 }
			 if (temp.isDirectory()) {
				  delAllFile(path + "/" + tempList[i]);// 先删除文件夹里面的文件
				  flag = true;
			 }
		}
		return flag;
	}

}
