package cn.kyle.util;

import javax.crypto.Mac;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class Tool {
	private static Mac _mac = null;
	public static Mac getMacInstance(){
		if (_mac==null){
			try {
				_mac = Mac.getInstance("MD5");
			} catch (NoSuchAlgorithmException e1) {
				e1.printStackTrace();
			}
		}else{
			_mac.reset();
		}
		return _mac;
	}
	
	public static Mac getNewMacInstance(){
		Mac mac = null;
		try {
			 Mac.getInstance("MD5");
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}
		return mac;
	}
	
	public static byte[] macFileArray(File[] f){
		Mac mac = getNewMacInstance();
		for (int i=0;i<f.length;i++){
			mac.update(macFile(f[i]));
		}
		return mac.doFinal();
	}
	
	public static byte[] macFile(File f){
		Mac mac = getMacInstance();
		try {
			FileInputStream fr = new FileInputStream(f);
			byte[] buffer = new byte[1024];
			while((fr.read(buffer))!=-1){
				mac.update(buffer);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return mac.doFinal();
	}
	
	public static byte[] macByteArray(byte[] buffer){
		Mac mac = getMacInstance();
		return mac.doFinal(buffer);
	}
	
}
