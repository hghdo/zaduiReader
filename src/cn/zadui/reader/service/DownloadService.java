package cn.zadui.reader.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.mcsoxford.rss.RSSFeed;
import org.mcsoxford.rss.RSSItem;
import org.mcsoxford.rss.RSSReader;
import org.mcsoxford.rss.RSSReaderException;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import cn.zadui.reader.helper.NetHelper;
import cn.zadui.reader.helper.RssHelper;
import cn.zadui.reader.helper.Settings;
import cn.zadui.reader.helper.StorageHelper;
import cn.zadui.reader.provider.ReaderArchive.Archives;

/**
 * 
 * @author David
 * TODO All HTTP connection should support HTTP 302 redirection
 *
 */
public class DownloadService extends Service {
	
	public static StateListener listener;
	
	public static boolean isRunning=false;
	
	private StorageHelper storageHelper;
	
	@Override
	public IBinder onBind(Intent arg0) {
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
		int netType=NetHelper.currentNetwork(getBaseContext());
		if (netType<0){
			if(listener!=null) listener.onStateChanged(ServiceState.ERROR,"没有网络链接");
			return;			
		}
		storageHelper=new StorageHelper(getPackageName());
		(new DownloadThread(netType)).start();
	}
	
	@Override
	public void onDestroy(){
		storageHelper=null;
		super.onDestroy();
	}
    
	public enum ServiceState {
		WORKING, FINISHED, ERROR, STOP;
    }	
	
	public interface StateListener{
		public void onStateChanged(ServiceState state,String info);
	}
	
