package cn.zadui.reader.service;

import java.util.Iterator;

import org.mcsoxford.rss.RSSFeed;
import org.mcsoxford.rss.RSSItem;
import org.mcsoxford.rss.RSSReader;
import org.mcsoxford.rss.RSSReaderException;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import cn.zadui.reader.provider.ReaderArchive.Archives;

public class DownloadService extends Service {

	public static final String FEED_URL="http://172.29.1.67:3000/archives/rss.xml";
	
	public static StateListener listener;
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}
	
    private void downloadArchiveFeeds(){
    	if (listener!=null)	listener.onStateChanged(ServiceState.DOWNLOADING,"");
		RSSReader reader = new RSSReader();
		RSSFeed feed;
		try {
			feed = reader.load(FEED_URL);
			//Save feed item into DB
			ContentValues cv=new ContentValues();
			cv.put(Archives.GUID, feed)
			
			Log.d(TAG, "AAAAAAAAAAAAAAAAAAAAAAAA"+feed.getTitle());
			Uri mUri = getContentResolver().insert(Archives.CONTENT_URI, null);		
			
			
			
			
			for (Iterator<RSSItem> iter = feed.getItems().iterator(); iter.hasNext();) {
				RSSItem item = iter.next();
				Log.d(TAG, item.getTitle());
			}
		} catch (RSSReaderException e) {
			if(listener!=null) listener.onStateChanged(ServiceState.ERROR,e.getMessage());
			e.printStackTrace();
		}

    }
    
	public enum ServiceState {
		DOWNLOADING, SUCCESSED, ERROR;
    }	
	
	public interface StateListener{
		public void onStateChanged(ServiceState state,String info);
	}
    
	private static final String TAG="DownloadService";

}
