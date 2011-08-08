package cn.zadui.reader.helper;

import java.io.File;

import org.mcsoxford.rss.RSSItem;

import android.content.ContentValues;
import android.os.Environment;
import android.util.Log;
import cn.zadui.reader.provider.ReaderArchive.Archives;

public class RssHelper {
	
	public static final String TAG="RssHelper";
	
	public static ContentValues feedItemToContentValues(RSSItem item){
		ContentValues cv=new ContentValues();
		cv.put(Archives._ID, Long.valueOf(item.getGuid()));
		cv.put(Archives.GUID, item.getGuid());
		cv.put(Archives.TITLE, item.getTitle());
		cv.put(Archives.DESC, item.getDescription());
		cv.put(Archives.LINK, item.getLink().toString());
		cv.put(Archives.THUMB_URL, item.getThumbUrl());
		cv.put(Archives.PUB_DATE, item.getPubDate().getTime());
		Log.d(TAG,item.getGuid()+"|"+item.getTitle()+"|"+item.getDescription());
		return cv;
	}
	
	public static File getArchiveDir(long archiveGuid){
		return new File(getArchivesDirInSdcard(),String.valueOf(archiveGuid));
	}
	
	public static File getAppDirInSdcard(){
		File sdcard=Environment.getExternalStorageDirectory();
		File zaduiHome=new File(sdcard,"zaduiReader");
		if(!zaduiHome.exists())zaduiHome.mkdirs();
		return zaduiHome;
	}
	
	public static File getArchivesDirInSdcard(){
		File adir=new File(getAppDirInSdcard(),"archives");
		adir.mkdirs();
		return adir;
	}
	
	public static boolean isSdcardWritable(){
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
		return (mExternalStorageAvailable && mExternalStorageWriteable);		
	}
	
	public static boolean deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}	
	
}
