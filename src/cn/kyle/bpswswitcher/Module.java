package cn.kyle.bpswswitcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import cn.kyle.util.G;
import cn.kyle.util.L;
import cn.kyle.util.Zip;

import android.content.Context;
import android.content.res.AssetFileDescriptor;

public class Module {
	public static enum BasebandFile{
	    File_GSM,
	    File_Seem_Flex_Tables
	}
	public static List<Baseband> basebands = null;
    public static String systemPath = "/system/etc/motorola/bp_nvm_default";
    public static String backupPath = "/mnt/sdcard/.bpsw_backup";
    private static String _unpackPath = null;
    
    public static String sysrw = "mount -o remount,rw /dev/block/mmcblk1p21 /system";
    public static String sysro = "mount -o remount,ro /dev/block/mmcblk1p21 /system";
    
    public static Baseband systemBaseband = new Baseband();
    public static Baseband backupBaseband = new Baseband();
    
    public static final String ID_SYSTEM = "[system]";
    public static final String ID_BACKUP = "[backup]";
    public static final String ID_UNKNOWN = "[unknown]";
    public static final String VER_UNKNOWN = "(无法读取基带版本号)";
	
    public static String getUnpackPath(){
    	if (_unpackPath==null)
    		_unpackPath = G.getDataStorageDirectory().getAbsolutePath();
    	return _unpackPath;
    }
    
    public static String getIdPath(String id){
    	if (Module.ID_SYSTEM.equalsIgnoreCase(id)){
    		return Module.systemPath;
    	}
    	if (Module.ID_BACKUP.equalsIgnoreCase(id)){
    		return Module.backupPath;
    	}
    	return getUnpackPath()+"/"+id;
    }
    
    public static boolean backup(){
    	File f = new File(backupPath);
    	f.mkdirs();
    	copy(systemPath,backupPath,true,false);
    	return Module.areSameSysAndBackup();
    }
    
    public static boolean restore(){
    	copy(backupPath,systemPath,true,true);
    	return Module.areSameSysAndBackup();
    }
    
    public static boolean switchTo(String id){
    	copy(Module.getIdPath(id),systemPath,true,true);
    	return Module.areSameBasebandFull(Module.getIdPath(id), systemPath);
    }

    public static void copy(String fromPath, String toPath, boolean deleteFirst, boolean setPerm){
    	//copy files to destination path 
    	StringBuilder sb = new StringBuilder();
    	sb.append(Module.sysrw+" ; ");
    	if (deleteFirst){
    		sb.append("rm "+toPath+"/* ; ");
    	}
    	sb.append("cp "+fromPath+"/* "+toPath+" ; ");
    	if (setPerm){
	    	sb.append("chown root.root "+toPath+"/* ; ");
	    	sb.append("chmod 644 "+toPath+"/* ; ");
    	}
    	sb.append(Module.sysro+" ; ");
    	G.execRootCmdSilent(sb.toString());
    }
    
    /**
     * 
     * @param id
     * @return return null if id not exist, return "unknown" if version unreadable 
     */
    public static String getVersionById(String id){
    	String path = null;
    	path = Module.getIdPath(id);
    	return getVersion(path);
    }
    
    public static String getVersion(String path){
    	File f = new File(path,"");
    	if (!f.exists()){
    		return null;
    	}
    	String cmd = "busybox grep -o U[A-Za-z0-9\\.]*[A-Za-z0-9\\.] " + path + "/File_GSM"+
			" > /tmp/bpsw_version ; " ;
		G.execRootCmdSilent(cmd);
		String verstr = G.readLine("/tmp/bpsw_version");
		return verstr==null||verstr.trim().length()==0?Module.VER_UNKNOWN:verstr;
    }
    