	public void checkNewVersion(){
		if (Settings.getBooleanPreferenceValue(this, Settings.PRE_HAS_NEW_VERSION, false)) return;
		String version=NetHelper.getStringFromNetIO(NetHelper.webPath("http", "/version"));
		if (version==null) return;
		try {
			int currentVersion=getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
			if (currentVersion>Integer.parseInt(version)){
				Settings.updateBooleanPreferenceValue(this, Settings.PRE_HAS_NEW_VERSION, true);
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static void updateSyncJob(Context ctx){
		// Initialize background sync task
		if (!Settings.getBooleanPreferenceValue(ctx, Settings.PRE_BACKGROUND_SYNC, true)) return;
		long now=System.currentTimeMillis();
		Calendar cal=new GregorianCalendar();
		cal.setTimeInMillis(now);		
		Intent sync=new Intent(ctx,DownloadService.class);
		PendingIntent.getService(ctx, 0, sync, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarm=(AlarmManager)ctx.getSystemService(ALARM_SERVICE);
		// the unit is minuts
		int interval=Integer.valueOf(Settings.getStringPreferenceValue(ctx, Settings.PRE_SYNC_INTERVAL, Settings.DEF_SYNC_INTERVAL));
		//interval=2;
		cal.add(Calendar.MINUTE, interval);
		alarm.setRepeating(
				AlarmManager.RTC, 
				cal.getTimeInMillis(), 
				interval*60*1000, 
				PendingIntent.getService(ctx, 0, sync, PendingIntent.FLAG_UPDATE_CURRENT)
				);
	}

	
	/**
	 * Download zip pkg from remote server and unzip.
	 * @param item
	 * @param buffer
	 * @return true if download and unzip successfully or false if failed
	 * @throws DownloadException 
	 */
	private boolean handleZipPkg(RSSItem item,byte[] buffer) throws DownloadException{
		if (!StorageHelper.isSdcardWritable()) return false;
		// Download the pkg.zip and extract it.
		int len=0;
		long st=System.currentTimeMillis();
		Log.d(TAG,"Begin download zip file==>"+String.valueOf(st));
		String zipFileName=item.getGuid()+"_"+Settings.getStringPreferenceValue(this, Settings.PRE_IMAGE_QUALITY, Settings.DEF_IMAGE_QUALITY)+".zip";
		File targetZip=new File(storageHelper.getArchivesDirInSdcard(),zipFileName);
		try {
			//Url
			String pkgUrl=item.getZipPkgUrl();
			pkgUrl=pkgUrl.substring(0,pkgUrl.lastIndexOf('/')+1)+zipFileName;
			URLConnection con=NetHelper.buildUrlConnection(item.getZipPkgUrl());
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
			throw new DownloadException(e);
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
					new File(storageHelper.getArchivesDirInSdcard(),entry.getName()).mkdirs();
					continue;
				}
				BufferedInputStream bis=new BufferedInputStream(zip.getInputStream(entry),8*1024);
				File img=new File(storageHelper.getArchivesDirInSdcard(),entry.getName());
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
			// delete target zip file
			targetZip.delete();
		} catch (ZipException e) {
			e.printStackTrace();
			throw new DownloadException(e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new DownloadException(e);
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new DownloadException(e);
		}
		return true;
	}
	
	/**
	 * TODO Remove hard code 'thumb96' here.
	 * @param item
	 * @param buffer
	 * @return
	 * @throws DownloadException 
	 */
	private String downloadThumbnail(RSSItem item,byte[] buffer) throws DownloadException{
		File thumb=new File(new File(storageHelper.getArchivesDirInSdcard(),String.valueOf(item.getGuid())),"thumb96");
		InputStream in=null;
		FileOutputStream out=null;
		try {
			URLConnection con=NetHelper.buildUrlConnection(item.getThumbUrl());
			con.connect();
			in=con.getInputStream();
			out=new FileOutputStream(thumb);
			int len=0;
			while((len=in.read(buffer))>0){
				out.write(buffer,0,len);
			}
			out.close();
			in.close();			
		} catch (IOException e) {
			Log.e(TAG,"Download thumb error");
			e.printStackTrace();
			throw new DownloadException(e);
		} 
		return thumb.getAbsolutePath();
	}
	
	static final String[] PROJECTION={Archives._ID,Archives.GUID};
    
	private static final String TAG="DownloadService";
	
	/**
	 * There are two kinds of download action, one is triggered by user click refresh button
	 * The other is started background. 
	 * @author david
	 *
	 */
	private class DownloadThread extends Thread{
		
		private int networkType;
		
		public DownloadThread(int netType){
			networkType=netType;
		}
		
		@Override
		public void run(){
			Log.i(TAG,"Beggin download process");
			isRunning=true;
	    	if (listener!=null)	listener.onStateChanged(ServiceState.WORKING,"");
	    	
			if (networkType!=ConnectivityManager.TYPE_WIFI && Settings.getBooleanPreferenceValue(DownloadService.this, Settings.PRE_WIFI_ONLY, false)){
				if(listener!=null) listener.onStateChanged(ServiceState.ERROR,"当前网络不是WIFI, 请使用WIFI连接");
//				UsageCollector.uploadCollectedUsageDate(DownloadService.this.getApplicationContext());
//				checkNewVersion();
				listener=null;
				isRunning=false;
				DownloadService.this.stopSelf();
				return;
			}
			
			// check archives
			String feed_url=NetHelper.webPath("http", "/archives/feed.xml");
			RSSReader reader = new RSSReader();
			RSSFeed feed;
			byte[] buffer=new byte[8*1024];		
			try {
				feed = reader.load(feed_url);	
				feed.getPubDate();
				if (feed.getPubDate().toGMTString().equals(Settings.getLastFeedPubDate(DownloadService.this))){
					Log.d(TAG,"SSSSSSSSSSSSSSSSSame feed xml");
					isRunning=false;
					if(listener!=null) listener.onStateChanged(ServiceState.FINISHED,"已经是最新数据");
					listener=null;
					DownloadService.this.stopSelf();
					return;
				}
				// Fetch all GUIDs of archive
		        Cursor cursor = DownloadService.this.getContentResolver().query(Archives.CONTENT_URI, PROJECTION, null, null,
		                Archives.DEFAULT_SORT_ORDER);
		        HashSet<Long> guids=new HashSet<Long>();
		        while(cursor.moveToNext()){
		        	guids.add(cursor.getLong(cursor.getColumnIndex(Archives.GUID)));
		        }
		        cursor.close();
				Log.d(TAG,"Items size is ==> "+String.valueOf(feed.getItems().size()));
				for (Iterator<RSSItem> iter = feed.getItems().iterator(); iter.hasNext();) {
					//Did this item already existed?
					RSSItem item=iter.next();
					Log.d(TAG,"Item in feed ==>"+item.getTitle());
					if (guids.contains(Long.valueOf(item.getGuid()))) continue;
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
				// Update feed time stamp
				Settings.updateLastFeedPubDate(DownloadService.this,feed.getPubDate().toGMTString());
			} catch (RSSReaderException e) {
				if(listener!=null) listener.onStateChanged(ServiceState.ERROR,e.getMessage());
				e.printStackTrace();
			} catch(DownloadException de){
				if(listener!=null) listener.onStateChanged(ServiceState.ERROR,"下载数据内容失败，请检查网络");
				de.printStackTrace();
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
					StorageHelper.deleteDirectory(storageHelper.getArchiveDir(guid));
				}
				oldItems.close();
			}
			// update next sync time
			DownloadService.updateSyncJob(DownloadService.this.getBaseContext());
			Log.i(TAG, "After update sync job");
	    	
			// upload collected data to Server
			UsageCollector.uploadCollectedUsageDate(DownloadService.this.getApplicationContext());
			// Check new version
			checkNewVersion();
			
			isRunning=false;
			if(listener!=null) listener.onStateChanged(ServiceState.FINISHED,"");
			listener=null;
			DownloadService.this.stopSelf();
		}
	}

}
