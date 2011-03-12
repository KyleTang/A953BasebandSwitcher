package cn.kyle.bpswswitcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import cn.kyle.util.G;
import cn.kyle.util.L;
import cn.kyle.util.Zip;

import android.content.res.AssetFileDescriptor;

public class Module {
	public static String BPSW_PATH="/system/etc/motorola/bp_nvm_default";
	public static String BAK_PATH="/mnt/sdcard/.bpsw_backup";
	
	public static enum BpswCompareResult{
		NoLeft,
		NoRight,
		Same,
		NotSame 
	}
	
	public static enum BPSW{
		a953_uk("A953欧版基带"),
		me722_zhcn("ME722国行基带"),
		me722_zhcn_new("ME722国行基带(新版)"),
		a953_au_vodafone("A953澳大利亚Vodafone基带"),
		a953_brazil("A953巴西基带"),
		other("未知基带");
	
		BPSW(String str){
			name=str;
		}
		
		public final String name;
		
		public String getName(){
			return name;
		}
	}
	
	public static void toA953_Au_Vodafone(){
		switchFrom(BPSW.a953_au_vodafone.toString());
	}
	
	public static void toA953_Brazil(){
		switchFrom(BPSW.a953_brazil.toString());
	}
	
	public static void toA953UK(){
		switchFrom(BPSW.a953_uk.toString());
	}
	
	public static void toME722ZHCN(){
		switchFrom(BPSW.me722_zhcn.toString());
	}
	
	public static void toME722ZHCN_New(){
		switchFrom(BPSW.me722_zhcn_new.toString());
	}
	
	public static boolean hasBackup(){
		File[] f = new File(BAK_PATH).listFiles();
		
		if (f!=null&&f.length>0){
			return true;
		}else{
			return false;
		}
	}
	
	public static boolean isBackupOK(){
		File[] fs = new File(BPSW_PATH).listFiles();
		for (File f : fs){
			if (areSame(f.getAbsolutePath(),BAK_PATH+"/"+f.getName())!=BpswCompareResult.Same){
				return false;
			}
		}
		return true;
	}
	
	public static boolean backup(){
		new File(BAK_PATH).mkdirs();
		G.execRootCmdSilent("/system/bin/rm "+BAK_PATH+"/*");
		G.execRootCmdSilent("/system/bin/cp "+BPSW_PATH+"/* "+BAK_PATH+"/");
		return isBackupOK();
	}
	
	public static void restore(){
		switchFrom(BAK_PATH);
	}
	
	public static void switchFrom(String basepath){
		if (!Module.isUnpack()){
			Module.unpack();
		}
		String frompath=null;
		if (!basepath.startsWith("/"))
			frompath = G.getExternalStorageDirectory().getAbsolutePath()+"/"+basepath;
		else
			frompath = basepath;
		if (new File(frompath).isDirectory()){
			mountrw();
			G.execRootCmdSilent("/system/bin/cp "+frompath+"/* "+BPSW_PATH+"/");
			G.execRootCmdSilent("/system/bin/chown root.root "+BPSW_PATH+"/*");
			G.execRootCmdSilent("/system/bin/chmod 644 "+BPSW_PATH+"/*");
		}
	}
	
	public static void mountrw(){
		G.execRootCmdSilent("mount -o remount,rw /dev/block/mmcblk1p21 /system");
	}
	
	public static BPSW checkCurrentBpsw(){
		return checkBpsw(BPSW_PATH);
	}
	
	public static BPSW checkBackupBpsw(){
		return checkBpsw(BAK_PATH);
	}
	
	public static BPSW checkBpsw(String bpswPath){
		if (!Module.isUnpack()) Module.unpack();
		if (areSameBpsw(bpswPath,BPSW.a953_uk.toString())==BpswCompareResult.Same){
			return BPSW.a953_uk;
		}else if (areSameBpsw(bpswPath,BPSW.me722_zhcn.toString())==BpswCompareResult.Same){
			return BPSW.me722_zhcn;
		}else if (areSameBpsw(bpswPath,BPSW.me722_zhcn_new.toString())==BpswCompareResult.Same){
			return BPSW.me722_zhcn_new;
		}else if (areSameBpsw(bpswPath,BPSW.a953_au_vodafone.toString())==BpswCompareResult.Same){
			return BPSW.a953_au_vodafone;
		}else if (areSameBpsw(bpswPath,BPSW.a953_brazil.toString())==BpswCompareResult.Same){
			return BPSW.a953_brazil;
		}else{
			return BPSW.other;
		}
	}
	
