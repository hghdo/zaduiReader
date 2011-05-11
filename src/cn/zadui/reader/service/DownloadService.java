package cn.zadui.reader.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

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
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import cn.zadui.reader.helper.NetworkHelper;
import cn.zadui.reader.helper.RssHelper;
import cn.zadui.reader.provider.ReaderArchive.Archives;

public class DownloadService extends Service {

	public static final String FEED_URL="http://172.29.1.67:8000/archives/feed.xml";
	
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
			byte[] buffer=new byte[8*1024];
			int len=0;
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
					
					boolean mExternalStorageAvailable = false;
					boolean mExternalStorageWriteable = false;
					String state = Environment.getExternalStorageState();

					if (Environment.MEDIA_MOUNTED.equals(state)) {
					    // We can read and write the media
					    mExternalStorageAvailable = mExternalStorageWriteable = true;
					} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
					    // We can only read the media
					    mExternalStorageAvailable = true;
					    mExternalStorageWriteable = false;
					} else {
					    // Something else is wrong. It may be one of many other states, but all we need
					    //  to know is we can neither read nor write
					    mExternalStorageAvailable = mExternalStorageWriteable = false;
					}	
					Log.d(TAG,"sdcard status is => "+ (mExternalStorageWriteable==true ? "writable" : "ERROR"));
					
					// Download the pkg.zip and extract it.
					Log.d(TAG,"Begin download zip file");
					String zipFileName=item.getGuid()+".pkg.zip";
					File targetZip=new File(RssHelper.getArchivesDirInSdcard(),zipFileName);
					//RssHelper.getArchivesDirInSdcard().mkdirs();
					//File pdir=targetZip.getParentFile();
					//boolean aaa=pdir.mkdirs();
					//Log.d(TAG,"Create dir in sdcard ==> "+ (aaa ? "true" : "false"));
					try {
						URLConnection con=NetworkHelper.buildUrlConnection(item.getZipPkgUrl());
						con.connect();
						FileOutputStream out=new FileOutputStream(targetZip);
						InputStream in=con.getInputStream();
						len=0;
						while((len=in.read(buffer))>0){
							out.write(buffer,0,len);
						}
						out.close();
						in.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						Log.e(TAG,"Downloa zip file error");
						e.printStackTrace();
						continue;
					}
					Log.d(TAG,"Finished download zip file, then unzip it");
					
					// Unzip the downloaded pkg.zip file
					try {
						ZipFile zip=new ZipFile(targetZip);
						Enumeration<?> entries = zip.entries();
						while(entries.hasMoreElements()){
							ZipEntry entry=(ZipEntry)entries.nextElement();
							if(entry.isDirectory()){
								new File(RssHelper.getArchivesDirInSdcard(),entry.getName()).mkdirs();
								continue;
							}
							BufferedInputStream bis=new BufferedInputStream(zip.getInputStream(entry));
							File img=new File(RssHelper.getArchivesDirInSdcard(),entry.getName());
							Log.d(TAG,"Unzip file => "+img.getPath());
							File parent=img.getParentFile();
							if(parent!=null && !parent.exists()) parent.mkdirs();
							BufferedOutputStream bos=new BufferedOutputStream(new FileOutputStream(img),8*1024);
							len=0;
							while((len=bis.read(buffer))>0){
								bos.write(buffer, 0, len);
							}
							bos.flush();
							bos.close();
							bis.close();
						}
						zip.close();
					} catch (ZipException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					//getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder)
					ContentValues cv=RssHelper.feedItemToContentValues(item);
					Uri mUri = DownloadService.this.getContentResolver().insert(Archives.CONTENT_URI, cv);	
					Log.d(TAG,"Get a new archive");
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
