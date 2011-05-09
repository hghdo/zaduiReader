package cn.zadui.reader.view;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
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
		//downloadArchiveRSS();
        
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
    
}