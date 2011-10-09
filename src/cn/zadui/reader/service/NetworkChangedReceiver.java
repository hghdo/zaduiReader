package cn.zadui.reader.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import cn.zadui.reader.helper.Settings;

public class NetworkChangedReceiver extends BroadcastReceiver {
	
	static final String TAG="NetworkChangedReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		
		if (activeNetInfo != null && activeNetInfo.getState()==NetworkInfo.State.CONNECTED) {
			if(activeNetInfo.getType()==ConnectivityManager.TYPE_WIFI){
				Log.d(TAG,"wifi connected!");
				startDownloadService(context);				
			}else if(activeNetInfo.getType()==ConnectivityManager.TYPE_MOBILE && !Settings.getBooleanPreferenceValue(context, Settings.PRE_WIFI_ONLY, Settings.DEF_WIFI_ONLY)){
				Log.d(TAG,"Mobile Network connected!");
				startDownloadService(context);					
			}else{
				//do nothing
			}
		}
	}

	private void startDownloadService(Context context) {
		Intent downIntent=new Intent(context,DownloadService.class);
		downIntent.putExtra(DownloadService.TRIGGER, "NetworkChangedReceiver");
		context.startService(downIntent);
	}

}
