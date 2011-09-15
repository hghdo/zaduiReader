package cn.zadui.reader.helper;

import java.io.File;

import android.os.Environment;

public class StorageHelper {
	
	private String appPkgName;
	private File appExtStorageRoot;
	
	public StorageHelper(String packageName){
		appPkgName=packageName;
		File sdcard=Environment.getExternalStorageDirectory();
		appExtStorageRoot=new File(sdcard,"Android/data/"+appPkgName);
		if(!appExtStorageRoot.exists())appExtStorageRoot.mkdirs();	
	}

	public File getArchiveDir(long archiveGuid){
		return new File(getArchivesDirInSdcard(),String.valueOf(archiveGuid));
	}
	
	public File getAppExtStorageRoot(){
		return appExtStorageRoot;
	}
	
	public File getArchivesDirInSdcard(){
		File adir=new File(getAppExtStorageRoot(),"files/archives");
		adir.mkdirs();
		return adir;
	}
	
	public static boolean isSdcardWritable(){
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    // We can read and write the media
		    mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    // We can only read the media
		    mExternalStorageAvailable = true;
		    mExternalStorageWriteable = false;
		} else {
		    // Something else is wrong. It may be one of many other states, but all we need
		    //  to know is we can neither read nor write
		    mExternalStorageAvailable = mExternalStorageWriteable = false;
		}	
		return (mExternalStorageAvailable && mExternalStorageWriteable);		
	}
	
	public static boolean deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}	
	
	
}
