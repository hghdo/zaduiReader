package cn.zadui.reader.view;

import java.util.Iterator;

import org.mcsoxford.rss.RSSFeed;
import org.mcsoxford.rss.RSSItem;
import org.mcsoxford.rss.RSSReader;
import org.mcsoxford.rss.RSSReaderException;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.SimpleCursorAdapter;
import cn.zadui.reader.R;
import cn.zadui.reader.provider.ReaderArchive.Archives;

public class MainScreen extends ListActivity {
	
	static final String TAG="MainScreen";
	
    /**
     * The columns we are interested in from the database
     */
    private static final String[] PROJECTION = new String[] {
            Archives._ID, // 0
            Archives.TITLE, // 1
            Archives.DESC, // 2
    };

    /** The index of the title column */
    private static final int COLUMN_INDEX_TITLE = 1;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
			downloadArchiveRSS();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        setContentView(R.layout.main);
        
        // If no data was given in the intent (because we were started
        // as a MAIN activity), then use our default content provider.
        Intent intent = getIntent();
        if (intent.getData() == null) {
            intent.setData(Archives.CONTENT_URI);
        }
        
        // Perform a managed query. The Activity will handle closing and requerying the cursor
        // when needed.
        Cursor cursor = managedQuery(getIntent().getData(), PROJECTION, null, null,
                Archives.DEFAULT_SORT_ORDER);

        // Used to map notes entries from the database to views
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.archives_item, cursor,
                new String[] { Archives.TITLE,Archives.DESC }, new int[] { R.id.tv_title,R.id.tv_desc });
        setListAdapter(adapter);
             
    }
    
    public void downloadArchiveRSS() throws Exception{
		Log.d(TAG, "DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD");
		RSSReader reader = new RSSReader();
		//String uri = "http://live.ifanr.com/feed";
		String uri="http://172.29.1.67:3000/archives/rss.xml";
		RSSFeed feed = reader.load(uri);
		Log.d(TAG, "AAAAAAAAAAAAAAAAAAAAAAAA"+feed.getTitle());
	
		for (Iterator<RSSItem> iter = feed.getItems().iterator(); iter.hasNext();) {
			RSSItem item = iter.next();
			Log.d(TAG, item.getTitle());
		}

    }
    
}