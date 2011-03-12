package cn.kyle.util;

import java.util.zip.*;
import java.io.*;

/**
 * 髡夔:zip揤坫﹜賤揤 
 * 佽隴:掛最唗籵徹ZipOutputStream睿ZipInputStream妗珋賸zip揤坫睿賤揤髡夔.
 * 恀枙:蚕衾java.util.zip婦甜祥盓厥犖趼,絞zip恅璃笢衄靡趼峈笢恅腔恅璃奀 憩頗堤珋祑都:
 * "Exception in thread "main * " java.lang.IllegalArgumentException 
 * 賤樵: ﹛﹛
 * 源楊1﹜党蜊import
 * 		java.util.zip.ZipInputStream睿ZipOutputStream.
 * 		java.util.zip硐盓厥UTF-8,Ant爵醱褫眕硌隅晤鎢. ﹛﹛
 * 源楊2﹜妏蚚Apache Ant爵枑鼎腔zip馱撿﹝
 * 		祥妏蚚java.util.zip腔婦,參ant.jar溫善classpath笢. 最唗笢妏蚚import org.apache.tools.zip.*;
 * 
 * @author Winty
 * @date 2008-8-3
 * @Usage: 
 * 	揤坫:java Zip -zip "directoryName" 
 * 	賤揤:java Zip -unzip "fileName.zip" [Extract_Path]
 */
public class Zip {
	private ZipInputStream zipIn; // 賤揤Zip
	private ZipOutputStream zipOut; // 揤坫Zip
	private ZipEntry zipEntry;
	private static int bufSize; // size of bytes
	private byte[] buf;
	private int readedBytes;

	public Zip() {
		this(512);
	}

	public Zip(int bufSize) {
		this.bufSize = bufSize;
		this.buf = new byte[this.bufSize];
	}

	// 揤坫恅璃標囀腔恅璃
	public void doZip(String zipDirectory) {// zipDirectoryPath:剒猁揤坫腔恅璃標靡
		File file;
		File zipDir;
		zipDir = new File(zipDirectory);
		String zipFileName = zipDir.getName() + ".zip";// 揤坫綴汜傖腔zip恅璃靡
		try {
			this.zipOut = new ZipOutputStream(new BufferedOutputStream(
					new FileOutputStream(zipFileName)));
			handleDir(zipDir, this.zipOut);
			this.zipOut.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	// 蚕doZip覃蚚,菰寥俇傖醴翹恅璃黍�
	private void handleDir(File dir, ZipOutputStream zipOut) throws IOException {
		FileInputStream fileIn;
		File[] files;
		files = dir.listFiles();
		if (files.length == 0) {// �彆醴翹峈諾,寀等黃斐膘眳.
			// ZipEntry腔isDirectory()源楊笢,醴翹眕"/"賦帣.
			this.zipOut.putNextEntry(new ZipEntry(dir.toString() + "/"));
			this.zipOut.closeEntry();
		} else {// �彆醴翹祥峈諾,寀煦梗揭燴醴翹睿恅璃.
			for (File fileName : files) {
				// System.out.println(fileName);
				if (fileName.isDirectory()) {
					handleDir(fileName, this.zipOut);
				} else {
					fileIn = new FileInputStream(fileName);
					this.zipOut.putNextEntry(new ZipEntry(fileName.toString()));
					while ((this.readedBytes = fileIn.read(this.buf)) > 0) {
						this.zipOut.write(this.buf, 0, this.readedBytes);
					}
					this.zipOut.closeEntry();
				}
			}
		}
	}

	// 賤揤硌隅zip恅璃
	public void unZip(String unZipfileName, String extractPath) {// unZipfileName剒猁賤揤腔zip恅璃靡
		FileOutputStream fileOut;
		File file;
		try {
			this.zipIn = new ZipInputStream(new BufferedInputStream(
					new FileInputStream(unZipfileName)));
			while ((this.zipEntry = this.zipIn.getNextEntry()) != null) {
				file = new File(extractPath,this.zipEntry.getName());
				System.out.println(file);
				if (this.zipEntry.isDirectory()) {
					file.mkdirs();
				} else {
					// �彆硌隅恅璃腔醴翹祥湔婓,寀斐膘眳.
					File parent = file.getParentFile();
					if (!parent.exists()) {
						parent.mkdirs();
					}
					fileOut = new FileOutputStream(file);
					while ((this.readedBytes = this.zipIn.read(this.buf)) > 0) {
						fileOut.write(this.buf, 0, this.readedBytes);
					}
					fileOut.close();
				}
				this.zipIn.closeEntry();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	// 扢离遣喳⑹湮苤
	public void setBufSize(int bufSize) {
		this.bufSize = bufSize;
	}

	// 聆彸Zip濬
	public static void main(String[] args) throws Exception {
		if (args.length >= 2) {
			String name = args[1];
			String path = args.length>=3 ? args[2] : ".";
			Zip zip = new Zip();
			if (args[0].equals("-zip"))
				zip.doZip(name);
			else if (args[0].equals("-unzip"))
				zip.unZip(name,path);
		} else {
			System.out.println("Usage:");
			System.out.println("揤坫:java Zip -zip directoryName");
			System.out.println("賤揤:java Zip -unzip fileName.zip [extract_path]");
			throw new Exception("Arguments error!");
		}
	}
}