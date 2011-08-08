package cn.zadui.reader.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Settings {
	
	public static final String PRE_LAST_SYNC="last_sync_at";
	
	public static final String PRE_SYNC_INTERVAL="sync_interval";
	
	public static final String PRE_LAST_FEED_PUB_DATE="last_feed_pub_date";
	
	public static final String PRE_MAX_ARCHIVE_LIST_SIZE="max_archive_list_size";
	
	
	public static int getMaxArchiveListSize(Context ctx){
		return getSharedPreferences(ctx).getInt(PRE_MAX_ARCHIVE_LIST_SIZE, 5);
	}
	
	public static void updateMaxArchiveListSize(Context ctx,int size){
		SharedPreferences spSettings=getSharedPreferences(ctx);//.getSharedPreferences(PrefStore.PREFS_NAME, 0);
		SharedPreferences.Editor editor = spSettings.edit();
		editor.putLong(PRE_MAX_ARCHIVE_LIST_SIZE, size);
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
