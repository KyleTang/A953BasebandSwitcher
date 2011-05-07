package cn.kyle.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Vector;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Build;
import android.os.Environment;
import android.view.View;

public final class G {

	public static final int CmdExitValue_InterruptedException = -999;
	public static final int CmdExitValue_IOException = -998;
	/**
	 * 执行su时，如果su不存在，
	 * 则会报“ Error running exec(). Command: [su] Working Directory: null Environment: null”
	 * 此时，标记为CmdExitValue_NotRoot
	 */
	public static final int CmdExitValue_NotRoot = -900;
	public static final int CmdExitValue_RootPermissionDenied = 255;
	public static final int CmdExitValue_OK = 0;
	public static final int CmdExitValue_NotFoundCommand = 127;

	private static Resources resources = null;
	private static Context context = null;
	private static int versionCode = 0;
	public static String versionName = null;
	
	public static long dataFreeSpaceKB = 0;
	public static long dataTotalSpaceKB =0;
	public static long sdcardFreeSpaceKB =0;
	public static long sdcardTotalSpaceKB =0;
	
    public static String myphoneid="51680001ffd80000015ef45b0e00601f";
    public static String phoneid="";
    public static boolean isMyPhone=false;
    
    public static String getFileLine(String pathname){
    	String [] lines = getFileLines(pathname,true);
    	return lines==null||lines.length==0?null:lines[0];
    }
    
    public static String[] getFileLines(String pathname, boolean oneline){
    	String line=null;
    	LinkedList<String> lines = new LinkedList<String>();
    	try {
			BufferedReader br = new BufferedReader(new FileReader(pathname));
			while((line = br.readLine())!=null){
				lines.add(line);
				if (oneline)
					break;
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lines.toArray(new String[0]);
    }
    
    public static String getPhoneId(){
    	return getFileLine("/proc/phoneid");
    }
    
    public static String getLastVersion(){
    	return getFileLine("/tmp/update.xml.txt");
    }
    
	/**
	 * 使用G的方法前，先执行此方法，部分G的方法以来于此方法
	 * @param ctx
	 */
	public static void setResourcesAndContext(Context ctx){
		context = ctx;
		resources = ctx.getResources();
		versionCode = getVersionCode(true);
		phoneid = getPhoneId();
		isMyPhone = G.myphoneid.equals(phoneid);
	}
	
	public static Resources getResources(){
		return resources;
	}
	
	public static Context getContext(){
		return context;
	}
	
	public static String getPackageName(){
		return context.getPackageName();
	}
	
	public static int getVersionCode(boolean force){
		if (force||versionCode==0){
			try {
				versionCode = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0).versionCode;
				versionName = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0).versionName;
			} catch (NameNotFoundException e) {
				versionCode = 0;
			}
		}
		return versionCode;
	}
	
