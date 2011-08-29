package cn.zadui.reader.helper;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.Log;
import cn.zadui.reader.service.DownloadService;

public class Settings {
	
	public static final String PRE_BACKGROUND_SYNC="background_sync";
	
	public static final String PRE_SYNC_INTERVAL="sync_interval";
	
	public static final String PRE_WIFI_ONLY="wifi_only";
	
	public static final String PRE_LAST_SYNC="last_sync_at";
	
	public static final String PRE_LAST_FEED_PUB_DATE="last_feed_pub_date";
	
	public static final String PRE_MAX_ARCHIVES="max_archives";
	
	public static final String PRE_LAST_OPENED_AT="last_open_at";
	
	public static final String PRE_COLLECTION_STARTED_AT="collection_started_at";
	
	public static final String PRE_USAGE="usage";
	
	public static final String PRE_HOUR_PREFER_USAGE="hour_prefer_usage";
	
	public static final String PRE_INSTALLED_AT="installed_at";
	
	public static final String PRE_HAS_NEW_VERSION="new_version_available";
	
	public static final String PRE_HARD_KILLED="hard_killed";
	
	public static final String PRE_IMAGE_QUALITY="image_quality";
	
	public static final String DEF_SYNC_INTERVAL="5";
	public static final boolean DEF_WIFI_ONLY=true;
	public static final String DEF_IMAGE_QUALITY="m";
	public static final String DEF_MAX_ARCHIVES="8";
	
	
	public static long getLongPreferenceValue(Context ctx,String preName,long defaultValue){
		return getSharedPreferences(ctx).getLong(preName, defaultValue);
	}
	
	public static void updateLongPreferenceValue(Context ctx,String preName,long value){
		SharedPreferences spSettings=getSharedPreferences(ctx);
		SharedPreferences.Editor editor = spSettings.edit();
		editor.putLong(preName, value);
		editor.commit();		
	}
	
	public static String getStringPreferenceValue(Context ctx,String preName,String defaultValue){
		return getSharedPreferences(ctx).getString(preName, defaultValue);
	}
	
	public static void updateStringPreferenceValue(Context ctx,String preName,String value){
		SharedPreferences spSettings=getSharedPreferences(ctx);
		SharedPreferences.Editor editor = spSettings.edit();
		editor.putString(preName, value);
		editor.commit();		
	}

	public static boolean getBooleanPreferenceValue(Context ctx,String preName,boolean defaultValue){
		return getSharedPreferences(ctx).getBoolean(preName, defaultValue);
	}
	
	public static void updateBooleanPreferenceValue(Context ctx,String preName,boolean value){
		SharedPreferences spSettings=getSharedPreferences(ctx);
		SharedPreferences.Editor editor = spSettings.edit();
		editor.putBoolean(preName, value);
		editor.commit();		
	}
	
	
	/*
	public static long getLastOpenedAt(Context ctx){
		return getSharedPreferences(ctx).getLong(PRE_LAST_OPENED_AT, 0);
	}
	
	public static void updateLastOpenedAt(Context ctx,long timeInMilesecond){
		SharedPreferences spSettings=getSharedPreferences(ctx);
		SharedPreferences.Editor editor = spSettings.edit();
		editor.putLong(PRE_LAST_OPENED_AT, timeInMilesecond);
		editor.commit();
	}	
	
	public static long getCollectionStartedAt(Context ctx){
		return getSharedPreferences(ctx).getLong(PRE_COLLECTION_STARTED_AT, 0);
	}
	
	public static void updateCollectionStartedAt(Context ctx,long timeInMilesecond){
		SharedPreferences spSettings=getSharedPreferences(ctx);
		SharedPreferences.Editor editor = spSettings.edit();
		editor.putLong(PRE_COLLECTION_STARTED_AT, timeInMilesecond);
		editor.commit();
	}	
	*/
	
	public static int getMaxArchiveListSize(Context ctx){
		String str=getSharedPreferences(ctx).getString(PRE_MAX_ARCHIVES, DEF_MAX_ARCHIVES);
		return Integer.valueOf(str);
	}
	
	public static void updateMaxArchiveListSize(Context ctx,int size){
		SharedPreferences spSettings=getSharedPreferences(ctx);//.getSharedPreferences(PrefStore.PREFS_NAME, 0);
		SharedPreferences.Editor editor = spSettings.edit();
		editor.putInt(PRE_MAX_ARCHIVES, size);
		editor.commit();
	}
//	
//	public static long getSyncIntervalInMin(Context ctx){
//		return getSharedPreferences(ctx).getLong(PRE_SYNC_INTERVAL, 2*60);
//	}

	public static long getLastSync(Context ctx){
		return getSharedPreferences(ctx).getLong(PRE_LAST_SYNC, 0);
	}
	
	public void updateLastSync(Context ctx,long timeInMillis){
		SharedPreferences spSettings=getSharedPreferences(ctx);//.getSharedPreferences(PrefStore.PREFS_NAME, 0);
		SharedPreferences.Editor editor = spSettings.edit();
		editor.putLong(PRE_LAST_SYNC, timeInMillis);
		editor.commit();
	}
	
	public static String getLastFeedPubDate(Context ctx){
		return getSharedPreferences(ctx).getString(PRE_LAST_FEED_PUB_DATE, "");		
	}
	
	public static void updateLastFeedPubDate(Context ctx,String pubDate){
		SharedPreferences spSettings=getSharedPreferences(ctx);//.getSharedPreferences(PrefStore.PREFS_NAME, 0);
		SharedPreferences.Editor editor = spSettings.edit();
		editor.putString(PRE_LAST_FEED_PUB_DATE, pubDate);
		editor.commit();
	}
	
	public static boolean hardKilled(Context ctx){
		return getSharedPreferences(ctx).getBoolean(PRE_HARD_KILLED, false);
	}
	
    public static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }
    
	public static void updateSyncJob(Context ctx){
		// Initialize background sync task
		if (!Settings.getBooleanPreferenceValue(ctx, Settings.PRE_BACKGROUND_SYNC, true)) return;
		long now=System.currentTimeMillis();
		Calendar cal=new GregorianCalendar();
		cal.setTimeInMillis(now);		
		Intent sync=new Intent(ctx,DownloadService.class);
		PendingIntent.getService(ctx, 0, sync, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarm=(AlarmManager)ctx.getSystemService(Context.ALARM_SERVICE);
		// the unit is minuts
		int interval=Integer.valueOf(Settings.getStringPreferenceValue(ctx, Settings.PRE_SYNC_INTERVAL, Settings.DEF_SYNC_INTERVAL));
		//interval=2;
		cal.add(Calendar.MINUTE, interval);
		alarm.setRepeating(
				AlarmManager.RTC, 
				cal.getTimeInMillis(), 
				interval*60*1000, 
				PendingIntent.getService(ctx, 0, sync, PendingIntent.FLAG_UPDATE_CURRENT)
				);
	}
	
	public static boolean installedFromGoogleMarket(Context ctx){
		PackageManager pm=ctx.getPackageManager();
		String installer=pm.getInstallerPackageName(ctx.getPackageName());
		return (installer!=null && installer.equals("com.google.android.feedback"));
		
//		List<ApplicationInfo> list=pm.getInstalledApplications(0);
//		for(Iterator<ApplicationInfo> iter=list.iterator();iter.hasNext();){
//			ApplicationInfo ai=iter.next();
//			Log.d("DDDDDDDDDDDD",ai.packageName+"-"+pm.getInstallerPackageName(ai.packageName));
//		}
//		return false;
	}
    

}
