package cn.zadui.reader.view;

import java.io.File;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import cn.zadui.reader.R;
import cn.zadui.reader.helper.StorageHelper;
import cn.zadui.reader.provider.ReaderArchive.Archives;

public class Archive extends Activity implements View.OnClickListener {

	private static final String TAG="Archive";
	
	String loadUrl;
	WebView web;
	StorageHelper sh;
	TextView title;
	ImageView btnHome;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);   
		setContentView(R.layout.archive);
		View v=findViewById(R.id.right_top_coner);
		v.setVisibility(View.GONE);
		title=(TextView)this.findViewById(R.id.tv_archive_title);
		btnHome=(ImageView)findViewById(R.id.btn_left_top);
		btnHome.setImageResource(R.drawable.img_home);
		btnHome.setOnClickListener(this);
		sh=new StorageHelper(getPackageName());
		web=(WebView)findViewById(R.id.webview);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG,"In onStart");
		Log.d(TAG,"webview original url=> "+web.getOriginalUrl());
		long id=getIntent().getExtras().getLong("id");
		File f=new File(sh.getArchiveDir(id),String.valueOf(id)+".html");
		Log.d(TAG,"Archive path is =>"+f.getAbsolutePath());
		String url="file://"+f.getAbsolutePath();
		title.setText(getIntent().getExtras().getString("title"));
		if(loadUrl==null || !loadUrl.equals(url)){
			loadUrl=url;
			web.loadUrl(loadUrl);
			// try to update file read status
			ContentValues cv=new ContentValues();
			cv.put(Archives.READED, true);
			getContentResolver().update(ContentUris.withAppendedId(Archives.CONTENT_URI,id), cv, null, null);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		sh=null;
		web.destroy();
		Log.d(TAG,"Archive method onDestroy called");
	}

	@Override
	public void onClick(View v) {
		if (v.getId()==btnHome.getId()){
			finish();
		}
		
	}
}
