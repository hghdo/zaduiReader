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
import android.os.IBinder;
import android.util.Log;
import cn.zadui.reader.helper.NetworkHelper;
import cn.zadui.reader.helper.RssHelper;
import cn.zadui.reader.provider.ReaderArchive.Archives;

public class DownloadService extends Service {

	public static final String FEED_URL="http://172.29.1.67:3389/archives/feed.xml";
	//public static final String FEED_URL="http://192.168.1.104:3000/archives/feed.xml";
	
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
		if(isRunning) return;
		(new DownloadThread()).start();
	}
    
	public enum ServiceState {
		DOWNLOADING, SUCCESSED, ERROR,STOP;
    }	
	
	public interface StateListener{
		public void onStateChanged(ServiceState state,String info);
	}
	
	/**
	 * TODO Optimize the logic.
	 * @author david
	 *
	 */
	private class DownloadThread extends Thread{
		@Override
		public void run(){
			isRunning=true;
	    	if (listener!=null)	listener.onStateChanged(ServiceState.DOWNLOADING,"");
			RSSReader reader = new RSSReader();
			RSSFeed feed;
			byte[] buffer=new byte[8*1024];		
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
					ContentValues cv=RssHelper.feedItemToContentValues(item);					
					if(handleZipPkg(item,buffer)){
						cv.put(Archives.CAHECED, true);
						//cv.put(Archives.THUMB_URL, "");
					}else{
						// download thumb image
						String localThumb=downloadThumbnail(item,buffer);
						if (localThumb!=null) cv.put(Archives.THUMB_URL, localThumb);
					}
					Uri mUri = DownloadService.this.getContentResolver().insert(Archives.CONTENT_URI, cv);	
					Log.d(TAG,"Get a new archive");
				}
				if(listener!=null) listener.onStateChanged(ServiceState.SUCCESSED,"");
			} catch (RSSReaderException e) {
				if(listener!=null) listener.onStateChanged(ServiceState.ERROR,e.getMessage());
				e.printStackTrace();
			} catch(Exception ce){
				if(listener!=null) listener.onStateChanged(ServiceState.ERROR,ce.getMessage());
				ce.printStackTrace();
			}
			
			//delete old items
			Cursor oldItems=DownloadService.this.getContentResolver().query(Archives.OLD_ARCHIVES_URI, PROJECTION, null, null,
	                Archives.DEFAULT_SORT_ORDER);
			if(oldItems!=null){
				while(oldItems.moveToNext()){
					long guid=oldItems.getLong(oldItems.getColumnIndex(Archives.GUID));
					// Delete item in db
					DownloadService.this.getContentResolver().delete(ContentUris.withAppendedId(Archives.ARCHIVE_GUID_URI,guid),null,null);
					// Delete folder in sdcard
					RssHelper.deleteDirectory(RssHelper.getArchiveDir(guid));
				}
				oldItems.close();
			}
			
			isRunning=false;
			if(listener!=null) listener.onStateChanged(ServiceState.STOP,"");
			listener=null;
			DownloadService.this.stopSelf();
		}
	}
	
	/**
	 * Download zip pkg from remote server and unzip.
	 * @param item
	 * @param buffer
	 * @return true if download and unzip successfully or false if failed
	 * @throws InterruptedException 
	 */
	private boolean handleZipPkg(RSSItem item,byte[] buffer){
		if (!RssHelper.isSdcardWritable()) return false;
		// Download the pkg.zip and extract it.
		int len=0;
		long st=System.currentTimeMillis();
		Log.d(TAG,"Begin download zip file==>"+String.valueOf(st));
		String zipFileName=item.getGuid()+".pkg.zip";
		File targetZip=new File(RssHelper.getArchivesDirInSdcard(),zipFileName);
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
			Log.e(TAG,"Downloa zip file error");
			e.printStackTrace();
			return false;
		}
		long a=(System.currentTimeMillis()-st)/1000;
		Log.d(TAG,"Download takes "+String.valueOf(a)+" secondes");
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
				BufferedInputStream bis=new BufferedInputStream(zip.getInputStream(entry),8*1024);
				File img=new File(RssHelper.getArchivesDirInSdcard(),entry.getName());
				Log.d(TAG,"Unzip file => "+img.getPath());
				File parent=img.getParentFile();
				if(parent!=null && !parent.exists()) parent.mkdirs();
				BufferedOutputStream bos=new BufferedOutputStream(new FileOutputStream(img),8*1024);
				len=0;
				while((len=bis.read(buffer))>0) bos.write(buffer, 0, len);
				bos.flush();
				bos.close();
				bis.close();
				Thread.sleep(30);
			}
			zip.close();
		} catch (ZipException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private String downloadThumbnail(RSSItem item,byte[] buffer){
		File thumb=new File(new File(RssHelper.getArchivesDirInSdcard(),String.valueOf(item.getGuid())),"thumb96");
		InputStream in=null;
		FileOutputStream out=null;
		try {
			URLConnection con=NetworkHelper.buildUrlConnection(item.getThumbUrl());
			con.connect();
			in=con.getInputStream();
			out=new FileOutputStream(thumb);
			int len=0;
			while((len=in.read(buffer))>0){
				out.write(buffer,0,len);
			}
		} catch (IOException e) {
			Log.e(TAG,"Downloa thumb error");
			e.printStackTrace();
			return null;
		} finally {
			try {
				out.close();
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return thumb.getAbsolutePath();
	}
	
	static final String[] PROJECTION={Archives._ID,Archives.GUID};
    
	private static final String TAG="DownloadService";

}
