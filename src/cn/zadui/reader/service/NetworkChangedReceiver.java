package cn.zadui.reader.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class NetworkChangedReceiver extends BroadcastReceiver {
	
	static final String TAG="NetworkChangedReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();

		if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
			NetworkInfo info = (NetworkInfo) intent
					.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
			if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
				// Sync archive from remote server!
				Log.d(TAG,"wifi connected!");
				context.startService(new Intent(context,DownloadService.class));
			}
		}
	}

}
