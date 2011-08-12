package cn.zadui.reader.service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import cn.zadui.reader.helper.FAQCalendar;
import cn.zadui.reader.helper.NetHelper;
import cn.zadui.reader.helper.Settings;

/**
 * Used for collecting app usage.
 * @author david
 *
 */
public class UsageCollector {
	
	public static final String HOUR_PREFER_STR="000000000000000000000000";
	
	static final String TAG="UsageCollector";

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
		String old=Settings.getStringPreferenceValue(ctx, Settings.PRE_HOUR_PREFER_USAGE,HOUR_PREFER_STR);
		Settings.updateStringPreferenceValue(ctx, Settings.PRE_HOUR_PREFER_USAGE, updateHourPreferUsageString(now,old));		
	}
	
	public static String updateHourPreferUsageString(Calendar now,String old){
		char[] preferUsageChars=old.toCharArray();
		int whichHour=now.get(Calendar.HOUR_OF_DAY);
		preferUsageChars[whichHour]=numberToChar(Character.getNumericValue(preferUsageChars[whichHour])+1);
		return new String(preferUsageChars);
	}
	
	public static void resetCollectedData(Context ctx){
		Settings.updateStringPreferenceValue(ctx, Settings.PRE_USAGE, "");
		Settings.updateStringPreferenceValue(ctx, Settings.PRE_HOUR_PREFER_USAGE,HOUR_PREFER_STR);
		Settings.updateLongPreferenceValue(ctx, Settings.PRE_COLLECTION_STARTED_AT, System.currentTimeMillis());
	}
	
	/**
	 * TODO The upload process should be in a thread and started by an Activity or Service.
	 * @param ctx
	 */
	public static void uploadCollectedUsageDate(Context ctx){
		String usageStr=Settings.getStringPreferenceValue(ctx, Settings.PRE_USAGE,"");
		if (usageStr.length()<5) return;
		URL url;
		try {
			url = new URL(NetHelper.webPath("http", "/collector")); //"http://172.29.1.67:3389/collector");
			HttpURLConnection uc = (HttpURLConnection) url.openConnection();
	        uc.setDoInput(true);
	        uc.setDoOutput(true);
	        uc.setRequestMethod("POST");
	        String data=generateHttpPostData(ctx);
	        Log.d(TAG,"upload data is => "+data);
	        uc.getOutputStream().write(data.getBytes("UTF-8")); 
	        uc.getOutputStream().close();
	        if (uc.getResponseCode()==HttpURLConnection.HTTP_CREATED){
	        	resetCollectedData(ctx);
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
	public static String generateHttpPostData(Context ctx){
		StringBuilder sb=new StringBuilder();
		sb.append("uid="+getDeviceId(ctx));
		DateFormat df=new SimpleDateFormat("yyyyMMdd");
		Date d=new Date(Settings.getLongPreferenceValue(ctx, Settings.PRE_COLLECTION_STARTED_AT, 0));
		sb.append("&from="+df.format(d));
		sb.append("&dev[os][name]="+"android");
		sb.append("&dev[os][codename]="+Build.VERSION.CODENAME);
		sb.append("&dev[os][incremental]="+Build.VERSION.INCREMENTAL);
		sb.append("&dev[os][release]="+Build.VERSION.RELEASE);
		sb.append("&dev[os][sdk]="+String.valueOf(Build.VERSION.SDK_INT));
		sb.append("&dev[model]="+Build.MODEL);
		sb.append("&dev[device]="+Build.DEVICE);
		//sb.append("&dev[device]="+Build.DEVICE);
		//DisplayMetrics displaymetrics = new DisplayMetrics();
		sb.append("&usage="+Settings.getStringPreferenceValue(ctx, Settings.PRE_USAGE, ""));
		sb.append("&hour="+Settings.getStringPreferenceValue(ctx, Settings.PRE_HOUR_PREFER_USAGE, ""));
		return sb.toString();
	}
	
	public static char numberToChar(int value){
		if (value>9){
			return (char)(value-9+64);
		}else{
			return Character.forDigit(value, 10);
		}
	}
	
	public static String getDeviceId(Context ctx){
		final TelephonyManager tm = (TelephonyManager)ctx.getSystemService(Context.TELEPHONY_SERVICE);
	    String tmDevice, tmSerial, tmPhone, androidId;
	    tmDevice = "" + tm.getDeviceId();
	    tmSerial = "" + tm.getSimSerialNumber();
	    androidId = "" + android.provider.Settings.Secure.getString(ctx.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
	    UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
	    //String deviceId = deviceUuid.toString();
	    return deviceUuid.toString();
	}
}
