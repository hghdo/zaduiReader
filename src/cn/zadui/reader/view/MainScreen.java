package cn.zadui.reader.view;

import java.io.File;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import cn.zadui.reader.R;
import cn.zadui.reader.helper.ImageHelper;
import cn.zadui.reader.helper.RssHelper;
import cn.zadui.reader.provider.ReaderArchive.Archives;
import cn.zadui.reader.service.DownloadService;
import cn.zadui.reader.service.DownloadService.ServiceState;


public class MainScreen extends ListActivity implements View.OnClickListener,DownloadService.StateListener{
	
	static final String TAG="MainScreen";
	
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
    
    SimpleCursorAdapter adapter;
    Button btnRefresh;
    Cursor cursor;
	//ImageView thumb;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setProgressBarIndeterminateVisibility(DownloadService.isRunning);
        
		//downloadArchiveRSS();
        
        setContentView(R.layout.main);
        btnRefresh=(Button)this.findViewById(R.id.btn_refresh);
        btnRefresh.setOnClickListener(this);
        // If no data was given in the intent (because we were started
        // as a MAIN activity), then use our default content provider.
        Intent intent = getIntent();
        if (intent.getData() == null) {
            intent.setData(Archives.CONTENT_URI);
        }
        
        // Perform a managed query. The Activity will handle closing and requerying the cursor
        // when needed.
        cursor = managedQuery(Archives.CONTENT_URI, PROJECTION, null, null,
                Archives.DEFAULT_SORT_ORDER);

        // Used to map notes entries from the database to views
        adapter = new SimpleCursorAdapter(this, R.layout.archives_item, cursor,
                new String[] { Archives.TITLE,Archives.DESC,Archives.THUMB_URL }, new int[] { R.id.tv_title,R.id.tv_desc,R.id.thumb });
        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			
			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				if(columnIndex==cursor.getColumnIndex(Archives.THUMB_URL)){
					File imgDir=new File(RssHelper.getArchivesDirInSdcard().getAbsolutePath(),cursor.getString(cursor.getColumnIndex(Archives.GUID)));
					ImageView v=(ImageView)view;
					Bitmap img=BitmapFactory.decodeFile(imgDir+"/thumb96");
					v.setImageBitmap(ImageHelper.getRoundedCornerBitmap(img,5));
					return true;
				}
				return false;
			}
		});
        
        setListAdapter(adapter);  
    }

	@Override
	public void onClick(View v) {
		DownloadService.listener=this;
		startService(new Intent(this,DownloadService.class));
	}
	

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		File f=new File(RssHelper.getArchiveDir(id),String.valueOf(id)+".html");
		Log.d(TAG,"Clicked file is =>"+f.getAbsolutePath());
		Intent i=new Intent();
		i.setClass(this, Archive.class);
		i.putExtra("path", f.getAbsolutePath());
		//i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(i);
	}
	

	@Override
	public void onStateChanged(ServiceState state, String info) {
		this.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				setProgressBarIndeterminateVisibility(DownloadService.isRunning);
				//if(state==DownloadService.ServiceState.SUCCESSED) adapter.notifyDataSetInvalidated();
			}
			
		});
		if(state==DownloadService.ServiceState.SUCCESSED)
			this.runOnUiThread(new Runnable(){
				@Override
				public void run(){
					adapter.notifyDataSetInvalidated();
				}
			});
	}
	
    
}