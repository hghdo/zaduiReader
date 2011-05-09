package cn.zadui.reader.service;

import java.util.Iterator;

import org.mcsoxford.rss.RSSFeed;
import org.mcsoxford.rss.RSSItem;
import org.mcsoxford.rss.RSSReader;
import org.mcsoxford.rss.RSSReaderException;

import android.app.Service;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import cn.zadui.reader.helper.RssHelper;
import cn.zadui.reader.provider.ReaderArchive.Archives;

public class DownloadService extends Service {

	public static final String FEED_URL="http://172.29.1.67:3000/archives/feed.xml";
	
	public static StateListener listener;
	
	public static boolean isRunning=false;
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		handleCommand(intent);
		return START_STICKY;
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
	    handleCommand(intent);
	}
	
	private void handleCommand(Intent intent){
		(new DownloadThread()).start();
	}
    
	public enum ServiceState {
		DOWNLOADING, SUCCESSED, ERROR;
    }	
	
	public interface StateListener{
		public void onStateChanged(ServiceState state,String info);
	}
	
	private class DownloadThread extends Thread{
		@Override
		public void run(){
			isRunning=true;
	    	if (listener!=null)	listener.onStateChanged(ServiceState.DOWNLOADING,"");
			RSSReader reader = new RSSReader();
			RSSFeed feed;
			try {
				feed = reader.load(FEED_URL);		
				Log.d(TAG,"Items size is ==> "+String.valueOf(feed.getItems().size()));
				for (Iterator<RSSItem> iter = feed.getItems().iterator(); iter.hasNext();) {
					//Did this item already existed?
					RSSItem item=iter.next();
					Log.d(TAG,"Item in feed ==>"+item.getTitle());
					Cursor cursor=DownloadService.this.getContentResolver().query(
							ContentUris.withAppendedId(Archives.ARCHIVE_GUID_URI,Long.valueOf(item.getGuid())),
							PROJECTION, null, null,
							Archives.DEFAULT_SORT_ORDER);
					boolean existed=(cursor.getCount()>0);
					cursor.close();
					if(existed) continue;
					//getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder)
					ContentValues cv=RssHelper.feedItemToContentValues(item);
					Uri mUri = DownloadService.this.getContentResolver().insert(Archives.CONTENT_URI, cv);	
					Log.d(TAG,"New archive downloaded");
				}
				if(listener!=null) listener.onStateChanged(ServiceState.SUCCESSED,"");
			} catch (RSSReaderException e) {
				if(listener!=null) listener.onStateChanged(ServiceState.ERROR,e.getMessage());
				e.printStackTrace();
			}
			listener=null;
			isRunning=false;
		}
	}
	
	static final String[] PROJECTION={Archives._ID,Archives.GUID};
    
	private static final String TAG="DownloadService";

}