	public static int execRootCmd(String cmd , Vector<String> output) {
		L.debug("G.execRootCmd("+cmd+") outout="+output);
		int ret = G.CmdExitValue_NotRoot;
		boolean hasRootPermission = true;
		boolean debugFlag = false;
		try {
			Process process = Runtime.getRuntime().exec("su");
			L.debug("G.execRootCmd: su - pass");
			ret = G.CmdExitValue_OK; //default set : CmdExitValue_OK

			OutputStream out = process.getOutputStream();
			DataOutputStream dataOut = new DataOutputStream(out);
			
			InputStream input = process.getInputStream();
			DataInputStream dataInput= new DataInputStream(input);
			
			dataOut.writeBytes(cmd + " 2>&1 \n");
			dataOut.flush();
			L.debug("G.execRootCmd: exec cmd flush - pass");
			
			if (debugFlag){
				String tmp = null;
				int lineNumber=0;
				while( (tmp=dataInput.readLine())!=null){
					L.debug("G.execRootCmd: output["+lineNumber+"] : "+tmp);
					lineNumber++;
					if (output!=null) output.add(tmp);
				}
			}
			L.debug("G.execRootCmd: exec cmd finish - pass");
			
			hasRootPermission = false;
			dataOut.writeBytes("exit\n");
			dataOut.flush();
			hasRootPermission = true;
			L.debug("G.execRootCmd: exec exit - pass");
			
			int i = process.waitFor();
			L.debug("G.execRootCmd: waitFor - pass, i="+i);
			int j = process.exitValue();
			L.debug("G.execRootCmd: exitValue - pass, j="+j);
			if (i == G.CmdExitValue_OK)
				ret = j;
			else
				ret = i;
		} catch (InterruptedException e) {
			ret = G.CmdExitValue_InterruptedException;
			L.debug("G.execRootCmd InterruptedException: "+e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			if (ret!=G.CmdExitValue_NotRoot){
				ret = G.CmdExitValue_IOException;
			}
			if (!hasRootPermission){
				ret = G.CmdExitValue_RootPermissionDenied;
			}
			//root权限被禁止，运行返回255
			if (ret == 255){
				ret = G.CmdExitValue_RootPermissionDenied;
			}
			L.debug("G.execRootCmd IOException: "+e);
			e.printStackTrace();
		}
		L.debug("G.execRootCmd: ret="+ret);
		return ret;
	}

	public static int execRootCmdSilent(String cmd) {
		return execRootCmd(cmd,null);
	}

	public static String getKernelVersion() {
		String str = getFileLine("/proc/version");
		if (str == null)
			str = "unknown";
		return str;
	}
	
	public static boolean hasBusybox(){
		if (!new File("/system/bin/busybox").exists() 
				|| G.execRootCmdSilent("/system/bin/busybox")==G.CmdExitValue_NotFoundCommand)
			return false;
		else
			return true;
	}

	public static boolean haveRoot() {
		int ret = execRootCmdSilent("echo test");
		if ( ret == G.CmdExitValue_OK )
			return true;
		else
			return false;
	}
	
	public static int checkRoot() {
		return execRootCmdSilent("echo test");
	}

	public static boolean isExternalStorageReadable() {
		String str = Environment.getExternalStorageState();
		L.debug("ExternalStorageState: "+str);
		if ((!"mounted".equals(str)) && (!"mounted_ro".equals(str)))
			return false;
		else
			return true;
	}

	public static boolean isExternalStorageWritable() {
		String str = Environment.getExternalStorageState();
		return "mounted".equals(str);
	}

	/**
	 * 是否安装过以前的版本
	 * @return
	 */
	public static boolean isOldVersionInstalled(){
		int count = 0;
		File [] files = G.getVersionFlagDirectory().listFiles();
		if (files!=null){
			for(File f : files){
				if (f.getName().startsWith("versionCode.")){
					if (!f.getName().equals("versionCode."+G.getVersionCode(false))){
						count++;
					}
				}
			}
		}
		return count == 0 ? false : true ;
	}

	public static File getVersionFlagFile(){
		return new File(G.getVersionFlagDirectory(),"versionCode."+G.getVersionCode(false));
	}
	
//	public static File getExternalStorageDirectory() {
//		File localFile = Environment.getExternalStorageDirectory();
//		return new File(localFile, "/Android/data/"+G.getPackageName()+"/files");
//	}
	
	public static File getDataStorageDirectory() {
		return new File("/data/data/"+G.getPackageName()+"/files");
	}
	
	public static File getVersionFlagDirectory() {
		return new File("/data/local/"+G.getPackageName());
	}

	public static boolean isModelMilestone2Compatible() {
		L.debug("isModelMilestone2Compatible: model is "+Build.MODEL);
		if ( Build.MODEL.equalsIgnoreCase("MotoA953")||
				Build.MODEL.equalsIgnoreCase("A953") || 
				Build.MODEL.equalsIgnoreCase("DROIDX") || 
				Build.MODEL.equalsIgnoreCase("ME722") ||
				Build.MODEL.equalsIgnoreCase("droid2") || 
				Build.MODEL.equalsIgnoreCase("droid2 Global"))
			return true;
		else
			return false;
	}

	public static String readLine(String filepathname) {
		String str = null;
		try {
			FileReader localFileReader = new FileReader(filepathname);
			BufferedReader localBufferedReader = new BufferedReader(
					localFileReader, 1024);
			str = localBufferedReader.readLine();
			localBufferedReader.close();

		} catch (Exception localException) {
			localException.printStackTrace();
		}
		return str;
	}
	
	public static void killProcessByFileKeyword(String keyword){
		G.execRootCmdSilent("busybox kill -9 `busybox ls -l /proc/*/fd/* 2>/dev/null | busybox grep "
				+keyword+" | busybox cut -c 64-67`") ; 
		G.execRootCmdSilent("busybox kill -9 `busybox ls -l /proc/*/fd/* 2>/dev/null | busybox grep "
				+keyword+" | busybox cut -c 64-68 | busybox grep -v /`") ; 
	}
}
