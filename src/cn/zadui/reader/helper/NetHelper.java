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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetHelper {

	public static final int CONNECT_TIMEOUT=20*1000;
	public static final int READ_TIMEOUT=20*1000;
	
	//public static final String HOST_NAME="172.29.1.67";
//	public static final String HOST_NAME="192.168.1.108";
	public static final String HOST_NAME="221.122.112.135";
	public static final String PORT="3389";
	
	public static String webPath(String protocol,String path){
		return protocol+"://"+HOST_NAME+":"+PORT+path;
	}
	
	/**
	 * 
	 * @param ctx
	 * @return
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
