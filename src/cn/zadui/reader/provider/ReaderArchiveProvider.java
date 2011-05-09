package cn.zadui.reader.provider;

import java.util.HashMap;

import cn.zadui.reader.provider.ReaderArchive.Archives;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class ReaderArchiveProvider extends ContentProvider {
	
    private static final String TAG = "NotePadProvider";
    private static final String DATABASE_NAME = "reader_archives.db";
    private static final int DATABASE_VERSION = 2;
    private static final String ARCHIVES_TABLE_NAME = "archives";

    private static HashMap<String, String> sArchivesProjectionMap;
    //private static HashMap<String, String> sLiveFolderProjectionMap;

    private static final int ARCHIVES = 1;
    private static final int ARCHIVE_ID = 2;

    private static final UriMatcher sUriMatcher;

    /**
     * This class helps open, create, and upgrade the database file.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + ARCHIVES_TABLE_NAME + " ("
                    + Archives._ID + " INTEGER PRIMARY KEY,"
                    + Archives.TITLE + " TEXT,"
                    + Archives.DESC + " TEXT,"
                    + Archives.LINK + " TEXT,"
                    + Archives.PUB_DATE + " INTEGER,"
                    + Archives.GUID + " INTEGER"
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
        }
    }

    private DatabaseHelper mOpenHelper;

    @Override
    public boolean onCreate(){
    	mOpenHelper=new DatabaseHelper(getContext());
    	return true;
    }
    
	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case ARCHIVES:
            count = db.delete(ARCHIVES_TABLE_NAME, where, whereArgs);
            break;

        case ARCHIVE_ID:
            String noteId = uri.getPathSegments().get(1);
            count = db.delete(ARCHIVES_TABLE_NAME, Archives._ID + "=" + noteId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
        // Validate the requested uri
        if (sUriMatcher.match(uri) != ARCHIVES) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

//        Long now = Long.valueOf(System.currentTimeMillis());

        // Make sure that the fields are all set
//        if (values.containsKey(Archives.PUB_DATE) == false) {
//            values.put(NotePad.Notes.CREATED_DATE, now);
//        }

//        if (values.containsKey(NotePad.Notes.MODIFIED_DATE) == false) {
//            values.put(NotePad.Notes.MODIFIED_DATE, now);
//        }

//        if (values.containsKey(NotePad.Notes.TITLE) == false) {
//            Resources r = Resources.getSystem();
//            values.put(NotePad.Notes.TITLE, r.getString(android.R.string.untitled));
//        }
//
//        if (values.containsKey(NotePad.Notes.NOTE) == false) {
//            values.put(NotePad.Notes.NOTE, "");
//        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(ARCHIVES_TABLE_NAME, "EMPTY", values);
        if (rowId > 0) {
            Uri archiveUri = ContentUris.withAppendedId(Archives.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(archiveUri, null);
            return archiveUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(ARCHIVES_TABLE_NAME);

        switch (sUriMatcher.match(uri)) {
        case ARCHIVES:
            qb.setProjectionMap(sArchivesProjectionMap);
            break;

        case ARCHIVE_ID:
            qb.setProjectionMap(sArchivesProjectionMap);
            qb.appendWhere(Archives._ID + "=" + uri.getPathSegments().get(1));
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // If no sort order is specified use the default
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = Archives.DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }

        // Get the database and run the query
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where,
			String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case ARCHIVES:
            count = db.update(ARCHIVES_TABLE_NAME, values, where, whereArgs);
            break;

        case ARCHIVE_ID:
            String noteId = uri.getPathSegments().get(1);
            count = db.update(ARCHIVES_TABLE_NAME, values, Archives._ID + "=" + noteId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(ReaderArchive.AUTHORITY, "archives", ARCHIVES);
        sUriMatcher.addURI(ReaderArchive.AUTHORITY, "archives/#", ARCHIVE_ID);
        //sUriMatcher.addURI(ReaderArchive.AUTHORITY, "live_folders/notes", LIVE_FOLDER_NOTES);

        sArchivesProjectionMap = new HashMap<String, String>();
        sArchivesProjectionMap.put(Archives._ID, Archives._ID);
        sArchivesProjectionMap.put(Archives.TITLE, Archives.TITLE);
        sArchivesProjectionMap.put(Archives.DESC, Archives.DESC);
        sArchivesProjectionMap.put(Archives.LINK, Archives.LINK);
        sArchivesProjectionMap.put(Archives.GUID, Archives.GUID);
        sArchivesProjectionMap.put(Archives.PUB_DATE, Archives.PUB_DATE);

        // Support for Live Folders.
//        sLiveFolderProjectionMap = new HashMap<String, String>();
//        sLiveFolderProjectionMap.put(LiveFolders._ID, Notes._ID + " AS " +
//                LiveFolders._ID);
//        sLiveFolderProjectionMap.put(LiveFolders.NAME, Notes.TITLE + " AS " +
//                LiveFolders.NAME);
        // Add more columns here for more robust Live Folders.
    }
}
