package it.auh.citytracker.utils;

public final class StringUtils {
	public static String substring(String string, int start, int end) {
		String res = null;
		if (string != null) {
			end = Math.min(string.length(), end);
			start = Math.max(0, start);
			res = string.substring(start, end);
		}
		return res;
	}
}
