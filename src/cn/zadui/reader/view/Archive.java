package cn.zadui.reader.view;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import cn.zadui.reader.R;

public class Archive extends Activity {

	private static final String TAG="Archive";
	
	String loadUrl;
	WebView web;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG,"In onCreate");
		setContentView(R.layout.archive);
		web=(WebView)findViewById(R.id.webview);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG,"In onStart");
		Log.d(TAG,"AAAAAAA=>"+web.getOriginalUrl());
		String url="file://"+getIntent().getExtras().getString("path");
		if(loadUrl==null || !loadUrl.equals(url)){
			Log.d(TAG,"RELOAD WEB View !!!!!!!!!!!!!!!!!!!!!!!!!!!");
			loadUrl=url;
			web.loadUrl(loadUrl);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		web.destroy();
	}
}
