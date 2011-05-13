package cn.zadui.reader.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import cn.zadui.reader.R;

public class Archive extends Activity {

	WebView web;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.archive);
		
		web=(WebView)findViewById(R.id.webview);
		String path=getIntent().getExtras().getString("path");
		web.loadUrl("file://"+path);
		//Intent i=this.getIntent();
		//String archiveAddress=getIntent().gets
		//web.l

	}

}
