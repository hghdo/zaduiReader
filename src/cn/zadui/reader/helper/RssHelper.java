package cn.zadui.reader.helper;

import org.mcsoxford.rss.RSSItem;

import android.content.ContentValues;
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
	
}
