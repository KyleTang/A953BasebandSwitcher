package cn.kyle.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

public class KyleApp {
	public String id;
	public String name_ZH;
	public String name_EN;
	public String summary_EN;
	public String summary_ZH;
	public String versionName;
	public String versionCode;
	public String downLink;
	public String moreLink;

	public static String getLocaleName(){
		return null;
	}
	
	public static String getLocaleSummary(){
		return null;
	}
	
	public static List<KyleApp> parseXml(String xmlFile){
		List<KyleApp> appList = new LinkedList<KyleApp>();
		boolean findKyleAppTag = false;
		
		File file = new File(xmlFile);
		XmlPullParser parser = Xml.newPullParser();
		try {
			parser.setInput(new FileInputStream(file), "UTF-8");
			int event = parser.getEventType();
			while (event != XmlPullParser.END_DOCUMENT) {
				switch (event) {
				case XmlPullParser.START_DOCUMENT:// 判断当前事件是否是文档开始事件
					break;
				case XmlPullParser.START_TAG:// 判断当前事件是否是标签元素开始事件
					if ("kyleapp".equalsIgnoreCase(parser.getName())) {// 判断开始标签元素是否是book
						findKyleAppTag = true;
					}
					if (findKyleAppTag) {
						if ("app".equalsIgnoreCase(parser.getName())){
							KyleApp k = new KyleApp();
							k.id = parser.getAttributeValue(null, "id");
							k.name_EN = k.name_ZH = parser.getAttributeValue(null, "name");
							k.versionName = parser.getAttributeValue(null, "versionName");
							k.versionCode = parser.getAttributeValue(null, "versionCode");
							k.downLink = parser.getAttributeValue(null, "down");
							k.summary_ZH = k.summary_EN = parser.getAttributeValue(null, "summary");
							appList.add(k);
						}
					}
					
					break;
				case XmlPullParser.END_TAG:// 判断当前事件是否是标签元素结束事件
					
					break;
				}
				event = parser.next();// 进入下一个元素并触发相应事件
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return appList;
	}
	
	public String getTmpFileName(){
		return id+"-"+versionName+".apk";
	}
	
	public String getAppText(){
		return name_ZH+"  版本："+versionName+"\n\t"+summary_ZH;
	}
	
}
