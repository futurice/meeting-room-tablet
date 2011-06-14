package com.futurice.android.reservator.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.util.Log;

public class Helpers {
	/**
	 * Read all from InputStream
	 * @param is Stream to read from
	 * @param size Guess the size of InputStream contents, give negative for the automatic
	 * @return
	 */
	public static String readFromInputStream(InputStream is, int size) {
		try {
			ByteArrayOutputStream os;
			if (size <= 0) {
				os = new ByteArrayOutputStream();
			} else {
				os = new ByteArrayOutputStream(size);
			}
			byte buffer[] = new byte[4096];
			int len;
			while ((len = is.read(buffer)) != -1) {
				os.write(buffer, 0, len);
			}
			return os.toString("UTF-8");
		} catch (IOException e) {
			Log.e("SOAP", "readFromInputStream", e);
			return "";
		}
	}
}
