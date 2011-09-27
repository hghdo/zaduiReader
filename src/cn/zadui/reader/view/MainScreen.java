package cn.zadui.reader.view;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import cn.zadui.reader.R;
import cn.zadui.reader.helper.NetHelper;
import cn.zadui.reader.helper.Settings;
import cn.zadui.reader.helper.StorageHelper;
import cn.zadui.reader.provider.ReaderArchive.Archives;
import cn.zadui.reader.service.DownloadService;
import cn.zadui.reader.service.DownloadService.ServiceState;
import cn.zadui.reader.service.UsageCollector;


public class MainScreen extends ListActivity implements View.OnClickListener,DownloadService.StateListener{
	
	static final String TAG="MainScreen";
	
	static final int DIALOG_NEW_VERSION=10;
	static final int DIALOG_HARD_KILLED=20;
	static final int DIALOG_COMMENT=30;
	static final int DIALOG_ABOUT=40;
	static final int DIALOG_UPDATE=50;
	
	static final int MENU_COMMENT=0;
	static final int MENU_ABOUT=1;
    /**
     * The columns we are interested in from the database
     */
    private static final String[] PROJECTION = new String[] {
            Archives._ID, // 0
            Archives.GUID,
            Archives.TITLE, // 1
            Archives.DESC, // 2
            Archives.THUMB_URL,
            Archives.READED,
            Archives.CAHECED
    };

    /** The index of the title column */
    private static final int COLUMN_INDEX_TITLE = 1;
    
    private static final int NO_NETWORK_AVAILABLE=0;
    private static final int DOWNLOAD_ERROR=1;
    private static final int DOWNLOAD_SUCCESS=2;
	private Handler updateHandler;
    SimpleCursorAdapter adapter;
    ImageView btnRefresh;
    ImageView btnSetting;
    TextView title;
    ProgressBar downProgress;
    Cursor cursor;
    StorageHelper sh;
    TextView tvUserComments;	

    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        title=(TextView)findViewById(R.id.tv_title);
        btnRefresh=(ImageView)this.findViewById(R.id.btn_left_top);
        btnRefresh.setOnClickListener(this);
        btnSetting=(ImageView)this.findViewById(R.id.btn_right_top);
        btnSetting.setOnClickListener(this);
        downProgress=(ProgressBar)findViewById(R.id.pb_download);
        
        // If no data was given in the intent (because we were started
        // as a MAIN activity), then use our default content provider.
        Intent intent = getIntent();
        if (intent.getData() == null) {
            intent.setData(Archives.CONTENT_URI);
        }
        sh=new StorageHelper(getPackageName());
        // Perform a managed query. The Activity will handle closing and requerying the cursor
        // when needed.
        cursor = managedQuery(Archives.CONTENT_URI, PROJECTION, null, null,
                Archives.DEFAULT_SORT_ORDER);

