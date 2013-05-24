package it.auh.citytracker.utils;

public final class StringUtils {
	public static String substring(String string, int start, int end) {
		String res = null;
		int rEnd = 0;
		if (string != null) {
			rEnd = Math.min(string.length(), end);
			int rStart = Math.max(0, start);
			res = string.substring(rStart, rEnd);
		}
		return res;
	}
	
	public static String ellipsis(String string, int start, int end) {
		int lenght = string.length();
		String res = substring(string, start, end);
		if (res.length() < lenght) res += "...";
		return res;
	}
	
	public static boolean isEmpty(String string) {
		return (string == null || string.equals(""));
	}
}
