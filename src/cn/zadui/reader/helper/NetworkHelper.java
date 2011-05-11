package cn.zadui.reader.helper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class NetworkHelper {
//	public static final String G_BLOG="blogs";
//	public static final String G_NEWS="news";
//	public static final String G_WEB="web";
	
	public static final int CONNECT_TIMEOUT=20*1000;
	public static final int READ_TIMEOUT=20*1000;
	
//	public static final String XML_FORMAT="xml";
//	public static final String JSON_FORMAT="json";
//	public static final String HOST_ROOT="http://172.29.1.67:8000/";
	//public static final String HOST_ROOT="http://17ttxs.com/";
	//public static final String HOST_ROOT="http://web4beidanci.appspot.com/";
	
		
	public static URLConnection buildUrlConnection(String url) throws IOException{
		URL u=new URL(url);
		HttpURLConnection con=(HttpURLConnection)u.openConnection();
		con.setConnectTimeout(NetworkHelper.CONNECT_TIMEOUT);
		con.setReadTimeout(NetworkHelper.READ_TIMEOUT);
		return con;
	}
	
	public static String getStringFromNetIO(URLConnection con){
		InputStream in=null;
		ByteArrayOutputStream out=null;
		String result="";
		try {
			in=con.getInputStream();
			out=new ByteArrayOutputStream();
			byte[] buf=new byte[1024*8];
			int readCount=0;
			while((readCount=in.read(buf))!=-1){
				out.write(buf, 0, readCount);
			}
			result = out.toString("UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				if(in!=null)in.close();
				if(out!=null)out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	

}
