package cn.zadui.reader.service;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Application;
import cn.zadui.reader.helper.Settings;

public class CustomApplication extends Application {
	
	static final String TAG="zaduiReader-CustomApplication";

	@Override
	public void onCreate() {
		super.onCreate();
		// If is the first launch then initialize collection data
		long ms=Settings.getLongPreferenceValue(this, Settings.PRE_INSTALLED_AT, 0);
		if (ms==0){
			long now=System.currentTimeMillis();
			Settings.updateLongPreferenceValue(this, Settings.PRE_INSTALLED_AT, now);
			Settings.updateLongPreferenceValue(this, Settings.PRE_COLLECTION_STARTED_AT, now);
			Settings.updateLongPreferenceValue(this, Settings.PRE_LAST_OPENED_AT, now);
			Settings.updateStringPreferenceValue(this, Settings.PRE_USAGE, "1");
			Calendar cal=new GregorianCalendar();
			cal.setTimeInMillis(now);			
			Settings.updateStringPreferenceValue(this, 
					Settings.PRE_HOUR_PREFER_USAGE, 
					UsageCollector.updateHourPreferUsageString(cal,UsageCollector.HOUR_PREFER_STR)
					);
		}
	}
}