        // Used to map notes entries from the database to views
        adapter = new SimpleCursorAdapter(this, R.layout.archives_item, cursor,
                new String[] { Archives.READED,Archives.TITLE,Archives.DESC,Archives.THUMB_URL }, new int[] { R.id.v_read,R.id.tv_title,R.id.tv_desc,R.id.thumb });
        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			
			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				if(view.getId()==R.id.v_read){
					if (cursor.getInt(columnIndex)==1){
						view.setVisibility(View.INVISIBLE);
					}else{
						view.setVisibility(View.VISIBLE);
					}
					return true;
				}
				if(columnIndex==cursor.getColumnIndex(Archives.THUMB_URL)){
					File imgDir=new File(sh.getArchivesDirInSdcard().getAbsolutePath(),cursor.getString(cursor.getColumnIndex(Archives.GUID)));
					ImageView v=(ImageView)view;
					Bitmap img=BitmapFactory.decodeFile(imgDir+"/thumb96.jpg");
					if (img==null)img=BitmapFactory.decodeResource(getResources(), R.drawable.default_thumb);
					v.setImageBitmap(img);
					return true;
				}
				return false;
			}
		});
        setListAdapter(adapter);  
        
    	updateHandler=new Handler(){
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				dismissDialog(DIALOG_UPDATE);
				switch(msg.what){
				case NO_NETWORK_AVAILABLE:
					Toast.makeText(MainScreen.this, R.string.no_network_available,Toast.LENGTH_SHORT).show();
					break;
				case DOWNLOAD_ERROR:
					Toast.makeText(MainScreen.this, R.string.download_error,Toast.LENGTH_SHORT).show();
					break;
				case DOWNLOAD_SUCCESS:
					File updateAPK=StorageHelper.getUpdateApkPath();
				    Intent intent = new Intent(Intent.ACTION_VIEW);
				    intent.setDataAndType(Uri.fromFile(updateAPK), "application/vnd.android.package-archive");
				    startActivity(intent);
				    break;
				}
			}
    	};

		long ms=Settings.getLongPreferenceValue(this, Settings.PRE_INSTALLED_AT, 0);
		if (ms==0){
			// First time open initial all collection data
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
			btnRefresh.setVisibility(View.GONE);
			downProgress.setVisibility(View.VISIBLE);
			DownloadService.listener=this;
			Intent sync=new Intent(getApplicationContext(),DownloadService.class);
			startService(sync);
		}else{
			UsageCollector.openApp(this.getApplicationContext());
			if (NetHelper.needUpdate(this)) showDialog(DIALOG_NEW_VERSION);
//			try {
//				int currentVersion=getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
//				if (currentVersion>=Settings.getLongPreferenceValue(this, Settings.PRE_LAST_BUILD, 0)){
//					Settings.updateBooleanPreferenceValue(this, Settings.PRE_HAS_NEW_VERSION, false);
//					Settings.updateLongPreferenceValue(this, Settings.PRE_LAST_BUILD, currentVersion);					
//				}else{
//					showDialog(DIALOG_NEW_VERSION);
//				}
//			} catch (NameNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}        
    }

	@Override
	public void onClick(View v) {
		if (v.getId()==btnRefresh.getId()){
			btnRefresh.setVisibility(View.GONE);
			downProgress.setVisibility(View.VISIBLE);
			DownloadService.listener=this;
			startService(new Intent(this,DownloadService.class));
			
		}else if(v.getId()==btnSetting.getId()){
			Log.d(TAG,"app settings=>"+Settings.getBooleanPreferenceValue(this, "background_sync", false));
			Intent i=new Intent();
			i.setClass(getBaseContext(), AppSettings.class);
			startActivity(i);
			//Log.d(TAG,Settings.getStringPreferenceValue(this, Settings.PRE_USAGE, ""));
			//Log.d(TAG,Settings.getStringPreferenceValue(this, Settings.PRE_HOUR_PREFER_USAGE, ""));
			Log.d(TAG,"Upload data String => "+ UsageCollector.generateHttpPostData(this.getBaseContext()));
			
		}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent i=new Intent();
		i.setClass(this, Archive.class);
		i.putExtra("id", id);
		cursor.moveToPosition(position);
		i.putExtra("title",cursor.getString(cursor.getColumnIndex(Archives.TITLE)));
		//i.putExtra("path", f.getAbsolutePath());
		//i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(i);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_COMMENT, 0, R.string.comments).setIcon(getResources().getDrawable(android.R.drawable.ic_menu_edit));
		menu.add(0, MENU_ABOUT,1,R.string.about).setIcon(getResources().getDrawable(android.R.drawable.ic_menu_info_details));
		return super.onCreateOptionsMenu(menu);
	}
	
	public boolean onOptionsItemSelected(MenuItem item){
		if (item.getItemId()==MENU_COMMENT){
			showDialog(DIALOG_COMMENT);
			return true;
		}else if (item.getItemId()==MENU_ABOUT){
			showDialog(DIALOG_ABOUT);
			return true;
		}
		return false;
	}

	@Override
	public void onStateChanged(final ServiceState state, final String info) {
		this.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				if (state==DownloadService.ServiceState.WORKING){
					btnRefresh.setVisibility(View.GONE);
					downProgress.setVisibility(View.VISIBLE);
				}else if (state==DownloadService.ServiceState.FINISHED){
					adapter.notifyDataSetInvalidated();
					btnRefresh.setVisibility(View.VISIBLE);
					downProgress.setVisibility(View.GONE);
				}else if (state==DownloadService.ServiceState.ERROR){
					btnRefresh.setVisibility(View.VISIBLE);
					downProgress.setVisibility(View.GONE);
					Toast.makeText(MainScreen.this, info,Toast.LENGTH_SHORT).show();					
				}
			}
		});
	}

	/**
	 * If the app was installed by Google market then disable the 
	 * update check
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		LayoutInflater factory;
		switch (id){
		case DIALOG_NEW_VERSION:
			return new AlertDialog.Builder(MainScreen.this)
				.setTitle(R.string.new_version_available_title)
				.setMessage(R.string.new_version_available_text)
				.setPositiveButton(R.string.ok,new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
			            new UpdateApp().start();
			            MainScreen.this.showDialog(DIALOG_UPDATE);			
					}
				})
				.setNegativeButton(R.string.cancel,new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// do nothing here!
					}
				})
				.create();
		case DIALOG_HARD_KILLED:
			return new AlertDialog.Builder(MainScreen.this)
				.setTitle(R.string.hard_kill_title)
				.setMessage(R.string.hard_kill_text)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO If the version is hard killed then user must update to new version.
					}
				}).create();
		case DIALOG_COMMENT:
            factory = LayoutInflater.from(MainScreen.this);
            final View textEntryView = factory.inflate(R.layout.comment_dialog, null);
            tvUserComments=(TextView)textEntryView.findViewById(R.id.tv_user_comments);
            return new AlertDialog.Builder(MainScreen.this)
                .setIcon(android.R.drawable.ic_menu_edit)
                .setTitle(R.string.please_comment)
                .setView(textEntryView)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	String comment=tvUserComments.getText().toString();
                    	if (comment!=null && comment.length()>10){
                    	Settings.updateStringPreferenceValue(MainScreen.this, Settings.PRE_USER_COMMENTS, tvUserComments.getText().toString());
                    	}
                    	tvUserComments.setText("");
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        /* User clicked cancel so do some stuff */
                    }
                })
                .create();
		case DIALOG_ABOUT:
            factory = LayoutInflater.from(MainScreen.this);
            final View aboutView = factory.inflate(R.layout.about_dialog, null);
            TextView tvAbout=(TextView)aboutView.findViewById(R.id.tv_about);
            tvAbout.setText(String.format(getString(R.string.about_text),getString(R.string.app_name)));
            TextView tvCurrentVersion=(TextView)aboutView.findViewById(R.id.tv_current_version);
            PackageInfo pi;
			try {
				pi = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
	            tvCurrentVersion.setText(String.format(getString(R.string.current_version), pi.versionName, pi.versionCode));
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			return new AlertDialog.Builder(MainScreen.this)
			.setTitle(R.string.app_name)
			.setView(aboutView)
			.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			}).create();
		case DIALOG_UPDATE:
			ProgressDialog pd = new ProgressDialog(this);
			pd.setMessage(getString(R.string.downloading_new_version));
            //pd.setIcon(R.drawable.alert_dialog_icon);
            //pd.setTitle(R.string.select_dialog);
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            return pd;
		}
		return null;
	}
	
	private class UpdateApp extends Thread{		
		public void run(){
			int netType=NetHelper.currentNetwork(getBaseContext());
			if (netType<0){
				updateHandler.sendEmptyMessage(NO_NETWORK_AVAILABLE);
				return;
			}
		    File updateAPK=StorageHelper.getUpdateApkPath();
			try {
				URL url = new URL(NetHelper.webPath("http", "/dl/client"));
			    HttpURLConnection c = (HttpURLConnection) url.openConnection();
			    c.setRequestMethod("GET");
			    c.setDoOutput(true);
			    c.connect();

			    FileOutputStream fos = new FileOutputStream(updateAPK);		
			    InputStream is = c.getInputStream();
			
			    byte[] buffer = new byte[1024];
			    int len1 = 0;
			    while ((len1 = is.read(buffer)) != -1) {
			        fos.write(buffer, 0, len1);
			    }
			    fos.close();
			    is.close();//till here, it works fine - .apk is download to my sdcard in download file
			    updateHandler.sendEmptyMessage(DOWNLOAD_SUCCESS);
			} catch (IOException e) {
				updateHandler.sendEmptyMessage(DOWNLOAD_ERROR);
			}
		}  		
	}
    
}