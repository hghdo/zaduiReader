package cn.zadui.reader.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Settings {
	
	public static final String PRE_LAST_SYNC="last_sync_at";
	
	public static final String PRE_SYNC_INTERVAL="sync_interval";
	
	public static final String PRE_LAST_FEED_PUB_DATE="last_feed_pub_date";
	
	public static final String PRE_MAX_ARCHIVE_LIST_SIZE="max_archive_list_size";
	
	public static final String PRE_LAST_OPENED_AT="last_open_at";
	
	public static final String PRE_COLLECTION_STARTED_AT="collection_started_at";
	
	public static final String PRE_USAGE="usage";
	
	public static final String PRE_HOUR_PREFER_USAGE="hour_prefer_usage";
	
	public static final String PRE_INSTALLED_AT="installed_at";
	
	public static final String PRE_HAS_NEW_VERSION="new_version_available";
	
	
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
		return getSharedPreferences(ctx).getInt(PRE_MAX_ARCHIVE_LIST_SIZE, 5);
	}
	
	public static void updateMaxArchiveListSize(Context ctx,int size){
		SharedPreferences spSettings=getSharedPreferences(ctx);//.getSharedPreferences(PrefStore.PREFS_NAME, 0);
		SharedPreferences.Editor editor = spSettings.edit();
		editor.putInt(PRE_MAX_ARCHIVE_LIST_SIZE, size);
		editor.commit();
	}
	
	public static long getSyncIntervalInMin(Context ctx){
		return getSharedPreferences(ctx).getLong(PRE_SYNC_INTERVAL, 2*60);
	}

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
	
    public static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

}
