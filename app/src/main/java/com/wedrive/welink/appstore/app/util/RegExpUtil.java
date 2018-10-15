/**
 * Created by wangzhichao on 2015年12月1日.
 * Copyright (c) 2015 北京图为先科技有限公司. All rights reserved.
 */
package com.wedrive.welink.appstore.app.util;

import java.util.regex.Pattern;

public class RegExpUtil {

	/**
	 * 
	 * <p>
	 * 功能描述
	 * </p>
	 * 验证邮箱格式
	 * 
	 * @param str
	 * @return
	 * @author wangzhichao
	 * @date 2015年12月1日
	 */

	public static boolean regExpEmail(String str) {
		String regex = "^[\\w-]+(\\.[\\w-]+)*\\@([\\.\\w-]+)+$";
		boolean flg = Pattern.matches(regex, str);
		System.out.println(flg);
		return flg;
	}

	/**
	 * 
	 * <p>
	 * 功能描述
	 * </p>
	 * 验证电话号码
	 * 
	 * @param str
	 * @return
	 * @author wangzhichao
	 * @date 2015年12月1日
	 */
	public static boolean regExpPhone(String str) {
		String regex = "(\\d+-)?(\\d{4}-\\d{7,8})(-\\d+)";
		boolean flg = Pattern.matches(regex, str);
		System.out.println(flg);
		return flg;
	}

	/**
	 * 
	 * <p>
	 * 功能描述
	 * </p>
	 * 验证手机号码
	 * 
	 * @param mobiles
	 * @return
	 * @author wangzhichao
	 * @date 2016年1月28日
	 */

	public static boolean regExpMobile(String mobiles) {
		/*
		 * 移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188
		 * 联通：130、131、132、152、155、156、185、186 电信：133、153、180、189、（1349卫通）
		 * 总结起来就是第一位必定为1，第二位必定为3或5或8，其他位置的可以为0-9
		 */
		String telRegex = "[1][358]\\d{9}";// "[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
		boolean flg = mobiles.matches(telRegex);
		return flg;
	}
}
