package cn.zadui.reader.service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

import android.content.Context;
import cn.zadui.reader.helper.FAQCalendar;
import cn.zadui.reader.helper.Settings;

/**
 * Used for collecting app usage.
 * @author david
 *
 */
public class UsageCollector {
	
	static final String HOUR_PREFER_STR="000000000000000000000000";

	public static void openApp(Context ctx){
		long currentTime=System.currentTimeMillis();
		long lastOpenTS=Settings.getLongPreferenceValue(ctx, Settings.PRE_LAST_OPENED_AT, currentTime);
		long interval=lastOpenTS-currentTime;
		// Return if the interval to last opened less than 15 minutes.
		if (interval<15*60*1000) return;
		String oldUsageStr=Settings.getStringPreferenceValue(ctx, Settings.PRE_USAGE,"");
		FAQCalendar lastOpened=new FAQCalendar(lastOpenTS);
		FAQCalendar now=new FAQCalendar(currentTime);
		if (now.getUnixDay()>lastOpened.getUnixDay()){
			long distance=now.getUnixDay()-lastOpened.getUnixDay();
			StringBuilder sb=new StringBuilder();
			sb.append(oldUsageStr);
			for(long i=0;i<distance-1;i++) sb.append("0");
			sb.append("1");
			Settings.updateStringPreferenceValue(ctx, Settings.PRE_USAGE, sb.toString());
		}else{
			char[] usageChars=oldUsageStr.toCharArray();
			int count=Character.getNumericValue(usageChars[usageChars.length-1]);
			usageChars[usageChars.length-1]=numberToChar(count+1);
			Settings.updateStringPreferenceValue(ctx, Settings.PRE_USAGE, new String(usageChars));
		}
		Settings.updateLongPreferenceValue(ctx, Settings.PRE_LAST_OPENED_AT, System.currentTimeMillis());
		// update hour prefer usage string
		char[] preferUsageChars=Settings.getStringPreferenceValue(ctx, Settings.PRE_HOUR_PREFER_USAGE,HOUR_PREFER_STR).toCharArray();
		int whichHour=now.get(Calendar.HOUR_OF_DAY);
		preferUsageChars[whichHour]=numberToChar(Character.getNumericValue(preferUsageChars[whichHour])+1);
		Settings.updateStringPreferenceValue(ctx, Settings.PRE_HOUR_PREFER_USAGE, new String(preferUsageChars));
	}
	
	public static void clear(Context ctx){
		
	}
	
	/**
	 * TODO The upload process should be in a thread and started by an Activity or Service.
	 * @param ctx
	 */
	public static void uploadCollectedUsageDate(Context ctx){
		URL url;
		try {
			url = new URL("http://172.29.1.67:3389/collector");
			HttpURLConnection uc = (HttpURLConnection) url.openConnection();
	        uc.setDoInput(true);
	        uc.setDoOutput(true);
	        uc.setRequestMethod("POST");
	        String data=generatePingStr(ctx);
	        uc.getOutputStream().write(data.getBytes("UTF-8")); 
	        uc.getOutputStream().close();
	        if (uc.getResponseCode()==HttpURLConnection.HTTP_CREATED){
	        	clear(ctx);
	        }else{
	        	// Do nothing
	        }
	        uc.disconnect();
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Ping string includes:
	 * uid
	 * this collection started at
	 * device information: os type and version; app version etc.
	 * usage string
	 * hour prefer usage string
	 * @param ctx
	 * @return
	 */
	public static String generatePingStr(Context ctx){
		StringBuilder sb=new StringBuilder();
		sb.append("uid="+"");
		sb.append("&from="+Settings.getStringPreferenceValue(ctx, Settings.PRE_COLLECTION_STARTED_AT, ""));
		sb.append("&dev="+"");
		sb.append("&usage="+Settings.getStringPreferenceValue(ctx, Settings.PRE_USAGE, ""));
		sb.append("&hour="+Settings.getStringPreferenceValue(ctx, Settings.PRE_HOUR_PREFER_USAGE, ""));
		return sb.toString();
	}
	
	public static char numberToChar(int value){
		if (value>9){
			return (char)(10-9+64);
		}else{
			return Character.forDigit(value, 10);
		}
	}
	
	//public static void 
}
