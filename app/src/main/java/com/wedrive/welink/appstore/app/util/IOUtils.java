package com.wedrive.welink.appstore.app.util;

import java.io.Closeable;
import java.io.IOException;

public class IOUtils {

	/** 关闭流 */
	public static boolean close(Closeable io) {
		if (io != null) {
			try {
				io.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}
}