	public static String loadBpswVersionStr(boolean currentTrueOrBackupFalse){
		String cmd = "busybox grep -o U[A-Za-z0-9\\.]*[A-Za-z0-9\\.] " +
				(currentTrueOrBackupFalse?BPSW_PATH+"/File_GSM " :BAK_PATH+"/File_GSM " )+
				"> /tmp/bpsw_version" ;
		G.execRootCmdSilent(cmd);
		String verstr = G.readLine("/tmp/bpsw_version");
		return verstr==null||verstr.trim().length()==0?"(无法读取基带版本号)":verstr;
	}
	
	
	public static BpswCompareResult areSameBpsw(String from,String path){
		String dir = null;
		if (!path.startsWith("/")){
			dir = G.getExternalStorageDirectory()+"/"+path;
		}else{
			dir = path;
		}
		BpswCompareResult ret = areSame(from+"/File_Seem_Flex_Tables",dir+"/File_Seem_Flex_Tables");
		return ret==BpswCompareResult.Same?areSame(from+"/File_GSM",dir+"/File_GSM"):ret;
	}
	
	public static BpswCompareResult areSame(String from, String to){
		BpswCompareResult ret = BpswCompareResult.Same;
		FileInputStream fisFrom = null;
		FileInputStream fisTo =null;
		File fFrom = new File(from);
		File fTo = new File(to);
		if (!fFrom.exists()) return BpswCompareResult.NoLeft;
		if (!fTo.exists()) return BpswCompareResult.NoRight;
		if (fFrom.length()!=fTo.length()) return BpswCompareResult.NotSame;
		try {
			fisFrom = new FileInputStream(from);
			fisTo = new FileInputStream(to);
			byte[] lineFrom = new byte[10240];
			int lineFromCount = 0;
			byte[] lineTo =  new byte[10240];
			int lineToCount = 0;
			do{
				lineFromCount = fisFrom.read(lineFrom,0,1024);
				lineToCount = fisTo.read(lineTo,0,1024);
				if (lineFromCount!=lineToCount)
					throw new IOException("");
				for(int i=0;i<lineFromCount;i++){
					if (lineFrom[i]!=lineTo[i]){
						throw new IOException("");
					}
				}
				
			}while(lineFrom!=null&&lineFrom.equals(lineTo));
		} catch (FileNotFoundException e) {
			ret = BpswCompareResult.NotSame;
			e.printStackTrace();
		} catch (IOException e) {
			ret = BpswCompareResult.NotSame;
			e.printStackTrace();
		}finally{
			if (fisFrom!=null){
				try {
					fisFrom.close();
				} catch (IOException e) {
				}
			}
			if (fisTo!=null){
				try {
					fisTo.close();
				} catch (IOException e) {
				}
			}
		}
		return ret;
	}
	
	/**
	 * 解压assets一级目录files里的文件，扩展名为.zip.mp3的按zip文件处理
	 */
	public static void unpack(){
		File file = G.getExternalStorageDirectory();
		if (!file.exists()) file.mkdirs();
		try {
			String[] files = G.getResources().getAssets().list("files");
			L.debug("getAssets:files.len="+files.length);
			for(int i=0;i<files.length;i++){
				unpackFile(files[i]);
				if (files[i].endsWith(".zip.mp3")){
					new Zip().unZip(file.getAbsolutePath()+"/"+files[i], file.getAbsolutePath());
					new File(file,files[i]).delete();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void unpackFile(String filename){
		//String dir = "files/";
		String dir = "files/";
		int count=0;
		byte[] bs = new byte[10240];
		try {
			L.debug("unpackFile { "+dir+filename);
			AssetFileDescriptor afd = G.getResources().getAssets().openFd(dir+filename);
			InputStream in = G.getResources().getAssets().open(dir+filename);
			//FileOutputStream out = context.openFileOutput(filename, Context.MODE_WORLD_READABLE);
			File file = new File(G.getExternalStorageDirectory().getAbsolutePath()+"/"+filename);
			FileOutputStream out = new FileOutputStream(file,false);
			while((count = in.read(bs))>=0){
				out.write(bs,0,count);
			}
			out.close();
			in.close();
			L.debug("unpackFile } "+dir+filename);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean isUnpack(){
		if ( !G.getExternalStorageDirectory().exists() )
			return false;
		if ( G.getExternalStorageDirectory().listFiles().length>0){
			if (!new File(G.getExternalStorageDirectory(),BPSW.a953_uk.toString()).exists())
				return false;
			if (!new File(G.getExternalStorageDirectory(),BPSW.me722_zhcn.toString()).exists())
				return false;
			if (!new File(G.getExternalStorageDirectory(),BPSW.me722_zhcn_new.toString()).exists())
				return false;
			if (!new File(G.getExternalStorageDirectory(),BPSW.a953_au_vodafone.toString()).exists())
				return false;
			if (!new File(G.getExternalStorageDirectory(),BPSW.a953_brazil.toString()).exists())
				return false;
			return true;
		}else{
			return false;
		}
	}
	
}