    public static void getAllBasebandVersion(){
    	//get system version
    	Module.systemBaseband.versionFull = getVersionById(Module.ID_SYSTEM);
    	systemBaseband.path = Module.systemPath;
    	resolvingVersion(systemBaseband);
    	
    	//get backup version
    	Module.backupBaseband.versionFull = getVersionById(Module.ID_BACKUP);
    	backupBaseband.path = Module.backupPath;
    	resolvingVersion(backupBaseband);
    	
    	//get other version
    	Baseband bb = null;
    	for(Iterator<Baseband> itr = Module.basebands.iterator();itr.hasNext();){
    		bb = itr.next();
    		bb.versionFull = getVersionById(bb.id);
    		resolvingVersion(bb);
    	}
    }
    
    public static void resolvingVersion(Baseband bb){
    	//resolvingVersion
    	bb.version=resolvingVersion(bb.versionFull);
    }
    
    public static String resolvingVersion(String verstr){
    	if (verstr.contains("MILESTONE2")){
    		return "(里程碑2专用) "+verstr.substring(verstr.indexOf("MILESTONE2")+"MILESTONE2".length());
    	}
//		File_GSMAGA:			US1 MILESTONE2 PERAR B125 LA 016.0R
//		File_GSMAO:				UCA JRDNEMARA B1B8 0AA 035.0R
//		File_GSMAT:				USA JRDNEMARA B1B5 TEL 02A.0R
//		File_GSMBC:				USA JORD B15 CLABR LA 011.0R
//		File_GSMC:				USA JRDN PRC  B1B5 0AA 02F.0R
//		File_GSMCEE:			UCA JRDNEMARA B1B8 0AA 02B.0R
//		File_GSMCEE3.4.2:		UCA JRDNEMARA B1B8 0AA 03A.0R
//		File_GSMEU:				UCA JRDNEMARA B1B8 0AA 039.0R
//		File_GSMF:				UCA JRDNEMARA B1B8 0AA 028.0R
//		File_GSMHTC:			UCA JRDNEMARA B1B8 0AA 030.0R
//		File_GSMINT:			UCA JRDNEMARA B1B5 0AA 028.0R
//		File_GSMPO:				USA JRDNEMARA B1B8 ORA PL 035.0R
//		File_GSMUK2.51.1:		USA JRDNEMARA B1B8 RT GB 02C.0R
//		File_GSMUK3.4.2-117:	USA JRDNEMARA B1B8 RT GB 035.0R
//		File_GSMUK3.4.3-3:		USA JRDNEMARA B1B8 RT GB 039.0R
//		File_GSMUKT_2.21.1:		USA JRDNEMARA B1B8 TM GB 028.0R
//		File_GSMUKT_2.51.1:		USA JRDNEMARA B1B8 TM GB 030.0R
//		File_GSMUST3.4.2-107:	USA JRDNTMO B1B4B5 DE1  035.0R
//		File_GSMUST3.4.2-107-9:	USA JRDNTMO B1B4B5 DE1  039.0R
//		File_GSMUST6.19.0:		USA JRDNTMO B1B4B5 DE1  028.0R
    	if (verstr.contains("JRDNEMARA")){
    		return "(defy专用) JRDNEMARA "+verstr.substring(verstr.indexOf("JRDNEMARA")+"JRDNEMARA".length());
    	}
    	if (verstr.contains("JORD")){
    		return "(defy专用) JORD "+verstr.substring(verstr.indexOf("JORD")+"JORD".length());
    	}
    	if (verstr.contains("JRDNPRC")){
    		return "(defy专用) 国行  "+verstr.substring(verstr.indexOf("JRDNPRC")+"JRDNPRC".length());
    	}
    	if (verstr.contains("JRDNTMO")){
    		return "(defy专用) JRDNTMO "+verstr.substring(verstr.indexOf("JRDNTMO")+"JRDNTMO".length());
    	}
    	
    	if (verstr.contains("JRDN")){
    		return "(defy专用) JRDN "+verstr.substring(verstr.indexOf("JRDN")+"JRDN".length());
    	}
    	return verstr;
    }
    
