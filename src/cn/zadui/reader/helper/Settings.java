package cn.zadui.reader.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Settings {
	
	public static final String PRE_LAST_SYNC="last_sync_at";
	
	public static final String PRE_SYNC_INTERVAL="sync_interval";
	
	public static final String PRE_LAST_FEED_PUB_DATE="last_feed_pub_date";
	
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
	
    public static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

}
