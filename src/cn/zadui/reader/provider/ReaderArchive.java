package cn.zadui.reader.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public final class ReaderArchive {

	public static final String AUTHORITY="cn.zadui.provider.ReaderArchive";
	
	private ReaderArchive(){}
	
	public static final class Archives implements BaseColumns{
		
		private Archives(){}
		
		public static final Uri CONTENT_URI=Uri.parse("content://"+AUTHORITY+"/archives");
		
		public static final String DEFAULT_SORT_ORDER = "published DESC";
		
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
         * The note itself
         * <P>Type: TEXT</P>
         */
        public static final String GUID = "guid";

        
        /**
         * The timestamp for when the note was created
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        public static final String PUB_DATE = "published";

        
	}
}
