package cn.zadui.reader.service;

import java.text.SimpleDateFormat;

import android.content.Context;
import cn.zadui.reader.helper.FAQCalendar;
import cn.zadui.reader.helper.Settings;

/**
 * Used for collecting app usage.
 * @author david
 *
 */
public class UsageCollector {

	public static void openApp(Context ctx){
		long lastOpenTS=Settings.getLongPreferenceValue(ctx, Settings.PRE_LAST_OPENED_AT, System.currentTimeMillis());
		long interval=lastOpenTS-System.currentTimeMillis();
		if (interval<30*60*1000) return;
		FAQCalendar lastOpened=new FAQCalendar(lastOpenTS);
		FAQCalendar now=new FAQCalendar(System.currentTimeMillis());
		if (now.getUnixDay()>lastOpened.getUnixDay()){
			
		}else{
			String usageStr=Settings.getStringPreferenceValue(ctx, Settings.PRE_USAGE);
			int count=Character.getNumericValue(usageStr.charAt(usageStr.length()-1));
			
		}

		
	}
	
	public static void clear(Context ctx){
		
	}
	
	//public static void 
}
