package cn.zadui.reader.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public final class ReaderArchive {

	public static final String AUTHORITY="cn.zadui.provider.ReaderArchive";
	
	private ReaderArchive(){}
	
	public static final class Archives implements BaseColumns{

        /**
         * The note itself
         * <P>Type: TEXT</P>
         */
        public static final String GUID = "guid";
		
        /**
         * The title of the note
         * <P>Type: TEXT</P>
         */
        public static final String TITLE = "title";

        /**
         * The note itself
         * <P>Type: TEXT</P>
         */
        public static final String DESC = "desc";

        /**
         * The note itself
         * <P>Type: TEXT</P>
         */
        public static final String LINK = "link";

        
        /**
         * The timestamp for when the note was created
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        public static final String PUB_DATE = "pubDate";
        
        //public static final String CREATED_AT = "createdAt";
        
        public static final String THUMB_URL="thumbUrl";
        
        public static final String READED="readed";
        
        public static final String CAHECED="cached";
		
		private Archives(){}
		
		public static final Uri CONTENT_URI=Uri.parse("content://"+AUTHORITY+"/archives");
		
		public static final Uri OLD_ARCHIVES_URI=Uri.parse("content://"+AUTHORITY+"/archives/old");
		
		public static final Uri ARCHIVE_GUID_URI=Uri.parse("content://"+AUTHORITY+"/archives/guid");
		
		
		public static final String DEFAULT_SORT_ORDER = PUB_DATE +" DESC";
        
//        public static Cursor getAll(Context ctx,String[] project,String order){
//        	return ctx.managedQuery(CONTENT_URI, project, null, null,DEFAULT_SORT_ORDER);
//        }

        
	}
}
