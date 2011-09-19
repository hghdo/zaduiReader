package cn.zadui.reader.helper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import cn.zadui.reader.service.DownloadService.ServiceState;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetHelper {

	public static final int CONNECT_TIMEOUT=20*1000;
	public static final int READ_TIMEOUT=20*1000;
	
	//public static final String HOST_NAME="172.29.1.67";
//	public static final String HOST_NAME="192.168.1.108";
	public static final String HOST_NAME="meili.51leiju.cn";
	public static final String PORT="3389";
	
	public static String webPath(String protocol,String path){
		return protocol+"://"+HOST_NAME+":"+PORT+path;
	}
	
	/**
	 * Detects current network connection type
	 * @param ctx
	 * @return -1 means no network available.
	 */
	public static int currentNetwork(Context ctx){
		ConnectivityManager connectivityManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();	
		return activeNetworkInfo==null ? -1 : activeNetworkInfo.getType();
	}
	
	public static HttpURLConnection buildUrlConnection(String url) throws IOException{
		URL u=new URL(url);
		HttpURLConnection con=(HttpURLConnection)u.openConnection();
		con.setConnectTimeout(NetHelper.CONNECT_TIMEOUT);
		con.setReadTimeout(NetHelper.READ_TIMEOUT);
		return con;
	}
	
	/**
	 * Check new version only for third party market because Google Android Market provides update function. 
	 */
	public static void checkNewVersion(Context ctx){
		if (Settings.installedFromGoogleMarket(ctx)) return;
		if (Settings.getBooleanPreferenceValue(ctx, Settings.PRE_HAS_NEW_VERSION, false)) return;
		String version=getStringFromNetIO(NetHelper.webPath("http", "/version"));
		if (version==null) return;
		try {
			int currentVersion=ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionCode;
			if (currentVersion>Integer.parseInt(version)){
				Settings.updateBooleanPreferenceValue(ctx, Settings.PRE_HAS_NEW_VERSION, true);
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	
	public static String getStringFromNetIO(String url){
		HttpURLConnection con=null;
		try {
			con=buildUrlConnection(url);
			InputStream in=con.getInputStream();
			ByteArrayOutputStream out=new ByteArrayOutputStream();
			byte[] buf=new byte[1024*8];
			int readCount=0;
			while((readCount=in.read(buf))!=-1){
				out.write(buf, 0, readCount);
			}
			String result=out.toString("UTF-8");
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally{
			con.disconnect();
		}
	}
}
