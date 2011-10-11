package cn.zadui.reader.view;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import cn.zadui.reader.R;
import cn.zadui.reader.helper.Settings;

public class AppSettings extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.addPreferencesFromResource(R.xml.preferences);
		
//		Preference interval=this.getPreferenceScreen().getPreference(1);
//		String sv=Settings.getStringPreferenceValue(this, Settings.PRE_SYNC_INTERVAL, Settings.DEF_SYNC_INTERVAL);
//		String[] values=this.getResources().getStringArray(R.array.sync_interval_values);
//		int i=0;
//		for(i=0;i<values.length;i++) if (values[i].equals(sv)) break;
//		interval.setSummary(getResources().getStringArray(R.array.sync_interval_items)[i]);
		int i=0;
		Preference wifi=this.getPreferenceScreen().getPreference(1);
		wifi.setSummary(Settings.getBooleanPreferenceValue(this, Settings.PRE_WIFI_ONLY, Settings.DEF_WIFI_ONLY) ? 
				getResources().getString(R.string.wifi_only_enabled_summ) : 
					getResources().getString(R.string.wifi_only_disabled_summ));
		
		String[] values=this.getResources().getStringArray(R.array.image_quality_values);
		String sv=Settings.getStringPreferenceValue(this, Settings.PRE_IMAGE_QUALITY, Settings.DEF_IMAGE_QUALITY);
		Preference quality=this.getPreferenceScreen().getPreference(2);
		for(i=0;i<values.length;i++) if (values[i].equals(sv)) break;
		quality.setSummary(getResources().getStringArray(R.array.image_quality_items)[i]);
		
		values=this.getResources().getStringArray(R.array.max_archives_values);
		sv=Settings.getStringPreferenceValue(this, Settings.PRE_MAX_ARCHIVES, Settings.DEF_MAX_ARCHIVES);
		Preference maxArchives=this.getPreferenceScreen().getPreference(3);
		for(i=0;i<values.length;i++) if (values[i].equals(sv)) break;
		maxArchives.setSummary(getResources().getStringArray(R.array.max_archives)[i]);
	}
	
	static final String TAG="AppSettings";

	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		
	}
	
	@Override
	protected void onPause(){
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,String key) {
//		if (key.equals(Settings.PRE_SYNC_INTERVAL)){
//			Preference interval=this.getPreferenceScreen().getPreference(1);
//			String sv=Settings.getStringPreferenceValue(this, Settings.PRE_SYNC_INTERVAL, Settings.DEF_SYNC_INTERVAL);
//			String[] values=this.getResources().getStringArray(R.array.sync_interval_values);
//			int i=0;
//			for(;i<values.length;i++) if (values[i].equals(sv)) break;
//			interval.setSummary(getResources().getStringArray(R.array.sync_interval_items)[i]);			
//		}else 
		if(key.equals(Settings.PRE_WIFI_ONLY)){
			Preference wifi=this.getPreferenceScreen().getPreference(1);
			wifi.setSummary(Settings.getBooleanPreferenceValue(this, key, false) ? 
				getResources().getString(R.string.wifi_only_enabled_summ) : 
				getResources().getString(R.string.wifi_only_disabled_summ));
		}else if(key.equals(Settings.PRE_IMAGE_QUALITY)){
			Preference quality=this.getPreferenceScreen().getPreference(2);
			String sv=Settings.getStringPreferenceValue(this, Settings.PRE_IMAGE_QUALITY, Settings.DEF_IMAGE_QUALITY);
			String[] values=this.getResources().getStringArray(R.array.image_quality_values);
			int i=0;
			for(;i<values.length;i++) if (values[i].equals(sv)) break;
			quality.setSummary(getResources().getStringArray(R.array.image_quality_items)[i]);
		}else if(key.equals(Settings.PRE_MAX_ARCHIVES)){
			Preference maxArchives=this.getPreferenceScreen().getPreference(3);
			String sv=Settings.getStringPreferenceValue(this, Settings.PRE_MAX_ARCHIVES, Settings.DEF_MAX_ARCHIVES);
			String[] values=this.getResources().getStringArray(R.array.max_archives_values);
			int i=0;
			for(;i<values.length;i++) if (values[i].equals(sv)) break;
			maxArchives.setSummary(getResources().getStringArray(R.array.max_archives)[i]);
		}
	}
}