    public static boolean hasBackup(){
		File[] f = new File(Module.backupPath).listFiles();
		
		if (f!=null&&f.length>0){
			return true;
		}else{
			return false;
		}
	}
    
	public static enum BpswCompareResult{
		NoLeft,
		NoRight,
		Same,
		NotSame 
	}
	
//	public static String loadBpswVersionStr(boolean currentTrueOrBackupFalse){
//		
//	}
	
	/**
	 * 解压assets一级目录files里的文件，扩展名为.zip.mp3的按zip文件处理
	 */
	public static void unpack(){
		File file = G.getDataStorageDirectory();
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
			File file = new File(G.getDataStorageDirectory().getAbsolutePath()+"/"+filename);
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
	
	

//	
//	public static void mountrw(){
//		G.execRootCmdSilent("");
//	}
	public static boolean areSameSysAndBackup(){
		return areSameBasebandFull(Module.systemPath,Module.backupPath);
	}
	
	public static boolean areSameBasebandFull(String fromPath,String toPath){
		File[] fs = new File(fromPath).listFiles();
		for (File f : fs){
			if (areSame(f.getAbsolutePath(),toPath+"/"+f.getName())!=BpswCompareResult.Same){
				return false;
			}
		}
		return true;
	}
	
	public static BpswCompareResult areSameBaseband(String from,String path){
		L.debug("areSameBaseband: from="+from+" ,path="+path);
		BpswCompareResult ret = areSame(from+"/File_GSM",path+"/File_GSM");
		String[] fromArray = new File(from).list();
		String[] pathArray = new File(path).list();
		if (fromArray!=null && pathArray!=null){
			if (fromArray.length!=pathArray.length){
				return BpswCompareResult.NotSame; 
			}
		}
		if (ret==BpswCompareResult.Same){
			if (areSameBasebandFull(from,path)){
				return BpswCompareResult.Same;
			}else{
				return BpswCompareResult.NotSame;
			}
		}else{
			return BpswCompareResult.NotSame;
		}
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
				lineFromCount = fisFrom.read(lineFrom,0,10240);
				lineToCount = fisTo.read(lineTo,0,10240);
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

	public static Baseband checkCurrentBaseband(){
		return checkBaseband(Module.systemPath);
	}
	
	public static Baseband checkBackupBaseband(){
		return checkBaseband(Module.backupPath);
	}
	
	public static Baseband checkBaseband(String bpswPath){
		if (!Module.isUnpack()) Module.unpack();
		for(Iterator<Baseband> itr = Module.basebands.iterator();itr.hasNext();){
			Baseband bb = itr.next();
			if (areSameBaseband(Module.getIdPath(bb.id),bpswPath)== BpswCompareResult.Same){
				return bb;
			}
		}
		L.debug("checkBaseband: no found, bpswPath="+bpswPath);
		return null;
	}
    ///////////////////////////////////////////////////////////////////////////////////
//		
//	public static void switchFrom(String basepath){
//		if (!Module.isUnpack()){
//			Module.unpack();
//		}
//		String frompath=null;
//		if (!basepath.startsWith("/"))
//			frompath = G.getExternalStorageDirectory().getAbsolutePath()+"/"+basepath;
//		else
//			frompath = basepath;
//		if (new File(frompath).isDirectory()){
//			mountrw();
//			G.execRootCmdSilent("/system/bin/cp "+frompath+"/* "+BPSW_PATH+"/");
//			G.execRootCmdSilent("/system/bin/chown root.root "+BPSW_PATH+"/*");
//			G.execRootCmdSilent("/system/bin/chmod 644 "+BPSW_PATH+"/*");
//		}
//	}

	
	/**
	 * 
	 * @return
	 */
	public static boolean isUnpack(){
		if ( !G.getDataStorageDirectory().exists() )
			return false;
		if ( G.getDataStorageDirectory().listFiles().length>0){
			return true;
		}else{
			return false;
		}
	}
	
}
